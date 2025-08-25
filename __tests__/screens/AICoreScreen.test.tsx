import React from 'react';
import { fireEvent } from '@testing-library/react-native';
import AICoreScreen from '../../src/screens/AICoreScreen';
import { renderWithProvider, defaultMockState } from '../utils/testUtils';

// Mock dependencies
jest.mock('react-native-image-picker', () => ({
  launchImageLibrary: jest.fn(),
}));

// Use shared mock components
jest.mock('../../src/components/Button', () => {
  return require('../utils/mockComponents').MockButton;
});

jest.mock('../../src/components/Card', () => {
  return require('../utils/mockComponents').MockCard;
});

jest.mock('../../src/components/ContentList', () => ({
  ContentList: require('../utils/mockComponents').MockContentList,
}));

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
        ...defaultMockState.ai,
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
        ...defaultMockState.ai,
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
        ...defaultMockState.ai,
        error: 'Test error message',
      },
    };

    const { getByText } = renderWithProvider(<AICoreScreen />, initialState);
    
    expect(getByText('Test error message')).toBeTruthy();
  });

  it('shows processing indicator when processing', () => {
    const initialState = {
      ai: {
        ...defaultMockState.ai,
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
        ...defaultMockState.ai,
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
        ...defaultMockState.ai,
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
        ...defaultMockState.ai,
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