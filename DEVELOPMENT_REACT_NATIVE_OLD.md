# Project Myriad Development Guidelines

This document provides essential information for developers working on Project Myriad, a React Native application for manga and anime content. Following these guidelines ensures consistency, maintainability, and quality across the codebase.

## Build and Configuration Instructions

### Prerequisites

- Node.js (v18 or higher)
- React Native CLI
- Android Studio (for Android development)
- Xcode (for iOS development, macOS only)
- JDK 11

### Setting Up the Development Environment

1. Clone the repository
2. Install dependencies:
   ```bash
   npm install
   ```

### Running the Application

#### Android

```bash
# Start the Metro bundler
npm start

# In a separate terminal, run the Android app
npm run android
```

#### Building a Release APK

```bash
# Generate a release build for Android
npm run build:android
```

The APK will be generated at `android/app/build/outputs/apk/release/app-release.apk`.

### Configuration Files

- **React Native Config**: `react-native.config.js` contains project-specific configuration.
- **Metro Config**: `metro.config.js` configures the Metro bundler.
- **Babel Config**: `babel.config.js` contains JavaScript transpilation settings.
- **TypeScript Config**: `tsconfig.json` defines TypeScript compilation options.

## Testing Information

### Test Framework

Project Myriad uses Jest and React Native Testing Library for testing. The test configuration is defined in:
- `jest.config.js`: Main Jest configuration
- `jest.setup.js`: Test setup and mocks

### Running Tests

```bash
# Run all tests
npm test

# Run a specific test file
npm test -- path/to/test.ts

# Run tests with coverage report
npm test -- --coverage
```

### Writing Tests

Tests should be placed in the `__tests__` directory or named with `.test.ts` or `.spec.ts` suffix.

#### Component Test Example

```typescript
import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import MyComponent from '../src/components/MyComponent';

describe('MyComponent', () => {
  it('renders correctly', () => {
    const { getByText } = render(<MyComponent title="Test" />);
    expect(getByText('Test')).toBeTruthy();
  });

  it('handles press events', () => {
    const onPressMock = jest.fn();
    const { getByText } = render(<MyComponent title="Press Me" onPress={onPressMock} />);
    fireEvent.press(getByText('Press Me'));
    expect(onPressMock).toHaveBeenCalled();
  });
});
```

#### Utility Function Test Example

```typescript
import { formatFileSize, truncateText } from '../src/utils/helpers';

describe('Helper Functions', () => {
  describe('formatFileSize', () => {
    it('should format bytes correctly', () => {
      expect(formatFileSize(0)).toBe('0 Bytes');
      expect(formatFileSize(1024)).toBe('1 KB');
      expect(formatFileSize(1048576)).toBe('1 MB');
    });
  });

  describe('truncateText', () => {
    it('should truncate text when it exceeds maxLength', () => {
      const longText = 'This is a very long text that needs to be truncated';
      expect(truncateText(longText, 20)).toBe('This is a very lon...');
    });
  });
});
```

### Mocking Dependencies

For components that use external dependencies, create mocks in the test file:

```typescript
// Mock a service
jest.mock('../src/services/ApiService', () => ({
  fetchData: jest.fn().mockResolvedValue({ data: 'mocked data' }),
}));

// Mock a hook
jest.mock('../src/hooks/useAuth', () => ({
  __esModule: true,
  default: () => ({
    user: { id: '123', name: 'Test User' },
    isAuthenticated: true,
    login: jest.fn(),
    logout: jest.fn(),
  }),
}));
```

## TypeScript Guidelines

### Types and Interfaces

- Use TypeScript interfaces for defining object shapes
- Prefer interfaces over type aliases for object definitions
- Use type aliases for union types and complex types
- Export all types and interfaces that are used across multiple files
- Place shared types in the `src/types` directory

```typescript
// Good
interface UserProfile {
  id: string;
  username: string;
  preferences: UserPreferences;
}

// Good
type ContentFormat = 'manga' | 'anime';
```

### Type Safety

- Avoid using `any` type whenever possible
- Use generics for reusable components and functions
- Add explicit return types to functions
- Use union types instead of optional parameters when appropriate

```typescript
// Good
function fetchContent<T extends ContentItem>(id: string): Promise<T> {
  // Implementation
}

// Avoid
function processData(data: any): any {
  // Implementation
}
```

## React Native Guidelines

### Component Structure

- Use functional components with hooks
- Organize component files with the following structure:
  1. Imports
  2. Types/Interfaces
  3. Constants
  4. Component definition
  5. Styles
- Keep components focused on a single responsibility
- Extract reusable logic into custom hooks

```typescript
import React, { useState, useEffect } from 'react';
import { View, Text, StyleSheet } from 'react-native';

interface CardProps {
  title: string;
  content: string;
}

const Card: React.FC<CardProps> = ({ title, content }) => {
  // Component logic

  return (
    <View style={styles.container}>
      <Text style={styles.title}>{title}</Text>
      <Text style={styles.content}>{content}</Text>
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    // Styles
  },
  title: {
    // Styles
  },
  content: {
    // Styles
  },
});

export default Card;
```

### State Management

- Use React Context for global state when appropriate
- Implement Redux or MobX for complex state management
- Keep component state minimal and focused
- Use the appropriate hooks for different state needs:
  - `useState` for simple state
  - `useReducer` for complex state logic
  - `useContext` for accessing shared state

### Performance Optimization

- Memoize expensive calculations with `useMemo`
- Optimize callback functions with `useCallback`
- Use `React.memo` for components that render often but with the same props
- Implement virtualized lists for long scrollable content
- Use image optimization techniques for faster loading

## File Organization

### Directory Structure

- Organize files by feature or domain rather than by file type
- Keep related files close to each other
- Use consistent naming conventions

```
src/
  components/     # Shared components
  screens/        # Screen components
  navigation/     # Navigation configuration
  services/       # API and service integrations
  utils/          # Utility functions
  hooks/          # Custom hooks
  types/          # TypeScript types and interfaces
  assets/         # Images, fonts, etc.
  constants/      # App constants
```

### Naming Conventions

- Use PascalCase for component files and component names
- Use camelCase for utility functions, hooks, and non-component files
- Use kebab-case for asset files
- Add descriptive suffixes to files:
  - `.component.tsx` for components
  - `.hook.ts` for custom hooks
  - `.service.ts` for services
  - `.util.ts` for utilities

## Code Style

### Formatting

- Use consistent indentation (2 spaces)
- Limit line length to 100 characters
- Use semicolons at the end of statements
- Use single quotes for strings
- Add trailing commas in multi-line object and array literals

### Comments and Documentation

- Write self-documenting code with clear variable and function names
- Add JSDoc comments for public APIs and complex functions
- Include comments for non-obvious code sections
- Document component props with descriptive comments

```typescript
/**
 * Fetches content from the specified source and applies filters
 * @param source - The content source identifier
 * @param filters - Optional filters to apply to the results
 * @returns A promise that resolves to an array of content items
 */
async function fetchContentFromSource(
  source: string, 
  filters?: ContentFilters
): Promise<ContentItem[]> {
  // Implementation
}
```

## Testing Guidelines

- Write unit tests for utility functions and hooks
- Write component tests for UI behavior
- Use integration tests for critical user flows
- Mock external dependencies in tests
- Aim for high test coverage of core functionality

## Accessibility Guidelines

- Use semantic components (`Button` instead of `TouchableOpacity` with a text)
- Add appropriate accessibility labels
- Ensure sufficient color contrast
- Support screen readers
- Test with accessibility tools

## Performance Guidelines

- Minimize render cycles
- Optimize image loading and caching
- Reduce bundle size by code splitting
- Implement efficient list rendering
- Profile and optimize slow operations

By following these guidelines, we ensure that Project Myriad maintains a high standard of code quality, performance, and user experience.