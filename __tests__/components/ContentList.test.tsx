import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import { View, Text, TouchableOpacity } from 'react-native';
import ContentList, { ContentItem } from '../../src/components/ContentList';
import { Manga } from '../../src/types';

// Mock data for testing
const mockMangaItems: Manga[] = [
  {
    id: '1',
    title: 'Test Manga 1',
    author: 'Author 1',
    description: 'Description 1',
    coverImage: 'https://example.com/cover1.jpg',
    chapters: [
      {
        id: 'c1',
        title: 'Chapter 1',
        chapterNumber: 1,
        pages: ['page1.jpg', 'page2.jpg'],
        readProgress: 0,
        isRead: false,
        dateAdded: new Date(),
      },
      {
        id: 'c2',
        title: 'Chapter 2',
        chapterNumber: 2,
        pages: ['page1.jpg', 'page2.jpg'],
        readProgress: 1,
        isRead: true,
        dateAdded: new Date(),
      },
    ],
    genres: ['Action', 'Adventure'],
    status: 'ongoing',
    rating: 4.5,
    tags: ['Popular', 'New'],
  },
  {
    id: '2',
    title: 'Test Manga 2',
    author: 'Author 2',
    description: 'Description 2',
    coverImage: 'https://example.com/cover2.jpg',
    chapters: [
      {
        id: 'c3',
        title: 'Chapter 1',
        chapterNumber: 1,
        pages: ['page1.jpg', 'page2.jpg'],
        readProgress: 1,
        isRead: true,
        dateAdded: new Date(),
      },
    ],
    genres: ['Comedy', 'Slice of Life'],
    status: 'completed',
    rating: 3.8,
    tags: ['Recommended'],
  },
];

// Mock the Card component since we're only testing ContentList
jest.mock('../../src/components/Card', () => {
  const { View } = require('react-native');
  return function MockCard({ children, style }: any) {
    return (
      <View testID="mock-card" style={style}>
        {children}
      </View>
    );
  };
});

describe('ContentList Component', () => {
  it('renders correctly with items', () => {
    const onItemPressMock = jest.fn();
    const { getByText } = render(
      <ContentList
        title="Test List"
        items={mockMangaItems}
        onItemPress={onItemPressMock}
      />
    );

    // Check if title is rendered
    expect(getByText('Test List')).toBeTruthy();

    // Check if manga titles are rendered
    expect(getByText('Test Manga 1')).toBeTruthy();
    expect(getByText('Test Manga 2')).toBeTruthy();
  });

  it('removes loading indicator after items are loaded', async () => {
    // Render with loading state
    const { getByText, queryByText, rerender } = render(
      <ContentList
        title="Test List"
        items={[]}
        onItemPress={() => {}}
        isLoading={true}
      />
    );

    // Assert loading indicator is present
    expect(getByText('Loading content...')).toBeTruthy();

    // Update to loaded state
    rerender(
      <ContentList
        title="Test List"
        items={mockMangaItems}
        onItemPress={() => {}}
        isLoading={false}
      />
    );

    // Assert loading indicator is absent
    expect(queryByText('Loading content...')).toBeNull();

    // Assert items are rendered
    expect(getByText('Test Manga 1')).toBeTruthy();
    expect(getByText('Test Manga 2')).toBeTruthy();
  });

  it('renders loading state correctly', () => {
    const { getByText } = render(
      <ContentList
        title="Loading List"
        items={[]}
        onItemPress={() => {}}
        isLoading={true}
      />
    );

    // Check if loading text is shown
    expect(getByText('Loading content...')).toBeTruthy();
  });

  it('removes loading indicator after items are loaded', () => {
    // Render with loading state
    const { getByText, queryByText, rerender } = render(
      <ContentList
        title="Test List"
        items={[]}
        onItemPress={() => {}}
        isLoading={true}
      />
    );

    // Assert loading indicator is present
    expect(getByText('Loading content...')).toBeTruthy();

    // Update to loaded state
    rerender(
      <ContentList
        title="Test List"
        items={mockMangaItems}
        onItemPress={() => {}}
        isLoading={false}
      />
    );

    // Assert loading indicator is absent
    expect(queryByText('Loading content...')).toBeNull();

    // Assert items are rendered
    expect(getByText('Test Manga 1')).toBeTruthy();
    expect(getByText('Test Manga 2')).toBeTruthy();
  });

  it('renders empty state correctly', () => {
    const customEmptyMessage = 'No manga found';
    const { getByText } = render(
      <ContentList
        title="Empty List"
        items={[]}
        onItemPress={() => {}}
        emptyMessage={customEmptyMessage}
      />
    );

    // Check if empty message is shown
    expect(getByText(customEmptyMessage)).toBeTruthy();
  });

  it('toggles between grid and list view', () => {
    const { getByText } = render(
      <ContentList
        title="Toggle View Test"
        items={mockMangaItems}
        onItemPress={() => {}}
      />
    );

    // Initially in grid view
    expect(getByText('☰')).toBeTruthy();
    
    // Toggle to list view
    fireEvent.press(getByText('☰'));
    
    // Now should be in list view
    expect(getByText('⊞')).toBeTruthy();
  });

  it('calls onItemPress when an item is pressed', () => {
    const onItemPressMock = jest.fn();
    const { getByText } = render(
      <ContentList
        items={mockMangaItems}
        onItemPress={onItemPressMock}
      />
    );

    // Press the first manga title
    fireEvent.press(getByText('Test Manga 1'));
    
    // Check if onItemPress was called
    expect(onItemPressMock).toHaveBeenCalled();
    // Check if onItemPress was called with the correct item
    expect(onItemPressMock).toHaveBeenCalledWith(mockMangaItems[0]);
  });
});