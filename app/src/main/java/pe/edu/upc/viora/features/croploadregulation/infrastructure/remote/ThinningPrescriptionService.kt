package pe.edu.upc.viora.features.croploadregulation.infrastructure.remote

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path
import retrofit2.http.Query

interface ThinningPrescriptionService {

    /** The plan of a plot in whatever state it is; 404 when nothing was sampled for the campaign (P61). */
    @GET("plots/{plotId}/thinning-prescriptions")
    suspend fun getPrescription(
        @Path("plotId") plotId: String,
        @Query("campaignYear") campaignYear: Int,
    ): Response<PrescriptionResponseDto>

    /** Records or corrects the observed full bloom; issues the prescription when it was the last input (P64). */
    @PUT("plots/{plotId}/thinning-prescriptions/full-bloom")
    suspend fun recordFullBloom(
        @Path("plotId") plotId: String,
        @Body request: RecordFullBloomRequestDto,
    ): Response<PrescriptionResponseDto>

    /** Records the thinning done in the field (P62); a date after the window is accepted and marked LATE. */
    @POST("thinning-prescriptions/{prescriptionId}/execution-confirmations")
    suspend fun confirmExecution(
        @Path("prescriptionId") prescriptionId: String,
        @Body request: ConfirmExecutionRequestDto,
    ): Response<ExecutionConfirmationResponseDto>

    /** The recorded thinning with its load, caliber and yield (P63); 404 while it was not recorded. */
    @GET("thinning-prescriptions/{prescriptionId}/execution-confirmations")
    suspend fun getExecution(@Path("prescriptionId") prescriptionId: String): Response<ExecutionConfirmationResponseDto>
}
