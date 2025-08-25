import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  TouchableOpacity,
  Image,
  ActivityIndicator,
  Alert,
  TextInput,
} from 'react-native';
import { useDispatch, useSelector } from 'react-redux';
import { launchImageLibrary } from 'react-native-image-picker';
import Button from '../components/Button';
import Card from '../components/Card';
import { ContentList } from '../components/ContentList';
import { ArtStyleMatch, Manga, Anime } from '../types';
import { RootState } from '../store';
import {
  analyzeArtStyle,
  clearArtStyleMatches,
  clearTranslations,
  extractMetadata,
  performNaturalLanguageSearch,
  setOfflineMode,
  translateText,
} from '../store/slices/aiSlice';

const AICoreScreen: React.FC = () => {
  const dispatch = useDispatch();
  const {
    isInitialized,
    isProcessing,
    isOfflineMode,
    error,
    translations,
    currentTranslation,
    artStyleMatches,
    metadata,
    searchResults: aiSearchResults,
  } = useSelector((state: RootState) => state.ai);
  const { manga, anime } = useSelector((state: RootState) => state.library);
  const settings = useSelector((state: RootState) => state.settings);

  const [activeFeature, setActiveFeature] = useState<
    'ocr' | 'artStyle' | 'metadata' | 'search'
  >('ocr');
  const [selectedImage, setSelectedImage] = useState<string | null>(null);
  const [searchQuery, setSearchQuery] = useState('');
  const [searchResults, setSearchResults] = useState<any[]>([]);

  useEffect(() => {
    if (aiSearchResults) {
      setSearchResults(aiSearchResults);
    }
  }, [aiSearchResults]);

  const handleImagePicker = () => {
    launchImageLibrary(
      { mediaType: 'photo', includeBase64: true },
      (response) => {
        if (response.didCancel) {
          console.log('User cancelled image picker');
        } else if (response.errorMessage) {
          console.log('ImagePicker Error: ', response.errorMessage);
          Alert.alert('Error', 'Could not select image.');
        } else if (response.assets && response.assets[0].base64) {
          setSelectedImage(response.assets[0].base64);
        }
      }
    );
  };

  const handleOCRTranslation = () => {
    if (!selectedImage) {
      Alert.alert('No Image', 'Please select an image first');
      return;
    }

    const options = {
      targetLanguage: settings.defaultTargetLanguage,
      confidence: 0.8,
    };

    dispatch(translateText({ imageBase64: selectedImage, options }));
  };

  const handleArtStyleAnalysis = () => {
    if (!selectedImage) {
      Alert.alert('No Image', 'Please select an image first');
      return;
    }

    const library = [...manga, ...anime];
    dispatch(analyzeArtStyle({ imageBase64: selectedImage, library }));
  };

  const handleMetadataExtraction = () => {
    if (!selectedImage) {
      Alert.alert('No Image', 'Please select an image first');
      return;
    }

    dispatch(extractMetadata(selectedImage));
  };

  const handleNaturalLanguageSearch = async () => {
    if (!searchQuery.trim()) {
      Alert.alert('No Query', 'Please enter a search query');
      return;
    }

    const library = [...manga, ...anime];
    const result = await dispatch(
      performNaturalLanguageSearch({ query: searchQuery, library })
    );
    if (result.payload) {
      setSearchResults(result.payload.results);
    }
  };

  const renderStatusCard = () => (
    <Card style={styles.statusCard}>
      <View style={styles.statusRow}>
        <Text style={styles.statusLabel}>AI Status:</Text>
        <Text
          style={[
            styles.statusValue,
            { color: isInitialized ? '#4caf50' : '#f44336' },
          ]}
        >
          {isInitialized ? 'Ready' : 'Initializing...'}
        </Text>
      </View>
      <View style={styles.statusRow}>
        <Text style={styles.statusLabel}>Mode:</Text>
        <Text
          style={[
            styles.statusValue,
            { color: isOfflineMode ? '#ff9800' : '#2196f3' },
          ]}
        >
          {isOfflineMode ? 'Offline' : 'Online'}
        </Text>
      </View>
      <TouchableOpacity
        style={styles.toggleButton}
        onPress={() => dispatch(setOfflineMode(!isOfflineMode))}
      >
        <Text style={styles.toggleButtonText}>
          Switch to {isOfflineMode ? 'Online' : 'Offline'}
        </Text>
      </TouchableOpacity>
    </Card>
  );

  const renderFeatureButtons = () => (
    <View style={styles.featureButtons}>
      {[
        { key: 'ocr', title: 'OCR Translation', icon: '🔤' },
        { key: 'artStyle', title: 'Art Style', icon: '🎨' },
        { key: 'metadata', title: 'Metadata', icon: '📊' },
        { key: 'search', title: 'NL Search', icon: '🔍' },
      ] as const).map(({ key, title, icon }) => (
        <TouchableOpacity
          key={key}
          style={[
            styles.featureButton,
            activeFeature === key && styles.activeFeatureButton,
          ]}
          onPress={() => setActiveFeature(key)}
        >
          <Text style={styles.featureIcon}>{icon}</Text>
          <Text
            style={[
              styles.featureButtonText,
              activeFeature === key && styles.activeFeatureButtonText,
            ]}
          >
            {title}
          </Text>
        </TouchableOpacity>
      ))}
    </View>
  );

  const renderOCRFeature = () => (
    <View style={styles.featureContainer}>
      <Text style={styles.featureTitle}>OCR Translation</Text>
      <Text style={styles.featureDescription}>
        Extract and translate text from manga panels using Tesseract OCR
      </Text>

      <Button
        title="Select Image"
        onPress={handleImagePicker}
        style={styles.actionButton}
      />

      {selectedImage && (
        <View style={styles.imageContainer}>
          <Image
            source={{ uri: `data:image/jpeg;base64,${selectedImage}` }}
            style={styles.selectedImage}
            resizeMode="contain"
          />
          <Button
            title="Translate Text"
            onPress={handleOCRTranslation}
            disabled={isProcessing}
            style={styles.actionButton}
          />
        </View>
      )}

      {currentTranslation && (
        <Card style={styles.translationCard}>
          <Text style={styles.translationLabel}>Original:</Text>
          <Text style={styles.translationText}>{currentTranslation.originalText}</Text>
          <Text style={styles.translationLabel}>Translation:</Text>
          <Text style={styles.translationText}>{currentTranslation.translatedText}</Text>
          <Text style={styles.translationMeta}>
            Confidence: {(currentTranslation.confidence * 100).toFixed(1)}%
          </Text>
        </Card>
      )}

      {translations.length > 0 && (
        <View style={styles.historyContainer}>
          <View style={styles.historyHeader}>
            <Text style={styles.historyTitle}>Translation History</Text>
            <TouchableOpacity onPress={() => dispatch(clearTranslations())}>
              <Text style={styles.clearButton}>Clear</Text>
            </TouchableOpacity>
          </View>
          <ScrollView style={styles.historyList}>
            {translations.slice(0, 5).map((translation, index) => (
              <Card key={index} style={styles.historyItem}>
                <Text style={styles.historyOriginal}>{translation.originalText}</Text>
                <Text style={styles.historyTranslation}>{translation.translatedText}</Text>
              </Card>
            ))}
          </ScrollView>
        </View>
      )}
    </View>
  );

  const renderArtStyleFeature = () => (
    <View style={styles.featureContainer}>
      <Text style={styles.featureTitle}>Art Style Analysis</Text>
      <Text style={styles.featureDescription}>
        Find similar content based on art style using computer vision
      </Text>

      <Button
        title="Select Image"
        onPress={handleImagePicker}
        style={styles.actionButton}
      />

      {selectedImage && (
        <View style={styles.imageContainer}>
          <Image
            source={{ uri: `data:image/jpeg;base64,${selectedImage}` }}
            style={styles.selectedImage}
            resizeMode="contain"
          />
          <Button
            title="Analyze Art Style"
            onPress={handleArtStyleAnalysis}
            disabled={isProcessing}
            style={styles.actionButton}
          />
        </View>
      )}

      {artStyleMatches.length > 0 && (
        <View style={styles.matchesContainer}>
          <View style={styles.historyHeader}>
            <Text style={styles.historyTitle}>Similar Content</Text>
            <TouchableOpacity onPress={() => dispatch(clearArtStyleMatches())}>
              <Text style={styles.clearButton}>Clear</Text>
            </TouchableOpacity>
          </View>
          <ContentList
            data={artStyleMatches.map((match: ArtStyleMatch) => match.item)}
            onItemPress={(item: Manga | Anime) => console.log('Open similar content:', item.title)}
            style={styles.matchesList}
          />
        </View>
      )}
    </View>
  );

  const renderMetadataFeature = () => (
    <View style={styles.featureContainer}>
      <Text style={styles.featureTitle}>Metadata Extraction</Text>
      <Text style={styles.featureDescription}>
        Extract metadata from a cover image
      </Text>

      <Button
        title="Select Image"
        onPress={handleImagePicker}
        style={styles.actionButton}
      />

      {selectedImage && (
        <View style={styles.imageContainer}>
          <Image
            source={{ uri: `data:image/jpeg;base64,${selectedImage}` }}
            style={styles.selectedImage}
            resizeMode="contain"
          />
          <Button
            title="Extract Metadata"
            onPress={handleMetadataExtraction}
            disabled={isProcessing}
            style={styles.actionButton}
          />
        </View>
      )}

      {metadata && (
        <Card style={styles.translationCard}>
          <Text style={styles.translationLabel}>Title:</Text>
          <Text style={styles.translationText}>{metadata.title}</Text>
          <Text style={styles.translationLabel}>Author:</Text>
          <Text style={styles.translationText}>{metadata.author}</Text>
          <Text style={styles.translationLabel}>Tags:</Text>
          <Text style={styles.translationText}>{metadata.tags.join(', ')}</Text>
        </Card>
      )}
    </View>
  );

  const renderSearchFeature = () => (
    <View style={styles.featureContainer}>
      <Text style={styles.featureTitle}>Natural Language Search</Text>
      <Text style={styles.featureDescription}>
        Search your library using natural language queries
      </Text>

      <TextInput
        style={styles.searchInput}
        value={searchQuery}
        onChangeText={setSearchQuery}
        placeholder="e.g., 'Show me action manga with romance'"
        multiline
      />

      <Button
        title="Search"
        onPress={handleNaturalLanguageSearch}
        disabled={isProcessing || !searchQuery.trim()}
        style={styles.actionButton}
      />

      {searchResults.length > 0 && (
        <View style={styles.searchResultsContainer}>
          <Text style={styles.historyTitle}>Search Results</Text>
          <ContentList
            data={searchResults}
            onItemPress={(item) => console.log('Open search result:', item.title)}
            style={styles.searchResultsList}
          />
        </View>
      )}
    </View>
  );

  const renderActiveFeature = () => {
    switch (activeFeature) {
      case 'ocr':
        return renderOCRFeature();
      case 'artStyle':
        return renderArtStyleFeature();
      case 'metadata':
        return renderMetadataFeature();
      case 'search':
        return renderSearchFeature();
      default:
        return renderOCRFeature();
    }
  };

  if (!isInitialized && isProcessing) {
    return (
      <View style={styles.loadingContainer}>
        <ActivityIndicator size="large" color="#007AFF" />
        <Text style={styles.loadingText}>Initializing AI Core...</Text>
      </View>
    );
  }

  return (
    <ScrollView style={styles.container}>
      <Text style={styles.title}>AI Core</Text>
      {renderStatusCard()}
      {renderFeatureButtons()}

      {error && (
        <View style={styles.errorContainer}>
          <Text style={styles.errorText}>{error}</Text>
        </View>
      )}

      {isProcessing && (
        <View style={styles.processingContainer}>
          <ActivityIndicator size="small" color="#007AFF" />
          <Text style={styles.processingText}>Processing...</Text>
        </View>
      )}

      {renderActiveFeature()}
    </ScrollView>
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
  title: {
    fontSize: 28,
    fontWeight: 'bold',
    color: '#333',
    paddingHorizontal: 16,
    paddingTop: 16,
    paddingBottom: 8,
  },
  statusCard: {
    margin: 16,
    padding: 16,
  },
  statusRow: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginBottom: 8,
  },
  statusLabel: {
    fontSize: 16,
    color: '#666',
  },
  statusValue: {
    fontSize: 16,
    fontWeight: '600',
  },
  toggleButton: {
    backgroundColor: '#007AFF',
    padding: 12,
    borderRadius: 8,
    alignItems: 'center',
    marginTop: 12,
  },
  toggleButtonText: {
    color: 'white',
    fontSize: 16,
    fontWeight: '600',
  },
  featureButtons: {
    flexDirection: 'row',
    marginHorizontal: 16,
    marginBottom: 16,
  },
  featureButton: {
    flex: 1,
    backgroundColor: 'white',
    padding: 12,
    margin: 2,
    borderRadius: 8,
    alignItems: 'center',
    borderWidth: 1,
    borderColor: '#ddd',
  },
  activeFeatureButton: {
    backgroundColor: '#007AFF',
    borderColor: '#007AFF',
  },
  featureIcon: {
    fontSize: 20,
    marginBottom: 4,
  },
  featureButtonText: {
    fontSize: 12,
    color: '#666',
    textAlign: 'center',
  },
  activeFeatureButtonText: {
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
  processingContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    backgroundColor: '#e3f2fd',
    margin: 16,
    padding: 12,
    borderRadius: 8,
  },
  processingText: {
    marginLeft: 8,
    color: '#1976d2',
    fontSize: 14,
  },
  featureContainer: {
    marginHorizontal: 16,
    marginBottom: 16,
  },
  featureTitle: {
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 8,
  },
  featureDescription: {
    fontSize: 14,
    color: '#666',
    marginBottom: 16,
  },
  actionButton: {
    marginBottom: 16,
  },
  imageContainer: {
    alignItems: 'center',
    marginBottom: 16,
  },
  selectedImage: {
    width: 200,
    height: 200,
    marginBottom: 16,
    borderRadius: 8,
  },
  translationCard: {
    padding: 16,
    marginBottom: 16,
  },
  translationLabel: {
    fontSize: 14,
    fontWeight: '600',
    color: '#666',
    marginBottom: 4,
  },
  translationText: {
    fontSize: 16,
    marginBottom: 12,
  },
  translationMeta: {
    fontSize: 12,
    color: '#999',
  },
  historyContainer: {
    marginTop: 16,
  },
  historyHeader: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    marginBottom: 12,
  },
  historyTitle: {
    fontSize: 18,
    fontWeight: 'bold',
  },
  clearButton: {
    color: '#007AFF',
    fontSize: 16,
  },
  historyList: {
    maxHeight: 200,
  },
  historyItem: {
    padding: 12,
    marginBottom: 8,
  },
  historyOriginal: {
    fontSize: 14,
    color: '#666',
    marginBottom: 4,
  },
  historyTranslation: {
    fontSize: 16,
  },
  matchesContainer: {
    marginTop: 16,
  },
  matchesList: {
    maxHeight: 300,
  },
  searchInput: {
    backgroundColor: 'white',
    padding: 16,
    borderRadius: 8,
    fontSize: 16,
    marginBottom: 16,
    minHeight: 60,
    textAlignVertical: 'top',
    borderWidth: 1,
    borderColor: '#ddd',
  },
  searchResultsContainer: {
    marginTop: 16,
  },
  searchResultsList: {
    maxHeight: 400,
  },
});

export default AICoreScreen;
