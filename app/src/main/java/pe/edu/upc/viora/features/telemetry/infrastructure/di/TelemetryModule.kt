package pe.edu.upc.viora.features.telemetry.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upc.viora.core.database.AppDatabase
import pe.edu.upc.viora.features.telemetry.infrastructure.local.IncidentDao
import pe.edu.upc.viora.features.telemetry.infrastructure.local.SensorNodeDao
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.IncidentService
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.SensorService
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object TelemetryModule {

    @Provides
    @Singleton
    fun provideSensorService(retrofit: Retrofit): SensorService =
        retrofit.create(SensorService::class.java)

    @Provides
    fun provideSensorNodeDao(database: AppDatabase): SensorNodeDao = database.sensorNodeDao()

    @Provides
    @Singleton
    fun provideIncidentService(retrofit: Retrofit): IncidentService =
        retrofit.create(IncidentService::class.java)

    @Provides
    fun provideIncidentDao(database: AppDatabase): IncidentDao = database.incidentDao()
}
