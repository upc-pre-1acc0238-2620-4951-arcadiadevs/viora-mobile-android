package pe.edu.upc.viora.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Qualifier
import javax.inject.Singleton
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pe.edu.upc.viora.BuildConfig
import pe.edu.upc.viora.core.network.AcceptLanguageInterceptor
import pe.edu.upc.viora.core.network.AccessTokenProvider
import pe.edu.upc.viora.core.network.AuthInterceptor
import pe.edu.upc.viora.core.network.SessionRefresher
import pe.edu.upc.viora.core.network.TokenAuthenticator
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

/**
 * Retrofit without session handling, for the public IAM endpoints (sign in, sign up, refresh...):
 * they must never try to renew a session or attach an old token.
 */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class SessionFree

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    /**
     * Strict about required fields (a missing non-null field fails loudly instead of becoming
     * `null` like with Gson) but tolerant of fields the backend adds later.
     */
    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        explicitNulls = false
    }

    /** The token goes out renewed: the backend treats an expired token as no session at all. */
    @Provides
    @Singleton
    fun provideAccessTokenProvider(refresher: SessionRefresher): AccessTokenProvider =
        AccessTokenProvider { refresher.freshAccessToken() }

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenProvider: AccessTokenProvider, refresher: SessionRefresher): OkHttpClient =
        baseClient()
            .addInterceptor(AuthInterceptor(tokenProvider))
            .authenticator(TokenAuthenticator(refresher))
            .withDebugLogging()
            .build()

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit = retrofit(okHttpClient, json)

    @Provides
    @Singleton
    @SessionFree
    fun provideSessionFreeRetrofit(json: Json): Retrofit = retrofit(baseClient().withDebugLogging().build(), json)

    private fun baseClient(): OkHttpClient.Builder =
        OkHttpClient.Builder()
            // Free-tier hosting (Render) can need up to about a minute to wake from sleep.
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(AcceptLanguageInterceptor())

    private fun OkHttpClient.Builder.withDebugLogging(): OkHttpClient.Builder = apply {
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
                // Never log bearer tokens, even in debug builds.
                redactHeader("Authorization")
            }
            addInterceptor(logging)
        }
    }

    private fun retrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
