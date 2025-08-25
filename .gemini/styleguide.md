# Project Myriad Development Style Guide

This document outlines the coding conventions and best practices for the Project Myriad application. It is based on the project's existing development guidelines.

## General Principles

- **Readability**: Code should be easy to understand for all team members.
- **Maintainability**: Code should be easy to modify and extend.
- **Consistency**: Adhering to a consistent style across all projects improves collaboration and reduces errors.
- **Performance**: While readability is paramount, code should be efficient.

## File Organization

- Organize files by feature or domain.
- Keep related files close to each other.
- Use consistent naming conventions:
  - Components: `PascalCase.tsx` (e.g., `UserProfile.tsx`)
  - Hooks: `useCamelCase.ts` (e.g., `useAuthentication.ts`)
  - Services: `camelCaseService.ts` (e.g., `dataService.ts`)
  - Utilities: `camelCaseUtils.ts` (e.g., `stringUtils.ts`)

## TypeScript Guidelines

- **Types and Interfaces**:
  - Use `interface` for defining object shapes.
  - Use `type` for union types, tuples, or other complex types.
  - Export types and interfaces used across multiple files.
  - Shared types should be in `src/types`.
- **Type Safety**:
  - Avoid `any` whenever possible. Use `unknown` for safer typing of unknown values.
  - Use generics for reusable components and functions.
  - Add explicit return types to all functions.

**Example of a well-typed function:**
```typescript
/**
 * Fetches content of a specific type.
 * @param id The ID of the content to fetch.
 * @returns A promise that resolves to the content item.
 */
function fetchContent<T extends ContentItem>(id: string): Promise<T> {
  // ... implementation
}
```

## React and React Native

- **Component Structure**:
  - Use functional components with hooks.
  - Keep components small and focused on a single responsibility.
  - Extract reusable logic into custom hooks.
- **State Management**:
  - Use `useState` for simple, local component state.
  - Use `useReducer` for more complex state logic within a component.
  - Use Redux Toolkit for global state management.
- **Performance**:
  - Use `React.memo` to memoize components and prevent unnecessary re-renders.
  - Use `useMemo` to memoize expensive calculations.
  - Use `useCallback` to memoize callback functions passed to child components.
  - Use `FlatList` or `SectionList` for long lists of data.
- **Styling**:
  - Use the `StyleSheet.create` API for styling.
  - Keep styles with the component, or in a separate file for very large components.

## Naming Conventions

- **Variables and Functions**: `camelCase`
- **Constants**: `UPPER_SNAKE_CASE`
- **Classes and Interfaces**: `PascalCase`
- **Components**: `PascalCase`

## Code Style and Formatting

- **Line Length**: Maximum 100 characters.
- **Indentation**: 2 spaces.
- **Quotes**: Single quotes (`'`) for strings.
- **Semicolons**: Use semicolons at the end of statements.
- **Trailing Commas**: Use trailing commas for multi-line arrays and objects.

## Documentation and Comments

- Write clear, self-documenting code.
- Use JSDoc comments for all public functions, hooks, and components.
- Explain the "why", not just the "what" in comments for complex or non-obvious code.

**JSDoc Example:**
```typescript
/**
 * Truncates a string to a specified length and appends an ellipsis.
 *
 * @param text The string to truncate.
 * @param maxLength The maximum length of the string.
 * @returns The truncated string.
 */
const truncateText = (text: string, maxLength: number): string => {
  // ... implementation
};
```

## Testing

- Write unit tests for all utility functions, hooks, and services.
- Write component tests to verify UI and interaction logic.
- Mock all external dependencies, such as APIs and native modules.
- Aim for high test coverage of critical application logic.

By following these guidelines, Gemini Code Assist can help maintain the quality and consistency of the Project Myriad codebase.
