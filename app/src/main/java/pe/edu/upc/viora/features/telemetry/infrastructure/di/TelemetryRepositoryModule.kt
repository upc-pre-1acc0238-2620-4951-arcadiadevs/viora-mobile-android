package pe.edu.upc.viora.features.telemetry.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.viora.features.telemetry.domain.repository.IncidentRepository
import pe.edu.upc.viora.features.telemetry.domain.repository.SensorRepository
import pe.edu.upc.viora.features.telemetry.infrastructure.repository.IncidentRepositoryImpl
import pe.edu.upc.viora.features.telemetry.infrastructure.repository.SensorRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
interface TelemetryRepositoryModule {

    @Binds
    fun bindSensorRepository(impl: SensorRepositoryImpl): SensorRepository

    @Binds
    fun bindIncidentRepository(impl: IncidentRepositoryImpl): IncidentRepository
}
