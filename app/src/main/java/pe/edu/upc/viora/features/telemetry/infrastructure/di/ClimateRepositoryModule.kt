package pe.edu.upc.viora.features.telemetry.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.viora.features.telemetry.domain.repository.ForecastRepository
import pe.edu.upc.viora.features.telemetry.domain.repository.TelemetryRepository
import pe.edu.upc.viora.features.telemetry.infrastructure.repository.ForecastRepositoryImpl
import pe.edu.upc.viora.features.telemetry.infrastructure.repository.TelemetryRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
interface ClimateRepositoryModule {

    @Binds
    fun bindTelemetryRepository(impl: TelemetryRepositoryImpl): TelemetryRepository

    @Binds
    fun bindForecastRepository(impl: ForecastRepositoryImpl): ForecastRepository
}
