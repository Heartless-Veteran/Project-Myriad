import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  FlatList,
  TouchableOpacity,
  Alert,
  TextInput,
  Modal,
} from 'react-native';
import { Collection, Manga, Anime } from '../types';
import { CollectionService } from '../services/CollectionService';
import CollectionCard from '../components/CollectionCard';

const collectionService = CollectionService.getInstance();

const CollectionsScreen: React.FC = () => {
  const [collections, setCollections] = useState<Collection[]>([]);
  const [loading, setLoading] = useState(true);
  const [showCreateModal, setShowCreateModal] = useState(false);
  const [newCollectionName, setNewCollectionName] = useState('');
  const [newCollectionDescription, setNewCollectionDescription] = useState('');
  const [selectedContentType, setSelectedContentType] = useState<'manga' | 'anime' | 'mixed'>('mixed');

  useEffect(() => {
    loadCollections();
  }, []);

  const loadCollections = async () => {
    try {
      setLoading(true);
      const collectionsData = await collectionService.getCollections();
      setCollections(collectionsData);
    } catch (error) {
      console.error('Failed to load collections:', error);
      Alert.alert('Error', 'Failed to load collections');
    } finally {
      setLoading(false);
    }
  };

  const handleCreateCollection = async () => {
    if (!newCollectionName.trim()) {
      Alert.alert('Error', 'Please enter a collection name');
      return;
    }

    try {
      const newCollection = await collectionService.createCollection({
        name: newCollectionName.trim(),
        description: newCollectionDescription.trim() || undefined,
        contentIds: [],
        contentType: selectedContentType,
        isDefault: false,
        sortOrder: collections.length,
      });

      setCollections([...collections, newCollection]);
      setShowCreateModal(false);
      setNewCollectionName('');
      setNewCollectionDescription('');
      setSelectedContentType('mixed');

      Alert.alert('Success', 'Collection created successfully');
    } catch (error) {
      console.error('Failed to create collection:', error);
      Alert.alert('Error', 'Failed to create collection');
    }
  };

  const handleDeleteCollection = (collection: Collection) => {
    if (collection.isDefault) {
      Alert.alert('Error', 'Cannot delete default collections');
      return;
    }

    Alert.alert(
      'Delete Collection',
      `Are you sure you want to delete "${collection.name}"?`,
      [
        { text: 'Cancel', style: 'cancel' },
        {
          text: 'Delete',
          style: 'destructive',
          onPress: async () => {
            try {
              await collectionService.deleteCollection(collection.id);
              setCollections(collections.filter(c => c.id !== collection.id));
              Alert.alert('Success', 'Collection deleted successfully');
            } catch (error) {
              console.error('Failed to delete collection:', error);
              Alert.alert('Error', 'Failed to delete collection');
            }
          },
        },
      ]
    );
  };

  const handleCollectionPress = (collection: Collection) => {
    // Navigate to collection detail screen (to be implemented)
    console.log('Navigate to collection:', collection.name);
  };

  const renderCollection = ({ item }: { item: Collection }) => (
    <CollectionCard
      collection={item}
      contentCount={item.contentIds.length}
      onPress={handleCollectionPress}
      onLongPress={handleDeleteCollection}
    />
  );

  const renderCreateModal = () => (
    <Modal
      visible={showCreateModal}
      transparent
      animationType="fade"
      onRequestClose={() => setShowCreateModal(false)}
    >
      <View style={styles.modalOverlay}>
        <View style={styles.modalContent}>
          <Text style={styles.modalTitle}>Create New Collection</Text>
          
          <Text style={styles.inputLabel}>Name *</Text>
          <TextInput
            style={styles.textInput}
            value={newCollectionName}
            onChangeText={setNewCollectionName}
            placeholder="Enter collection name"
            placeholderTextColor="#888"
          />

          <Text style={styles.inputLabel}>Description</Text>
          <TextInput
            style={[styles.textInput, styles.multilineInput]}
            value={newCollectionDescription}
            onChangeText={setNewCollectionDescription}
            placeholder="Enter description (optional)"
            placeholderTextColor="#888"
            multiline
            numberOfLines={3}
          />

          <Text style={styles.inputLabel}>Content Type</Text>
          <View style={styles.contentTypeContainer}>
            {(['mixed', 'manga', 'anime'] as const).map(type => (
              <TouchableOpacity
                key={type}
                style={[
                  styles.contentTypeButton,
                  selectedContentType === type && styles.contentTypeButtonActive
                ]}
                onPress={() => setSelectedContentType(type)}
              >
                <Text style={[
                  styles.contentTypeText,
                  selectedContentType === type && styles.contentTypeTextActive
                ]}>
                  {type.charAt(0).toUpperCase() + type.slice(1)}
                </Text>
              </TouchableOpacity>
            ))}
          </View>

          <View style={styles.modalButtons}>
            <TouchableOpacity
              style={styles.modalButton}
              onPress={() => setShowCreateModal(false)}
            >
              <Text style={styles.modalButtonText}>Cancel</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.modalButton, styles.modalButtonPrimary]}
              onPress={handleCreateCollection}
            >
              <Text style={styles.modalButtonPrimaryText}>Create</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Collections</Text>
        <TouchableOpacity
          style={styles.addButton}
          onPress={() => setShowCreateModal(true)}
        >
          <Text style={styles.addButtonText}>+</Text>
        </TouchableOpacity>
      </View>

      {loading ? (
        <View style={styles.loadingContainer}>
          <Text style={styles.loadingText}>Loading collections...</Text>
        </View>
      ) : collections.length === 0 ? (
        <View style={styles.emptyContainer}>
          <Text style={styles.emptyText}>No collections yet</Text>
          <Text style={styles.emptySubtext}>Tap + to create your first collection</Text>
        </View>
      ) : (
        <FlatList
          data={collections}
          renderItem={renderCollection}
          keyExtractor={item => item.id}
          numColumns={2}
          showsVerticalScrollIndicator={false}
          contentContainerStyle={styles.listContainer}
        />
      )}

      {renderCreateModal()}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1A1A1A',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#333',
  },
  title: {
    color: '#FFFFFF',
    fontSize: 24,
    fontWeight: 'bold',
  },
  addButton: {
    width: 40,
    height: 40,
    backgroundColor: '#007BFF',
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
  },
  addButtonText: {
    color: '#FFFFFF',
    fontSize: 24,
    fontWeight: 'bold',
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
  },
  loadingText: {
    color: '#CCCCCC',
    fontSize: 16,
  },
  emptyContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    padding: 40,
  },
  emptyText: {
    color: '#FFFFFF',
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 8,
  },
  emptySubtext: {
    color: '#CCCCCC',
    fontSize: 14,
    textAlign: 'center',
  },
  listContainer: {
    padding: 8,
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.8)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalContent: {
    backgroundColor: '#2A2A2A',
    borderRadius: 12,
    padding: 20,
    width: '90%',
    maxWidth: 400,
  },
  modalTitle: {
    color: '#FFFFFF',
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 20,
    textAlign: 'center',
  },
  inputLabel: {
    color: '#CCCCCC',
    fontSize: 14,
    fontWeight: '600',
    marginBottom: 8,
    marginTop: 12,
  },
  textInput: {
    backgroundColor: '#333',
    color: '#FFFFFF',
    borderRadius: 8,
    padding: 12,
    fontSize: 16,
    marginBottom: 8,
  },
  multilineInput: {
    height: 80,
    textAlignVertical: 'top',
  },
  contentTypeContainer: {
    flexDirection: 'row',
    marginBottom: 20,
  },
  contentTypeButton: {
    flex: 1,
    paddingVertical: 10,
    paddingHorizontal: 12,
    marginHorizontal: 4,
    backgroundColor: '#333',
    borderRadius: 6,
    alignItems: 'center',
  },
  contentTypeButtonActive: {
    backgroundColor: '#007BFF',
  },
  contentTypeText: {
    color: '#CCCCCC',
    fontSize: 14,
    fontWeight: '600',
  },
  contentTypeTextActive: {
    color: '#FFFFFF',
  },
  modalButtons: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    marginTop: 20,
  },
  modalButton: {
    flex: 1,
    paddingVertical: 12,
    paddingHorizontal: 20,
    backgroundColor: '#444',
    borderRadius: 6,
    marginHorizontal: 8,
    alignItems: 'center',
  },
  modalButtonPrimary: {
    backgroundColor: '#007BFF',
  },
  modalButtonText: {
    color: '#CCCCCC',
    fontSize: 16,
    fontWeight: '600',
  },
  modalButtonPrimaryText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '600',
  },
});

export default CollectionsScreen;