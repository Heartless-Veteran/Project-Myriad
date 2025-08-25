import React from 'react';
import { fireEvent } from '@testing-library/react-native';
import { Alert } from 'react-native';
import LibraryScreen from '../../src/screens/LibraryScreen';
import { Manga, Anime } from '../../src/types';
import { renderWithProvider } from '../utils/testUtils';

// Mock dependencies
jest.mock('react-native-document-picker', () => ({
  pick: jest.fn(),
  isCancel: jest.fn(),
  types: {
    zip: 'application/zip',
    video: 'video/*',
  },
}));

jest.mock('react-native', () => {
  const RN = jest.requireActual('react-native');
  return {
    ...RN,
    Alert: {
      alert: jest.fn(),
    },
  };
});

// Use shared mock components
jest.mock('../../src/components/SearchBar', () => {
  return require('../utils/mockComponents').MockSearchBar;
});

jest.mock('../../src/components/FilterPanel', () => {
  return require('../utils/mockComponents').MockFilterPanel;
});

jest.mock('../../src/components/Card', () => {
  return require('../utils/mockComponents').MockCard;
});

jest.mock('../../src/components/Button', () => {
  return require('../utils/mockComponents').MockButton;
});

jest.mock('../../src/components/ContentList', () => ({
  ContentList: require('../utils/mockComponents').MockContentList,
}));

// Mock data
const mockManga: Manga[] = [
  {
    id: '1',
    title: 'Test Manga 1',
    author: 'Author 1',
    description: 'Description 1',
    coverImage: 'cover1.jpg',
    chapters: [],
    genres: ['Action', 'Adventure'],
    status: 'ongoing',
    rating: 4.5,
    tags: ['Popular'],
    readingProgress: 0.5,
  },
];

const mockAnime: Anime[] = [
  {
    id: '2',
    title: 'Test Anime 1',
    description: 'Anime Description 1',
    coverImage: 'anime1.jpg',
    episodes: [],
    genres: ['Action', 'Sci-Fi'],
    status: 'ongoing',
    rating: 4.2,
    studio: 'Test Studio',
    tags: ['Popular'],
    watchProgress: 0.3,
  },
];

describe('LibraryScreen', () => {
  beforeEach(() => {
    jest.clearAllMocks();
  });

  it('renders loading state correctly', () => {
    const initialState = {
      library: {
        manga: [],
        anime: [],
        isLoading: true,
        stats: { totalManga: 0, totalAnime: 0, totalSize: 0, lastUpdated: '', recentlyAdded: [] },
        recommendations: [],
        searchResults: [],
        isImporting: false,
        error: null,
        filters: { genre: [], status: [], rating: 0 },
        importTasks: [],
      },
    };

    const { getByText } = renderWithProvider(<LibraryScreen />, initialState);
    expect(getByText('Loading your library...')).toBeTruthy();
  });

  it('renders library with content', () => {
    const initialState = {
      library: {
        manga: mockManga,
        anime: mockAnime,
        stats: { totalManga: 1, totalAnime: 1, totalSize: 0, lastUpdated: '', recentlyAdded: [] },
        recommendations: [],
        searchResults: [],
        isLoading: false,
        isImporting: false,
        error: null,
        filters: { genre: [], status: [], rating: 0 },
        importTasks: [],
      },
    };

    const { getByText, getByTestId } = renderWithProvider(<LibraryScreen />, initialState);
    
    expect(getByText('Library Statistics')).toBeTruthy();
    expect(getByText('1')).toBeTruthy(); // Total manga count
    expect(getByTestId('content-list')).toBeTruthy();
  });

  it('renders search bar and handles search', () => {
    const { getByTestId } = renderWithProvider(<LibraryScreen />);
    
    const searchInput = getByTestId('search-input');
    fireEvent.changeText(searchInput, 'test query');
    
    expect(searchInput.props.value).toBe('test query');
  });

  it('toggles filter panel when filter button is pressed', () => {
    const { getByTestId, queryByTestId } = renderWithProvider(<LibraryScreen />);
    
    // Initially filter panel should not be visible
    expect(queryByTestId('filter-panel')).toBeNull();
    
    // Press filter button
    fireEvent.press(getByTestId('filter-button'));
    
    // Filter panel should now be visible
    expect(getByTestId('filter-panel')).toBeTruthy();
  });

  it('renders import buttons', () => {
    const { getByTestId } = renderWithProvider(<LibraryScreen />);
    
    expect(getByTestId('button-import-manga')).toBeTruthy();
    expect(getByTestId('button-import-anime')).toBeTruthy();
  });

  it('renders tab bar with correct tabs', () => {
    const { getByText } = renderWithProvider(<LibraryScreen />);
    
    expect(getByText('All')).toBeTruthy();
    expect(getByText('Manga')).toBeTruthy();
    expect(getByText('Anime')).toBeTruthy();
    expect(getByText('Recommendations')).toBeTruthy();
  });

  it('switches tabs correctly', () => {
    const { getByText } = renderWithProvider(<LibraryScreen />);
    
    fireEvent.press(getByText('Manga'));
    fireEvent.press(getByText('Anime'));
    fireEvent.press(getByText('Recommendations'));
    
    // Component should handle tab switches without errors
    expect(getByText('Manga')).toBeTruthy();
  });

  it('displays error message when present', () => {
    const initialState = {
      library: {
        manga: [],
        anime: [],
        error: 'Test error message',
        stats: { totalManga: 0, totalAnime: 0, totalSize: 0, lastUpdated: '', recentlyAdded: [] },
        recommendations: [],
        searchResults: [],
        isLoading: false,
        isImporting: false,
        filters: { genre: [], status: [], rating: 0 },
        importTasks: [],
      },
    };

    const { getByText } = renderWithProvider(<LibraryScreen />, initialState);
    expect(getByText('Test error message')).toBeTruthy();
  });

  it('displays importing indicator when importing', () => {
    const initialState = {
      library: {
        manga: [],
        anime: [],
        isImporting: true,
        stats: { totalManga: 0, totalAnime: 0, totalSize: 0, lastUpdated: '', recentlyAdded: [] },
        recommendations: [],
        searchResults: [],
        isLoading: false,
        error: null,
        filters: { genre: [], status: [], rating: 0 },
        importTasks: [],
      },
    };

    const { getByText } = renderWithProvider(<LibraryScreen />, initialState);
    expect(getByText('Importing file...')).toBeTruthy();
  });

  it('handles content item press', () => {
    const consoleSpy = jest.spyOn(console, 'log').mockImplementation();
    
    const initialState = {
      library: {
        manga: mockManga,
        anime: [],
        stats: { totalManga: 1, totalAnime: 0, totalSize: 0, lastUpdated: '', recentlyAdded: [] },
        recommendations: [],
        searchResults: [],
        isLoading: false,
        isImporting: false,
        error: null,
        filters: { genre: [], status: [], rating: 0 },
        importTasks: [],
      },
    };

    const { getByTestId } = renderWithProvider(<LibraryScreen />, initialState);
    
    fireEvent.press(getByTestId('content-item-1'));
    expect(consoleSpy).toHaveBeenCalledWith('Open content:', 'Test Manga 1');
    
    consoleSpy.mockRestore();
  });

  it('handles content item long press with delete confirmation', () => {
    const initialState = {
      library: {
        manga: mockManga,
        anime: [],
        stats: { totalManga: 1, totalAnime: 0, totalSize: 0, lastUpdated: '', recentlyAdded: [] },
        recommendations: [],
        searchResults: [],
        isLoading: false,
        isImporting: false,
        error: null,
        filters: { genre: [], status: [], rating: 0 },
        importTasks: [],
      },
    };

    const { getByTestId } = renderWithProvider(<LibraryScreen />, initialState);
    
    fireEvent(getByTestId('content-item-1'), 'longPress');
    expect(Alert.alert).toHaveBeenCalledWith(
      'Delete Item',
      'Are you sure you want to delete "Test Manga 1"?',
      expect.any(Array)
    );
  });
});
