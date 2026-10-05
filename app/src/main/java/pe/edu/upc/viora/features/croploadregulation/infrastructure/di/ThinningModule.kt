package pe.edu.upc.viora.features.croploadregulation.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upc.viora.core.database.AppDatabase
import pe.edu.upc.viora.features.croploadregulation.infrastructure.local.DraftTreeSampleDao
import pe.edu.upc.viora.features.croploadregulation.infrastructure.remote.ThinningService
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object ThinningModule {

    @Provides
    @Singleton
    fun provideThinningService(retrofit: Retrofit): ThinningService =
        retrofit.create(ThinningService::class.java)

    @Provides
    fun provideDraftTreeSampleDao(database: AppDatabase): DraftTreeSampleDao =
        database.draftTreeSampleDao()
}
