package pe.edu.upc.viora.features.assistant.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.viora.features.assistant.domain.repository.AssistantRepository
import pe.edu.upc.viora.features.assistant.domain.service.DailySummaryReminder
import pe.edu.upc.viora.features.assistant.infrastructure.notification.WorkManagerDailySummaryReminder
import pe.edu.upc.viora.features.assistant.infrastructure.repository.AssistantRepositoryImpl

@Module
@InstallIn(SingletonComponent::class)
interface AssistantRepositoryModule {

    @Binds
    fun bindAssistantRepository(impl: AssistantRepositoryImpl): AssistantRepository

    @Binds
    fun bindDailySummaryReminder(impl: WorkManagerDailySummaryReminder): DailySummaryReminder
}
