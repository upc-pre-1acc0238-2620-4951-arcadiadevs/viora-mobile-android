package pe.edu.upc.viora.features.subscription.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upc.viora.features.subscription.domain.repository.ProducerSubscriptionRepository
import pe.edu.upc.viora.features.subscription.infrastructure.remote.ProducerSubscriptionService
import pe.edu.upc.viora.features.subscription.infrastructure.repository.ProducerSubscriptionRepositoryImpl
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object ProducerSubscriptionModule {
    @Provides
    @Singleton
    fun provideProducerSubscriptionService(retrofit: Retrofit): ProducerSubscriptionService =
        retrofit.create(ProducerSubscriptionService::class.java)
}

@Module
@InstallIn(SingletonComponent::class)
interface ProducerSubscriptionBindings {
    @Binds
    @Singleton
    fun bindProducerSubscriptionRepository(impl: ProducerSubscriptionRepositoryImpl): ProducerSubscriptionRepository
}
