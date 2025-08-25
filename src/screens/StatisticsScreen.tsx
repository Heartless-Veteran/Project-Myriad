import React, { useState, useEffect } from 'react';
import {
  View,
  Text,
  StyleSheet,
  TouchableOpacity,
  Alert,
  Modal,
  TextInput,
} from 'react-native';
import { ReadingStatistics, DailyReadingGoal } from '../types';
import { StatisticsService } from '../services/StatisticsService';
import ReadingStatisticsDisplay from '../components/ReadingStatisticsDisplay';

const statisticsService = StatisticsService.getInstance();

const StatisticsScreen: React.FC = () => {
  const [statistics, setStatistics] = useState<ReadingStatistics | null>(null);
  const [dailyGoal, setDailyGoal] = useState<DailyReadingGoal | null>(null);
  const [loading, setLoading] = useState(true);
  const [showGoalModal, setShowGoalModal] = useState(false);
  const [goalInput, setGoalInput] = useState('');

  useEffect(() => {
    loadStatistics();
  }, []);

  const loadStatistics = async () => {
    try {
      setLoading(true);
      const [stats, goal] = await Promise.all([
        statisticsService.getStatistics(),
        statisticsService.getTodayGoal(),
      ]);
      
      setStatistics(stats);
      setDailyGoal(goal);
    } catch (error) {
      console.error('Failed to load statistics:', error);
      Alert.alert('Error', 'Failed to load statistics');
    } finally {
      setLoading(false);
    }
  };

  const handleSetDailyGoal = async () => {
    const minutes = parseInt(goalInput);
    if (isNaN(minutes) || minutes <= 0) {
      Alert.alert('Error', 'Please enter a valid number of minutes');
      return;
    }

    try {
      await statisticsService.setDailyReadingGoal(minutes);
      await loadStatistics();
      setShowGoalModal(false);
      setGoalInput('');
      Alert.alert('Success', 'Daily reading goal set successfully');
    } catch (error) {
      console.error('Failed to set daily goal:', error);
      Alert.alert('Error', 'Failed to set daily goal');
    }
  };

  const renderGoalModal = () => (
    <Modal
      visible={showGoalModal}
      transparent
      animationType="fade"
      onRequestClose={() => setShowGoalModal(false)}
    >
      <View style={styles.modalOverlay}>
        <View style={styles.modalContent}>
          <Text style={styles.modalTitle}>Set Daily Reading Goal</Text>
          <Text style={styles.modalDescription}>
            How many minutes would you like to read today?
          </Text>
          
          <TextInput
            style={styles.goalInput}
            value={goalInput}
            onChangeText={setGoalInput}
            placeholder="Enter minutes (e.g., 30)"
            placeholderTextColor="#888"
            keyboardType="numeric"
          />

          <View style={styles.modalButtons}>
            <TouchableOpacity
              style={styles.modalButton}
              onPress={() => setShowGoalModal(false)}
            >
              <Text style={styles.modalButtonText}>Cancel</Text>
            </TouchableOpacity>
            <TouchableOpacity
              style={[styles.modalButton, styles.modalButtonPrimary]}
              onPress={handleSetDailyGoal}
            >
              <Text style={styles.modalButtonPrimaryText}>Set Goal</Text>
            </TouchableOpacity>
          </View>
        </View>
      </View>
    </Modal>
  );

  if (loading) {
    return (
      <View style={styles.loadingContainer}>
        <Text style={styles.loadingText}>Loading statistics...</Text>
      </View>
    );
  }

  if (!statistics) {
    return (
      <View style={styles.errorContainer}>
        <Text style={styles.errorText}>Failed to load statistics</Text>
        <TouchableOpacity style={styles.retryButton} onPress={loadStatistics}>
          <Text style={styles.retryButtonText}>Retry</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <View style={styles.container}>
      <View style={styles.header}>
        <Text style={styles.title}>Reading Statistics</Text>
        <TouchableOpacity
          style={styles.goalButton}
          onPress={() => setShowGoalModal(true)}
        >
          <Text style={styles.goalButtonText}>🎯</Text>
        </TouchableOpacity>
      </View>

      <ReadingStatisticsDisplay statistics={statistics} dailyGoal={dailyGoal} />
      {renderGoalModal()}
    </View>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1A1A1A',
  },
  header: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'center',
    padding: 16,
    borderBottomWidth: 1,
    borderBottomColor: '#333',
  },
  title: {
    color: '#FFFFFF',
    fontSize: 24,
    fontWeight: 'bold',
  },
  goalButton: {
    width: 40,
    height: 40,
    backgroundColor: '#007BFF',
    borderRadius: 20,
    justifyContent: 'center',
    alignItems: 'center',
  },
  goalButtonText: {
    fontSize: 20,
  },
  loadingContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#1A1A1A',
  },
  loadingText: {
    color: '#CCCCCC',
    fontSize: 16,
  },
  errorContainer: {
    flex: 1,
    justifyContent: 'center',
    alignItems: 'center',
    backgroundColor: '#1A1A1A',
    padding: 40,
  },
  errorText: {
    color: '#FF6B6B',
    fontSize: 18,
    marginBottom: 20,
    textAlign: 'center',
  },
  retryButton: {
    backgroundColor: '#007BFF',
    paddingVertical: 12,
    paddingHorizontal: 24,
    borderRadius: 6,
  },
  retryButtonText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '600',
  },
  modalOverlay: {
    flex: 1,
    backgroundColor: 'rgba(0, 0, 0, 0.8)',
    justifyContent: 'center',
    alignItems: 'center',
  },
  modalContent: {
    backgroundColor: '#2A2A2A',
    borderRadius: 12,
    padding: 20,
    width: '90%',
    maxWidth: 350,
  },
  modalTitle: {
    color: '#FFFFFF',
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 12,
    textAlign: 'center',
  },
  modalDescription: {
    color: '#CCCCCC',
    fontSize: 14,
    marginBottom: 20,
    textAlign: 'center',
    lineHeight: 20,
  },
  goalInput: {
    backgroundColor: '#333',
    color: '#FFFFFF',
    borderRadius: 8,
    padding: 12,
    fontSize: 16,
    marginBottom: 20,
    textAlign: 'center',
  },
  modalButtons: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  modalButton: {
    flex: 1,
    paddingVertical: 12,
    paddingHorizontal: 20,
    backgroundColor: '#444',
    borderRadius: 6,
    marginHorizontal: 8,
    alignItems: 'center',
  },
  modalButtonPrimary: {
    backgroundColor: '#007BFF',
  },
  modalButtonText: {
    color: '#CCCCCC',
    fontSize: 16,
    fontWeight: '600',
  },
  modalButtonPrimaryText: {
    color: '#FFFFFF',
    fontSize: 16,
    fontWeight: '600',
  },
});

export default StatisticsScreen;