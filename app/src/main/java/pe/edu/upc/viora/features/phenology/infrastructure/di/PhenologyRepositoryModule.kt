package pe.edu.upc.viora.features.phenology.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.viora.features.phenology.domain.repository.ChillRepository
import pe.edu.upc.viora.features.phenology.domain.repository.HarvestRecordRepository
import pe.edu.upc.viora.features.phenology.domain.repository.PhaseAdjustmentsRepository
import pe.edu.upc.viora.features.phenology.infrastructure.repository.ChillRepositoryImpl
import pe.edu.upc.viora.features.phenology.infrastructure.repository.HarvestRecordRepositoryImpl
import pe.edu.upc.viora.features.phenology.infrastructure.repository.PhaseAdjustmentsRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
interface PhenologyRepositoryModule {

    @Binds
    fun bindHarvestRecordRepository(impl: HarvestRecordRepositoryImpl): HarvestRecordRepository

    @Binds
    fun bindChillRepository(impl: ChillRepositoryImpl): ChillRepository

    @Binds
    fun bindPhaseAdjustmentsRepository(impl: PhaseAdjustmentsRepositoryImpl): PhaseAdjustmentsRepository
}
