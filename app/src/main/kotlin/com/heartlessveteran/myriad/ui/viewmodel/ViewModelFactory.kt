package com.heartlessveteran.myriad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.heartlessveteran.myriad.DIContainer
import com.heartlessveteran.myriad.feature.ai.viewmodel.AIViewModel

/**
 * ViewModelFactory that uses manual dependency injection.
 * Creates ViewModels with required dependencies from DIContainer.
 */
class ViewModelFactory(
    private val diContainer: DIContainer,
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        when (modelClass) {
            LibraryViewModel::class.java -> {
                LibraryViewModel(
                    getLibraryMangaUseCase = diContainer.getLibraryMangaUseCase,
                    getMangaDetailsUseCase = diContainer.getMangaDetailsUseCase,
                    addMangaToLibraryUseCase = diContainer.addMangaToLibraryUseCase,
                ) as T
            }
            ReaderViewModel::class.java -> {
                ReaderViewModel(
                    getChapterPagesUseCase = diContainer.getChapterPagesUseCase,
                ) as T
            }
            AIViewModel::class.java -> {
                AIViewModel(
                    aiProviderRegistry = diContainer.aiProviderRegistry,
                ) as T
            }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
}
