package pe.edu.upc.viora.features.subscription.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upc.viora.di.AppDatabase
import pe.edu.upc.viora.core.di.SessionFree
import pe.edu.upc.viora.features.subscription.domain.repository.ProducerPlanRepository
import pe.edu.upc.viora.features.subscription.infrastructure.local.ProducerPlanDao
import pe.edu.upc.viora.features.subscription.infrastructure.remote.ProducerPlanService
import pe.edu.upc.viora.features.subscription.infrastructure.repository.ProducerPlanRepositoryImpl
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object ProducerPlanModule {
    @Provides
    @Singleton
    fun provideProducerPlanService(@SessionFree retrofit: Retrofit): ProducerPlanService =
        retrofit.create(ProducerPlanService::class.java)

    @Provides
    fun provideProducerPlanDao(database: AppDatabase): ProducerPlanDao = database.producerPlanDao()
}

@Module
@InstallIn(SingletonComponent::class)
interface ProducerPlanRepositoryModule {
    @Binds
    @Singleton
    fun bindProducerPlanRepository(impl: ProducerPlanRepositoryImpl): ProducerPlanRepository
}
