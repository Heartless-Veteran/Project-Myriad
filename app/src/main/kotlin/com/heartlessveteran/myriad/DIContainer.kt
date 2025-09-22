package com.heartlessveteran.myriad

import android.content.Context
import androidx.room.Room
import com.heartlessveteran.myriad.core.data.ai.AIProviderRegistry
import com.heartlessveteran.myriad.core.data.ai.GeminiProvider
import com.heartlessveteran.myriad.core.data.ai.OpenAIProvider
import com.heartlessveteran.myriad.core.data.database.MyriadDatabase
import com.heartlessveteran.myriad.core.data.manager.PluginManagerImpl
import com.heartlessveteran.myriad.core.data.manager.SearchManagerImpl
import com.heartlessveteran.myriad.core.data.repository.AnimeRepositoryImpl
import com.heartlessveteran.myriad.core.data.repository.MangaRepositoryImpl
import com.heartlessveteran.myriad.core.data.repository.PluginRepositoryImpl
import com.heartlessveteran.myriad.core.data.source.LocalAnimeSource
import com.heartlessveteran.myriad.core.domain.manager.PluginManager
import com.heartlessveteran.myriad.core.domain.manager.SearchManager
import com.heartlessveteran.myriad.core.domain.repository.AnimeRepository
import com.heartlessveteran.myriad.core.domain.repository.MangaRepository
import com.heartlessveteran.myriad.core.domain.repository.PluginRepository
import com.heartlessveteran.myriad.core.domain.repository.Source
import com.heartlessveteran.myriad.core.domain.usecase.AddAnimeToLibraryUseCase
import com.heartlessveteran.myriad.core.domain.usecase.AddMangaToLibraryUseCase
import com.heartlessveteran.myriad.core.domain.usecase.GetAnimeDetailsUseCase
import com.heartlessveteran.myriad.core.domain.usecase.GetAnimeEpisodesUseCase
import com.heartlessveteran.myriad.core.domain.usecase.GetChapterPagesUseCase
import com.heartlessveteran.myriad.core.domain.usecase.GetLibraryAnimeUseCase
import com.heartlessveteran.myriad.core.domain.usecase.GetLibraryMangaUseCase
import com.heartlessveteran.myriad.core.domain.usecase.GetMangaDetailsUseCase
import com.heartlessveteran.myriad.core.domain.usecase.GetNextUnwatchedEpisodeUseCase
import com.heartlessveteran.myriad.core.domain.usecase.SearchLibraryAnimeUseCase
import com.heartlessveteran.myriad.core.domain.usecase.UpdateEpisodeProgressUseCase
import com.heartlessveteran.myriad.feature.browser.viewmodel.GlobalSearchViewModel
import com.heartlessveteran.myriad.feature.browser.viewmodel.PluginManagementViewModel

// TODO: Re-enable when AI module is implemented
// import com.heartlessveteran.myriad.feature.ai.service.BackgroundAIProcessor

/**
 * Manual dependency injection container.
 * Temporary solution while KSP/Hilt compatibility issues are resolved.
 * Follows the same architectural patterns as Hilt would provide.
 */
class DIContainer(
    context: Context,
) {
    // Database layer
    private val database: MyriadDatabase by lazy {
        Room
            .databaseBuilder(
                context.applicationContext,
                MyriadDatabase::class.java,
                MyriadDatabase.DATABASE_NAME,
            ).fallbackToDestructiveMigration()
            .build()
    }

    private val mangaDao by lazy { database.mangaDao() }
    private val chapterDao by lazy { database.chapterDao() }
    private val pluginDao by lazy { database.pluginDao() }

    // Repository layer
    val pluginRepository: PluginRepository by lazy {
        PluginRepositoryImpl(pluginDao)
    }

    val mangaRepository: MangaRepository by lazy {
        MangaRepositoryImpl(mangaDao, chapterDao)
    }

    // Manager layer
    val pluginManager: PluginManager by lazy {
        PluginManagerImpl(pluginRepository)
    }

    val searchManager: SearchManager by lazy {
        SearchManagerImpl(pluginManager)
    }

    // Sources (placeholder for now)
    private val sources: Map<String, Source> by lazy {
        emptyMap() // TODO: Initialize actual sources when implemented
    }

    // Use case layer
    val getLibraryMangaUseCase: GetLibraryMangaUseCase by lazy {
        GetLibraryMangaUseCase(mangaRepository)
    }

    val getMangaDetailsUseCase: GetMangaDetailsUseCase by lazy {
        GetMangaDetailsUseCase(mangaRepository)
    }

    val addMangaToLibraryUseCase: AddMangaToLibraryUseCase by lazy {
        AddMangaToLibraryUseCase(mangaRepository)
    }

    val getChapterPagesUseCase: GetChapterPagesUseCase by lazy {
        GetChapterPagesUseCase(sources)
    }

    // Anime data sources
    private val localAnimeSource: LocalAnimeSource by lazy {
        LocalAnimeSource()
    }

    // Anime repository
    val animeRepository: AnimeRepository by lazy {
        AnimeRepositoryImpl(localAnimeSource)
    }

    // Anime use cases
    val getLibraryAnimeUseCase: GetLibraryAnimeUseCase by lazy {
        GetLibraryAnimeUseCase(animeRepository)
    }

    val getAnimeDetailsUseCase: GetAnimeDetailsUseCase by lazy {
        GetAnimeDetailsUseCase(animeRepository)
    }

    val addAnimeToLibraryUseCase: AddAnimeToLibraryUseCase by lazy {
        AddAnimeToLibraryUseCase(animeRepository)
    }

    val getAnimeEpisodesUseCase: GetAnimeEpisodesUseCase by lazy {
        GetAnimeEpisodesUseCase(animeRepository)
    }

    val updateEpisodeProgressUseCase: UpdateEpisodeProgressUseCase by lazy {
        UpdateEpisodeProgressUseCase(animeRepository)
    }

    val getNextUnwatchedEpisodeUseCase: GetNextUnwatchedEpisodeUseCase by lazy {
        GetNextUnwatchedEpisodeUseCase(animeRepository)
    }

    val searchLibraryAnimeUseCase: SearchLibraryAnimeUseCase by lazy {
        SearchLibraryAnimeUseCase(animeRepository)
    }

    // Browser ViewModels
    val pluginManagementViewModel: PluginManagementViewModel by lazy {
        PluginManagementViewModel(pluginManager)
    }

    val globalSearchViewModel: GlobalSearchViewModel by lazy {
        GlobalSearchViewModel(searchManager)
    }

    // AI providers
    val geminiProvider: GeminiProvider by lazy {
        GeminiProvider()
    }

    val openAIProvider: OpenAIProvider by lazy {
        OpenAIProvider()
    }

    val aiProviderRegistry: AIProviderRegistry by lazy {
        AIProviderRegistry(geminiProvider, openAIProvider)
    }

    // TODO: Re-enable when AI module is implemented
    // val backgroundAIProcessor: BackgroundAIProcessor by lazy {
    //     BackgroundAIProcessor(aiProviderRegistry)
    // }
}
