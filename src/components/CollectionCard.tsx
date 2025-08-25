import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Image,
} from 'react-native';
import { Collection } from '../types';

interface CollectionCardProps {
  collection: Collection;
  onPress: (collection: Collection) => void;
  onLongPress?: (collection: Collection) => void;
  contentCount?: number;
}

const CollectionCard: React.FC<CollectionCardProps> = ({
  collection,
  onPress,
  onLongPress,
  contentCount = 0,
}) => {
  return (
    <TouchableOpacity
      style={styles.container}
      onPress={() => onPress(collection)}
      onLongPress={() => onLongPress?.(collection)}
    >
      <View style={styles.imageContainer}>
        {collection.coverImage ? (
          <Image source={{ uri: collection.coverImage }} style={styles.coverImage} />
        ) : (
          <View style={styles.placeholderImage}>
            <Text style={styles.placeholderText}>
              {collection.contentType === 'manga' ? '📚' : 
               collection.contentType === 'anime' ? '🎬' : '📱'}
            </Text>
          </View>
        )}
        <View style={styles.countBadge}>
          <Text style={styles.countText}>{contentCount}</Text>
        </View>
      </View>
      
      <View style={styles.infoContainer}>
        <Text style={styles.title} numberOfLines={2}>{collection.name}</Text>
        {collection.description && (
          <Text style={styles.description} numberOfLines={2}>
            {collection.description}
          </Text>
        )}
        <View style={styles.metaContainer}>
          <Text style={styles.metaText}>
            {collection.contentType === 'mixed' ? 'Mixed' :
             collection.contentType === 'manga' ? 'Manga' : 'Anime'}
          </Text>
          {collection.isDefault && (
            <Text style={styles.defaultBadge}>Default</Text>
          )}
        </View>
      </View>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  container: {
    backgroundColor: '#2A2A2A',
    borderRadius: 8,
    marginHorizontal: 8,
    marginVertical: 4,
    padding: 12,
    elevation: 2,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.1,
    shadowRadius: 4,
  },
  imageContainer: {
    position: 'relative',
    marginBottom: 12,
  },
  coverImage: {
    width: '100%',
    height: 120,
    borderRadius: 6,
    backgroundColor: '#333',
  },
  placeholderImage: {
    width: '100%',
    height: 120,
    borderRadius: 6,
    backgroundColor: '#333',
    justifyContent: 'center',
    alignItems: 'center',
  },
  placeholderText: {
    fontSize: 32,
  },
  countBadge: {
    position: 'absolute',
    top: 8,
    right: 8,
    backgroundColor: '#007BFF',
    borderRadius: 12,
    paddingHorizontal: 8,
    paddingVertical: 2,
    minWidth: 24,
  },
  countText: {
    color: '#FFFFFF',
    fontSize: 12,
    fontWeight: 'bold',
    textAlign: 'center',
  },
  infoContainer: {
    flex: 1,
  },
  title: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  description: {
    color: '#CCCCCC',
    fontSize: 12,
    marginBottom: 8,
    lineHeight: 16,
  },
  metaContainer: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
  },
  metaText: {
    color: '#888888',
    fontSize: 11,
    textTransform: 'uppercase',
  },
  defaultBadge: {
    color: '#FFD700',
    fontSize: 10,
    fontWeight: 'bold',
    textTransform: 'uppercase',
  },
});

export default CollectionCard;