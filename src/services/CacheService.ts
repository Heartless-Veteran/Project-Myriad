/**
 * Smart Caching Service
 * Phase 2: Core Features - Smart caching implementation
 * 
 * Provides intelligent caching for images, data, and files with
 * automatic cleanup, compression, and performance optimization
 */

import AsyncStorage from '@react-native-async-storage/async-storage';
import RNFS from 'react-native-fs';
import { loggingService } from './LoggingService';
import { errorService, ErrorType, ErrorSeverity } from './ErrorService';

// Cache configuration
export interface CacheConfig {
  maxMemorySize: number; // bytes
  maxDiskSize: number; // bytes
  defaultTTL: number; // milliseconds
  cleanupInterval: number; // milliseconds
  compressionEnabled: boolean;
  preloadEnabled: boolean;
  analyticsEnabled: boolean;
}

// Cache entry metadata
export interface CacheEntry {
  key: string;
  data: any;
  size: number;
  createdAt: Date;
  lastAccessed: Date;
  expiresAt: Date;
  accessCount: number;
  priority: CachePriority;
  tags: string[];
  compressed: boolean;
  checksum?: string;
}

// Cache priority levels
export enum CachePriority {
  LOW = 1,
  NORMAL = 2,
  HIGH = 3,
  CRITICAL = 4,
}

// Cache types
export enum CacheType {
  MEMORY = 'memory',
  DISK = 'disk',
  HYBRID = 'hybrid',
}

// Cache statistics
export interface CacheStats {
  memorySize: number;
  diskSize: number;
  entryCount: number;
  hitRate: number;
  missRate: number;
  totalHits: number;
  totalMisses: number;
  averageAccessTime: number;
  cleanupCount: number;
  lastCleanup: Date;
}

// Cache analytics
export interface CacheAnalytics {
  mostAccessed: string[];
  leastAccessed: string[];
  largestEntries: string[];
  expiredEntries: string[];
  performanceMetrics: {
    averageGetTime: number;
    averageSetTime: number;
    averageDeleteTime: number;
  };
}

export class SmartCacheService {
  private static instance: SmartCacheService;
  private readonly TAG = 'SmartCacheService';
  
  // Cache storages
  private memoryCache: Map<string, CacheEntry> = new Map();
  private diskCachePath: string;
  
  // Configuration
  private config: CacheConfig = {
    maxMemorySize: 50 * 1024 * 1024, // 50MB
    maxDiskSize: 500 * 1024 * 1024, // 500MB
    defaultTTL: 24 * 60 * 60 * 1000, // 24 hours
    cleanupInterval: 60 * 60 * 1000, // 1 hour
    compressionEnabled: true,
    preloadEnabled: true,
    analyticsEnabled: true,
  };
  
  // Statistics
  private stats: CacheStats = {
    memorySize: 0,
    diskSize: 0,
    entryCount: 0,
    hitRate: 0,
    missRate: 0,
    totalHits: 0,
    totalMisses: 0,
    averageAccessTime: 0,
    cleanupCount: 0,
    lastCleanup: new Date(),
  };
  
  // Cleanup interval
  private cleanupInterval?: NodeJS.Timeout;
  
  // Storage keys
  private readonly CACHE_CONFIG_KEY = '@ProjectMyriad:CacheConfig';
  private readonly CACHE_STATS_KEY = '@ProjectMyriad:CacheStats';

  private constructor() {
    this.diskCachePath = `${RNFS.CachesDirectoryPath}/ProjectMyriad`;
    this.initialize();
  }

  public static getInstance(): SmartCacheService {
    if (!SmartCacheService.instance) {
      SmartCacheService.instance = new SmartCacheService();
    }
    return SmartCacheService.instance;
  }

  /**
   * Initialize the cache service
   */
  private async initialize(): Promise<void> {
    try {
      // Create cache directory
      await RNFS.mkdir(this.diskCachePath);
      
      // Load configuration
      await this.loadConfig();
      
      // Load statistics
      await this.loadStats();
      
      // Start cleanup interval
      this.startCleanupInterval();
      
      // Preload critical data if enabled
      if (this.config.preloadEnabled) {
        await this.preloadCriticalData();
      }
      
      loggingService.info(this.TAG, 'Smart cache service initialized');
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to initialize cache service', error);
      errorService.captureError(error as Error, ErrorType.SYSTEM, ErrorSeverity.ERROR);
    }
  }

  /**
   * Get data from cache
   */
  public async get<T>(
    key: string, 
    cacheType: CacheType = CacheType.HYBRID
  ): Promise<T | null> {
    const startTime = Date.now();
    
    try {
      let entry: CacheEntry | null = null;
      
      // Try memory cache first for hybrid/memory
      if (cacheType === CacheType.MEMORY || cacheType === CacheType.HYBRID) {
        entry = this.memoryCache.get(key) || null;
        
        if (entry && !this.isExpired(entry)) {
          this.updateAccessStats(entry);
          this.stats.totalHits++;
          this.updatePerformanceMetrics('get', Date.now() - startTime);
          return this.deserializeData(entry.data);
        }
      }
      
      // Try disk cache for hybrid/disk
      if (!entry && (cacheType === CacheType.DISK || cacheType === CacheType.HYBRID)) {
        entry = await this.getDiskEntry(key);
        
        if (entry && !this.isExpired(entry)) {
          // Move to memory cache if using hybrid
          if (cacheType === CacheType.HYBRID) {
            this.memoryCache.set(key, entry);
          }
          
          this.updateAccessStats(entry);
          this.stats.totalHits++;
          this.updatePerformanceMetrics('get', Date.now() - startTime);
          return this.deserializeData(entry.data);
        }
      }
      
      // Cache miss
      this.stats.totalMisses++;
      this.updateHitRate();
      this.updatePerformanceMetrics('get', Date.now() - startTime);
      return null;
      
    } catch (error) {
      loggingService.error(this.TAG, `Failed to get cache entry ${key}`, error);
      this.stats.totalMisses++;
      this.updatePerformanceMetrics('get', Date.now() - startTime);
      return null;
    }
  }

  /**
   * Set data in cache
   */
  public async set<T>(
    key: string,
    data: T,
    options: {
      ttl?: number;
      priority?: CachePriority;
      cacheType?: CacheType;
      tags?: string[];
      compress?: boolean;
    } = {}
  ): Promise<boolean> {
    const startTime = Date.now();
    
    try {
      const {
        ttl = this.config.defaultTTL,
        priority = CachePriority.NORMAL,
        cacheType = CacheType.HYBRID,
        tags = [],
        compress = this.config.compressionEnabled,
      } = options;

      const serializedData = await this.serializeData(data, compress);
      const size = this.calculateSize(serializedData);
      
      const entry: CacheEntry = {
        key,
        data: serializedData,
        size,
        createdAt: new Date(),
        lastAccessed: new Date(),
        expiresAt: new Date(Date.now() + ttl),
        accessCount: 0,
        priority,
        tags,
        compressed: compress,
      };

      let success = false;
      
      // Set in memory cache
      if (cacheType === CacheType.MEMORY || cacheType === CacheType.HYBRID) {
        if (this.stats.memorySize + size <= this.config.maxMemorySize) {
          this.memoryCache.set(key, entry);
          this.stats.memorySize += size;
          success = true;
        } else {
          // Try to free up space
          await this.evictMemoryEntries(size);
          if (this.stats.memorySize + size <= this.config.maxMemorySize) {
            this.memoryCache.set(key, entry);
            this.stats.memorySize += size;
            success = true;
          }
        }
      }
      
      // Set in disk cache
      if (cacheType === CacheType.DISK || cacheType === CacheType.HYBRID) {
        const diskSuccess = await this.setDiskEntry(entry);
        success = success || diskSuccess;
      }

      if (success) {
        this.stats.entryCount++;
        this.updatePerformanceMetrics('set', Date.now() - startTime);
        loggingService.debug(this.TAG, `Cached entry ${key} (${size} bytes)`);
      }

      return success;
      
    } catch (error) {
      loggingService.error(this.TAG, `Failed to set cache entry ${key}`, error);
      this.updatePerformanceMetrics('set', Date.now() - startTime);
      return false;
    }
  }

  /**
   * Delete entry from cache
   */
  public async delete(key: string): Promise<boolean> {
    const startTime = Date.now();
    
    try {
      let success = false;
      
      // Remove from memory cache
      const memoryEntry = this.memoryCache.get(key);
      if (memoryEntry) {
        this.memoryCache.delete(key);
        this.stats.memorySize -= memoryEntry.size;
        this.stats.entryCount--;
        success = true;
      }
      
      // Remove from disk cache
      const diskSuccess = await this.deleteDiskEntry(key);
      success = success || diskSuccess;
      
      this.updatePerformanceMetrics('delete', Date.now() - startTime);
      return success;
      
    } catch (error) {
      loggingService.error(this.TAG, `Failed to delete cache entry ${key}`, error);
      this.updatePerformanceMetrics('delete', Date.now() - startTime);
      return false;
    }
  }

  /**
   * Check if key exists in cache
   */
  public async has(key: string): Promise<boolean> {
    try {
      const entry = await this.get(key);
      return entry !== null;
    } catch {
      return false;
    }
  }

  /**
   * Clear all cache entries
   */
  public async clear(): Promise<void> {
    try {
      // Clear memory cache
      this.memoryCache.clear();
      
      // Clear disk cache
      await RNFS.unlink(this.diskCachePath);
      await RNFS.mkdir(this.diskCachePath);
      
      // Reset stats
      this.stats = {
        ...this.stats,
        memorySize: 0,
        diskSize: 0,
        entryCount: 0,
      };
      
      await this.saveStats();
      loggingService.info(this.TAG, 'All cache cleared');
      
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to clear cache', error);
      errorService.captureError(error as Error, ErrorType.SYSTEM, ErrorSeverity.ERROR);
    }
  }

  /**
   * Clear cache entries by tags
   */
  public async clearByTags(tags: string[]): Promise<number> {
    let clearedCount = 0;
    
    try {
      // Clear from memory cache
      for (const [key, entry] of this.memoryCache.entries()) {
        if (entry.tags.some(tag => tags.includes(tag))) {
          await this.delete(key);
          clearedCount++;
        }
      }
      
      loggingService.info(this.TAG, `Cleared ${clearedCount} entries by tags`);
      return clearedCount;
      
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to clear cache by tags', error);
      return clearedCount;
    }
  }

  /**
   * Get cache statistics
   */
  public getStats(): CacheStats {
    this.updateHitRate();
    return { ...this.stats };
  }

  /**
   * Get cache analytics
   */
  public getAnalytics(): CacheAnalytics {
    const entries = Array.from(this.memoryCache.values());
    
    // Most accessed entries
    const mostAccessed = entries
      .sort((a, b) => b.accessCount - a.accessCount)
      .slice(0, 10)
      .map(entry => entry.key);
    
    // Least accessed entries
    const leastAccessed = entries
      .sort((a, b) => a.accessCount - b.accessCount)
      .slice(0, 10)
      .map(entry => entry.key);
    
    // Largest entries
    const largestEntries = entries
      .sort((a, b) => b.size - a.size)
      .slice(0, 10)
      .map(entry => entry.key);
    
    // Expired entries
    const expiredEntries = entries
      .filter(entry => this.isExpired(entry))
      .map(entry => entry.key);

    return {
      mostAccessed,
      leastAccessed,
      largestEntries,
      expiredEntries,
      performanceMetrics: {
        averageGetTime: this.stats.averageAccessTime,
        averageSetTime: this.stats.averageAccessTime, // Simplified for now
        averageDeleteTime: this.stats.averageAccessTime, // Simplified for now
      },
    };
  }

  /**
   * Preload critical data
   */
  private async preloadCriticalData(): Promise<void> {
    try {
      // This would be implemented to preload frequently accessed data
      // For now, just log that preloading is enabled
      loggingService.info(this.TAG, 'Preloading critical data...');
      
      // Example: Preload user preferences, recent items, etc.
      // const criticalKeys = ['user_preferences', 'recent_manga', 'recent_anime'];
      // for (const key of criticalKeys) {
      //   await this.get(key);
      // }
      
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to preload critical data', error);
    }
  }

  /**
   * Start automatic cleanup interval
   */
  private startCleanupInterval(): void {
    this.cleanupInterval = setInterval(async () => {
      await this.performCleanup();
    }, this.config.cleanupInterval);
  }

  /**
   * Perform cache cleanup
   */
  private async performCleanup(): Promise<void> {
    try {
      loggingService.info(this.TAG, 'Starting cache cleanup...');
      
      let removedCount = 0;
      const now = new Date();
      
      // Clean expired entries
      for (const [key, entry] of this.memoryCache.entries()) {
        if (this.isExpired(entry)) {
          await this.delete(key);
          removedCount++;
        }
      }
      
      // Clean least recently used entries if memory is full
      if (this.stats.memorySize > this.config.maxMemorySize * 0.9) {
        const lruEntries = Array.from(this.memoryCache.entries())
          .sort(([,a], [,b]) => a.lastAccessed.getTime() - b.lastAccessed.getTime());
          
        const targetSize = this.config.maxMemorySize * 0.7;
        let currentSize = this.stats.memorySize;
        
        for (const [key, entry] of lruEntries) {
          if (currentSize <= targetSize) break;
          if (entry.priority !== CachePriority.CRITICAL) {
            await this.delete(key);
            currentSize -= entry.size;
            removedCount++;
          }
        }
      }
      
      this.stats.cleanupCount++;
      this.stats.lastCleanup = now;
      await this.saveStats();
      
      loggingService.info(this.TAG, `Cleanup completed. Removed ${removedCount} entries`);
      
    } catch (error) {
      loggingService.error(this.TAG, 'Cache cleanup failed', error);
    }
  }

  /**
   * Evict memory entries to make space
   */
  private async evictMemoryEntries(requiredSpace: number): Promise<void> {
    const entries = Array.from(this.memoryCache.entries())
      .filter(([, entry]) => entry.priority !== CachePriority.CRITICAL)
      .sort(([, a], [, b]) => {
        // Sort by priority (lower first) then by last accessed
        if (a.priority !== b.priority) {
          return a.priority - b.priority;
        }
        return a.lastAccessed.getTime() - b.lastAccessed.getTime();
      });

    let freedSpace = 0;
    for (const [key, entry] of entries) {
      if (freedSpace >= requiredSpace) break;
      
      this.memoryCache.delete(key);
      this.stats.memorySize -= entry.size;
      freedSpace += entry.size;
    }
    
    loggingService.debug(this.TAG, `Evicted entries to free ${freedSpace} bytes`);
  }

  /**
   * Serialize data for storage
   */
  private async serializeData(data: any, compress: boolean): Promise<any> {
    try {
      let serialized = JSON.stringify(data);
      
      if (compress && serialized.length > 1000) {
        // In a real implementation, you would use a compression library
        // For now, we'll just mark it as compressed
        return { compressed: true, data: serialized };
      }
      
      return serialized;
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to serialize data', error);
      throw error;
    }
  }

  /**
   * Deserialize data from storage
   */
  private deserializeData(data: any): any {
    try {
      if (typeof data === 'object' && data.compressed) {
        return JSON.parse(data.data);
      }
      
      if (typeof data === 'string') {
        return JSON.parse(data);
      }
      
      return data;
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to deserialize data', error);
      throw error;
    }
  }

  /**
   * Calculate data size
   */
  private calculateSize(data: any): number {
    // Estimate size as number of UTF-16 code units in JSON string
    return JSON.stringify(data).length;
  }

  /**
   * Check if cache entry is expired
   */
  private isExpired(entry: CacheEntry): boolean {
    return new Date() > entry.expiresAt;
  }

  /**
   * Update access statistics for cache entry
   */
  private updateAccessStats(entry: CacheEntry): void {
    entry.lastAccessed = new Date();
    entry.accessCount++;
  }

  /**
   * Update hit rate statistics
   */
  private updateHitRate(): void {
    const total = this.stats.totalHits + this.stats.totalMisses;
    if (total > 0) {
      this.stats.hitRate = this.stats.totalHits / total;
      this.stats.missRate = this.stats.totalMisses / total;
    }
  }

  /**
   * Update performance metrics
   */
  private updatePerformanceMetrics(operation: string, duration: number): void {
    // Simple moving average for now
    this.stats.averageAccessTime = (this.stats.averageAccessTime + duration) / 2;
  }

  /**
   * Get disk cache entry
   */
  private async getDiskEntry(key: string): Promise<CacheEntry | null> {
    try {
      const filePath = `${this.diskCachePath}/${key}.json`;
      if (await RNFS.exists(filePath)) {
        const content = await RNFS.readFile(filePath, 'utf8');
        const entry: CacheEntry = JSON.parse(content);
        
        // Convert date strings back to Date objects
        entry.createdAt = new Date(entry.createdAt);
        entry.lastAccessed = new Date(entry.lastAccessed);
        entry.expiresAt = new Date(entry.expiresAt);
        
        return entry;
      }
      return null;
    } catch (error) {
      loggingService.error(this.TAG, `Failed to get disk entry ${key}`, error);
      return null;
    }
  }

  /**
   * Set disk cache entry
   */
  private async setDiskEntry(entry: CacheEntry): Promise<boolean> {
    try {
      const filePath = `${this.diskCachePath}/${entry.key}.json`;
      await RNFS.writeFile(filePath, JSON.stringify(entry), 'utf8');
      return true;
    } catch (error) {
      loggingService.error(this.TAG, `Failed to set disk entry ${entry.key}`, error);
      return false;
    }
  }

  /**
   * Delete disk cache entry
   */
  private async deleteDiskEntry(key: string): Promise<boolean> {
    try {
      const filePath = `${this.diskCachePath}/${key}.json`;
      if (await RNFS.exists(filePath)) {
        await RNFS.unlink(filePath);
        return true;
      }
      return false;
    } catch (error) {
      loggingService.error(this.TAG, `Failed to delete disk entry ${key}`, error);
      return false;
    }
  }

  /**
   * Load configuration
   */
  private async loadConfig(): Promise<void> {
    try {
      const configData = await AsyncStorage.getItem(this.CACHE_CONFIG_KEY);
      if (configData) {
        const savedConfig = JSON.parse(configData);
        this.config = { ...this.config, ...savedConfig };
      }
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to load cache config', error);
    }
  }

  /**
   * Save configuration
   */
  private async saveConfig(): Promise<void> {
    try {
      await AsyncStorage.setItem(this.CACHE_CONFIG_KEY, JSON.stringify(this.config));
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to save cache config', error);
    }
  }

  /**
   * Load statistics
   */
  private async loadStats(): Promise<void> {
    try {
      const statsData = await AsyncStorage.getItem(this.CACHE_STATS_KEY);
      if (statsData) {
        const savedStats = JSON.parse(statsData);
        // Convert date strings back to Date objects
        savedStats.lastCleanup = new Date(savedStats.lastCleanup);
        this.stats = { ...this.stats, ...savedStats };
      }
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to load cache stats', error);
    }
  }

  /**
   * Save statistics
   */
  private async saveStats(): Promise<void> {
    try {
      await AsyncStorage.setItem(this.CACHE_STATS_KEY, JSON.stringify(this.stats));
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to save cache stats', error);
    }
  }

  /**
   * Update cache configuration
   */
  public async updateConfig(newConfig: Partial<CacheConfig>): Promise<void> {
    this.config = { ...this.config, ...newConfig };
    await this.saveConfig();
    
    // Restart cleanup interval if changed
    if (newConfig.cleanupInterval && this.cleanupInterval) {
      clearInterval(this.cleanupInterval);
      this.startCleanupInterval();
    }
    
    loggingService.info(this.TAG, 'Cache configuration updated');
  }

  /**
   * Destroy the cache service
   */
  public destroy(): void {
    if (this.cleanupInterval) {
      clearInterval(this.cleanupInterval);
      this.cleanupInterval = undefined;
    }
    
    this.memoryCache.clear();
    this.saveStats();
  }
}

// Export singleton instance
export const smartCacheService = SmartCacheService.getInstance();