package com.civiq.app.di

import com.civiq.app.utils.DefaultNetworkConnectivityObserver
import com.civiq.app.utils.NetworkConnectivityObserver
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import dagger.Provides
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AppModule {

    @Binds
    @Singleton
    abstract fun bindNetworkConnectivityObserver(
        impl: DefaultNetworkConnectivityObserver,
    ): NetworkConnectivityObserver

    companion object {
        /** Application-scoped coroutine scope for fire-and-forget work (e.g. analytics, token sync). */
        @Provides
        @Singleton
        @ApplicationScope
        fun provideApplicationScope(): CoroutineScope =
            CoroutineScope(SupervisorJob() + Dispatchers.Default)
    }
}
