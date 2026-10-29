package pe.edu.upc.viora.features.iam.infrastructure.di

import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton
import pe.edu.upc.viora.core.di.SessionFree
import pe.edu.upc.viora.core.network.SessionRefresher
import pe.edu.upc.viora.features.iam.domain.repository.AuthRepository
import pe.edu.upc.viora.features.iam.domain.repository.OnboardingAnswersRepository
import pe.edu.upc.viora.features.iam.infrastructure.local.OnboardingStore
import pe.edu.upc.viora.features.iam.infrastructure.remote.AccountService
import pe.edu.upc.viora.features.iam.infrastructure.remote.IamService
import pe.edu.upc.viora.features.iam.infrastructure.repository.AuthRepositoryImpl
import pe.edu.upc.viora.features.iam.infrastructure.session.IamSessionRefresher
import retrofit2.Retrofit

@Module
@InstallIn(SingletonComponent::class)
abstract class IamModule {

    @Binds
    abstract fun bindSessionRefresher(refresher: IamSessionRefresher): SessionRefresher

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: AuthRepositoryImpl): AuthRepository

    @Binds
    abstract fun bindOnboardingAnswersRepository(store: OnboardingStore): OnboardingAnswersRepository

    companion object {
        /** The IAM endpoints are public: they go out without a token and never trigger a renewal. */
        @Provides
        @Singleton
        fun provideIamService(@SessionFree retrofit: Retrofit): IamService = retrofit.create(IamService::class.java)

        /** Changing the password and deleting the account need the session, like any other feature call. */
        @Provides
        @Singleton
        fun provideAccountService(retrofit: Retrofit): AccountService = retrofit.create(AccountService::class.java)
    }
}
