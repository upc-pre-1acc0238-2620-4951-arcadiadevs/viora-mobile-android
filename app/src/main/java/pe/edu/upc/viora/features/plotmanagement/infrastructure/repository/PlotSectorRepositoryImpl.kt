package pe.edu.upc.viora.features.plotmanagement.infrastructure.repository

import javax.inject.Inject
import pe.edu.upc.viora.core.domain.AppError
import pe.edu.upc.viora.core.domain.AppResult
import pe.edu.upc.viora.core.network.ApiCaller
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotSector
import pe.edu.upc.viora.features.plotmanagement.domain.entity.PlotSectorStatus
import pe.edu.upc.viora.features.plotmanagement.domain.entity.SectorChoice
import pe.edu.upc.viora.features.plotmanagement.domain.entity.SectorZone
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotSectorRepository
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.AssignPlotSectorRequestDto
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.PlotSectorAssignmentDto
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.PlotSectorService

class PlotSectorRepositoryImpl @Inject constructor(
    private val service: PlotSectorService,
    private val apiCaller: ApiCaller,
) : PlotSectorRepository {

    override suspend fun sectors(): AppResult<List<SectorZone>> =
        when (val result = apiCaller.call { service.sectors() }) {
            is AppResult.Success -> AppResult.Success(result.value.map { SectorZone(it.id, it.name) })
            is AppResult.Failure -> result
        }

    override suspend fun plotSectors(): AppResult<List<PlotSector>> =
        when (val result = apiCaller.call { service.plotSectors() }) {
            // A status this app does not know is skipped: the plot simply reads as without a sector.
            is AppResult.Success -> AppResult.Success(result.value.mapNotNull { it.toDomain() })
            is AppResult.Failure -> result
        }

    override suspend fun assign(plotId: String, choice: SectorChoice): AppResult<PlotSector> {
        val body = when (choice) {
            is SectorChoice.Listed -> AssignPlotSectorRequestDto(sectorZoneId = choice.sector.id)
            is SectorChoice.Written -> AssignPlotSectorRequestDto(name = choice.name.trim())
        }
        return when (val result = apiCaller.call { service.assign(plotId, body) }) {
            is AppResult.Success -> result.value.toDomain()?.let { AppResult.Success(it) } ?: AppResult.Failure(AppError.Unknown())
            is AppResult.Failure -> result
        }
    }
}

internal fun PlotSectorAssignmentDto.toDomain(): PlotSector? {
    val status = PlotSectorStatus.entries.firstOrNull { it.name == status } ?: return null
    return PlotSector(
        plotId = plotId,
        status = status,
        sectorName = sectorZoneName?.takeIf { it.isNotBlank() },
        proposedName = proposedName?.takeIf { it.isNotBlank() },
    )
}
