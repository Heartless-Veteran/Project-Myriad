import React, { useEffect, useState } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ActivityIndicator,
  Alert,
  RefreshControl,
  TouchableOpacity,
} from 'react-native';
import { useDispatch, useSelector } from 'react-redux';
import DocumentPicker from 'react-native-document-picker';
import { AppDispatch, RootState } from '../store';
import {
  loadLibrary,
  importManga,
  importAnime,
  deleteManga,
  deleteAnime,
  setFilters,
  updateMangaProgress,
  updateAnimeProgress,
} from '../store/slices/librarySlice';
import { Manga, Anime } from '../types';
import SearchBar from '../components/SearchBar';
import FilterPanel from '../components/FilterPanel';
import Card from '../components/Card';
import Button from '../components/Button';
import { ContentList } from '../components/ContentList';

export const LibraryScreen: React.FC = () => {
  const dispatch = useDispatch<AppDispatch>();
  const {
    manga,
    anime,
    isLoading,
    isImporting,
    error,
    stats,
    recommendations,
    filters,
    searchResults,
  } = useSelector((state: RootState) => state.library);

  const [searchQuery, setSearchQuery] = useState('');
  const [showFilters, setShowFilters] = useState(false);
  const [activeTab, setActiveTab] = useState<
    'all' | 'manga' | 'anime' | 'recommendations'
  >('all');

  useEffect(() => {
    dispatch(loadLibrary());
  }, [dispatch]);

  const handleSearch = (query: string) => {
    setSearchQuery(query);
  };

  const handleImport = async (type: 'manga' | 'anime') => {
    try {
      const result = await DocumentPicker.pick({
        type:
          type === 'manga'
            ? [DocumentPicker.types.zip, 'application/vnd.comicbook+zip']
            : [DocumentPicker.types.video],
        allowMultiSelection: false,
      });

      if (result.length > 0) {
        const file = result[0];
        if (type === 'manga') {
          dispatch(importManga({ uri: file.uri, name: file.name || 'Unknown' }));
        } else {
          dispatch(importAnime({ uri: file.uri, name: file.name || 'Unknown', generateThumbnail: true }));
        }
      }
    } catch (err) {
      if (!DocumentPicker.isCancel(err)) {
        Alert.alert('Import Error', 'Failed to import the selected file.');
      }
    }
  };

  const handleDelete = (item: Manga | Anime, type: 'manga' | 'anime') => {
    Alert.alert(
      'Delete Item',
      `Are you sure you want to delete "${item.title}"?`,
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: () => {
            if (type === 'manga') {
              dispatch(deleteManga(item.id));
            } else {
              dispatch(deleteAnime(item.id));
            }
          },
        },
      ]
    );
  };

  const renderStatsCard = () => (
    <Card style={styles.statsCard}>
      <Text style={styles.statsTitle}>Library Statistics</Text>
      <View style={styles.statsRow}>
        <View style={styles.statItem}>
          <Text style={styles.statNumber}>{stats.totalManga}</Text>
          <Text style={styles.statLabel}>Manga</Text>
        </View>
        <View style={styles.statItem}>
          <Text style={styles.statNumber}>{stats.totalAnime}</Text>
          <Text style={styles.statLabel}>Anime</Text>
        </View>
        <View style={styles.statItem}>
          <Text style={styles.statNumber}>{recommendations.length}</Text>
          <Text style={styles.statLabel}>Recommendations</Text>
        </View>
      </View>
    </Card>
  );

  const renderImportButtons = () => (
    <View style={styles.importContainer}>
      <Button
        title="Import Manga"
        onPress={() => handleImport('manga')}
        disabled={isImporting}
        style={styles.importButton}
      />
      <Button
        title="Import Anime"
        onPress={() => handleImport('anime')}
        disabled={isImporting}
        style={styles.importButton}
      />
    </View>
  );

  const renderTabBar = () => (
    <View style={styles.tabBar}>
      {(['all', 'manga', 'anime', 'recommendations'] as const).map((tab) => (
        <TouchableOpacity
          key={tab}
          style={[styles.tab, activeTab === tab && styles.activeTab]}
          onPress={() => setActiveTab(tab)}
        >
          <Text
            style={[styles.tabText, activeTab === tab && styles.activeTabText]}
          >
            {tab.charAt(0).toUpperCase() + tab.slice(1)}
          </Text>
        </TouchableOpacity>
      ))}
    </View>
  );

  const getFilteredContent = () => {
    let content: (Manga | Anime)[] = [];

    if (searchQuery && searchResults.length > 0) {
      content = searchResults;
    } else {
      switch (activeTab) {
        case 'manga':
          content = manga;
          break;
        case 'anime':
          content = anime;
          break;
        case 'recommendations':
          content = recommendations.map((rec) => rec.item);
          break;
        default:
          content = [...manga, ...anime];
      }
    }

    if (filters.genre.length > 0) {
      content = content.filter((item) =>
        item.genres.some((genre) => filters.genre.includes(genre))
      );
    }

    if (filters.status.length > 0) {
      content = content.filter((item) => filters.status.includes(item.status));
    }

    if (filters.rating > 0) {
      content = content.filter((item) => item.rating >= filters.rating);
    }

    return content;
  };

  if (isLoading && manga.length === 0 && anime.length === 0) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#007AFF" />
        <Text style={styles.loadingText}>Loading your library...</Text>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <SearchBar
        value={searchQuery}
        onChangeText={handleSearch}
        onFilterPress={() => setShowFilters(!showFilters)}
        placeholder="Search your library..."
      />

      {showFilters && (
        <FilterPanel
          filters={filters}
          onFiltersChange={(newFilters) => dispatch(setFilters(newFilters))}
          availableGenres={[
            ...new Set([...manga, ...anime].flatMap((item) => item.genres)),
          ]}
        />
      )}

      {renderStatsCard()}
      {renderImportButtons()}
      {renderTabBar()}

      {error && (
        <View style={styles.errorContainer}>
          <Text style={styles.errorText}>{error}</Text>
        </View>
      )}

      {isImporting && (
        <View style={styles.importingContainer}>
          <ActivityIndicator size="small" color="#007AFF" />
          <Text style={styles.importingText}>Importing file...</Text>
        </View>
      )}

      <ContentList
        data={getFilteredContent()}
        onItemPress={(item) => {
          console.log('Open content:', item.title);
        }}
        onItemLongPress={(item) => {
          const type = 'chapters' in item ? 'manga' : 'anime';
          handleDelete(item, type);
        }}
        renderItem={({ item }) => {
          const isManga = 'chapters' in item;
          const progress = isManga ? item.readingProgress : item.watchProgress;
          return (
            <View>
              <Card
                title={item.title}
                imageUrl={item.coverImage}
                tags={item.genres}
                progress={progress}
                onPress={() => console.log('Open content:', item.title)}
              />
              <View style={styles.progressButtons}>
                <Button
                  title="-1"
                  onPress={() => {
                    const newProgress = Math.max(0, (progress || 0) - 0.1);
                    if (isManga) {
                      dispatch(updateMangaProgress({ id: item.id, progress: newProgress }));
                    } else {
                      dispatch(updateAnimeProgress({ id: item.id, progress: newProgress }));
                    }
                  }}
                />
                <Button
                  title="+1"
                  onPress={() => {
                    const newProgress = Math.min(1, (progress || 0) + 0.1);
                    if (isManga) {
                      dispatch(updateMangaProgress({ id: item.id, progress: newProgress }));
                    } else {
                      dispatch(updateAnimeProgress({ id: item.id, progress: newProgress }));
                    }
                  }}
                />
              </View>
            </View>
          );
        }}
        refreshControl={
          <RefreshControl
            refreshing={isLoading}
            onRefresh={() => dispatch(loadLibrary())}
          />
        }
      />
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#f5f5f5',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    marginTop: 16,
    fontSize: 16,
    color: '#666',
  },
  statsCard: {
    margin: 16,
    padding: 16,
    borderRadius: 8,
  },
  statsTitle: {
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 12,
    textAlign: 'center',
  },
  statsRow: {
    flexDirection: 'row',
    justifyContent: 'space-around',
  },
  statItem: {
    alignItems: 'center',
  },
  statNumber: {
    fontSize: 20,
    fontWeight: 'bold',
    color: '#007AFF',
  },
  statLabel: {
    fontSize: 12,
    color: '#666',
    marginTop: 4,
  },
  importContainer: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    marginHorizontal: 16,
    marginBottom: 16,
  },
  importButton: {
    flex: 1,
    marginHorizontal: 8,
  },
  tabBar: {
    flexDirection: 'row',
    backgroundColor: 'white',
    marginHorizontal: 16,
    marginBottom: 16,
    borderRadius: 8,
    padding: 4,
  },
  tab: {
    flex: 1,
    alignItems: 'center',
    paddingVertical: 8,
    borderRadius: 6,
  },
  activeTab: {
    backgroundColor: '#007AFF',
  },
  tabText: {
    fontSize: 14,
    color: '#666',
    fontWeight: '600',
  },
  activeTabText: {
    color: 'white',
  },
  errorContainer: {
    backgroundColor: '#ffebee',
    margin: 16,
    padding: 12,
    borderRadius: 8,
    borderLeftWidth: 4,
    borderLeftColor: '#f44336',
  },
  errorText: {
    color: '#c62828',
    fontSize: 14,
  },
  importingContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#e3f2fd',
    margin: 16,
    padding: 12,
    borderRadius: 8,
  },
  importingText: {
    marginLeft: 8,
    color: '#1976d2',
    fontSize: 14,
  },
  progressButtons: {
    flexDirection: 'row',
    justifyContent: 'space-around',
    marginTop: -16,
    paddingBottom: 8,
  },
});

export default LibraryScreen;
