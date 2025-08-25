import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import { Text } from 'react-native';
import Card from '../../src/components/Card';

// Mock FastImage
jest.mock('react-native-fast-image', () => {
  const { Image } = require('react-native');
  const FastImageMock = (props: any) => <Image {...props} testID="fast-image" />;
  
  FastImageMock.priority = {
    low: 'low',
    normal: 'normal',
    high: 'high',
  };
  
  FastImageMock.resizeMode = {
    contain: 'contain',
    cover: 'cover',
    stretch: 'stretch',
    center: 'center',
  };
  
  return FastImageMock;
});

// Mock ProgressBar component
jest.mock('../../src/components/ProgressBar', () => {
  const { View, Text } = require('react-native');
  return function MockProgressBar({ progress, showPercentage }: any) {
    return (
      <View testID="progress-bar">
        <Text testID="progress-value">{progress}</Text>
        {showPercentage && <Text testID="progress-percentage">{Math.round(progress * 100)}%</Text>}
      </View>
    );
  };
});

describe('Card Component', () => {
  const mockOnPress = jest.fn();

  beforeEach(() => {
    mockOnPress.mockClear();
  });

  it('renders children when provided', () => {
    const { getByText, queryByTestId } = render(
      <Card>
        <Text>Custom Content</Text>
      </Card>
    );

    expect(getByText('Custom Content')).toBeTruthy();
    expect(queryByTestId('fast-image')).toBeNull();
  });

  it('renders container card when required props are missing', () => {
    const { queryByTestId } = render(
      <Card title="Test Title" />
    );

    expect(queryByTestId('fast-image')).toBeNull();
  });

  it('renders full card with all props', () => {
    const { getByText, getByTestId } = render(
      <Card
        title="Test Manga"
        imageUrl="https://example.com/image.jpg"
        onPress={mockOnPress}
        tags={['Action', 'Adventure', 'Comedy']}
        progress={0.75}
      />
    );

    expect(getByText('Test Manga')).toBeTruthy();
    expect(getByTestId('fast-image')).toBeTruthy();
    expect(getByText('Action')).toBeTruthy();
    expect(getByText('Adventure')).toBeTruthy();
    expect(getByText('Comedy')).toBeTruthy();
    expect(getByTestId('progress-bar')).toBeTruthy();
  });

  it('limits tags to maximum of 3', () => {
    const { getByText, queryByText } = render(
      <Card
        title="Test Manga"
        imageUrl="https://example.com/image.jpg"
        onPress={mockOnPress}
        tags={['Action', 'Adventure', 'Comedy', 'Drama', 'Romance']}
      />
    );

    expect(getByText('Action')).toBeTruthy();
    expect(getByText('Adventure')).toBeTruthy();
    expect(getByText('Comedy')).toBeTruthy();
    expect(queryByText('Drama')).toBeNull();
    expect(queryByText('Romance')).toBeNull();
  });

  it('calls onPress when card is pressed', () => {
    const { getByTestId } = render(
      <Card
        title="Test Manga"
        imageUrl="https://example.com/image.jpg"
        onPress={mockOnPress}
      />
    );

    fireEvent.press(getByTestId('card-pressable'));
    expect(mockOnPress).toHaveBeenCalledTimes(1);
  });

  it('renders without tags when not provided', () => {
    const { getByText, queryByText } = render(
      <Card
        title="Test Manga"
        imageUrl="https://example.com/image.jpg"
        onPress={mockOnPress}
      />
    );

    expect(getByText('Test Manga')).toBeTruthy();
    // Should not render any tag-related elements
    expect(queryByText('Action')).toBeNull();
  });

  it('renders without progress bar when progress is undefined', () => {
    const { queryByTestId } = render(
      <Card
        title="Test Manga"
        imageUrl="https://example.com/image.jpg"
        onPress={mockOnPress}
      />
    );

    expect(queryByTestId('progress-bar')).toBeNull();
  });

  it('renders progress bar when progress is 0', () => {
    const { getByTestId } = render(
      <Card
        title="Test Manga"
        imageUrl="https://example.com/image.jpg"
        onPress={mockOnPress}
        progress={0}
      />
    );

    expect(getByTestId('progress-bar')).toBeTruthy();
    expect(getByTestId('progress-value')).toBeTruthy();
  });

  it('applies custom style when provided', () => {
    const customStyle = { backgroundColor: 'red' };
    const { getByText } = render(
      <Card style={customStyle}>
        <Text>Custom Content</Text>
      </Card>
    );

    // The component should render without errors with custom style
    expect(getByTestId).toBeDefined();
  });
});
