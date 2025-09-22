package com.heartlessveteran.myriad.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.heartlessveteran.myriad.DIContainer
// TODO: Re-enable when AI module is implemented
// import com.heartlessveteran.myriad.feature.ai.viewmodel.AIViewModel

/**
 * ViewModelFactory that uses manual dependency injection.
 * Creates ViewModels with required dependencies from DIContainer.
 *
 * @deprecated This class is deprecated in favor of Hilt dependency injection.
 * ViewModels are now annotated with @HiltViewModel and injected automatically.
 * This class will be removed once all ViewModels are migrated to Hilt.
 */
@Deprecated(
    message = "Use Hilt @HiltViewModel annotation instead",
    replaceWith = ReplaceWith("hiltViewModel()"),
)
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
            // TODO: Re-enable when AI module is implemented
            // AIViewModel::class.java -> {
            //     AIViewModel(
            //         aiProviderRegistry = diContainer.aiProviderRegistry,
            //     ) as T
            // }
            else -> throw IllegalArgumentException("Unknown ViewModel class: ${modelClass.name}")
        }
}
