package pe.edu.upc.viora.features.croploadregulation.infrastructure.local

import androidx.room3.Dao
import androidx.room3.Query
import androidx.room3.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface ThinningPrescriptionDao {

    @Query("SELECT * FROM thinning_prescriptions WHERE plot_id = :plotId AND campaign_year = :campaignYear")
    fun observe(plotId: String, campaignYear: Int): Flow<ThinningPrescriptionEntity?>

    @Query("SELECT * FROM thinning_prescriptions WHERE campaign_year = :campaignYear")
    fun observeCampaign(campaignYear: Int): Flow<List<ThinningPrescriptionEntity>>

    @Query("SELECT * FROM thinning_prescriptions WHERE plot_id = :plotId AND campaign_year = :campaignYear")
    suspend fun find(plotId: String, campaignYear: Int): ThinningPrescriptionEntity?

    @Upsert
    suspend fun upsert(entity: ThinningPrescriptionEntity)

    @Query(
        "UPDATE thinning_prescriptions SET execution_json = :executionJson " +
            "WHERE plot_id = :plotId AND campaign_year = :campaignYear",
    )
    suspend fun updateExecution(plotId: String, campaignYear: Int, executionJson: String?)
}
