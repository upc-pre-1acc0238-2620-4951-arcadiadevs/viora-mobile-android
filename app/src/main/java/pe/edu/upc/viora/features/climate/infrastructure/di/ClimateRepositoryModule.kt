package pe.edu.upc.viora.features.climate.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.viora.features.climate.domain.repository.WeatherForecastRepository
import pe.edu.upc.viora.features.climate.infrastructure.repository.WeatherForecastRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
interface ClimateRepositoryModule {

    @Binds
    fun bindWeatherForecastRepository(impl: WeatherForecastRepositoryImpl): WeatherForecastRepository
}
