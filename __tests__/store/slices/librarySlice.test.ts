import libraryReducer, {
  updateMangaProgress,
  updateAnimeProgress,
  setFilters,
  clearSearchResults,
  clearError,
  loadLibrary,
  importManga,
  importAnime,
  deleteManga,
  deleteAnime,
  LibraryState,
} from '../../../src/store/slices/librarySlice';
import { Manga, Anime } from '../../../src/types';

// Mock VaultService
jest.mock('../../../src/services/VaultService', () => ({
  VaultService: {
    getInstance: jest.fn(() => ({
      getMangaLibrary: jest.fn(),
      getAnimeLibrary: jest.fn(),
      importManga: jest.fn(),
      importAnime: jest.fn(),
      deleteManga: jest.fn(),
      deleteAnime: jest.fn(),
    })),
  },
}));

describe('librarySlice', () => {
  let initialState: LibraryState;
  let mockManga: Manga;
  let mockAnime: Anime;

  beforeEach(() => {
    mockManga = {
      id: '1',
      title: 'Test Manga',
      author: 'Test Author',
      description: 'Test Description',
      coverImage: 'test.jpg',
      chapters: [],
      genres: ['Action', 'Adventure'],
      status: 'ongoing',
      rating: 4.5,
      tags: ['Popular'],
      readingProgress: 0,
    };

    mockAnime = {
      id: '1',
      title: 'Test Anime',
      description: 'Test Description',
      coverImage: 'test.jpg',
      episodes: [],
      genres: ['Action', 'Sci-Fi'],
      status: 'ongoing',
      rating: 4.2,
      studio: 'Test Studio',
      tags: ['Popular'],
      watchProgress: 0,
    };

    initialState = {
      manga: [mockManga],
      anime: [mockAnime],
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

  describe('reducers', () => {
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

    it('should clamp anime progress between 0 and 1', () => {
      // Test progress above 1
      let action = updateAnimeProgress({ id: '1', progress: 1.5 });
      let newState = libraryReducer(initialState, action);
      expect(newState.anime[0].watchProgress).toEqual(1);

      // Test progress below 0
      action = updateAnimeProgress({ id: '1', progress: -0.5 });
      newState = libraryReducer(initialState, action);
      expect(newState.anime[0].watchProgress).toEqual(0);
    });

    it('should handle setFilters', () => {
      const newFilters = { genre: ['Action'], status: ['ongoing'], rating: 4 };
      const action = setFilters(newFilters);
      const newState = libraryReducer(initialState, action);
      
      expect(newState.filters).toEqual({
        genre: ['Action'],
        status: ['ongoing'],
        rating: 4,
      });
    });

    it('should handle partial filter updates', () => {
      const partialFilters = { genre: ['Comedy'] };
      const action = setFilters(partialFilters);
      const newState = libraryReducer(initialState, action);
      
      expect(newState.filters).toEqual({
        genre: ['Comedy'],
        status: [],
        rating: 0,
      });
    });

    it('should handle clearSearchResults', () => {
      const stateWithResults = {
        ...initialState,
        searchResults: [mockManga, mockAnime],
      };
      
      const action = clearSearchResults();
      const newState = libraryReducer(stateWithResults, action);
      
      expect(newState.searchResults).toEqual([]);
    });

    it('should handle clearError', () => {
      const stateWithError = {
        ...initialState,
        error: 'Test error',
      };
      
      const action = clearError();
      const newState = libraryReducer(stateWithError, action);
      
      expect(newState.error).toBeNull();
    });

    it('should not update progress for non-existent manga', () => {
      const action = updateMangaProgress({ id: 'non-existent', progress: 0.5 });
      const newState = libraryReducer(initialState, action);
      
      expect(newState.manga[0].readingProgress).toEqual(0); // Should remain unchanged
    });

    it('should not update progress for non-existent anime', () => {
      const action = updateAnimeProgress({ id: 'non-existent', progress: 0.5 });
      const newState = libraryReducer(initialState, action);
      
      expect(newState.anime[0].watchProgress).toEqual(0); // Should remain unchanged
    });
  });

  describe('async thunks', () => {
    describe('loadLibrary', () => {
      it('should handle loadLibrary.pending', () => {
        const action = { type: loadLibrary.pending.type };
        const newState = libraryReducer(initialState, action);
        
        expect(newState.isLoading).toBe(true);
      });

      it('should handle loadLibrary.fulfilled', () => {
        const payload = {
          manga: [mockManga],
          anime: [mockAnime],
        };
        
        const action = { type: loadLibrary.fulfilled.type, payload };
        const newState = libraryReducer(initialState, action);
        
        expect(newState.isLoading).toBe(false);
        expect(newState.manga).toEqual([mockManga]);
        expect(newState.anime).toEqual([mockAnime]);
        expect(newState.stats.totalManga).toBe(1);
        expect(newState.stats.totalAnime).toBe(1);
      });

      it('should handle loadLibrary.rejected', () => {
        const action = { 
          type: loadLibrary.rejected.type, 
          payload: 'Failed to load library' 
        };
        const newState = libraryReducer(initialState, action);
        
        expect(newState.isLoading).toBe(false);
        expect(newState.error).toBe('Failed to load library');
      });
    });

    describe('importManga', () => {
      it('should handle importManga.pending', () => {
        const action = { type: importManga.pending.type };
        const newState = libraryReducer(initialState, action);
        
        expect(newState.isImporting).toBe(true);
        expect(newState.error).toBeNull();
      });

      it('should handle importManga.fulfilled', () => {
        const newManga = { ...mockManga, id: '2', title: 'New Manga' };
        const action = { type: importManga.fulfilled.type, payload: newManga };
        const newState = libraryReducer(initialState, action);
        
        expect(newState.isImporting).toBe(false);
        expect(newState.manga).toHaveLength(2);
        expect(newState.manga[1]).toEqual(newManga);
        expect(newState.stats.totalManga).toBe(2);
      });

      it('should handle importManga.rejected', () => {
        const action = { 
          type: importManga.rejected.type, 
          payload: 'Import failed' 
        };
        const newState = libraryReducer(initialState, action);
        
        expect(newState.isImporting).toBe(false);
        expect(newState.error).toBe('Import failed');
      });
    });

    describe('importAnime', () => {
      it('should handle importAnime.pending', () => {
        const action = { type: importAnime.pending.type };
        const newState = libraryReducer(initialState, action);
        
        expect(newState.isImporting).toBe(true);
        expect(newState.error).toBeNull();
      });

      it('should handle importAnime.fulfilled', () => {
        const newAnime = { ...mockAnime, id: '2', title: 'New Anime' };
        const action = { type: importAnime.fulfilled.type, payload: newAnime };
        const newState = libraryReducer(initialState, action);
        
        expect(newState.isImporting).toBe(false);
        expect(newState.anime).toHaveLength(2);
        expect(newState.anime[1]).toEqual(newAnime);
        expect(newState.stats.totalAnime).toBe(2);
      });

      it('should handle importAnime.rejected', () => {
        const action = { 
          type: importAnime.rejected.type, 
          payload: 'Import failed' 
        };
        const newState = libraryReducer(initialState, action);
        
        expect(newState.isImporting).toBe(false);
        expect(newState.error).toBe('Import failed');
      });
    });

    describe('deleteManga', () => {
      it('should handle deleteManga.fulfilled', () => {
        const action = { type: deleteManga.fulfilled.type, payload: '1' };
        const newState = libraryReducer(initialState, action);
        
        expect(newState.manga).toHaveLength(0);
        expect(newState.stats.totalManga).toBe(0);
      });
    });

    describe('deleteAnime', () => {
      it('should handle deleteAnime.fulfilled', () => {
        const action = { type: deleteAnime.fulfilled.type, payload: '1' };
        const newState = libraryReducer(initialState, action);
        
        expect(newState.anime).toHaveLength(0);
        expect(newState.stats.totalAnime).toBe(0);
      });
    });
  });

  describe('edge cases', () => {
    it('should handle importManga.fulfilled with null payload', () => {
      const action = { type: importManga.fulfilled.type, payload: null };
      const newState = libraryReducer(initialState, action);
      
      expect(newState.isImporting).toBe(false);
      expect(newState.manga).toHaveLength(1); // Should remain unchanged
      expect(newState.stats.totalManga).toBe(1); // Should remain unchanged
    });

    it('should handle importAnime.fulfilled with null payload', () => {
      const action = { type: importAnime.fulfilled.type, payload: null };
      const newState = libraryReducer(initialState, action);
      
      expect(newState.isImporting).toBe(false);
      expect(newState.anime).toHaveLength(1); // Should remain unchanged
      expect(newState.stats.totalAnime).toBe(1); // Should remain unchanged
    });

    it('should handle unknown action types', () => {
      const action = { type: 'unknown/action' };
      const newState = libraryReducer(initialState, action);
      
      expect(newState).toEqual(initialState);
    });
  });
});