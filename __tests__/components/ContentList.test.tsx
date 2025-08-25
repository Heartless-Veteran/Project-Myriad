import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import { Text } from 'react-native';
import ContentList, { ContentItem } from '../../src/components/ContentList';
import { Manga, Anime } from '../../src/types';

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
        dateAdded: '2024-01-01T00:00:00.000Z',
      },
    ],
    genres: ['Action', 'Adventure'],
    status: 'ongoing',
    rating: 4.5,
    tags: ['Popular', 'New'],
    readingProgress: 0.5,
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
        dateAdded: '2024-01-01T00:00:00.000Z',
      },
    ],
    genres: ['Comedy', 'Slice of Life'],
    status: 'completed',
    rating: 3.8,
    tags: ['Recommended'],
    readingProgress: 1.0,
  },
];

const mockAnimeItems: Anime[] = [
  {
    id: '3',
    title: 'Test Anime 1',
    description: 'Anime Description 1',
    coverImage: 'https://example.com/anime1.jpg',
    episodes: [
      {
        id: 'e1',
        title: 'Episode 1',
        episodeNumber: 1,
        duration: 1440,
        watchProgress: 0.8,
        isWatched: false,
        dateAdded: '2024-01-01T00:00:00.000Z',
      },
    ],
    genres: ['Action', 'Sci-Fi'],
    status: 'ongoing',
    rating: 4.2,
    studio: 'Test Studio',
    tags: ['Popular'],
    watchProgress: 0.3,
  },
];

// Mock the Card component since we're only testing ContentList
jest.mock('../../src/components/Card', () => {
  const { View, Text, TouchableOpacity } = require('react-native');
  return function MockCard({ title, imageUrl, tags, progress, onPress, children, style }: any) {
    return (
      <TouchableOpacity testID="mock-card" style={style} onPress={onPress}>
        {children}
        {title && <Text testID="card-title">{title}</Text>}
        {imageUrl && <Text testID="card-image">{imageUrl}</Text>}
        {tags && tags.map((tag: string) => (
          <Text key={tag} testID={`card-tag-${tag}`}>{tag}</Text>
        ))}
        {progress !== undefined && <Text testID="card-progress">{progress}</Text>}
      </TouchableOpacity>
    );
  };
});

describe('ContentList Component', () => {
  const mockOnItemPress = jest.fn();
  const mockOnItemLongPress = jest.fn();

  beforeEach(() => {
    mockOnItemPress.mockClear();
    mockOnItemLongPress.mockClear();
  });

  it('renders correctly with manga items', () => {
    const { getByText } = render(
      <ContentList
        data={mockMangaItems}
        onItemPress={mockOnItemPress}
      />
    );

    expect(getByText('Test Manga 1')).toBeTruthy();
    expect(getByText('Test Manga 2')).toBeTruthy();
  });

  it('renders correctly with anime items', () => {
    const { getByText } = render(
      <ContentList
        data={mockAnimeItems}
        onItemPress={mockOnItemPress}
      />
    );

    expect(getByText('Test Anime 1')).toBeTruthy();
  });

  it('renders correctly with mixed content', () => {
    const mixedData = [...mockMangaItems, ...mockAnimeItems];
    const { getByText } = render(
      <ContentList
        data={mixedData}
        onItemPress={mockOnItemPress}
      />
    );

    expect(getByText('Test Manga 1')).toBeTruthy();
    expect(getByText('Test Anime 1')).toBeTruthy();
  });

  it('renders loading state correctly', () => {
    const { getByText } = render(
      <ContentList
        data={[]}
        onItemPress={mockOnItemPress}
        isLoading={true}
      />
    );

    expect(getByText('Loading content...')).toBeTruthy();
  });

  it('renders empty state with default message', () => {
    const { getByText } = render(
      <ContentList
        data={[]}
        onItemPress={mockOnItemPress}
        isLoading={false}
      />
    );

    expect(getByText('No items found')).toBeTruthy();
  });

  it('renders empty state with custom message', () => {
    const customEmptyMessage = 'No manga found';
    const { getByText } = render(
      <ContentList
        data={[]}
        onItemPress={mockOnItemPress}
        emptyMessage={customEmptyMessage}
      />
    );

    expect(getByText(customEmptyMessage)).toBeTruthy();
  });

  it('calls onItemPress when an item is pressed', () => {
    const { getAllByTestId } = render(
      <ContentList
        data={mockMangaItems}
        onItemPress={mockOnItemPress}
      />
    );

    const cards = getAllByTestId('mock-card');
    fireEvent.press(cards[0]);
    
    expect(mockOnItemPress).toHaveBeenCalledTimes(1);
    expect(mockOnItemPress).toHaveBeenCalledWith(mockMangaItems[0]);
  });

  it('calls onItemLongPress when an item is long pressed', () => {
    const { getAllByTestId } = render(
      <ContentList
        data={mockMangaItems}
        onItemPress={mockOnItemPress}
        onItemLongPress={mockOnItemLongPress}
      />
    );

    const cards = getAllByTestId('mock-card');
    fireEvent(cards[0], 'longPress');
    
    expect(mockOnItemLongPress).toHaveBeenCalledTimes(1);
    expect(mockOnItemLongPress).toHaveBeenCalledWith(mockMangaItems[0]);
  });

  it('renders custom item when renderItem is provided', () => {
    const customRenderItem = ({ item }: { item: ContentItem }) => (
      <Text testID="custom-item">{item.title} - Custom</Text>
    );

    const { getByText } = render(
      <ContentList
        data={mockMangaItems}
        onItemPress={mockOnItemPress}
        renderItem={customRenderItem}
      />
    );

    expect(getByText('Test Manga 1 - Custom')).toBeTruthy();
  });

  it('displays correct progress for manga items', () => {
    const { getAllByTestId } = render(
      <ContentList
        data={mockMangaItems}
        onItemPress={mockOnItemPress}
      />
    );

    const progressElements = getAllByTestId('card-progress');
    expect(progressElements[0]).toHaveTextContent('0.5');
    expect(progressElements[1]).toHaveTextContent('1');
  });

  it('displays correct progress for anime items', () => {
    const { getByTestId } = render(
      <ContentList
        data={mockAnimeItems}
        onItemPress={mockOnItemPress}
      />
    );

    const progressElement = getByTestId('card-progress');
    expect(progressElement).toHaveTextContent('0.3');
  });

  it('renders with grid display mode by default', () => {
    const { container } = render(
      <ContentList
        data={mockMangaItems}
        onItemPress={mockOnItemPress}
      />
    );

    // Component should render without errors in grid mode
    expect(container).toBeTruthy();
  });

  it('renders with list display mode when specified', () => {
    const { container } = render(
      <ContentList
        data={mockMangaItems}
        onItemPress={mockOnItemPress}
        displayMode="list"
      />
    );

    // Component should render without errors in list mode
    expect(container).toBeTruthy();
  });

  it('applies custom style when provided', () => {
    const customStyle = { backgroundColor: 'red' };
    const { container } = render(
      <ContentList
        data={mockMangaItems}
        onItemPress={mockOnItemPress}
        style={customStyle}
      />
    );

    expect(container).toBeTruthy();
  });

  it('renders with refresh control when provided', () => {
    const mockRefreshControl = <Text testID="refresh-control">Refresh</Text>;
    const { getByTestId } = render(
      <ContentList
        data={mockMangaItems}
        onItemPress={mockOnItemPress}
        refreshControl={mockRefreshControl}
      />
    );

    expect(getByTestId('refresh-control')).toBeTruthy();
  });

  it('handles empty data gracefully', () => {
    const { getByText } = render(
      <ContentList
        data={[]}
        onItemPress={mockOnItemPress}
      />
    );

    expect(getByText('No items found')).toBeTruthy();
  });

  it('renders tags correctly for items', () => {
    const { getByTestId } = render(
      <ContentList
        data={mockMangaItems}
        onItemPress={mockOnItemPress}
      />
    );

    expect(getByTestId('card-tag-Action')).toBeTruthy();
    expect(getByTestId('card-tag-Adventure')).toBeTruthy();
  });
});