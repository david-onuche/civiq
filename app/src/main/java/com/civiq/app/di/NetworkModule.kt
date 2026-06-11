package com.civiq.app.di

import com.civiq.app.BuildConfig
import com.civiq.app.data.remote.api.GeminiApiService
import com.civiq.app.data.remote.api.OpenAiApiService
import kotlinx.serialization.json.Json
import okhttp3.Interceptor
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Response
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import java.util.concurrent.TimeUnit
import javax.inject.Singleton
import com.civiq.app.utils.NetworkConfig

/**
 * Provides Retrofit/OkHttp instances for the AI provider abstraction layer.
 *
 * Two independently-configured Retrofit clients are exposed:
 *  - [GeminiApi]: Google Gemini, authenticated via an `?key=` query parameter.
 *  - [OpenAiApi]: OpenAI, authenticated via an `Authorization: Bearer` header.
 *
 * Both share the same [Json] (kotlinx.serialization) configuration and base
 * timeout/logging behavior.
 */
@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    @Provides
    @Singleton
    fun provideJson(): Json = Json {
        ignoreUnknownKeys = true
        isLenient = true
        coerceInputValues = true
        explicitNulls = false
    }

    @Provides
    @Singleton
    fun provideLoggingInterceptor(): HttpLoggingInterceptor =
        HttpLoggingInterceptor().apply {
            level = if (BuildConfig.DEBUG) {
                HttpLoggingInterceptor.Level.BODY
            } else {
                HttpLoggingInterceptor.Level.NONE
            }
        }

    /** Appends `?key=<GEMINI_API_KEY>` to every request, as required by the Gemini REST API. */
    private class GeminiApiKeyInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val urlWithKey = original.url.newBuilder()
                .addQueryParameter("key", BuildConfig.GEMINI_API_KEY)
                .build()
            return chain.proceed(original.newBuilder().url(urlWithKey).build())
        }
    }

    /** Adds `Authorization: Bearer <OPENAI_API_KEY>` to every request, as required by the OpenAI API. */
    private class OpenAiAuthInterceptor : Interceptor {
        override fun intercept(chain: Interceptor.Chain): Response {
            val original = chain.request()
            val authenticated = original.newBuilder()
                .addHeader("Authorization", "Bearer ${BuildConfig.OPENAI_API_KEY}")
                .addHeader("Content-Type", "application/json")
                .build()
            return chain.proceed(authenticated)
        }
    }

    @Provides
    @Singleton
    @GeminiApi
    fun provideGeminiOkHttpClient(logging: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(NetworkConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NetworkConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NetworkConfig.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(GeminiApiKeyInterceptor())
            .addInterceptor(logging)
            .build()

    @Provides
    @Singleton
    @OpenAiApi
    fun provideOpenAiOkHttpClient(logging: HttpLoggingInterceptor): OkHttpClient =
        OkHttpClient.Builder()
            .connectTimeout(NetworkConfig.CONNECT_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(NetworkConfig.READ_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(NetworkConfig.WRITE_TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .addInterceptor(OpenAiAuthInterceptor())
            .addInterceptor(logging)
            .build()

    @Provides
    @Singleton
    @GeminiApi
    fun provideGeminiRetrofit(@GeminiApi client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.GEMINI_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    @OpenAiApi
    fun provideOpenAiRetrofit(@OpenAiApi client: OkHttpClient, json: Json): Retrofit =
        Retrofit.Builder()
            .baseUrl(BuildConfig.OPENAI_BASE_URL)
            .client(client)
            .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
            .build()

    @Provides
    @Singleton
    fun provideGeminiApiService(@GeminiApi retrofit: Retrofit): GeminiApiService =
        retrofit.create(GeminiApiService::class.java)

    @Provides
    @Singleton
    fun provideOpenAiApiService(@OpenAiApi retrofit: Retrofit): OpenAiApiService =
        retrofit.create(OpenAiApiService::class.java)
}
