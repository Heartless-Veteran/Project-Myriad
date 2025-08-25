#!/bin/bash

echo "Running unit tests for files changed in PR #100..."

# Run specific test files
npm test -- __tests__/components/Card.test.tsx
npm test -- __tests__/components/ContentList.test.tsx
npm test -- __tests__/navigation/AppNavigator.test.tsx
npm test -- __tests__/screens/AICoreScreen.test.tsx
npm test -- __tests__/screens/HomeScreen.test.tsx
npm test -- __tests__/screens/LibraryScreen.test.tsx
npm test -- __tests__/store/slices/librarySlice.test.ts