import React from 'react';
import { View, Text, StyleSheet, TouchableOpacity, ViewStyle } from 'react-native';
import FastImage from 'react-native-fast-image';
import ProgressBar from './ProgressBar';

interface CardProps {
  title?: string;
  imageUrl?: string;
  onPress?: () => void;
  tags?: string[];
  progress?: number; // Add progress prop
  
  children?: React.ReactNode;
  style?: ViewStyle;
}

const Card: React.FC<CardProps> = ({ 
  title, 
  imageUrl, 
  onPress, 
  tags, 
  progress,
  children, 
  style 
}) => {
  if (children) {
    return (
      <View style={[styles.containerCard, style]}>
        {children}
      </View>
    );
  }

  if (!title || !imageUrl || !onPress) {
    return (
      <View style={[styles.containerCard, style]}>
        {children}
      </View>
    );
  }

  return (
    <TouchableOpacity style={[styles.card, style]} onPress={onPress} testID="card-pressable">
      <FastImage
        style={styles.image}
        source={{
          uri: imageUrl,
          priority: FastImage.priority.normal,
        }}
        resizeMode={FastImage.resizeMode.cover}
      />
      <View style={styles.content}>
        <Text style={styles.title} numberOfLines={2}>{title}</Text>
        {tags && (
          <View style={styles.tagsContainer}>
            {tags.slice(0, 3).map(tag => (
              <View key={tag} style={styles.tag}>
                <Text style={styles.tagText}>{tag}</Text>
              </View>
            ))}
          </View>
        )}
        {progress !== undefined && (
          <ProgressBar progress={progress} showPercentage style={styles.progressBar} />
        )}
      </View>
    </TouchableOpacity>
  );
};

const styles = StyleSheet.create({
  containerCard: {
    backgroundColor: '#2c2c2c',
    borderRadius: 8,
    overflow: 'hidden',
    elevation: 3,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.3,
    shadowRadius: 3,
  },
  card: {
    backgroundColor: '#2c2c2c',
    borderRadius: 8,
    overflow: 'hidden',
    marginBottom: 16,
    elevation: 3,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 2 },
    shadowOpacity: 0.3,
    shadowRadius: 3,
  },
  image: {
    width: '100%',
    height: 180,
  },
  content: {
    padding: 12,
  },
  title: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: 'bold',
    marginBottom: 8,
  },
  tagsContainer: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    marginBottom: 8,
  },
  tag: {
    backgroundColor: '#444',
    borderRadius: 12,
    paddingVertical: 4,
    paddingHorizontal: 8,
    marginRight: 8,
    marginBottom: 8,
  },
  tagText: {
    color: '#FFFFFF',
    fontSize: 12,
  },
  progressBar: {
    marginTop: 8,
  },
});

export default Card;
