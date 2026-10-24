package pe.edu.upc.viora.features.plotmanagement.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upc.viora.core.database.AppDatabase
import pe.edu.upc.viora.features.plotmanagement.domain.repository.FieldNoteRepository
import pe.edu.upc.viora.features.plotmanagement.domain.service.FieldNoteSyncScheduler
import pe.edu.upc.viora.features.plotmanagement.domain.service.NotePhotoStore
import pe.edu.upc.viora.features.plotmanagement.infrastructure.local.FieldNoteDao
import pe.edu.upc.viora.features.plotmanagement.infrastructure.photo.JpegNotePhotoStore
import pe.edu.upc.viora.features.plotmanagement.infrastructure.remote.FieldNoteService
import pe.edu.upc.viora.features.plotmanagement.infrastructure.repository.FieldNoteRepositoryImpl
import pe.edu.upc.viora.features.plotmanagement.infrastructure.sync.WorkManagerFieldNoteSyncScheduler
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
object FieldNoteModule {

    @Provides
    @Singleton
    fun provideFieldNoteService(retrofit: Retrofit): FieldNoteService = retrofit.create(FieldNoteService::class.java)

    @Provides
    fun provideFieldNoteDao(database: AppDatabase): FieldNoteDao = database.fieldNoteDao()
}

@Module
@InstallIn(SingletonComponent::class)
interface FieldNoteBindingsModule {

    @Binds
    fun bindFieldNoteRepository(impl: FieldNoteRepositoryImpl): FieldNoteRepository

    @Binds
    fun bindFieldNoteSyncScheduler(impl: WorkManagerFieldNoteSyncScheduler): FieldNoteSyncScheduler

    @Binds
    fun bindNotePhotoStore(impl: JpegNotePhotoStore): NotePhotoStore
}
