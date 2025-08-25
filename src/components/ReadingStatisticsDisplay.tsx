import React from 'react';
import {
  View,
  Text,
  StyleSheet,
  ScrollView,
  Dimensions,
} from 'react-native';
import { ReadingStatistics, DailyReadingGoal } from '../types';

const { width } = Dimensions.get('window');

interface StatisticsCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  icon?: string;
}

const StatisticsCard: React.FC<StatisticsCardProps> = ({ title, value, subtitle, icon }) => (
  <View style={styles.statCard}>
    <View style={styles.statHeader}>
      {icon && <Text style={styles.statIcon}>{icon}</Text>}
      <Text style={styles.statTitle}>{title}</Text>
    </View>
    <Text style={styles.statValue}>{value}</Text>
    {subtitle && <Text style={styles.statSubtitle}>{subtitle}</Text>}
  </View>
);

interface ReadingStatisticsDisplayProps {
  statistics: ReadingStatistics;
  dailyGoal?: DailyReadingGoal;
}

const ReadingStatisticsDisplay: React.FC<ReadingStatisticsDisplayProps> = ({ 
  statistics, 
  dailyGoal 
}) => {
  const formatTime = (minutes: number): string => {
    if (minutes < 60) return `${minutes}m`;
    const hours = Math.floor(minutes / 60);
    const mins = minutes % 60;
    return mins > 0 ? `${hours}h ${mins}m` : `${hours}h`;
  };

  const goalProgress = dailyGoal 
    ? Math.min((dailyGoal.currentMinutes / dailyGoal.targetMinutes) * 100, 100)
    : 0;

  return (
    <ScrollView style={styles.container} showsVerticalScrollIndicator={false}>
      {/* Daily Goal */}
      {dailyGoal && (
        <View style={styles.goalSection}>
          <Text style={styles.sectionTitle}>Today's Reading Goal</Text>
          <View style={styles.goalCard}>
            <View style={styles.goalProgress}>
              <View 
                style={[styles.goalProgressBar, { width: `${goalProgress}%` }]} 
              />
            </View>
            <View style={styles.goalInfo}>
              <Text style={styles.goalText}>
                {formatTime(dailyGoal.currentMinutes)} / {formatTime(dailyGoal.targetMinutes)}
              </Text>
              <Text style={styles.goalStatus}>
                {dailyGoal.achieved ? '🎉 Goal Achieved!' : `${Math.round(goalProgress)}% Complete`}
              </Text>
            </View>
          </View>
        </View>
      )}

      {/* Overview Statistics */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Reading Overview</Text>
        <View style={styles.statsGrid}>
          <StatisticsCard
            title="Total Sessions"
            value={statistics.totalReadingSessions}
            icon="📊"
          />
          <StatisticsCard
            title="Reading Time"
            value={formatTime(statistics.totalReadingTime)}
            icon="📚"
          />
          <StatisticsCard
            title="Watching Time"
            value={formatTime(statistics.totalWatchingTime)}
            icon="🎬"
          />
          <StatisticsCard
            title="Average Session"
            value={formatTime(statistics.averageSessionTime)}
            icon="⏱️"
          />
        </View>
      </View>

      {/* Progress Statistics */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Progress</Text>
        <View style={styles.statsGrid}>
          <StatisticsCard
            title="Books Completed"
            value={statistics.booksCompleted}
            icon="✅"
          />
          <StatisticsCard
            title="Chapters Read"
            value={statistics.chaptersRead}
            icon="📖"
          />
          <StatisticsCard
            title="Episodes Watched"
            value={statistics.episodesWatched}
            icon="🎭"
          />
          <StatisticsCard
            title="Current Streak"
            value={`${statistics.streakDays} days`}
            subtitle={`Best: ${statistics.longestStreak} days`}
            icon="🔥"
          />
        </View>
      </View>

      {/* Recent Activity */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Recent Activity</Text>
        <View style={styles.activityGrid}>
          <StatisticsCard
            title="This Week"
            value={statistics.completedThisWeek}
            subtitle="completed"
            icon="📅"
          />
          <StatisticsCard
            title="This Month"
            value={statistics.completedThisMonth}
            subtitle="completed"
            icon="🗓️"
          />
          <StatisticsCard
            title="This Year"
            value={statistics.completedThisYear}
            subtitle="completed"
            icon="🎊"
          />
        </View>
      </View>

      {/* Reading Pattern */}
      <View style={styles.section}>
        <Text style={styles.sectionTitle}>Reading Pattern (Last 7 Days)</Text>
        <View style={styles.chartContainer}>
          {statistics.readingTimeByDay.slice(-7).map((day, index) => (
            <View key={index} style={styles.chartBar}>
              <View 
                style={[
                  styles.bar, 
                  { height: Math.max(4, (day.minutes / 120) * 60) } // Max 60px height for 2 hours
                ]} 
              />
              <Text style={styles.chartLabel}>
                {new Date(day.date).toLocaleDateString('en', { weekday: 'short' })}
              </Text>
              <Text style={styles.chartValue}>
                {day.minutes}m
              </Text>
            </View>
          ))}
        </View>
      </View>
    </ScrollView>
  );
};

const styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: '#1A1A1A',
    padding: 16,
  },
  sectionTitle: {
    color: '#FFFFFF',
    fontSize: 20,
    fontWeight: 'bold',
    marginBottom: 16,
  },
  section: {
    marginBottom: 24,
  },
  goalSection: {
    marginBottom: 24,
  },
  goalCard: {
    backgroundColor: '#2A2A2A',
    borderRadius: 12,
    padding: 16,
  },
  goalProgress: {
    height: 8,
    backgroundColor: '#444',
    borderRadius: 4,
    overflow: 'hidden',
    marginBottom: 12,
  },
  goalProgressBar: {
    height: '100%',
    backgroundColor: '#007BFF',
    borderRadius: 4,
  },
  goalInfo: {
    alignItems: 'center',
  },
  goalText: {
    color: '#FFFFFF',
    fontSize: 18,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  goalStatus: {
    color: '#CCCCCC',
    fontSize: 14,
  },
  statsGrid: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    justifyContent: 'space-between',
  },
  activityGrid: {
    flexDirection: 'row',
    justifyContent: 'space-between',
  },
  statCard: {
    backgroundColor: '#2A2A2A',
    borderRadius: 8,
    padding: 12,
    marginBottom: 12,
    width: (width - 48) / 2,
  },
  statHeader: {
    flexDirection: 'row',
    alignItems: 'center',
    marginBottom: 8,
  },
  statIcon: {
    fontSize: 20,
    marginRight: 8,
  },
  statTitle: {
    color: '#CCCCCC',
    fontSize: 12,
    fontWeight: '600',
    textTransform: 'uppercase',
    flex: 1,
  },
  statValue: {
    color: '#FFFFFF',
    fontSize: 24,
    fontWeight: 'bold',
    marginBottom: 4,
  },
  statSubtitle: {
    color: '#888888',
    fontSize: 11,
  },
  chartContainer: {
    flexDirection: 'row',
    justifyContent: 'space-between',
    alignItems: 'flex-end',
    height: 100,
    backgroundColor: '#2A2A2A',
    borderRadius: 8,
    padding: 16,
  },
  chartBar: {
    alignItems: 'center',
    flex: 1,
  },
  bar: {
    backgroundColor: '#007BFF',
    width: 16,
    borderRadius: 2,
    marginBottom: 8,
  },
  chartLabel: {
    color: '#CCCCCC',
    fontSize: 10,
    marginBottom: 2,
  },
  chartValue: {
    color: '#888888',
    fontSize: 9,
  },
});

export default ReadingStatisticsDisplay;