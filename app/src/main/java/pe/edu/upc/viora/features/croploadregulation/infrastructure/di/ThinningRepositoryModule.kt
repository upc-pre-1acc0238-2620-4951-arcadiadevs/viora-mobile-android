package pe.edu.upc.viora.features.croploadregulation.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.viora.features.croploadregulation.domain.repository.ThinningRepository
import pe.edu.upc.viora.features.croploadregulation.infrastructure.repository.ThinningRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
interface ThinningRepositoryModule {

    @Binds
    fun bindThinningRepository(impl: ThinningRepositoryImpl): ThinningRepository
}
