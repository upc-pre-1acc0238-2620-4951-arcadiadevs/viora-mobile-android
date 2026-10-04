package pe.edu.upc.viora.features.home.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.viora.features.home.domain.repository.HomeTourRepository
import pe.edu.upc.viora.features.home.infrastructure.repository.HomeTourRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
interface HomeTourModule {

    @Binds
    fun bindHomeTourRepository(impl: HomeTourRepositoryImpl): HomeTourRepository
}
