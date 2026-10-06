package pe.edu.upc.viora.features.telemetry.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upc.viora.core.database.AppDatabase
import pe.edu.upc.viora.features.telemetry.infrastructure.local.ForecastDayDao
import pe.edu.upc.viora.features.telemetry.infrastructure.local.TelemetryReadingDao
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.ForecastService
import pe.edu.upc.viora.features.telemetry.infrastructure.remote.TelemetryService
import retrofit2.Retrofit

/** Services and DAOs of the plot climate (US17 series, US19 forecast). */
@Module
@InstallIn(SingletonComponent::class)
object ClimateModule {

    @Provides
    @Singleton
    fun provideTelemetryService(retrofit: Retrofit): TelemetryService =
        retrofit.create(TelemetryService::class.java)

    @Provides
    @Singleton
    fun provideForecastService(retrofit: Retrofit): ForecastService =
        retrofit.create(ForecastService::class.java)

    @Provides
    fun provideTelemetryReadingDao(database: AppDatabase): TelemetryReadingDao = database.telemetryReadingDao()

    @Provides
    fun provideForecastDayDao(database: AppDatabase): ForecastDayDao = database.forecastDayDao()
}
