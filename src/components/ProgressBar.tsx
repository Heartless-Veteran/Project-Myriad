import React from 'react';
import { View, Text, StyleSheet, ViewStyle, TextStyle } from 'react-native';

interface ProgressBarProps {
  progress: number; // 0 to 1
  height?: number;
  width?: number | string;
  backgroundColor?: string;
  fillColor?: string;
  borderRadius?: number;
  showPercentage?: boolean;
  percentageStyle?: TextStyle;
  style?: ViewStyle;
  label?: string;
  labelStyle?: TextStyle;
  animated?: boolean;
  testID?: string;
}

const ProgressBar: React.FC<ProgressBarProps> = ({
  progress,
  height = 8,
  width = '100%',
  backgroundColor = '#333',
  fillColor = '#007BFF',
  borderRadius = 4,
  showPercentage = false,
  percentageStyle,
  style,
  label,
  labelStyle,
  animated = true,
  testID,
}) => {
  // Ensure progress is between 0 and 1
  const normalizedProgress = Math.min(Math.max(progress, 0), 1);
  
  // Calculate percentage for display
  const percentage = Math.round(normalizedProgress * 100);

  return (
    <View style={[styles.container, style]} testID={testID}>
      {label && (
        <Text style={[styles.label, labelStyle]}>{label}</Text>
      )}
      
      <View
        style={[
          styles.progressContainer,
          {
            height,
            width,
            backgroundColor,
            borderRadius,
          },
        ]}
      >
        <View
          style={[
            styles.progressFill,
            {
              width: `${percentage}%`,
              height: '100%',
              backgroundColor: fillColor,
              borderRadius,
              // Add transition animation if enabled
              ...(animated ? styles.animated : {}),
            },
          ]}
        />
      </View>
      
      {showPercentage && (
        <Text style={[styles.percentage, percentageStyle]}>
          {percentage}%
        </Text>
      )}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flexDirection: 'column',
  },
  progressContainer: {
    overflow: 'hidden',
  },
  progressFill: {
    position: 'absolute',
    left: 0,
    top: 0,
  },
  animated: {
    transition: 'width 0.3s ease',
  },
  percentage: {
    color: '#FFFFFF',
    fontSize: 12,
    marginTop: 4,
    textAlign: 'right',
  },
  label: {
    color: '#CCCCCC',
    fontSize: 12,
    marginBottom: 4,
  },
});

export default ProgressBar;