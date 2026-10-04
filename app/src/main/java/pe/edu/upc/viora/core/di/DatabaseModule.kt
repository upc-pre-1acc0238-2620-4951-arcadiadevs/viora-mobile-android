package pe.edu.upc.viora.core.di

import android.content.Context
import androidx.room3.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upc.viora.core.database.AppDatabase
import pe.edu.upc.viora.core.database.AppMigrations
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
}
