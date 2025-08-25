import React, { useState, useRef, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  ScrollView,
  Dimensions,
  TouchableOpacity,
  ActivityIndicator,
  SafeAreaView,
  StatusBar,
} from 'react-native';
import FastImage from 'react-native-fast-image';
import { MangaChapter, AnimeEpisode } from '../types';
import { StatisticsService } from '../services/StatisticsService';

const { width, height } = Dimensions.get('window');
const statisticsService = StatisticsService.getInstance();

type ContentType = 'manga' | 'anime';
type ReadingMode = 'single' | 'double' | 'webtoon' | 'continuous' | 'fit-width' | 'fit-height';

interface ContentViewerProps {
  contentType: ContentType;
  title: string;
  content: MangaChapter | AnimeEpisode;
  contentId: string;
  onClose: () => void;
  onNext?: () => void;
  onPrevious?: () => void;
  readingDirection?: 'ltr' | 'rtl' | 'vertical';
  readingMode?: ReadingMode;
  onProgressUpdate: (progress: number) => void;
}

const ContentViewer: React.FC<ContentViewerProps> = ({
  contentType,
  title,
  content,
  contentId,
  onClose,
  onNext,
  onPrevious,
  readingDirection = 'ltr',
  readingMode = 'single',
  onProgressUpdate,
}) => {
  const [showControls, setShowControls] = useState(true);
  const [currentIndex, setCurrentIndex] = useState(0);
  const [loading, setLoading] = useState(true);
  const [sessionId, setSessionId] = useState<string>('');
  const flatListRef = useRef<FlatList>(null);
  const scrollViewRef = useRef<ScrollView>(null);

  // Determine if we're viewing manga or anime
  const isManga = contentType === 'manga';
  const mangaChapter = isManga ? (content as MangaChapter) : null;
  const animeEpisode = !isManga ? (content as AnimeEpisode) : null;

  // Get the content items (pages for manga, video for anime)
  const contentItems = isManga ? mangaChapter?.pages || [] : [animeEpisode?.videoUrl || animeEpisode?.localPath || ''];
  
  // Set up the flatlist scroll direction based on reading direction
  const isHorizontal = readingDirection !== 'vertical';
  const reverseOrder = readingDirection === 'rtl';

  useEffect(() => {
    // Hide controls after 3 seconds
    const timer = setTimeout(() => {
      setShowControls(false);
    }, 3000);

    return () => clearTimeout(timer);
  }, [showControls]);

  useEffect(() => {
    // Reset to first item when content changes
    setCurrentIndex(0);
    setLoading(true);
    if (flatListRef.current) {
      flatListRef.current.scrollToOffset({ offset: 0, animated: false });
    }
  }, [content]);

  // Start reading session on component mount
  useEffect(() => {
    const startSession = async () => {
      try {
        const newSessionId = await statisticsService.startReadingSession(
          contentId,
          contentType,
          mangaChapter?.id,
          animeEpisode?.id
        );
        setSessionId(newSessionId);
      } catch (error) {
        console.warn('Failed to start reading session:', error);
      }
    };

    startSession();

    // End session on unmount
    return () => {
      if (sessionId) {
        statisticsService.endReadingSession(
          sessionId,
          mangaChapter?.pages.length,
          false // We don't know if it was completed
        ).catch(error => console.warn('Failed to end reading session:', error));
      }
    };
  }, [contentId, contentType, mangaChapter?.id, animeEpisode?.id]);

  const toggleControls = () => {
    setShowControls(!showControls);
  };

  const handleScroll = (event: any) => {
    const scrollPosition = event.nativeEvent.contentOffset[isHorizontal ? 'x' : 'y'];
    const itemSize = isHorizontal ? width : height;
    const index = Math.round(scrollPosition / itemSize);
    
    if (index !== currentIndex) {
      setCurrentIndex(index);
      
      // Calculate progress percentage
      const progress = (index + 1) / contentItems.length;
      onProgressUpdate(progress);
    }
  };

  const getPageDimensions = (mode: ReadingMode) => {
    const resizeMode = 'contain'; // FastImage.resizeMode.contain equivalent
    switch (mode) {
      case 'fit-width':
        return { width, height: undefined, resizeMode };
      case 'fit-height':
        return { width: undefined, height, resizeMode };
      case 'double':
        return { width: width / 2, height, resizeMode };
      default:
        return { width, height, resizeMode };
    }
  };

  const renderMangaPage = ({ item, index }: { item: string; index: number }) => {
    const { width: pageWidth, height: pageHeight, resizeMode } = getPageDimensions(readingMode);
    
    return (
      <TouchableOpacity
        activeOpacity={1}
        style={[
          styles.pageContainer, 
          { 
            width: pageWidth || width, 
            height: readingMode === 'webtoon' ? undefined : (pageHeight || height),
            minHeight: readingMode === 'webtoon' ? height : undefined
          }
        ]}
        onPress={toggleControls}
      >
        <FastImage
          source={{ uri: item, priority: FastImage.priority.normal }}
          style={[
            styles.pageImage,
            readingMode === 'webtoon' && styles.webtoonImage,
            readingMode === 'fit-width' && styles.fitWidthImage,
            readingMode === 'fit-height' && styles.fitHeightImage
          ]}
          resizeMode={resizeMode}
          onLoadStart={() => setLoading(true)}
          onLoadEnd={() => setLoading(false)}
        />
        {loading && (
          <View style={styles.loadingOverlay}>
            <ActivityIndicator size="large" color="#007BFF" />
          </View>
        )}
      </TouchableOpacity>
    );
  };

  const renderDoublePage = ({ item, index }: { item: string[]; index: number }) => {
    return (
      <View style={[styles.doublePageContainer, { width, height }]}>
        {item.map((pageUri, pageIndex) => (
          <TouchableOpacity
            key={pageIndex}
            activeOpacity={1}
            style={styles.doublePageItem}
            onPress={toggleControls}
          >
            <FastImage
              source={{ uri: pageUri, priority: FastImage.priority.normal }}
              style={styles.doublePageImage}
              resizeMode="contain"
            />
          </TouchableOpacity>
        ))}
      </View>
    );
  };

  const renderContinuousScroll = () => {
    return (
      <ScrollView
        ref={scrollViewRef}
        style={styles.continuousScrollContainer}
        showsVerticalScrollIndicator={false}
        onScroll={(event) => {
          const { contentOffset, layoutMeasurement, contentSize } = event.nativeEvent;
          const progress = (contentOffset.y + layoutMeasurement.height) / contentSize.height;
          onProgressUpdate(Math.min(progress, 1));
        }}
        scrollEventThrottle={16}
      >
        {contentItems.map((item, index) => (
          <TouchableOpacity
            key={index}
            activeOpacity={1}
            style={styles.continuousPageContainer}
            onPress={toggleControls}
          >
            <FastImage
              source={{ uri: item, priority: FastImage.priority.normal }}
              style={styles.continuousPageImage}
              resizeMode="contain"
            />
          </TouchableOpacity>
        ))}
      </ScrollView>
    );
  };

  // Prepare data for double page mode
  const prepareDoublePageData = (pages: string[]): string[][] => {
    const doublePages: string[][] = [];
    for (let i = 0; i < pages.length; i += 2) {
      if (i + 1 < pages.length) {
        doublePages.push([pages[i], pages[i + 1]]);
      } else {
        doublePages.push([pages[i]]);
      }
    }
    return doublePages;
  };

  // For anime, we would render a video player component here
  // This is a placeholder for now
  const renderAnimePlayer = () => {
    return (
      <View style={styles.videoContainer}>
        <Text style={styles.placeholderText}>
          Video Player Placeholder
          {'\n'}
          {animeEpisode?.title || 'Episode'}
        </Text>
      </View>
    );
  };

  return (
    <SafeAreaView style={styles.container}>
      <StatusBar hidden={!showControls} />
      
      {/* Content Viewer */}
      {isManga ? (
        readingMode === 'continuous' ? (
          renderContinuousScroll()
        ) : readingMode === 'double' ? (
          <FlatList
            ref={flatListRef}
            data={prepareDoublePageData(contentItems)}
            renderItem={renderDoublePage}
            keyExtractor={(_, index) => `double-page-${index}`}
            horizontal={isHorizontal}
            pagingEnabled
            showsHorizontalScrollIndicator={false}
            showsVerticalScrollIndicator={false}
            onScroll={handleScroll}
            scrollEventThrottle={16}
          />
        ) : (
          <FlatList
            ref={flatListRef}
            data={reverseOrder ? [...contentItems].reverse() : contentItems}
            renderItem={renderMangaPage}
            keyExtractor={(_, index) => `page-${index}`}
            horizontal={isHorizontal && readingMode !== 'webtoon'}
            pagingEnabled={readingMode !== 'webtoon'}
            showsHorizontalScrollIndicator={false}
            showsVerticalScrollIndicator={false}
            onScroll={handleScroll}
            scrollEventThrottle={16}
          />
        )
      ) : (
        renderAnimePlayer()
      )}
      
      {/* Controls Overlay */}
      {showControls && (
        <View style={styles.controlsOverlay}>
          <View style={styles.header}>
            <TouchableOpacity onPress={onClose} style={styles.closeButton}>
              <Text style={styles.closeButtonText}>✕</Text>
            </TouchableOpacity>
            <Text style={styles.title} numberOfLines={1}>{title}</Text>
            <View style={styles.spacer} />
          </View>
          
          <View style={styles.navigationControls}>
            {onPrevious && (
              <TouchableOpacity onPress={onPrevious} style={styles.navButton}>
                <Text style={styles.navButtonText}>Previous</Text>
              </TouchableOpacity>
            )}
            
            <View style={styles.progressIndicator}>
              <Text style={styles.progressText}>
                {isManga 
                  ? `${currentIndex + 1}/${contentItems.length}`
                  : `${Math.floor((animeEpisode?.watchProgress || 0) * 100)}%`
                }
              </Text>
            </View>
            
            {onNext && (
              <TouchableOpacity onPress={onNext} style={styles.navButton}>
                <Text style={styles.navButtonText}>Next</Text>
              </TouchableOpacity>
            )}
          </View>
        </View>
      )}
    </SafeAreaView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#000',
  },
  pageContainer: {
    justifyContent: 'center',
    alignItems: 'center',
  },
  pageImage: {
    width: '100%',
    height: '100%',
  },
  webtoonImage: {
    width: '100%',
    height: undefined,
    aspectRatio: undefined,
  },
  fitWidthImage: {
    width: '100%',
    height: undefined,
    aspectRatio: undefined,
  },
  fitHeightImage: {
    width: undefined,
    height: '100%',
    aspectRatio: undefined,
  },
  doublePageContainer: {
    flexDirection: 'row',
    justifyContent: 'center',
    alignItems: 'center',
  },
  doublePageItem: {
    flex: 1,
    height: '100%',
  },
  doublePageImage: {
    width: '100%',
    height: '100%',
  },
  continuousScrollContainer: {
    flex: 1,
    backgroundColor: '#000',
  },
  continuousPageContainer: {
    width: '100%',
    minHeight: height * 0.8,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 4,
  },
  continuousPageImage: {
    width: '100%',
    minHeight: height * 0.8,
  },
  videoContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#111',
  },
  placeholderText: {
    color: '#FFFFFF',
    fontSize: 18,
    textAlign: 'center',
  },
  loadingOverlay: {
    ...StyleSheet.absoluteFillObject,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
  },
  controlsOverlay: {
    ...StyleSheet.absoluteFillObject,
    justifyContent: 'space-between',
    backgroundColor: 'rgba(0, 0, 0, 0.5)',
  },
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    padding: 16,
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
  },
  closeButton: {
    width: 40,
    height: 40,
    justifyContent: 'center',
    alignItems: 'center',
  },
  closeButtonText: {
    color: '#FFFFFF',
    fontSize: 24,
  },
  title: {
    flex: 1,
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: 'bold',
    marginHorizontal: 16,
    textAlign: 'center',
  },
  spacer: {
    width: 40,
  },
  navigationControls: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16,
    backgroundColor: 'rgba(0, 0, 0, 0.7)',
  },
  navButton: {
    paddingVertical: 8,
    paddingHorizontal: 16,
    backgroundColor: '#007BFF',
    borderRadius: 4,
  },
  navButtonText: {
    color: '#FFFFFF',
    fontSize: 14,
    fontWeight: 'bold',
  },
  progressIndicator: {
    paddingVertical: 4,
    paddingHorizontal: 12,
    backgroundColor: 'rgba(255, 255, 255, 0.2)',
    borderRadius: 12,
  },
  progressText: {
    color: '#FFFFFF',
    fontSize: 14,
  },
});

export default ContentViewer;