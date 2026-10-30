package pe.edu.upc.viora.features.croploadregulation.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.viora.features.croploadregulation.domain.service.SamplingSyncScheduler
import pe.edu.upc.viora.features.croploadregulation.infrastructure.sync.WorkManagerSamplingSyncScheduler

@Module
@InstallIn(SingletonComponent::class)
abstract class SamplingSyncModule {

    @Binds
    abstract fun bindSamplingSyncScheduler(impl: WorkManagerSamplingSyncScheduler): SamplingSyncScheduler
}
