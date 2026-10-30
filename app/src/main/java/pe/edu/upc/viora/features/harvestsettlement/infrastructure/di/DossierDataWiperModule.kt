package pe.edu.upc.viora.features.harvestsettlement.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import pe.edu.upc.viora.core.database.AccountDataWiper
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.DossierFilesWiper

/** Signing out (or another account signing in) also clears the downloaded dossiers. */
@Module
@InstallIn(SingletonComponent::class)
abstract class DossierDataWiperModule {

    @Binds
    @IntoSet
    abstract fun bindDossierFilesWiper(wiper: DossierFilesWiper): AccountDataWiper
}
