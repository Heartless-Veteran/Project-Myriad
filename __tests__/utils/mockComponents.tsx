// Shared mock components used across multiple test files

// Mock Button component
export const mockButton = () => {
  const { TouchableOpacity, Text } = require('react-native');
  return function MockButton({ title, onPress, disabled, style }: any) {
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
  return function MockCard({ title, imageUrl, tags, progress, onPress, children, style }: any) {
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
  ContentList: function MockContentList({ data, onItemPress, onItemLongPress, renderItem, refreshControl }: any) {
    const { View, Text, TouchableOpacity, ScrollView } = require('react-native');
    return (
      <ScrollView testID="content-list" refreshControl={refreshControl}>
        {data.map((item: any, index: number) => {
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
  return function MockSearchBar({ value, onChangeText, onFilterPress, placeholder }: any) {
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
  return function MockFilterPanel({ filters, onFiltersChange, availableGenres }: any) {
    return (
      <View testID="filter-panel">
        <Text>Filter Panel</Text>
      </View>
    );
  };
};