import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import SearchBar from '../../src/components/SearchBar';

describe('SearchBar Component', () => {
  // Capture console warnings to verify animation warnings are suppressed
  let consoleSpy: jest.SpyInstance;
  let warnings: string[] = [];

  beforeAll(() => {
    jest.useFakeTimers();
  });

  afterAll(() => {
    jest.useRealTimers();
  });

  beforeEach(() => {
    warnings = [];
    consoleSpy = jest.spyOn(console, 'warn').mockImplementation((message: string) => {
      warnings.push(message);
    });
  });

  afterEach(() => {
    consoleSpy.mockRestore();
    // Clear any pending timers to avoid Jest environment issues
    jest.clearAllTimers();
  });

  const defaultProps = {
    placeholder: 'Search...',
    onSearch: jest.fn(),
    onClear: jest.fn(),
  };

  it('renders correctly with default props', () => {
    const { getByPlaceholderText } = render(<SearchBar {...defaultProps} />);
    expect(getByPlaceholderText('Search...')).toBeTruthy();
  });

  it('calls onSearch when text changes', () => {
    const onSearchMock = jest.fn();
    const { getByPlaceholderText } = render(
      <SearchBar {...defaultProps} onSearch={onSearchMock} />
    );

    const input = getByPlaceholderText('Search...');
    fireEvent.changeText(input, 'test query');

    expect(onSearchMock).toHaveBeenCalledWith('test query');
  });

  it('shows clear button when text is entered', () => {
    const { getByPlaceholderText, getByText } = render(<SearchBar {...defaultProps} />);

    const input = getByPlaceholderText('Search...');
    fireEvent.changeText(input, 'test');

    expect(getByText('✕')).toBeTruthy();
  });

  it('calls onClear when clear button is pressed', () => {
    const onClearMock = jest.fn();
    const onSearchMock = jest.fn();
    const { getByPlaceholderText, getByText } = render(
      <SearchBar {...defaultProps} onClear={onClearMock} onSearch={onSearchMock} />
    );

    const input = getByPlaceholderText('Search...');
    fireEvent.changeText(input, 'test');
    
    const clearButton = getByText('✕');
    fireEvent.press(clearButton);

    expect(onClearMock).toHaveBeenCalled();
    expect(onSearchMock).toHaveBeenCalledWith('');
  });

  it('shows filter button when showFilterButton is true', () => {
    const onFilterPressMock = jest.fn();
    const { getByText } = render(
      <SearchBar 
        {...defaultProps} 
        showFilterButton={true} 
        onFilterPress={onFilterPressMock} 
      />
    );

    expect(getByText('⚙')).toBeTruthy();
  });

  it('calls onFilterPress when filter button is pressed', () => {
    const onFilterPressMock = jest.fn();
    const { getByText } = render(
      <SearchBar 
        {...defaultProps} 
        showFilterButton={true} 
        onFilterPress={onFilterPressMock} 
      />
    );

    const filterButton = getByText('⚙');
    fireEvent.press(filterButton);

    expect(onFilterPressMock).toHaveBeenCalled();
  });

  it('triggers focus animation without useNativeDriver warnings', () => {
    const { getByPlaceholderText } = render(<SearchBar {...defaultProps} />);

    const input = getByPlaceholderText('Search...');
    
    // Trigger focus which should start the animation
    fireEvent(input, 'focus');
    
    // Check that no useNativeDriver warnings were produced
    const nativeDriverWarnings = warnings.filter(warning => 
      warning.includes('useNativeDriver') || warning.includes('native animated module')
    );
    
    expect(nativeDriverWarnings).toHaveLength(0);
  });

  it('triggers blur animation without useNativeDriver warnings', () => {
    const { getByPlaceholderText } = render(<SearchBar {...defaultProps} />);

    const input = getByPlaceholderText('Search...');
    
    // Trigger focus then blur to test both animation states
    fireEvent(input, 'focus');
    fireEvent(input, 'blur');
    
    // Check that no useNativeDriver warnings were produced
    const nativeDriverWarnings = warnings.filter(warning => 
      warning.includes('useNativeDriver') || warning.includes('native animated module')
    );
    
    expect(nativeDriverWarnings).toHaveLength(0);
  });
});