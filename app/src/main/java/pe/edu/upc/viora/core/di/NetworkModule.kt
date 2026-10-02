package pe.edu.upc.viora.core.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import kotlinx.coroutines.runBlocking
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import pe.edu.upc.viora.BuildConfig
import pe.edu.upc.viora.core.datastore.SessionStore
import pe.edu.upc.viora.core.network.AcceptLanguageInterceptor
import pe.edu.upc.viora.core.network.AccessTokenProvider
import pe.edu.upc.viora.core.network.AuthInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory

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

    @Provides
    @Singleton
    fun provideAccessTokenProvider(sessionStore: SessionStore): AccessTokenProvider =
        AccessTokenProvider { runBlocking { sessionStore.accessToken() } }

    @Provides
    @Singleton
    fun provideOkHttpClient(tokenProvider: AccessTokenProvider): OkHttpClient {
        val builder = OkHttpClient.Builder()
            // Free-tier hosting (Render) can need up to about a minute to wake from sleep.
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(60, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(AcceptLanguageInterceptor())
            .addInterceptor(AuthInterceptor(tokenProvider))
        if (BuildConfig.DEBUG) {
            val logging = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
                // Never log bearer tokens, even in debug builds.
                redactHeader("Authorization")
            }
            builder.addInterceptor(logging)
        }
        return builder.build()
    }

    @Provides
    @Singleton
    fun provideRetrofit(okHttpClient: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.API_BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()
}
