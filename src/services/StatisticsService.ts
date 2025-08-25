import AsyncStorage from '@react-native-async-storage/async-storage';
import { ReadingSession, ReadingStatistics, DailyReadingGoal, Manga, Anime } from '../types';
import { LoggingService } from './LoggingService';

const loggingService = LoggingService.getInstance();

export class StatisticsService {
  private static instance: StatisticsService;
  private readonly TAG = 'StatisticsService';
  private readonly SESSIONS_KEY = '@project_myriad_reading_sessions';
  private readonly STATISTICS_KEY = '@project_myriad_statistics';
  private readonly GOALS_KEY = '@project_myriad_goals';

  private constructor() {}

  public static getInstance(): StatisticsService {
    if (!StatisticsService.instance) {
      StatisticsService.instance = new StatisticsService();
    }
    return StatisticsService.instance;
  }

  /**
   * Start a new reading session
   */
  async startReadingSession(
    contentId: string,
    contentType: 'manga' | 'anime',
    chapterId?: string,
    episodeId?: string
  ): Promise<string> {
    try {
      const session: ReadingSession = {
        id: `session_${Date.now()}_${Math.random().toString(36).substring(2, 11)}`,
        contentId,
        contentType,
        chapterId,
        episodeId,
        startTime: new Date().toISOString(),
        endTime: '',
        duration: 0,
        completed: false,
      };

      const sessions = await this.getReadingSessions();
      sessions.push(session);
      await this.saveReadingSessions(sessions);

      loggingService.info(this.TAG, `Started reading session for ${contentType}: ${contentId}`);
      return session.id;
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to start reading session', error);
      throw error;
    }
  }

  /**
   * End a reading session
   */
  async endReadingSession(
    sessionId: string,
    pagesRead?: number,
    completed: boolean = false
  ): Promise<void> {
    try {
      const sessions = await this.getReadingSessions();
      const sessionIndex = sessions.findIndex(s => s.id === sessionId);

      if (sessionIndex === -1) {
        loggingService.warn(this.TAG, `Session ${sessionId} not found`);
        return;
      }

      const session = sessions[sessionIndex];
      const endTime = new Date();
      const startTime = new Date(session.startTime);
      const duration = Math.round((endTime.getTime() - startTime.getTime()) / (1000 * 60)); // minutes

      sessions[sessionIndex] = {
        ...session,
        endTime: endTime.toISOString(),
        duration,
        pagesRead,
        completed,
      };

      await this.saveReadingSessions(sessions);
      await this.updateStatistics(sessions[sessionIndex]);

      loggingService.info(this.TAG, `Ended reading session ${sessionId}, duration: ${duration} minutes`);
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to end reading session', error);
      throw error;
    }
  }

  /**
   * Get reading statistics
   */
  async getStatistics(): Promise<ReadingStatistics> {
    try {
      const statisticsData = await AsyncStorage.getItem(this.STATISTICS_KEY);
      if (statisticsData) {
        return JSON.parse(statisticsData);
      }

      // Generate initial statistics
      const sessions = await this.getReadingSessions();
      return this.generateStatistics(sessions);
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to get statistics', error);
      return this.createEmptyStatistics();
    }
  }

  /**
   * Update statistics after a reading session
   */
  private async updateStatistics(session: ReadingSession): Promise<void> {
    try {
      const currentStats = await this.getStatistics();
      const sessions = await this.getReadingSessions();
      
      const updatedStats = this.generateStatistics(sessions);
      await AsyncStorage.setItem(this.STATISTICS_KEY, JSON.stringify(updatedStats));

      // Update daily reading goal
      await this.updateDailyGoal(session.duration);

      loggingService.info(this.TAG, 'Statistics updated successfully');
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to update statistics', error);
    }
  }

  /**
   * Generate statistics from sessions
   */
  private generateStatistics(sessions: ReadingSession[]): ReadingStatistics {
    const now = new Date();
    const oneWeekAgo = new Date(now.getTime() - 7 * 24 * 60 * 60 * 1000);
    const oneMonthAgo = new Date(now.getTime() - 30 * 24 * 60 * 60 * 1000);
    const oneYearAgo = new Date(now.getTime() - 365 * 24 * 60 * 60 * 1000);

    const totalReadingTime = sessions
      .filter(s => s.contentType === 'manga')
      .reduce((total, s) => total + s.duration, 0);

    const totalWatchingTime = sessions
      .filter(s => s.contentType === 'anime')
      .reduce((total, s) => total + s.duration, 0);

    const averageSessionTime = sessions.length > 0 
      ? Math.round((totalReadingTime + totalWatchingTime) / sessions.length)
      : 0;

    const chaptersRead = sessions
      .filter(s => s.contentType === 'manga' && s.chapterId)
      .length;

    const episodesWatched = sessions
      .filter(s => s.contentType === 'anime' && s.episodeId)
      .length;

    const booksCompleted = sessions
      .filter(s => s.completed)
      .length;

    // Calculate reading streak
    const { streakDays, longestStreak } = this.calculateReadingStreak(sessions);

    // Generate reading time by day for the last 30 days
    const readingTimeByDay = this.generateDailyReadingTime(sessions, 30);

    // Count completed content in different time periods
    const completedThisWeek = sessions
      .filter(s => s.completed && new Date(s.startTime) >= oneWeekAgo)
      .length;

    const completedThisMonth = sessions
      .filter(s => s.completed && new Date(s.startTime) >= oneMonthAgo)
      .length;

    const completedThisYear = sessions
      .filter(s => s.completed && new Date(s.startTime) >= oneYearAgo)
      .length;

    return {
      totalReadingSessions: sessions.length,
      totalReadingTime,
      totalWatchingTime,
      averageSessionTime,
      streakDays,
      longestStreak,
      booksCompleted,
      chaptersRead,
      episodesWatched,
      favoriteGenres: [], // This would need genre data from content
      readingTimeByDay,
      completedThisWeek,
      completedThisMonth,
      completedThisYear,
      lastUpdated: new Date().toISOString(),
    };
  }

  /**
   * Calculate current and longest reading streak
   */
  private calculateReadingStreak(sessions: ReadingSession[]): { streakDays: number; longestStreak: number } {
    if (sessions.length === 0) {
      return { streakDays: 0, longestStreak: 0 };
    }

    // Group sessions by date
    const sessionsByDate = new Map<string, ReadingSession[]>();
    sessions.forEach(session => {
      const date = new Date(session.startTime).toDateString();
      if (!sessionsByDate.has(date)) {
        sessionsByDate.set(date, []);
      }
      sessionsByDate.get(date)!.push(session);
    });

    const dates = Array.from(sessionsByDate.keys()).sort();
    
    let currentStreak = 0;
    let longestStreak = 0;
    let tempStreak = 1;

    // Calculate current streak from today backwards
    const today = new Date().toDateString();
    const todayIndex = dates.indexOf(today);
    
    if (todayIndex !== -1) {
      currentStreak = 1;
      for (let i = todayIndex - 1; i >= 0; i--) {
        const currentDate = new Date(dates[i]);
        const nextDate = new Date(dates[i + 1]);
        const diffDays = Math.floor((nextDate.getTime() - currentDate.getTime()) / (1000 * 60 * 60 * 24));
        
        if (diffDays === 1) {
          currentStreak++;
        } else {
          break;
        }
      }
    }

    // Calculate longest streak
    for (let i = 1; i < dates.length; i++) {
      const currentDate = new Date(dates[i]);
      const prevDate = new Date(dates[i - 1]);
      const diffDays = Math.floor((currentDate.getTime() - prevDate.getTime()) / (1000 * 60 * 60 * 24));
      
      if (diffDays === 1) {
        tempStreak++;
      } else {
        longestStreak = Math.max(longestStreak, tempStreak);
        tempStreak = 1;
      }
    }
    longestStreak = Math.max(longestStreak, tempStreak);

    return { streakDays: currentStreak, longestStreak };
  }

  /**
   * Generate daily reading time for the last N days
   */
  private generateDailyReadingTime(sessions: ReadingSession[], days: number): Array<{ date: string; minutes: number }> {
    const result: Array<{ date: string; minutes: number }> = [];
    const now = new Date();

    for (let i = days - 1; i >= 0; i--) {
      const date = new Date(now.getTime() - i * 24 * 60 * 60 * 1000);
      const dateString = date.toISOString().split('T')[0];
      
      const dayMinutes = sessions
        .filter(s => {
          const sessionDate = new Date(s.startTime).toISOString().split('T')[0];
          return sessionDate === dateString;
        })
        .reduce((total, s) => total + s.duration, 0);

      result.push({ date: dateString, minutes: dayMinutes });
    }

    return result;
  }

  /**
   * Set daily reading goal
   */
  async setDailyReadingGoal(targetMinutes: number): Promise<void> {
    try {
      const today = new Date().toISOString().split('T')[0];
      const goals = await this.getDailyGoals();
      
      const existingGoalIndex = goals.findIndex(g => g.date === today);
      const goal: DailyReadingGoal = {
        targetMinutes,
        currentMinutes: existingGoalIndex !== -1 ? goals[existingGoalIndex].currentMinutes : 0,
        date: today,
        achieved: false,
      };

      if (existingGoalIndex !== -1) {
        goals[existingGoalIndex] = goal;
      } else {
        goals.push(goal);
      }

      goal.achieved = goal.currentMinutes >= goal.targetMinutes;

      await AsyncStorage.setItem(this.GOALS_KEY, JSON.stringify(goals));
      loggingService.info(this.TAG, `Set daily reading goal: ${targetMinutes} minutes`);
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to set daily reading goal', error);
      throw error;
    }
  }

  /**
   * Get today's reading goal
   */
  async getTodayGoal(): Promise<DailyReadingGoal | null> {
    try {
      const today = new Date().toISOString().split('T')[0];
      const goals = await this.getDailyGoals();
      return goals.find(g => g.date === today) || null;
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to get today goal', error);
      return null;
    }
  }

  /**
   * Update daily reading goal progress
   */
  private async updateDailyGoal(minutesToAdd: number): Promise<void> {
    try {
      const today = new Date().toISOString().split('T')[0];
      const goals = await this.getDailyGoals();
      
      const goalIndex = goals.findIndex(g => g.date === today);
      if (goalIndex !== -1) {
        goals[goalIndex].currentMinutes += minutesToAdd;
        goals[goalIndex].achieved = goals[goalIndex].currentMinutes >= goals[goalIndex].targetMinutes;
        
        await AsyncStorage.setItem(this.GOALS_KEY, JSON.stringify(goals));
      }
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to update daily goal', error);
    }
  }

  // Private helper methods
  private async getReadingSessions(): Promise<ReadingSession[]> {
    try {
      const sessions = await AsyncStorage.getItem(this.SESSIONS_KEY);
      return sessions ? JSON.parse(sessions) : [];
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to get reading sessions', error);
      return [];
    }
  }

  private async saveReadingSessions(sessions: ReadingSession[]): Promise<void> {
    await AsyncStorage.setItem(this.SESSIONS_KEY, JSON.stringify(sessions));
  }

  private async getDailyGoals(): Promise<DailyReadingGoal[]> {
    try {
      const goals = await AsyncStorage.getItem(this.GOALS_KEY);
      return goals ? JSON.parse(goals) : [];
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to get daily goals', error);
      return [];
    }
  }

  private createEmptyStatistics(): ReadingStatistics {
    return {
      totalReadingSessions: 0,
      totalReadingTime: 0,
      totalWatchingTime: 0,
      averageSessionTime: 0,
      streakDays: 0,
      longestStreak: 0,
      booksCompleted: 0,
      chaptersRead: 0,
      episodesWatched: 0,
      favoriteGenres: [],
      readingTimeByDay: [],
      completedThisWeek: 0,
      completedThisMonth: 0,
      completedThisYear: 0,
      lastUpdated: new Date().toISOString(),
    };
  }
}