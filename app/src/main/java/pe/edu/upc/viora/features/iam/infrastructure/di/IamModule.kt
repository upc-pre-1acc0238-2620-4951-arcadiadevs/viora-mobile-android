package pe.edu.upc.viora.features.iam.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upc.viora.features.iam.infrastructure.remote.IamService
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object IamModule {
    @Provides
    @Singleton
    fun provideIamService(retrofit: Retrofit): IamService = retrofit.create(IamService::class.java)
}
