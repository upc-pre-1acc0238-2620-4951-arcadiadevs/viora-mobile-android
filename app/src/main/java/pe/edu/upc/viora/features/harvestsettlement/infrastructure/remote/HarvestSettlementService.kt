package pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Header
import retrofit2.http.POST
import retrofit2.http.Path

interface HarvestSettlementService {

    /** Settlements of a plot, newest campaign first; empty when none. 403 not owner; 404 plot not found. */
    @GET("plots/{plotId}/harvest-settlements")
    suspend fun getSettlements(@Path("plotId") plotId: String): Response<List<HarvestSettlementDto>>

    /**
     * Settles the plot's harvest for a campaign. 201 created; 200 replay of the same
     * [idempotencyKey] (original settlement, no new receipt); 400 validation; 403 not owner;
     * 404 plot not found; 409 already settled, with the `existingSettlement` problem property;
     * 422 key reused for another plot or campaign.
     */
    @POST("plots/{plotId}/harvest-settlements")
    suspend fun settle(
        @Path("plotId") plotId: String,
        @Header("Idempotency-Key") idempotencyKey: String,
        @Body request: SettleHarvestRequestDto,
    ): Response<HarvestSettlementDto>
}
