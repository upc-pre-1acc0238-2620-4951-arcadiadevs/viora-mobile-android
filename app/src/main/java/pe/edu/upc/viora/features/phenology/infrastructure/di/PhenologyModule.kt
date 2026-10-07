package pe.edu.upc.viora.features.phenology.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upc.viora.core.database.AppDatabase
import pe.edu.upc.viora.features.phenology.infrastructure.local.BearingIndexDao
import pe.edu.upc.viora.features.phenology.infrastructure.local.ChillTrackerDao
import pe.edu.upc.viora.features.phenology.infrastructure.local.HarvestRecordDao
import pe.edu.upc.viora.features.phenology.infrastructure.remote.PhenologyService
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object PhenologyModule {

    @Provides
    @Singleton
    fun providePhenologyService(retrofit: Retrofit): PhenologyService =
        retrofit.create(PhenologyService::class.java)

    @Provides
    fun provideHarvestRecordDao(database: AppDatabase): HarvestRecordDao = database.harvestRecordDao()

    @Provides
    fun provideBearingIndexDao(database: AppDatabase): BearingIndexDao = database.bearingIndexDao()

    @Provides
    fun provideChillTrackerDao(database: AppDatabase): ChillTrackerDao = database.chillTrackerDao()
}
