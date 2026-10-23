package pe.edu.upc.viora.features.subscription.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import retrofit2.Retrofit
import pe.edu.upc.viora.core.database.AppDatabase
import pe.edu.upc.viora.features.subscription.domain.repository.IndividualPurchaseRepository
import pe.edu.upc.viora.features.subscription.infrastructure.repository.IndividualPurchaseRepositoryImpl
import pe.edu.upc.viora.features.subscription.infrastructure.remote.IndividualPurchaseService

@Module
@InstallIn(SingletonComponent::class)
object IndividualPurchaseModule {
    @Provides @Singleton
    fun service(retrofit: Retrofit): IndividualPurchaseService = retrofit.create(IndividualPurchaseService::class.java)
    @Provides
    fun dao(database: AppDatabase) = database.individualPurchaseDao()
}

@Module
@InstallIn(SingletonComponent::class)
interface IndividualPurchaseBindings {
    @Binds @Singleton
    fun repository(impl: IndividualPurchaseRepositoryImpl): IndividualPurchaseRepository
}
