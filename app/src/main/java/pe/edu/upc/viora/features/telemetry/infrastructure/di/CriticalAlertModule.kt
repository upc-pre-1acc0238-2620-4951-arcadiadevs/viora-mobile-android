package pe.edu.upc.viora.features.telemetry.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.viora.features.telemetry.domain.notification.CriticalAlertNotifier
import pe.edu.upc.viora.features.telemetry.domain.notification.CriticalAlertScheduler
import pe.edu.upc.viora.features.telemetry.domain.notification.CriticalAlertSettings
import pe.edu.upc.viora.features.telemetry.infrastructure.notification.AndroidCriticalAlertNotifier
import pe.edu.upc.viora.features.telemetry.infrastructure.notification.CriticalAlertPreferences
import pe.edu.upc.viora.features.telemetry.infrastructure.notification.WorkManagerCriticalAlertScheduler

@Module
@InstallIn(SingletonComponent::class)
abstract class CriticalAlertModule {
    @Binds abstract fun settings(impl: CriticalAlertPreferences): CriticalAlertSettings
    @Binds abstract fun notifier(impl: AndroidCriticalAlertNotifier): CriticalAlertNotifier
    @Binds abstract fun scheduler(impl: WorkManagerCriticalAlertScheduler): CriticalAlertScheduler
}
