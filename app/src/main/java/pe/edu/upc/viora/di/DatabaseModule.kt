package pe.edu.upc.viora.di

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
import pe.edu.upc.viora.core.database.CacheMetadataDao

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

    /**
     * Everything the phone keeps for an account, wiped in order: pending uploads stop first so none runs
     * against wiped data, then what each context registered (its tables, files and preferences), then the
     * cache metadata core keeps and the copies prepared to share.
     */
    @Provides
    fun provideAccountDataWiper(
        cacheMetadata: CacheMetadataDao,
        @ApplicationContext context: Context,
        contextWipers: Set<@JvmSuppressWildcards AccountDataWiper>,
    ): AccountDataWiper =
        AccountDataWiper {
            WorkManager.getInstance(context).cancelAllWork()
            contextWipers.forEach { it.wipe() }
            cacheMetadata.wipeAll()
            File(context.cacheDir, SHARED_CACHE_DIR).deleteRecursively()
        }

    /** Where alerts and dossiers are copied to be shared (the FileProvider's cache path). */
    private const val SHARED_CACHE_DIR = "shared"
}
