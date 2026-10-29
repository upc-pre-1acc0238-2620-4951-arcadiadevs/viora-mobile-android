package pe.edu.upc.viora.features.telemetry.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import pe.edu.upc.viora.core.database.AccountDataWiper
import pe.edu.upc.viora.features.telemetry.infrastructure.local.TelemetryTablesWiper

/** Signing out (or another account signing in) empties the tables of this context. */
@Module
@InstallIn(SingletonComponent::class)
abstract class TelemetryTablesWiperModule {

    @Binds
    @IntoSet
    abstract fun bindTelemetryTablesWiper(wiper: TelemetryTablesWiper): AccountDataWiper
}
