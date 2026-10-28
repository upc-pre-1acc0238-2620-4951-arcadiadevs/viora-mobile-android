package pe.edu.upc.viora.core.di

import android.content.Context
import androidx.room3.Room
import androidx.work.WorkManager
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import java.io.File
import javax.inject.Singleton
import pe.edu.upc.viora.core.database.AccountDataWiper
import pe.edu.upc.viora.core.database.AppDatabase
import pe.edu.upc.viora.core.database.AppMigrations
import pe.edu.upc.viora.core.database.CacheMetadataDao
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.repository.AgronomicDossierRepositoryImpl
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
            // Pending uploads of the account stop first, so none runs against the wiped data.
            WorkManager.getInstance(context).cancelAllWork()
            database.clearAllTables()
            // Photos of notes that never reached the server belong to the previous account too.
            File(context.filesDir, FieldNoteRepositoryImpl.PHOTO_DIR).deleteRecursively()
            // So do the sealed dossiers downloaded and the files prepared to share.
            File(context.filesDir, AgronomicDossierRepositoryImpl.DOSSIER_DIR).deleteRecursively()
            File(context.cacheDir, SHARED_CACHE_DIR).deleteRecursively()
        }

    /** Where alerts and dossiers are copied to be shared (the FileProvider's cache path). */
    private const val SHARED_CACHE_DIR = "shared"
}
