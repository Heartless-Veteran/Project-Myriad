/**
 * Logging Service for Project Myriad
 * Provides centralized logging with persistent storage
 */
import { Platform } from 'react-native';
import AsyncStorage from '@react-native-async-storage/async-storage';

// Define log levels
export enum LogLevel {
  DEBUG = 0,
  INFO = 1,
  WARN = 2,
  ERROR = 3,
}

// Define log entry structure
export interface LogEntry {
  timestamp: number;
  level: LogLevel;
  tag: string;
  message: string;
  data?: any;
}

// Define storage options
interface LogStorageOptions {
  enabled: boolean;
  maxEntries: number;
  persistenceKey: string;
}

/**
 * Centralized logging service with persistence support
 */
export class LoggingService {
  private static instance: LoggingService;
  private currentLogLevel: LogLevel = __DEV__ ? LogLevel.DEBUG : LogLevel.INFO;
  private logs: LogEntry[] = [];
  private storageOptions: LogStorageOptions = {
    enabled: true,
    maxEntries: 1000,
    persistenceKey: '@ProjectMyriad:logs',
  };

  // Private constructor for singleton pattern
  private constructor() {
    this.loadLogs();
  }

  /**
   * Get singleton instance
   */
  public static getInstance(): LoggingService {
    if (!LoggingService.instance) {
      LoggingService.instance = new LoggingService();
    }
    return LoggingService.instance;
  }

  /**
   * Set the current log level
   */
  public setLogLevel(level: LogLevel): void {
    this.currentLogLevel = level;
  }

  /**
   * Get the current log level
   */
  public getLogLevel(): LogLevel {
    return this.currentLogLevel;
  }

  /**
   * Configure storage options
   */
  public configureStorage(options: Partial<LogStorageOptions>): void {
    this.storageOptions = { ...this.storageOptions, ...options };
  }

  /**
   * Log a debug message
   */
  public debug(tag: string, message: string, data?: any): void {
    this.log(LogLevel.DEBUG, tag, message, data);
  }

  /**
   * Log an info message
   */
  public info(tag: string, message: string, data?: any): void {
    this.log(LogLevel.INFO, tag, message, data);
  }

  /**
   * Log a warning message
   */
  public warn(tag: string, message: string, data?: any): void {
    this.log(LogLevel.WARN, tag, message, data);
  }

  /**
   * Log an error message
   */
  public error(tag: string, message: string, data?: any): void {
    this.log(LogLevel.ERROR, tag, message, data);
  }

  /**
   * Core logging method
   */
  private log(level: LogLevel, tag: string, message: string, data?: any): void {
    if (level < this.currentLogLevel) {
      return;
    }

    const entry: LogEntry = {
      timestamp: Date.now(),
      level,
      tag,
      message,
      data,
    };

    // Add to in-memory logs
    this.logs.push(entry);

    // Trim logs if necessary
    if (this.logs.length > this.storageOptions.maxEntries) {
      this.logs = this.logs.slice(-this.storageOptions.maxEntries);
    }

    // Console output in development
    if (__DEV__) {
      const levelName = LogLevel[level];
      const timestamp = new Date(entry.timestamp).toISOString();
      console.log(`[${timestamp}] ${levelName} ${tag}: ${message}`, data || '');
    }

    // Store logs if enabled
    if (this.storageOptions.enabled) {
      this.storeLogs();
    }
  }

  /**
   * Get all stored logs
   */
  public getLogs(): LogEntry[] {
    return [...this.logs];
  }

  /**
   * Get logs filtered by level
   */
  public getLogsByLevel(level: LogLevel): LogEntry[] {
    return this.logs.filter(log => log.level === level);
  }

  /**
   * Get logs filtered by tag
   */
  public getLogsByTag(tag: string): LogEntry[] {
    return this.logs.filter(log => log.tag === tag);
  }

  /**
   * Clear all logs
   */
  public clearLogs(): void {
    this.logs = [];
    if (this.storageOptions.enabled) {
      this.storeLogs();
    }
  }

  /**
   * Store logs to persistent storage
   */
  private async storeLogs(): Promise<void> {
    try {
      await AsyncStorage.setItem(
        this.storageOptions.persistenceKey,
        JSON.stringify(this.logs)
      );
    } catch (error) {
      console.error('Failed to store logs:', error);
    }
  }

  /**
   * Load logs from storage
   */
  private async loadLogs(): Promise<void> {
    try {
      const storedLogs = await AsyncStorage.getItem(this.storageOptions.persistenceKey);
      if (storedLogs) {
        this.logs = JSON.parse(storedLogs);
      }
    } catch (error) {
      console.error('Failed to load logs from storage:', error);
    }
  }

  /**
   * Export logs as JSON string
   */
  public exportLogs(): string {
    return JSON.stringify(this.logs, null, 2);
  }
}