package pe.edu.upc.viora.features.subscription.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upc.viora.features.subscription.domain.repository.CooperativeCodeRepository
import pe.edu.upc.viora.features.subscription.domain.repository.DataSharingConsentRepository
import pe.edu.upc.viora.features.subscription.infrastructure.remote.CooperativeCodeRedemptionService
import pe.edu.upc.viora.features.subscription.infrastructure.remote.DataSharingConsentService
import pe.edu.upc.viora.features.subscription.infrastructure.repository.CooperativeCodeRepositoryImpl
import pe.edu.upc.viora.features.subscription.infrastructure.repository.DataSharingConsentRepositoryImpl
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object SubscriptionModule {

    @Provides
    @Singleton
    fun provideCooperativeCodeRedemptionService(retrofit: Retrofit): CooperativeCodeRedemptionService =
        retrofit.create(CooperativeCodeRedemptionService::class.java)

    @Provides
    @Singleton
    fun provideDataSharingConsentService(retrofit: Retrofit): DataSharingConsentService =
        retrofit.create(DataSharingConsentService::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
interface SubscriptionRepositoryModule {

    @Binds
    fun bindCooperativeCodeRepository(impl: CooperativeCodeRepositoryImpl): CooperativeCodeRepository

    @Binds
    fun bindDataSharingConsentRepository(impl: DataSharingConsentRepositoryImpl): DataSharingConsentRepository
}
