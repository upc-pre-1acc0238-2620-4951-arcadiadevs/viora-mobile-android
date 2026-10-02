package pe.edu.upc.viora.features.plotmanagement.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upc.viora.core.database.AppDatabase
import pe.edu.upc.viora.features.plotmanagement.infrastructure.local.PlotDao
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.PlotService
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object PlotModule {

    @Provides
    @Singleton
    fun providePlotService(retrofit: Retrofit): PlotService = retrofit.create(PlotService::class.java)

    @Provides
    fun providePlotDao(database: AppDatabase): PlotDao = database.plotDao()
}
