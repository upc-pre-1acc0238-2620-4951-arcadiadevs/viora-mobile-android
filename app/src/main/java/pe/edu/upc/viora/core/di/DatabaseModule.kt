package pe.edu.upc.viora.core.di

import android.content.Context
import androidx.room3.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import java.io.File
import pe.edu.upc.viora.core.database.AccountDataWiper
import pe.edu.upc.viora.core.database.AppDatabase
import pe.edu.upc.viora.core.database.AppMigrations
import pe.edu.upc.viora.core.database.CacheMetadataDao
import pe.edu.upc.viora.features.plotmanagement.infrastructure.repository.FieldNoteRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase =
        Room.databaseBuilder<AppDatabase>(context, "viora.db")
            .addMigrations(*AppMigrations.ALL)
            .build()

    @Provides
    fun provideCacheMetadataDao(database: AppDatabase): CacheMetadataDao =
        database.cacheMetadataDao()

    @Provides
    fun provideAccountDataWiper(database: AppDatabase, @ApplicationContext context: Context): AccountDataWiper =
        AccountDataWiper {
            database.clearAllTables()
            // Photos of notes that never reached the server belong to the previous account too.
            File(context.filesDir, FieldNoteRepositoryImpl.PHOTO_DIR).deleteRecursively()
        }
}
