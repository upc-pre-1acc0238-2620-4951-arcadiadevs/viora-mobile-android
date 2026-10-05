package pe.edu.upc.viora.features.harvestsettlement.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upc.viora.core.database.AppDatabase
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.HarvestSettlementRepository
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.HarvestSettlementDao
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.HarvestSettlementService
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.repository.HarvestSettlementRepositoryImpl
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object HarvestSettlementModule {

    @Provides
    @Singleton
    fun provideHarvestSettlementService(retrofit: Retrofit): HarvestSettlementService =
        retrofit.create(HarvestSettlementService::class.java)

    @Provides
    fun provideHarvestSettlementDao(database: AppDatabase): HarvestSettlementDao =
        database.harvestSettlementDao()
}

@Module
@InstallIn(SingletonComponent::class)
interface HarvestSettlementRepositoryModule {

    @Binds
    fun bindHarvestSettlementRepository(impl: HarvestSettlementRepositoryImpl): HarvestSettlementRepository
}
