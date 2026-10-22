package pe.edu.upc.viora.features.profiles.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upc.viora.features.profiles.domain.repository.ProfileRepository
import pe.edu.upc.viora.features.profiles.infrastructure.remote.ProfileService
import pe.edu.upc.viora.features.profiles.infrastructure.repository.ProfileRepositoryImpl
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class ProfilesModule {

    @Binds
    @Singleton
    abstract fun bindProfileRepository(impl: ProfileRepositoryImpl): ProfileRepository

    companion object {
        @Provides
        @Singleton
        fun provideProfileService(retrofit: Retrofit): ProfileService =
            retrofit.create(ProfileService::class.java)
    }
}
