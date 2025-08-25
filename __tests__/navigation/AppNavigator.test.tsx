import React from 'react';
import { render } from '@testing-library/react-native';
import { NavigationContainer } from '@react-navigation/native';
import AppNavigator from '../../src/navigation/AppNavigator';

// Mock navigation dependencies
jest.mock('@react-navigation/native', () => {
  const actualNav = jest.requireActual('@react-navigation/native');
  return {
    ...actualNav,
    NavigationContainer: ({ children }: { children: React.ReactNode }) => children,
  };
});

jest.mock('@react-navigation/stack', () => ({
  createStackNavigator: () => ({
    Navigator: ({ children }: { children: React.ReactNode }) => children,
    Screen: ({ name, component }: { name: string; component: React.ComponentType<any> }) => {
      const Component = component;
      return <Component testID={`screen-${name}`} />;
    },
  }),
}));

jest.mock('@react-navigation/bottom-tabs', () => ({
  createBottomTabNavigator: () => ({
    Navigator: ({ children }: any) => children,
    Screen: ({ name, component }: any) => {
      const Component = component;
      return <Component testID={`tab-${name}`} />;
    },
  }),
}));

// Mock screen components
jest.mock('../../src/screens/HomeScreen', () => {
  const { View, Text } = require('react-native');
  return function MockHomeScreen(props: any) {
    return (
      <View {...props}>
        <Text>Home Screen</Text>
      </View>
    );
  };
});

jest.mock('../../src/screens/LibraryScreen', () => {
  const { View, Text } = require('react-native');
  return function MockLibraryScreen(props: any) {
    return (
      <View {...props}>
        <Text>Library Screen</Text>
      </View>
    );
  };
});

jest.mock('../../src/screens/SettingsScreen', () => {
  const { View, Text } = require('react-native');
  return function MockSettingsScreen(props: any) {
    return (
      <View {...props}>
        <Text>Settings Screen</Text>
      </View>
    );
  };
});

jest.mock('../../src/screens/BrowseScreen', () => {
  const { View, Text } = require('react-native');
  return function MockBrowseScreen(props: any) {
    return (
      <View {...props}>
        <Text>Browse Screen</Text>
      </View>
    );
  };
});

jest.mock('../../src/screens/AICoreScreen', () => {
  const { View, Text } = require('react-native');
  return function MockAICoreScreen(props: any) {
    return (
      <View {...props}>
        <Text>AI Core Screen</Text>
      </View>
    );
  };
});

describe('AppNavigator', () => {
  it('renders without crashing', () => {
    const { container } = render(<AppNavigator />);
    expect(container).toBeTruthy();
  });

  it('renders tab screens correctly', () => {
    const { getByText } = render(<AppNavigator />);
    
    expect(getByText('Home Screen')).toBeTruthy();
    expect(getByText('Library Screen')).toBeTruthy();
    expect(getByText('Settings Screen')).toBeTruthy();
  });

  it('renders stack screens correctly', () => {
    const { getByText } = render(<AppNavigator />);
    
    expect(getByText('Browse Screen')).toBeTruthy();
    expect(getByText('AI Core Screen')).toBeTruthy();
  });

  it('renders navigation container', () => {
    const { container } = render(<AppNavigator />);
    expect(container).toBeTruthy();
  });
});
