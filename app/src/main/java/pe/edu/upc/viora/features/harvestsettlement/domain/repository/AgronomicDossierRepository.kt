package pe.edu.upc.viora.features.harvestsettlement.domain.repository

import java.io.File
import kotlinx.coroutines.flow.Flow
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.CachedDossier
import pe.edu.upc.viora.features.harvestsettlement.domain.entity.DossierVerification

/** The sealed agronomic dossier of a settled campaign (P76), its PDF (P77) and its verification (P78). */
interface AgronomicDossierRepository {

    /** The cached dossier of [plotId] for [campaignYear]; null until it was sealed or downloaded once. */
    fun observeDossier(plotId: String, campaignYear: Int): Flow<CachedDossier?>

    /**
     * Seals the dossier of a settled campaign and caches it. Sealing is idempotent: a campaign already sealed answers
     * its stored dossier unchanged, so this is also how the phone downloads it again.
     */
    suspend fun seal(plotId: String, campaignYear: Int): AppResult<Unit>

    /** The newest campaign of the plot with a sealed dossier, or null when none was sealed. */
    suspend fun latestSealedCampaign(plotId: String): AppResult<Int?>

    /** Downloads the exact sealed PDF into the app's files and remembers where it is. */
    suspend fun downloadPdf(plotId: String, campaignYear: Int): AppResult<File>

    /** Asks Viora whether [verificationHash] matches a stored dossier; NotFound when it matches none. */
    suspend fun verify(verificationHash: String): AppResult<DossierVerification>
}
