package pe.edu.upc.viora.features.plotmanagement.infrastructure.remote

import kotlinx.serialization.Serializable
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PUT
import retrofit2.http.Path

/** The sectors of the license that sponsors the member, and the sector of each of their plots. */
interface PlotSectorService {

    /** 200 the license's list; 403 `territory.producer.not_sponsored` for a producer without a sponsored plan. */
    @GET("sector-zones")
    suspend fun sectors(): Response<List<SectorZoneDto>>

    /** The member's own plots that have a sector, chosen or written. */
    @GET("plot-sector-assignments")
    suspend fun plotSectors(): Response<List<PlotSectorAssignmentDto>>

    /** Send the id of a listed sector, or the name the member wrote; never both. */
    @PUT("plot-sector-assignments/{plotId}")
    suspend fun assign(@Path("plotId") plotId: String, @Body body: AssignPlotSectorRequestDto): Response<PlotSectorAssignmentDto>
}

@Serializable
data class SectorZoneDto(val id: String, val name: String)

@Serializable
data class PlotSectorAssignmentDto(
    val plotId: String,
    val status: String,
    val sectorZoneId: String? = null,
    val sectorZoneName: String? = null,
    val proposedName: String? = null,
)

@Serializable
data class AssignPlotSectorRequestDto(val sectorZoneId: String? = null, val name: String? = null)
