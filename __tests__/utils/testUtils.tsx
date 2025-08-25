import React from 'react';
import { render } from '@testing-library/react-native';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';

// Default mock state structure
const createDefaultMockState = () => ({
  ai: {
    isInitialized: true,
    isProcessing: false,
    isOfflineMode: false,
    error: null,
    translations: [],
    currentTranslation: null,
    artStyleMatches: [],
    metadata: null,
    searchResults: [],
  },
  library: {
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
  },
  settings: {
    defaultTargetLanguage: 'en',
  },
});

// Create mock store with customizable initial state
export const createMockStore = (initialState = {}) => {
  const defaultState = createDefaultMockState();
  const mergedState = { ...defaultState, ...initialState };

  return configureStore({
    reducer: {
      ai: (state = mergedState.ai) => state,
      library: (state = mergedState.library, action) => {
        switch (action.type) {
          case 'library/loadLibrary/pending':
            return { ...state, isLoading: true };
          case 'library/loadLibrary/fulfilled':
            return { ...state, isLoading: false, ...action.payload };
          default:
            return state;
        }
      },
      settings: (state = mergedState.settings) => state,
    },
    preloadedState: mergedState,
  });
};

// Render component with Redux provider
export const renderWithProvider = (
  component: React.ReactElement,
  initialState = {}
) => {
  const store = createMockStore(initialState);
  return render(
    <Provider store={store}>
      {component}
    </Provider>
  );
};

// Export default mock state for reference
export const defaultMockState = createDefaultMockState();
