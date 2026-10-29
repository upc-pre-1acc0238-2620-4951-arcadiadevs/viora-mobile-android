package pe.edu.upc.viora.features.subscription.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import dagger.multibindings.IntoSet
import pe.edu.upc.viora.core.database.AccountDataWiper
import pe.edu.upc.viora.features.subscription.infrastructure.local.PlanExtensionAccountDataWiper

/** Signing out (or another account signing in) also clears the pending plan extension. */
@Module
@InstallIn(SingletonComponent::class)
abstract class SubscriptionDataWiperModule {

    @Binds
    @IntoSet
    abstract fun bindPlanExtensionAccountDataWiper(wiper: PlanExtensionAccountDataWiper): AccountDataWiper
}
