package pe.edu.upc.viora.features.plotmanagement.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import javax.inject.Singleton
import pe.edu.upc.viora.core.database.AccountDataWiper
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotSectorRepository
import pe.edu.upc.viora.features.plotmanagement.domain.repository.SectorPromptRepository
import pe.edu.upc.viora.features.plotmanagement.infrastructure.local.SectorPromptRepositoryImpl
import pe.edu.upc.viora.features.plotmanagement.infrastructure.local.SectorPromptWiper
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.PlotSectorService
import pe.edu.upc.viora.features.plotmanagement.infrastructure.repository.PlotSectorRepositoryImpl
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object PlotSectorModule {

    @Provides
    @Singleton
    fun providePlotSectorService(retrofit: Retrofit): PlotSectorService = retrofit.create(PlotSectorService::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
interface PlotSectorBindingsModule {

    @Binds
    fun bindPlotSectorRepository(impl: PlotSectorRepositoryImpl): PlotSectorRepository

    @Binds
    fun bindSectorPromptRepository(impl: SectorPromptRepositoryImpl): SectorPromptRepository

    /** Signing out (or another account signing in) asks P06b again. */
    @Binds
    @IntoSet
    fun bindSectorPromptWiper(wiper: SectorPromptWiper): AccountDataWiper
}
