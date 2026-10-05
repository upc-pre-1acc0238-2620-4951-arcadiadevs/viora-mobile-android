package pe.edu.upc.viora.features.croploadregulation.domain.entity

import java.time.Instant
import java.time.LocalDate
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.ThinningEventType
import pe.edu.upc.viora.features.croploadregulation.domain.valueobject.Timeliness

/**
 * A timeline event (sampling milestone or executed thinning labor) for the bitácora.
 */
data class ThinningEvent(
    val id: String,
    val eventType: ThinningEventType,
    val prescriptionId: String,
    val executionId: String? = null,
    val plotId: String,
    val plotName: String,
    val campaignYear: Int,
    val occurredAt: Instant,
    val evaluatedTreesCount: Int? = null,
    val totalShootsCount: Int? = null,
    val totalFruitsCount: Int? = null,
    val meanFruitsPerShoot: Double? = null,
    val isRepresentative: Boolean? = null,
    val removalPercentage: Double? = null,
    val removedKg: Double? = null,
    val executionDate: LocalDate? = null,
    val laborCrewSize: Int? = null,
    val timeliness: Timeliness? = null,
)
