package pe.edu.upc.viora.features.assistant.infrastructure.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import pe.edu.upc.viora.di.AppDatabase
import pe.edu.upc.viora.features.assistant.infrastructure.local.AssistantAnswerDao
import pe.edu.upc.viora.features.assistant.infrastructure.remote.AssistantService
import retrofit2.Retrofit
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AssistantModule {

    @Provides
    @Singleton
    fun provideAssistantService(retrofit: Retrofit): AssistantService = retrofit.create(AssistantService::class.java)

    @Provides
    fun provideAssistantAnswerDao(database: AppDatabase): AssistantAnswerDao = database.assistantAnswerDao()
}
