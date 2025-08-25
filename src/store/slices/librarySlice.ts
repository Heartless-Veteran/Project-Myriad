import { createSlice, createAsyncThunk, PayloadAction } from '@reduxjs/toolkit';
import { Manga, Anime, LibraryStats, ImportTask } from '../../types';
import { VaultService } from '../../services/VaultService';
import { AIService } from '../../services/AIService';

export interface LibraryState {
  manga: Manga[];
  anime: Anime[];
  stats: LibraryStats;
  recommendations: any[];
  searchResults: (Manga | Anime)[];
  isLoading: boolean;
  isImporting: boolean;
  error: string | null;
  filters: {
    genre: string[];
    status: string[];
    rating: number;
  };
  importTasks: ImportTask[];
}

const initialState: LibraryState = {
  manga: [],
  anime: [],
  stats: {
    totalManga: 0,
    totalAnime: 0,
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

export const loadLibrary = createAsyncThunk('library/loadLibrary', async (_, { rejectWithValue }) => {
  try {
    const vaultService = VaultService.getInstance();
    const manga = await vaultService.getMangaLibrary();
    const anime = await vaultService.getAnimeLibrary();
    return { manga, anime };
  } catch (error: any) {
    return rejectWithValue(error.message);
  }
});

export const importManga = createAsyncThunk(
  'library/importManga',
  async ({ uri, name }: { uri: string; name: string }, { rejectWithValue }) => {
    try {
      const vaultService = VaultService.getInstance();
      const newManga = await vaultService.importManga(uri, name);
      return newManga;
    } catch (error: any) {
      return rejectWithValue(error.message);
    }
  }
);

export const importAnime = createAsyncThunk(
  'library/importAnime',
  async ({ uri, name, generateThumbnail }: { uri: string; name: string; generateThumbnail: boolean }, { rejectWithValue }) => {
    try {
      const vaultService = VaultService.getInstance();
      const newAnime = await vaultService.importAnime(uri, name, generateThumbnail);
      return newAnime;
    } catch (error: any) {
      return rejectWithValue(error.message);
    }
  }
);

export const deleteManga = createAsyncThunk(
  'library/deleteManga',
  async (mangaId: string, { rejectWithValue }) => {
    try {
      const vaultService = VaultService.getInstance();
      await vaultService.deleteManga(mangaId);
      return mangaId;
    } catch (error: any) {
      return rejectWithValue(error.message);
    }
  }
);

export const deleteAnime = createAsyncThunk(
  'library/deleteAnime',
  async (animeId: string, { rejectWithValue }) => {
    try {
      const vaultService = VaultService.getInstance();
      await vaultService.deleteAnime(animeId);
      return animeId;
    } catch (error: any) {
      return rejectWithValue(error.message);
    }
  }
);

const librarySlice = createSlice({
  name: 'library',
  initialState,
  reducers: {
    setFilters: (state, action: PayloadAction<Partial<LibraryState['filters']>>) => {
      state.filters = { ...state.filters, ...action.payload };
    },
    clearSearchResults: (state) => {
      state.searchResults = [];
    },
    updateMangaProgress: (state, action: PayloadAction<{ id: string; progress: number }>) => {
      const manga = state.manga.find((m) => m.id === action.payload.id);
      if (manga) {
        manga.readingProgress = action.payload.progress;
      }
    },
    updateAnimeProgress: (state, action: PayloadAction<{ id: string; progress: number }>) => {
      const anime = state.anime.find((a) => a.id === action.payload.id);
      if (anime) {
        anime.watchProgress = action.payload.progress;
      }
    },
    clearError: (state) => {
      state.error = null;
    },
  },
  extraReducers: (builder) => {
    builder
      .addCase(loadLibrary.pending, (state) => {
        state.isLoading = true;
      })
      .addCase(loadLibrary.fulfilled, (state, action) => {
        state.isLoading = false;
        state.manga = action.payload.manga;
        state.anime = action.payload.anime;
        state.stats = {
          totalManga: action.payload.manga.length,
          totalAnime: action.payload.anime.length,
          totalSize: 0,
          lastUpdated: new Date().toISOString(),
          recentlyAdded: [],
        };
      })
      .addCase(loadLibrary.rejected, (state, action) => {
        state.isLoading = false;
        state.error = action.payload as string;
      })
      .addCase(importManga.pending, (state) => {
        state.isImporting = true;
        state.error = null;
      })
      .addCase(importManga.fulfilled, (state, action) => {
        state.isImporting = false;
        if (action.payload) {
          state.manga.push(action.payload);
          state.stats.totalManga += 1;
        }
      })
      .addCase(importManga.rejected, (state, action) => {
        state.isImporting = false;
        state.error = action.payload as string;
      })
      .addCase(importAnime.pending, (state) => {
        state.isImporting = true;
        state.error = null;
      })
      .addCase(importAnime.fulfilled, (state, action) => {
        state.isImporting = false;
        if (action.payload) {
          state.anime.push(action.payload);
          state.stats.totalAnime += 1;
        }
      })
      .addCase(importAnime.rejected, (state, action) => {
        state.isImporting = false;
        state.error = action.payload as string;
      })
      .addCase(deleteManga.fulfilled, (state, action) => {
        state.manga = state.manga.filter((m) => m.id !== action.payload);
        state.stats.totalManga -= 1;
      })
      .addCase(deleteAnime.fulfilled, (state, action) => {
        state.anime = state.anime.filter((a) => a.id !== action.payload);
        state.stats.totalAnime -= 1;
      });
  },
});

export const {
  setFilters,
  clearSearchResults,
  updateMangaProgress,
  updateAnimeProgress,
  clearError,
} = librarySlice.actions;

export default librarySlice.reducer;