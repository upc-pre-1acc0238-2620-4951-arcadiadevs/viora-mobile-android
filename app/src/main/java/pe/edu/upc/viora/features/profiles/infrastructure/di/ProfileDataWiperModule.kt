package pe.edu.upc.viora.features.profiles.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import pe.edu.upc.viora.core.database.AccountDataWiper
import pe.edu.upc.viora.features.profiles.infrastructure.local.ProfileAccountDataWiper

/** Signing out, deleting the account or another account signing in also forgets the kept profile. */
@Module
@InstallIn(SingletonComponent::class)
abstract class ProfileDataWiperModule {

    @Binds
    @IntoSet
    abstract fun bindProfileAccountDataWiper(wiper: ProfileAccountDataWiper): AccountDataWiper
}
