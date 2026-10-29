package pe.edu.upc.viora.features.harvestsettlement.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import pe.edu.upc.viora.core.database.AccountDataWiper
import pe.edu.upc.viora.features.harvestsettlement.infrastructure.local.SettlementTablesWiper

/** Signing out (or another account signing in) empties the tables of this context. */
@Module
@InstallIn(SingletonComponent::class)
abstract class SettlementTablesWiperModule {

    @Binds
    @IntoSet
    abstract fun bindSettlementTablesWiper(wiper: SettlementTablesWiper): AccountDataWiper
}
