package pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote

import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Path

interface HarvestSettlementService {

    /** Settlements of a plot, newest campaign first; empty when none. 403 not owner; 404 plot not found. */
    @GET("plots/{plotId}/harvest-settlements")
    suspend fun getSettlements(@Path("plotId") plotId: String): Response<List<HarvestSettlementDto>>
}
