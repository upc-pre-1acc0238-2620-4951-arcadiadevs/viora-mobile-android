package pe.edu.upc.viora.features.home.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.viora.features.home.domain.repository.FocusedPlotRepository
import pe.edu.upc.viora.features.home.infrastructure.repository.FocusedPlotRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
interface FocusedPlotModule {

    @Binds
    fun bindFocusedPlotRepository(impl: FocusedPlotRepositoryImpl): FocusedPlotRepository
}
