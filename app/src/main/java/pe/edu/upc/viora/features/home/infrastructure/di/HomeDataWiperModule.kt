package pe.edu.upc.viora.features.home.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import pe.edu.upc.viora.core.database.AccountDataWiper
import pe.edu.upc.viora.features.home.infrastructure.local.HomeAccountDataWiper

/** Signing out (or another account signing in) also clears the Home preferences. */
@Module
@InstallIn(SingletonComponent::class)
abstract class HomeDataWiperModule {

    @Binds
    @IntoSet
    abstract fun bindHomeAccountDataWiper(wiper: HomeAccountDataWiper): AccountDataWiper
}
