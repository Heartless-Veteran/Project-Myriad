import libraryReducer, {
  updateMangaProgress,
  updateAnimeProgress,
  LibraryState,
} from '../../../src/store/slices/librarySlice';
import { Manga, Anime } from '../../../src/types';

describe('librarySlice', () => {
  let initialState: LibraryState;

  beforeEach(() => {
    const manga: Manga = {
      id: '1',
      title: 'Test Manga',
      author: 'Test Author',
      description: 'Test Description',
      coverImage: 'test.jpg',
      chapters: [],
      genres: [],
      status: 'ongoing',
      rating: 0,
      tags: [],
      readingProgress: 0,
    };

    const anime: Anime = {
      id: '1',
      title: 'Test Anime',
      description: 'Test Description',
      coverImage: 'test.jpg',
      episodes: [],
      genres: [],
      status: 'ongoing',
      rating: 0,
      studio: 'Test Studio',
      tags: [],
      watchProgress: 0,
    };

    initialState = {
      manga: [manga],
      anime: [anime],
      stats: {
        totalManga: 1,
        totalAnime: 1,
        totalSize: 0,
        lastUpdated: '',
        recentlyAdded: [],
      },
      recommendations: [],
      searchResults: [],
      isLoading: false,
      isImporting: false,
      error: null,
      filters: {
        genre: [],
        status: [],
        rating: 0,
      },
      importTasks: [],
    };
  });

  it('should handle updateMangaProgress', () => {
    const newProgress = 0.5;
    const action = updateMangaProgress({ id: '1', progress: newProgress });
    const newState = libraryReducer(initialState, action);
    expect(newState.manga[0].readingProgress).toEqual(newProgress);
  });

  it('should handle updateAnimeProgress', () => {
    const newProgress = 0.7;
    const action = updateAnimeProgress({ id: '1', progress: newProgress });
    const newState = libraryReducer(initialState, action);
    expect(newState.anime[0].watchProgress).toEqual(newProgress);
  });
});
