package pe.edu.upc.viora.features.harvestsettlement.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface AgronomicDossierDao {

    @Query("SELECT * FROM agronomic_dossiers WHERE plot_id = :plotId AND campaign_year = :campaignYear")
    fun observe(plotId: String, campaignYear: Int): Flow<AgronomicDossierEntity?>

    @Query("SELECT * FROM agronomic_dossiers WHERE plot_id = :plotId AND campaign_year = :campaignYear")
    suspend fun find(plotId: String, campaignYear: Int): AgronomicDossierEntity?

    @Upsert
    suspend fun upsert(entity: AgronomicDossierEntity)

    @Query(
        "UPDATE agronomic_dossiers SET pdf_path = :pdfPath, pdf_saved_at_epoch_ms = :savedAtEpochMs " +
            "WHERE plot_id = :plotId AND campaign_year = :campaignYear",
    )
    suspend fun updatePdf(plotId: String, campaignYear: Int, pdfPath: String, savedAtEpochMs: Long)

    /** Signing out (or another account signing in): the whole table goes, see the context's tables wiper. */
    @Query("DELETE FROM agronomic_dossiers")
    suspend fun wipeAll()
}
