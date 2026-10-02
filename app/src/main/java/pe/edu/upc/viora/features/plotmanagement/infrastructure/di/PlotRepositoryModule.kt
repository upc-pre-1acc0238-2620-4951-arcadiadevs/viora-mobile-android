package pe.edu.upc.viora.features.plotmanagement.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.viora.features.plotmanagement.domain.repository.PlotRepository
import pe.edu.upc.viora.features.plotmanagement.infrastructure.repository.PlotRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
interface PlotRepositoryModule {

    @Binds
    fun bindPlotRepository(impl: PlotRepositoryImpl): PlotRepository
}
