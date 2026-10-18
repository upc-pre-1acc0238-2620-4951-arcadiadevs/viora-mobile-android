package pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.Path
import retrofit2.http.Streaming

interface AgronomicDossierService {

    /**
     * Seals the dossier of a settled campaign: 201 sealed now, 200 it already was (stored dossier unchanged);
     * 422 the campaign is not settled.
     */
    @POST("plots/{plotId}/agronomic-dossiers")
    suspend fun seal(
        @Path("plotId") plotId: String,
        @Body request: SealDossierRequestDto,
    ): Response<AgronomicDossierDto>

    /** The sealed dossiers of a plot, newest campaign first; empty when none. */
    @GET("plots/{plotId}/agronomic-dossiers")
    suspend fun getDossiers(@Path("plotId") plotId: String): Response<List<AgronomicDossierDto>>

    /** The exact stored PDF of a campaign; 404 when it was never sealed. */
    @Streaming
    @Headers("Accept: application/pdf")
    @GET("plots/{plotId}/agronomic-dossiers/{campaignYear}")
    suspend fun downloadPdf(
        @Path("plotId") plotId: String,
        @Path("campaignYear") campaignYear: Int,
    ): Response<ResponseBody>

    /** 200 when the code matches a dossier stored by Viora, 404 when it matches none. */
    @GET("agronomic-dossiers/verifications/{verificationHash}")
    suspend fun verify(@Path("verificationHash") verificationHash: String): Response<DossierVerificationDto>
}
