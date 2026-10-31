package pe.edu.upc.viora.features.assistant.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import pe.edu.upc.viora.core.database.AccountDataWiper
import pe.edu.upc.viora.features.assistant.infrastructure.local.AssistantTablesWiper

/** Signing out (or another account signing in) empties the answers kept on the phone. */
@Module
@InstallIn(SingletonComponent::class)
abstract class AssistantTablesWiperModule {

    @Binds
    @IntoSet
    abstract fun bindAssistantTablesWiper(wiper: AssistantTablesWiper): AccountDataWiper
}
