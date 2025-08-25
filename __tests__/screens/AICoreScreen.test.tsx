import React from 'react';
import { render, fireEvent, waitFor } from '@testing-library/react-native';
import { Provider } from 'react-redux';
import { configureStore } from '@reduxjs/toolkit';
import AICoreScreen from '../../src/screens/AICoreScreen';

// Mock dependencies
jest.mock('react-native-image-picker', () => ({
  launchImageLibrary: jest.fn(),
}));

jest.mock('../../src/components/Button', () => {
  const { TouchableOpacity, Text } = require('react-native');
  return function MockButton({ title, onPress, disabled, style }: any) {
    return (
      <TouchableOpacity
        testID={`button-${title.toLowerCase().replace(/\s+/g, '-')}`}
        onPress={onPress}
        disabled={disabled}
        style={style}
      >
        <Text>{title}</Text>
      </TouchableOpacity>
    );
  };
});

jest.mock('../../src/components/Card', () => {
  const { View } = require('react-native');
  return function MockCard({ children, style }: any) {
    return (
      <View testID="card" style={style}>
        {children}
      </View>
    );
  };
});

jest.mock('../../src/components/ContentList', () => ({
  ContentList: function MockContentList({ data, onItemPress }: any) {
    const { View, Text, TouchableOpacity } = require('react-native');
    return (
      <View testID="content-list">
        {data.map((item: any, index: number) => (
          <TouchableOpacity
            key={index}
            testID={`content-item-${index}`}
            onPress={() => onItemPress(item)}
          >
            <Text>{item.title}</Text>
          </TouchableOpacity>
        ))}
      </View>
    );
  },
}));

// Mock store slices
const mockAiSlice = {
  name: 'ai',
  initialState: {
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
  reducers: {},
  extraReducers: () => {},
};

const mockLibrarySlice = {
  name: 'library',
  initialState: {
    manga: [],
    anime: [],
  },
  reducers: {},
};

const mockSettingsSlice = {
  name: 'settings',
  initialState: {
    defaultTargetLanguage: 'en',
  },
  reducers: {},
};

// Create mock store
const createMockStore = (initialState = {}) => {
  return configureStore({
    reducer: {
      ai: (state = mockAiSlice.initialState) => state,
      library: (state = mockLibrarySlice.initialState) => state,
      settings: (state = mockSettingsSlice.initialState) => state,
    },
    preloadedState: initialState,
  });
};

const renderWithProvider = (component: React.ReactElement, initialState = {}) => {
  const store = createMockStore(initialState);
  return render(
    <Provider store={store}>
      {component}
    </Provider>
  );
};

describe('AICoreScreen', () => {
  it('renders correctly when AI is initialized', () => {
    const { getByText } = renderWithProvider(<AICoreScreen />);
    
    expect(getByText('AI Core')).toBeTruthy();
    expect(getByText('Ready')).toBeTruthy();
    expect(getByText('Online')).toBeTruthy();
  });

  it('shows loading state when AI is not initialized', () => {
    const initialState = {
      ai: {
        ...mockAiSlice.initialState,
        isInitialized: false,
        isProcessing: true,
      },
    };

    const { getByText } = renderWithProvider(<AICoreScreen />, initialState);
    
    expect(getByText('Initializing AI Core...')).toBeTruthy();
  });

  it('shows offline mode correctly', () => {
    const initialState = {
      ai: {
        ...mockAiSlice.initialState,
        isOfflineMode: true,
      },
    };

    const { getByText } = renderWithProvider(<AICoreScreen />, initialState);
    
    expect(getByText('Offline')).toBeTruthy();
    expect(getByText('Switch to Online')).toBeTruthy();
  });

  it('displays error message when present', () => {
    const initialState = {
      ai: {
        ...mockAiSlice.initialState,
        error: 'Test error message',
      },
    };

    const { getByText } = renderWithProvider(<AICoreScreen />, initialState);
    
    expect(getByText('Test error message')).toBeTruthy();
  });

  it('shows processing indicator when processing', () => {
    const initialState = {
      ai: {
        ...mockAiSlice.initialState,
        isProcessing: true,
      },
    };

    const { getByText } = renderWithProvider(<AICoreScreen />, initialState);
    
    expect(getByText('Processing...')).toBeTruthy();
  });

  it('renders feature buttons correctly', () => {
    const { getByText } = renderWithProvider(<AICoreScreen />);
    
    expect(getByText('OCR Translation')).toBeTruthy();
    expect(getByText('Art Style')).toBeTruthy();
    expect(getByText('Metadata')).toBeTruthy();
    expect(getByText('NL Search')).toBeTruthy();
  });

  it('switches between features when buttons are pressed', () => {
    const { getByText } = renderWithProvider(<AICoreScreen />);
    
    // Initially shows OCR feature
    expect(getByText('Extract and translate text from manga panels using Tesseract OCR')).toBeTruthy();
    
    // Switch to Art Style feature
    fireEvent.press(getByText('Art Style'));
    expect(getByText('Find similar content based on art style using computer vision')).toBeTruthy();
    
    // Switch to Metadata feature
    fireEvent.press(getByText('Metadata'));
    expect(getByText('Extract metadata from a cover image')).toBeTruthy();
    
    // Switch to Search feature
    fireEvent.press(getByText('NL Search'));
    expect(getByText('Search your library using natural language queries')).toBeTruthy();
  });

  it('renders current translation when available', () => {
    const mockTranslation = {
      originalText: 'こんにちは',
      translatedText: 'Hello',
      confidence: 0.95,
    };

    const initialState = {
      ai: {
        ...mockAiSlice.initialState,
        currentTranslation: mockTranslation,
      },
    };

    const { getByText } = renderWithProvider(<AICoreScreen />, initialState);
    
    expect(getByText('こんにちは')).toBeTruthy();
    expect(getByText('Hello')).toBeTruthy();
    expect(getByText('Confidence: 95.0%')).toBeTruthy();
  });

  it('renders art style matches when available', () => {
    const mockMatches = [
      { item: { id: '1', title: 'Similar Manga 1' } },
      { item: { id: '2', title: 'Similar Manga 2' } },
    ];

    const initialState = {
      ai: {
        ...mockAiSlice.initialState,
        artStyleMatches: mockMatches,
      },
    };

    // Switch to art style feature first
    const { getByText, getByTestId } = renderWithProvider(<AICoreScreen />, initialState);
    fireEvent.press(getByText('Art Style'));
    
    expect(getByText('Similar Content')).toBeTruthy();
    expect(getByTestId('content-list')).toBeTruthy();
  });

  it('renders metadata when available', () => {
    const mockMetadata = {
      title: 'Test Manga',
      author: 'Test Author',
      tags: ['Action', 'Adventure'],
    };

    const initialState = {
      ai: {
        ...mockAiSlice.initialState,
        metadata: mockMetadata,
      },
    };

    // Switch to metadata feature first
    const { getByText } = renderWithProvider(<AICoreScreen />, initialState);
    fireEvent.press(getByText('Metadata'));
    
    expect(getByText('Test Manga')).toBeTruthy();
    expect(getByText('Test Author')).toBeTruthy();
    expect(getByText('Action, Adventure')).toBeTruthy();
  });

  it('handles search input correctly', () => {
    const { getByText, getByDisplayValue } = renderWithProvider(<AICoreScreen />);
    
    // Switch to search feature
    fireEvent.press(getByText('NL Search'));
    
    const searchInput = getByDisplayValue('');
    fireEvent.changeText(searchInput, 'action manga');
    
    expect(getByDisplayValue('action manga')).toBeTruthy();
  });
});
