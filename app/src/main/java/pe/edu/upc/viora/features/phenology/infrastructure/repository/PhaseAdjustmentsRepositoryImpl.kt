package pe.edu.upc.viora.features.phenology.infrastructure.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import java.time.MonthDay
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import pe.edu.upc.viora.features.phenology.domain.OlivePhase
import pe.edu.upc.viora.features.phenology.domain.PhaseAdjustments
import pe.edu.upc.viora.features.phenology.domain.repository.PhaseAdjustmentsRepository
import pe.edu.upc.viora.features.phenology.infrastructure.local.PhaseAdjustmentsCodec

/**
 * Keeps the start dates each producer corrected in the app's preferences DataStore. They are the producer's own
 * choices, not a copy of the server's data, so they need no table and work without signal.
 */
@Singleton
class PhaseAdjustmentsRepositoryImpl @Inject constructor(
    private val dataStore: DataStore<Preferences>,
) : PhaseAdjustmentsRepository {

    override fun observe(plotId: String): Flow<PhaseAdjustments> = dataStore.data
        .map { PhaseAdjustmentsCodec.decode(it[PHASE_ADJUSTMENTS])[plotId] ?: PhaseAdjustments.NONE }
        .distinctUntilChanged()

    override suspend fun adjust(plotId: String, phase: OlivePhase, on: MonthDay) = update(plotId) { it.adjusted(phase, on) }

    override suspend fun reset(plotId: String, phase: OlivePhase) = update(plotId) { it.reset(phase) }

    private suspend fun update(plotId: String, change: (PhaseAdjustments) -> PhaseAdjustments) {
        dataStore.edit { prefs ->
            val byPlot = PhaseAdjustmentsCodec.decode(prefs[PHASE_ADJUSTMENTS]).toMutableMap()
            byPlot[plotId] = change(byPlot[plotId] ?: PhaseAdjustments.NONE)
            val encoded = PhaseAdjustmentsCodec.encode(byPlot)
            if (encoded.isEmpty()) prefs.remove(PHASE_ADJUSTMENTS) else prefs[PHASE_ADJUSTMENTS] = encoded
        }
    }

    internal companion object {
        val PHASE_ADJUSTMENTS = stringPreferencesKey("phenology_phase_adjustments")
    }
}
