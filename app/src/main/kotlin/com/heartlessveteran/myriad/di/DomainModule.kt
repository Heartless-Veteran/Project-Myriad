package com.heartlessveteran.myriad.di

import com.heartlessveteran.myriad.core.data.manager.PluginManagerImpl
import com.heartlessveteran.myriad.core.data.manager.SearchManagerImpl
import com.heartlessveteran.myriad.core.domain.manager.PluginManager
import com.heartlessveteran.myriad.core.domain.manager.SearchManager
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

/**
 * Hilt module for domain layer bindings.
 * Binds manager implementations to their interfaces.
 */
@Module
@InstallIn(SingletonComponent::class)
abstract class DomainModule {
    @Binds
    @Singleton
    abstract fun bindPluginManager(pluginManagerImpl: PluginManagerImpl): PluginManager

    @Binds
    @Singleton
    abstract fun bindSearchManager(searchManagerImpl: SearchManagerImpl): SearchManager
}
