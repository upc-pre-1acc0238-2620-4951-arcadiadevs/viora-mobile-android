package pe.edu.upc.viora.features.plotmanagement.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import pe.edu.upc.viora.core.database.AccountDataWiper
import pe.edu.upc.viora.features.plotmanagement.infrastructure.local.FieldNotePhotosWiper

/** Signing out (or another account signing in) also clears the field-note photos. */
@Module
@InstallIn(SingletonComponent::class)
abstract class FieldNoteDataWiperModule {

    @Binds
    @IntoSet
    abstract fun bindFieldNotePhotosWiper(wiper: FieldNotePhotosWiper): AccountDataWiper
}
