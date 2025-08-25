import React from 'react';
import { render, fireEvent } from '@testing-library/react-native';
import HomeScreen from '../../src/screens/HomeScreen';

// Mock navigation
const mockNavigate = jest.fn();
jest.mock('@react-navigation/native', () => ({
  useNavigation: () => ({
    navigate: mockNavigate,
  }),
}));

// Use shared mock components
jest.mock('../../src/components/Button', () => {
  return require('../utils/mockComponents').MockButton;
});

jest.mock('../../src/components/Card', () => {
  return require('../utils/mockComponents').MockCard;
});

describe('HomeScreen', () => {
  beforeEach(() => {
    mockNavigate.mockClear();
  });

  it('renders correctly', () => {
    const { getByText } = render(<HomeScreen />);
    
    expect(getByText('Project Myriad')).toBeTruthy();
    expect(getByText('The Definitive Manga and Anime Platform')).toBeTruthy();
  });

  it('renders quick actions section', () => {
    const { getByText } = render(<HomeScreen />);
    
    expect(getByText('Quick Actions')).toBeTruthy();
    expect(getByText('Import Local Media')).toBeTruthy();
    expect(getByText('Browse Online')).toBeTruthy();
  });

  it('renders recently added section', () => {
    const { getByText } = render(<HomeScreen />);
    
    expect(getByText('Recently Added')).toBeTruthy();
    expect(getByText('Attack on Titan')).toBeTruthy();
    expect(getByText('One Piece')).toBeTruthy();
  });

  it('renders features section', () => {
    const { getByText } = render(<HomeScreen />);
    
    expect(getByText('Features')).toBeTruthy();
    expect(getByText('AI Core')).toBeTruthy();
    expect(getByText('Access AI-powered tools for your media')).toBeTruthy();
    expect(getByText('App Settings')).toBeTruthy();
    expect(getByText('Configure application preferences and options')).toBeTruthy();
  });

  it('navigates to Library when Import Local Media is pressed', () => {
    const { getByTestId } = render(<HomeScreen />);
    
    fireEvent.press(getByTestId('button-import-local-media'));
    expect(mockNavigate).toHaveBeenCalledWith('Library');
  });

  it('navigates to Browse when Browse Online is pressed', () => {
    const { getByTestId } = render(<HomeScreen />);
    
    fireEvent.press(getByTestId('button-browse-online'));
    expect(mockNavigate).toHaveBeenCalledWith('Browse');
  });

  it('navigates to AICore when AI Core feature is pressed', () => {
    const { getByText } = render(<HomeScreen />);
    
    fireEvent.press(getByText('AI Core'));
    expect(mockNavigate).toHaveBeenCalledWith('AICore');
  });

  it('navigates to Settings when App Settings feature is pressed', () => {
    const { getByText } = render(<HomeScreen />);
    
    fireEvent.press(getByText('App Settings'));
    expect(mockNavigate).toHaveBeenCalledWith('Settings');
  });

  it('renders recent items with correct data', () => {
    const { getByTestId } = render(<HomeScreen />);
    
    // Check Attack on Titan card
    expect(getByTestId('card-Attack on Titan')).toBeTruthy();
    expect(getByTestId('card-tag-Action')).toBeTruthy();
    expect(getByTestId('card-tag-Drama')).toBeTruthy();
    
    // Check One Piece card
    expect(getByTestId('card-One Piece')).toBeTruthy();
    expect(getByTestId('card-tag-Adventure')).toBeTruthy();
    expect(getByTestId('card-tag-Comedy')).toBeTruthy();
  });

  it('handles recent item press correctly', () => {
    // Mock console.log to verify it's called
    const consoleSpy = jest.spyOn(console, 'log').mockImplementation();
    
    const { getByTestId } = render(<HomeScreen />);
    
    fireEvent.press(getByTestId('card-Attack on Titan'));
    expect(consoleSpy).toHaveBeenCalledWith('Pressed Attack on Titan');
    
    consoleSpy.mockRestore();
  });
});
