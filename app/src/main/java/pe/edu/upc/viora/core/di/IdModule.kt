package pe.edu.upc.viora.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upc.viora.core.domain.IdGenerator
import pe.edu.upc.viora.core.domain.RandomIdGenerator

/** Injectable identifier source so code that issues ids or idempotency keys can be tested with fixed ones. */
@Module
@InstallIn(SingletonComponent::class)
object IdModule {

    @Provides
    @Singleton
    fun provideIdGenerator(): IdGenerator = RandomIdGenerator()
}
