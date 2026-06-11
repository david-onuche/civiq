package com.civiq.app.di

import javax.inject.Qualifier

/** Marks the [retrofit2.Retrofit] / [okhttp3.OkHttpClient] instance configured for the Gemini API. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class GeminiApi

/** Marks the [retrofit2.Retrofit] / [okhttp3.OkHttpClient] instance configured for the OpenAI API. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class OpenAiApi

/** Marks a [kotlinx.coroutines.CoroutineScope] tied to the application lifecycle. */
@Qualifier
@Retention(AnnotationRetention.BINARY)
annotation class ApplicationScope
