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

// Mock components
jest.mock('../../src/components/SearchBar', () => {
  const { View, TextInput, TouchableOpacity, Text } = require('react-native');
  return function MockSearchBar({ value, onChangeText, onFilterPress, placeholder }: any) {
    return (
      <View testID="search-bar">
        <TextInput
          testID="search-input"
          value={value}
          onChangeText={onChangeText}
          placeholder={placeholder}
        />
        <TouchableOpacity testID="filter-button" onPress={onFilterPress}>
          <Text>Filter</Text>
        </TouchableOpacity>
      </View>
    );
  };
});

jest.mock('../../src/components/FilterPanel', () => {
  const { View, Text } = require('react-native');
  return function MockFilterPanel({ filters, onFiltersChange, availableGenres }: any) {
    return (
      <View testID="filter-panel">
        <Text>Filter Panel</Text>
      </View>
    );
  };
});

jest.mock('../../src/components/Card', () => {
  const { TouchableOpacity, Text } = require('react-native');
  return function MockCard({ title, imageUrl, tags, progress, onPress }: any) {
    return (
      <TouchableOpacity testID={`card-${title}`} onPress={onPress}>
        <Text testID="card-title">{title}</Text>
        {progress !== undefined && <Text testID="card-progress">{progress}</Text>}
      </TouchableOpacity>
    );
  };
});

jest.mock('../../src/components/Button', () => {
  const { TouchableOpacity, Text } = require('react-native');
  return function MockButton({ title, onPress, disabled }: any) {
    return (
      <TouchableOpacity
        testID={`button-${title.toLowerCase().replace(/\s+/g, '-')}`}
        onPress={onPress}
        disabled={disabled}
      >
        <Text>{title}</Text>
      </TouchableOpacity>
    );
  };
});

jest.mock('../../src/components/ContentList', () => ({
  ContentList: function MockContentList({ data, onItemPress, onItemLongPress, renderItem, refreshControl }: any) {
    const { View, Text, TouchableOpacity, ScrollView } = require('react-native');
    return (
      <ScrollView testID="content-list" refreshControl={refreshControl}>
        {data.map((item: any, index: number) => {
          if (renderItem) {
            return renderItem({ item });
          }
          return (
            <TouchableOpacity
              key={item.id}
              testID={`content-item-${item.id}`}
              onPress={() => onItemPress(item)}
              onLongPress={() => onItemLongPress && onItemLongPress(item)}
            >
              <Text>{item.title}</Text>
            </TouchableOpacity>
          );
        })}
      </ScrollView>
    );
  },
}));

// Define constants used in LibraryScreen
const PROGRESS_ADJUSTMENT_STEP = 0.1;
const PROGRESS_ADJUSTMENT = 0.1;

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