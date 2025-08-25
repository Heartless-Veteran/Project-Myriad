import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import ContentViewer from '../../src/components/ContentViewer';
import { MangaChapter, AnimeEpisode } from '../../src/types';

// Mock FastImage
jest.mock('react-native-fast-image', () => {
  const React = require('react');
  return {
    __esModule: true,
    default: React.forwardRef((props: any, ref: any) => {
      const MockedFastImage = require('react-native').Image;
      return <MockedFastImage {...props} ref={ref} />;
    }),
    priority: {
      normal: 'normal',
    },
    resizeMode: {
      contain: 'contain',
    },
  };
});

describe('ContentViewer Component', () => {
  const mockMangaChapter: MangaChapter = {
    id: 'chapter-1',
    title: 'Test Chapter',
    chapterNumber: 1,
    pages: [
      'https://example.com/page1.jpg',
      'https://example.com/page2.jpg',
      'https://example.com/page3.jpg',
    ],
    readProgress: 0,
    isRead: false,
    dateAdded: new Date().toISOString(),
  };

  const mockAnimeEpisode: AnimeEpisode = {
    id: 'episode-1',
    title: 'Test Episode',
    episodeNumber: 1,
    duration: 1440, // 24 minutes
    watchProgress: 0.5,
    isWatched: false,
    videoUrl: 'https://example.com/episode1.mp4',
    dateAdded: new Date().toISOString(),
  };

  const defaultProps = {
    title: 'Test Content',
    onClose: jest.fn(),
    onProgressUpdate: jest.fn(),
  };

  beforeEach(() => {
    jest.clearAllMocks();
  });

  describe('Manga Viewer', () => {
    it('renders manga content correctly', () => {
      const { getByText } = render(
        <ContentViewer
          {...defaultProps}
          contentType="manga"
          content={mockMangaChapter}
        />
      );

      expect(getByText('Test Content')).toBeTruthy();
    });

    it('shows correct page count', () => {
      const { getByText } = render(
        <ContentViewer
          {...defaultProps}
          contentType="manga"
          content={mockMangaChapter}
        />
      );

      expect(getByText('1/3')).toBeTruthy();
    });

    it('calls onClose when close button is pressed', () => {
      const onCloseMock = jest.fn();
      const { getByText } = render(
        <ContentViewer
          {...defaultProps}
          contentType="manga"
          content={mockMangaChapter}
          onClose={onCloseMock}
        />
      );

      const closeButton = getByText('✕');
      fireEvent.press(closeButton);

      expect(onCloseMock).toHaveBeenCalled();
    });

    it('shows navigation buttons when provided', () => {
      const { getByText } = render(
        <ContentViewer
          {...defaultProps}
          contentType="manga"
          content={mockMangaChapter}
          onNext={jest.fn()}
          onPrevious={jest.fn()}
        />
      );

      expect(getByText('Previous')).toBeTruthy();
      expect(getByText('Next')).toBeTruthy();
    });

    it('calls navigation callbacks when buttons are pressed', () => {
      const onNextMock = jest.fn();
      const onPreviousMock = jest.fn();
      const { getByText } = render(
        <ContentViewer
          {...defaultProps}
          contentType="manga"
          content={mockMangaChapter}
          onNext={onNextMock}
          onPrevious={onPreviousMock}
        />
      );

      fireEvent.press(getByText('Next'));
      fireEvent.press(getByText('Previous'));

      expect(onNextMock).toHaveBeenCalled();
      expect(onPreviousMock).toHaveBeenCalled();
    });
  });

  describe('Anime Viewer', () => {
    it('renders anime content correctly', () => {
      const { getByText } = render(
        <ContentViewer
          {...defaultProps}
          contentType="anime"
          content={mockAnimeEpisode}
        />
      );

      expect(getByText('Test Content')).toBeTruthy();
      expect(getByText(/Video Player Placeholder/)).toBeTruthy();
    });

    it('shows correct watch progress', () => {
      const { getByText } = render(
        <ContentViewer
          {...defaultProps}
          contentType="anime"
          content={mockAnimeEpisode}
        />
      );

      expect(getByText('50%')).toBeTruthy();
    });
  });
});
