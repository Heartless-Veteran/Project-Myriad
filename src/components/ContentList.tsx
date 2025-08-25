import React, { useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  ActivityIndicator,
  TouchableOpacity,
  ViewStyle,
} from 'react-native';
import { Manga, Anime } from '../types';
import Card from './Card';

export type ContentItem = Manga | Anime;

interface ContentListProps {
  data: ContentItem[];
  onItemPress: (item: ContentItem) => void;
  onItemLongPress?: (item: ContentItem) => void;
  renderItem?: (item: { item: ContentItem }) => React.ReactElement;
  isLoading?: boolean;
  emptyMessage?: string;
  style?: ViewStyle;
  displayMode?: 'grid' | 'list';
  refreshControl?: React.ReactElement;
}

const ContentList: React.FC<ContentListProps> = ({
  data,
  onItemPress,
  onItemLongPress,
  renderItem: customRenderItem,
  isLoading = false,
  emptyMessage = 'No items found',
  style,
  displayMode = 'grid',
  refreshControl,
}) => {
  const [viewMode, setViewMode] = useState<'grid' | 'list'>(displayMode);

  const toggleViewMode = () => {
    setViewMode(viewMode === 'grid' ? 'list' : 'grid');
  };

  const defaultRenderItem = ({ item }: { item: ContentItem }) => {
    const isManga = 'chapters' in item;
    const progress = isManga ? item.readingProgress : item.watchProgress;

    return (
      <TouchableOpacity
        style={[
          styles.itemContainer,
          viewMode === 'list' && styles.listItemContainer,
        ]}
        onPress={() => onItemPress(item)}
        onLongPress={() => onItemLongPress && onItemLongPress(item)}
        activeOpacity={0.7}
      >
        <Card
          title={item.title}
          imageUrl={item.coverImage}
          tags={item.genres}
          progress={progress}
          onPress={() => onItemPress(item)}
        />
      </TouchableOpacity>
    );
  };

  const getStatusColor = (status: string) => {
    switch (status) {
      case 'ongoing':
        return '#4CAF50';
      case 'completed':
        return '#2196F3';
      case 'hiatus':
        return '#FF9800';
      case 'upcoming':
        return '#9C27B0';
      default:
        return '#757575';
    }
  };

  const renderEmpty = () => (
    <View style={styles.emptyContainer}>
      <Text style={styles.emptyText}>{emptyMessage}</Text>
    </View>
  );

  if (isLoading) {
    return (
      <View style={[styles.container, style]}>
        <View style={styles.loadingContainer}>
          <ActivityIndicator size="large" color="#007AFF" />
          <Text style={styles.loadingText}>Loading content...</Text>
        </View>
      </View>
    );
  }

  return (
    <View style={[styles.container, style]}>
      <FlatList
        data={data}
        renderItem={customRenderItem || defaultRenderItem}
        keyExtractor={(item) => item.id}
        numColumns={viewMode === 'grid' ? 2 : 1}
        key={viewMode} // Force re-render when view mode changes
        contentContainerStyle={styles.listContainer}
        showsVerticalScrollIndicator={false}
        ListEmptyComponent={renderEmpty}
        refreshControl={refreshControl}
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#f0f0f0',
  },
  headerTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#333',
  },
  viewModeButton: {
    padding: 8,
    borderRadius: 6,
    backgroundColor: '#f0f0f0',
  },
  viewModeButtonText: {
    fontSize: 16,
    color: '#666',
  },
  listContainer: {
    padding: 8,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    marginTop: 12,
    fontSize: 16,
    color: '#666',
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 32,
  },
  emptyText: {
    fontSize: 16,
    color: '#999',
    textAlign: 'center',
  },
  itemContainer: {
    flex: 1,
    margin: 8,
    maxWidth: '47%',
  },
  listItemContainer: {
    maxWidth: '100%',
    marginVertical: 4,
    marginHorizontal: 8,
  },
  itemCard: {
    padding: 12,
    height: 280,
  },
  listItemCard: {
    flexDirection: 'row',
    height: 120,
    padding: 12,
  },
  coverImage: {
    width: '100%',
    height: 120,
    backgroundColor: '#f0f0f0',
    borderRadius: 8,
    justifyContent: 'center',
    alignItems: 'center',
    marginBottom: 8,
  },
  listCoverImage: {
    width: 80,
    height: 96,
    marginBottom: 0,
    marginRight: 12,
  },
  coverPlaceholder: {
    fontSize: 32,
  },
  itemDetails: {
    flex: 1,
  },
  listItemDetails: {
    flex: 1,
    justifyContent: 'space-between',
  },
  itemTitle: {
    fontSize: 14,
    fontWeight: 'bold',
    color: '#333',
    marginBottom: 4,
  },
  itemAuthor: {
    fontSize: 12,
    color: '#666',
    marginBottom: 4,
  },
  itemStudio: {
    fontSize: 12,
    color: '#666',
    marginBottom: 4,
  },
  itemProgress: {
    fontSize: 12,
    color: '#999',
    marginBottom: 8,
  },
  statusContainer: {
    marginBottom: 8,
  },
  statusBadge: {
    alignSelf: 'flex-start',
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4,
  },
  statusText: {
    fontSize: 10,
    color: 'white',
    fontWeight: 'bold',
  },
  ratingContainer: {
    marginBottom: 8,
  },
  ratingText: {
    fontSize: 12,
    color: '#FF9800',
  },
  progressBarContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  progressBarBackground: {
    flex: 1,
    height: 4,
    backgroundColor: '#e0e0e0',
    borderRadius: 2,
    marginRight: 8,
  },
  progressBarFill: {
    height: '100%',
    backgroundColor: '#4CAF50',
    borderRadius: 2,
  },
  progressPercentage: {
    fontSize: 10,
    color: '#666',
    minWidth: 30,
    textAlign: 'right',
  },
  genresContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginTop: 4,
  },
  genreTag: {
    backgroundColor: '#e3f2fd',
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4,
    marginRight: 4,
    marginBottom: 4,
  },
  genreText: {
    fontSize: 10,
    color: '#1976d2',
  },
  moreGenres: {
    fontSize: 10,
    color: '#666',
    alignSelf: 'center',
  },
});

export default ContentList;