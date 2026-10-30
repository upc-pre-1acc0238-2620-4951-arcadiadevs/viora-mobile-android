package pe.edu.upc.viora.features.harvestsettlement.infrastructure.di

import android.content.Context
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Named
import javax.inject.Singleton
import pe.edu.upc.viora.di.AppDatabase
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.AgronomicDossierRepository
import pe.edu.upc.viora.features.harvestsettlement.domain.repository.HarvestSettlementRepository
import pe.edu.upc.viora.features.harvestsettlement.domain.service.SettlementSyncScheduler
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.AgronomicDossierDao
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.HarvestSettlementDao
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.PendingSettlementDao
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.AgronomicDossierService
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.remote.HarvestSettlementService
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.repository.AgronomicDossierRepositoryImpl
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.repository.HarvestSettlementRepositoryImpl
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.sync.WorkManagerSettlementSyncScheduler
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object HarvestSettlementModule {

    @Provides
    @Singleton
    fun provideHarvestSettlementService(retrofit: Retrofit): HarvestSettlementService =
        retrofit.create(HarvestSettlementService::class.java)

    @Provides
    @Singleton
    fun provideAgronomicDossierService(retrofit: Retrofit): AgronomicDossierService =
        retrofit.create(AgronomicDossierService::class.java)

    @Provides
    @Named(AgronomicDossierRepositoryImpl.DOSSIER_DIR)
    fun provideDossierDirectory(@ApplicationContext context: Context): File =
        File(context.filesDir, AgronomicDossierRepositoryImpl.DOSSIER_DIR)

    @Provides
    fun provideAgronomicDossierDao(database: AppDatabase): AgronomicDossierDao =
        database.agronomicDossierDao()

    @Provides
    fun provideHarvestSettlementDao(database: AppDatabase): HarvestSettlementDao =
        database.harvestSettlementDao()

    @Provides
    fun providePendingSettlementDao(database: AppDatabase): PendingSettlementDao =
        database.pendingSettlementDao()
}

@Module
@InstallIn(SingletonComponent::class)
interface HarvestSettlementRepositoryModule {

    @Binds
    fun bindHarvestSettlementRepository(impl: HarvestSettlementRepositoryImpl): HarvestSettlementRepository

    @Binds
    fun bindAgronomicDossierRepository(impl: AgronomicDossierRepositoryImpl): AgronomicDossierRepository

    @Binds
    fun bindSettlementSyncScheduler(impl: WorkManagerSettlementSyncScheduler): SettlementSyncScheduler
}
