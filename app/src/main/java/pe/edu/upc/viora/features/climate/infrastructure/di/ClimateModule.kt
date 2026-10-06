package pe.edu.upc.viora.features.climate.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upc.viora.core.database.AppDatabase
import pe.edu.upc.viora.features.climate.infrastructure.local.WeatherForecastDao
import pe.edu.upc.viora.features.climate.infrastructure.remote.ClimateService
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object ClimateModule {

    @Provides
    @Singleton
    fun provideClimateService(retrofit: Retrofit): ClimateService =
        retrofit.create(ClimateService::class.java)

    @Provides
    fun provideWeatherForecastDao(database: AppDatabase): WeatherForecastDao =
        database.weatherForecastDao()
}
