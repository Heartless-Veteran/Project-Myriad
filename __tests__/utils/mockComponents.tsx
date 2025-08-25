import React from 'react';
import { ViewStyle, TextStyle } from 'react-native';
import { Manga, Anime } from '../../src/types';

// Type definitions for mock components based on actual component interfaces

// Union type for content items
type ContentItem = Manga | Anime;

// Filter state type
interface FilterState {
  genre: string[];
  status: string[];
  rating: number;
}

interface MockButtonProps {
  title: string;
  onPress: () => void;
  disabled?: boolean;
  style?: ViewStyle;
  textStyle?: TextStyle;
}

interface MockCardProps {
  title?: string;
  imageUrl?: string;
  tags?: string[];
  progress?: number;
  onPress?: () => void;
  children?: React.ReactNode;
  style?: ViewStyle;
}

interface MockContentListProps {
  data: ContentItem[];
  onItemPress: (item: ContentItem) => void;
  onItemLongPress?: (item: ContentItem) => void;
  renderItem?: (item: { item: ContentItem }) => React.ReactElement;
  refreshControl?: React.ReactElement;
}

interface MockSearchBarProps {
  value?: string;
  onChangeText?: (text: string) => void;
  onFilterPress?: () => void;
  placeholder?: string;
}

interface MockFilterPanelProps {
  filters: FilterState;
  onFiltersChange: (filters: Partial<FilterState>) => void;
  availableGenres: string[];
}

// Mock Button component
export const mockButton = () => {
  const { TouchableOpacity, Text } = require('react-native');
  return function MockButton({ title, onPress, disabled, style }: MockButtonProps) {
    return (
      <TouchableOpacity
        testID={`button-${title.toLowerCase().replace(/\s+/g, '-')}`}
        onPress={onPress}
        disabled={disabled}
        style={style}
      >
        <Text>{title}</Text>
      </TouchableOpacity>
    );
  };
};

// Mock Card component
export const mockCard = () => {
  const { TouchableOpacity, Text, View } = require('react-native');
  return function MockCard({ title, imageUrl, tags, progress, onPress, children, style }: MockCardProps) {
    return (
      <TouchableOpacity testID={`card-${title || 'card'}`} style={style} onPress={onPress}>
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
};

// Mock ContentList component
export const mockContentList = () => ({
  ContentList: function MockContentList({ data, onItemPress, onItemLongPress, renderItem, refreshControl }: MockContentListProps) {
    const { View, Text, TouchableOpacity, ScrollView } = require('react-native');
    return (
      <ScrollView testID="content-list" refreshControl={refreshControl}>
        {data.map((item: ContentItem, index: number) => {
          if (renderItem) {
            return renderItem({ item });
          }
          return (
            <TouchableOpacity
              key={item.id || index}
              testID={`content-item-${item.id || index}`}
              onPress={() => onItemPress(item)}
              onLongPress={() => onItemLongPress && onItemLongPress(item)}
            >
              <Text>{item.title}</Text>
            </TouchableOpacity>
          );
        })}
      </ScrollView>
    );
  },
});

// Mock SearchBar component
export const mockSearchBar = () => {
  const { View, TextInput, TouchableOpacity, Text } = require('react-native');
  return function MockSearchBar({ value, onChangeText, onFilterPress, placeholder }: MockSearchBarProps) {
    return (
      <View testID="search-bar">
        <TextInput
          testID="search-input"
          value={value}
          onChangeText={onChangeText}
          placeholder={placeholder}
        />
        <TouchableOpacity testID="filter-button" onPress={onFilterPress}>
          <Text>Filter</Text>
        </TouchableOpacity>
      </View>
    );
  };
};

// Mock FilterPanel component
export const mockFilterPanel = () => {
  const { View, Text } = require('react-native');
  return function MockFilterPanel({ filters, onFiltersChange, availableGenres }: MockFilterPanelProps) {
    return (
      <View testID="filter-panel">
        <Text>Filter Panel</Text>
      </View>
    );
  };
};