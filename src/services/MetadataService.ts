/**
 * Enhanced Metadata Management Service
 * Phase 2: Core Features - Metadata management enhancement
 * 
 * Provides comprehensive metadata management for manga and anime content
 * including auto-detection, scraping, caching, and synchronization
 */

import AsyncStorage from '@react-native-async-storage/async-storage';
import { Manga, Anime, MangaChapter, AnimeEpisode } from '../types';
import { loggingService } from './LoggingService';
import { errorService, ErrorType, ErrorSeverity } from './ErrorService';

// Enhanced metadata interfaces
export interface EnhancedMetadata {
  // Basic metadata
  title: string;
  description: string;
  genres: string[];
  tags: string[];
  status: 'ongoing' | 'completed' | 'hiatus' | 'upcoming';
  rating: number;
  
  // Enhanced fields
  alternativeTitles: string[];
  originalLanguage: string;
  publicationYear: number;
  lastUpdated: Date;
  source: 'local' | 'scraped' | 'manual' | 'api';
  
  // Content-specific metadata
  contentRating: 'G' | 'PG' | 'PG-13' | 'R' | 'NC-17';
  themes: string[];
  demographics: string[];
  
  // Technical metadata
  fileSize?: number;
  quality?: 'low' | 'medium' | 'high' | 'ultra';
  format?: string;
  checksum?: string;
  
  // User metadata
  personalRating?: number;
  notes?: string;
  dateAdded: Date;
  lastAccessed?: Date;
  readingProgress?: number;
  favorited: boolean;
  
  // Sync metadata
  syncStatus: 'synced' | 'pending' | 'conflict' | 'error';
  lastSyncDate?: Date;
}

export interface MetadataCache {
  key: string;
  metadata: EnhancedMetadata;
  expiryDate: Date;
  hits: number;
  lastAccessed: Date;
}

export interface MetadataSource {
  name: string;
  priority: number;
  enabled: boolean;
  apiKey?: string;
  baseUrl?: string;
  rateLimit: number;
  lastUsed: Date;
}

export interface MetadataScrapingResult {
  success: boolean;
  metadata?: Partial<EnhancedMetadata>;
  source: string;
  confidence: number;
  timestamp: Date;
  error?: string;
}

export class MetadataService {
  private static instance: MetadataService;
  private readonly TAG = 'MetadataService';
  private cache: Map<string, MetadataCache> = new Map();
  private sources: MetadataSource[] = [];
  
  // Storage keys
  private readonly METADATA_CACHE_KEY = '@ProjectMyriad:MetadataCache';
  private readonly METADATA_SOURCES_KEY = '@ProjectMyriad:MetadataSources';
  private readonly METADATA_CONFIG_KEY = '@ProjectMyriad:MetadataConfig';
  
  // Configuration
  private config = {
    cacheExpiry: 7 * 24 * 60 * 60 * 1000, // 7 days in milliseconds
    maxCacheSize: 1000,
    autoScrape: true,
    scrapingTimeout: 30000, // 30 seconds
    minConfidence: 0.7,
    retryAttempts: 3,
  };

  private constructor() {
    this.initializeDefaultSources();
    this.loadCache();
  }

  public static getInstance(): MetadataService {
    if (!MetadataService.instance) {
      MetadataService.instance = new MetadataService();
    }
    return MetadataService.instance;
  }

  /**
   * Initialize default metadata sources
   */
  private initializeDefaultSources(): void {
    this.sources = [
      {
        name: 'MyAnimeList',
        priority: 1,
        enabled: true,
        baseUrl: 'https://api.myanimelist.net/v2',
        rateLimit: 1000, // ms between requests
        lastUsed: new Date(0),
      },
      {
        name: 'AniList',
        priority: 2,
        enabled: true,
        baseUrl: 'https://graphql.anilist.co',
        rateLimit: 1000,
        lastUsed: new Date(0),
      },
      {
        name: 'MangaUpdates',
        priority: 3,
        enabled: true,
        baseUrl: 'https://api.mangaupdates.com/v1',
        rateLimit: 2000,
        lastUsed: new Date(0),
      },
      {
        name: 'LocalFile',
        priority: 10,
        enabled: true,
        rateLimit: 0,
        lastUsed: new Date(0),
      },
    ];
  }

  /**
   * Get enhanced metadata for content
   */
  public async getMetadata(
    contentId: string, 
    contentType: 'manga' | 'anime',
    forceRefresh = false
  ): Promise<EnhancedMetadata | null> {
    try {
      // Check cache first unless force refresh
      if (!forceRefresh) {
        const cached = await this.getCachedMetadata(contentId);
        if (cached && !this.isCacheExpired(cached)) {
          this.updateCacheStats(contentId);
          return cached.metadata;
        }
      }

      // Scrape metadata from available sources
      const scrapingResult = await this.scrapeMetadata(contentId, contentType);
      
      if (scrapingResult.success && scrapingResult.metadata) {
        // Cache the result
        await this.cacheMetadata(contentId, scrapingResult.metadata as EnhancedMetadata);
        return scrapingResult.metadata as EnhancedMetadata;
      }

      return null;
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to get metadata', error);
      errorService.captureError(error as Error, ErrorType.DATA, ErrorSeverity.ERROR);
      return null;
    }
  }

  /**
   * Scrape metadata from external sources
   */
  private async scrapeMetadata(
    contentId: string, 
    contentType: 'manga' | 'anime'
  ): Promise<MetadataScrapingResult> {
    const results: MetadataScrapingResult[] = [];
    
    // Sort sources by priority
    const enabledSources = this.sources
      .filter(source => source.enabled)
      .sort((a, b) => a.priority - b.priority);

    for (const source of enabledSources) {
      try {
        // Respect rate limiting
        await this.respectRateLimit(source);

        const result = await this.scrapeFromSource(source, contentId, contentType);
        results.push(result);

        // If we got a high confidence result, use it
        if (result.success && result.confidence >= this.config.minConfidence) {
          return result;
        }
      } catch (error) {
        loggingService.error(this.TAG, `Scraping failed for source ${source.name}`, error);
        results.push({
          success: false,
          source: source.name,
          confidence: 0,
          timestamp: new Date(),
          error: (error as Error).message,
        });
      }
    }

    // Return the best result if no high confidence match
    const bestResult = results
      .filter(r => r.success)
      .sort((a, b) => b.confidence - a.confidence)[0];

    return bestResult || {
      success: false,
      source: 'none',
      confidence: 0,
      timestamp: new Date(),
      error: 'No metadata sources available',
    };
  }

  /**
   * Scrape from a specific source
   */
  private async scrapeFromSource(
    source: MetadataSource,
    contentId: string,
    contentType: 'manga' | 'anime'
  ): Promise<MetadataScrapingResult> {
    source.lastUsed = new Date();
    
    // This would be implemented differently for each source
    // For now, return a mock implementation
    loggingService.info(this.TAG, `Scraping ${contentType} ${contentId} from ${source.name}`);
    
    // Mock metadata based on source
    const mockMetadata: Partial<EnhancedMetadata> = {
      title: `Mock ${contentType} title from ${source.name}`,
      description: `Mock description scraped from ${source.name}`,
      genres: ['Action', 'Adventure'],
      status: 'ongoing',
      rating: 4.5,
      source: 'scraped',
      lastUpdated: new Date(),
      publicationYear: 2024,
      originalLanguage: 'japanese',
    };

    return {
      success: true,
      metadata: mockMetadata,
      source: source.name,
      confidence: 0.8,
      timestamp: new Date(),
    };
  }

  /**
   * Respect rate limiting for sources
   */
  private async respectRateLimit(source: MetadataSource): Promise<void> {
    if (source.rateLimit === 0) return;

    const timeSinceLastUse = Date.now() - source.lastUsed.getTime();
    const waitTime = source.rateLimit - timeSinceLastUse;
    
    if (waitTime > 0) {
      await new Promise(resolve => setTimeout(resolve, waitTime));
    }
  }

  /**
   * Cache metadata
   */
  private async cacheMetadata(contentId: string, metadata: EnhancedMetadata): Promise<void> {
    const cacheEntry: MetadataCache = {
      key: contentId,
      metadata,
      expiryDate: new Date(Date.now() + this.config.cacheExpiry),
      hits: 0,
      lastAccessed: new Date(),
    };

    this.cache.set(contentId, cacheEntry);
    
    // Clean up cache if it's too large
    if (this.cache.size > this.config.maxCacheSize) {
      await this.cleanCache();
    }

    // Persist to storage
    await this.saveCache();
  }

  /**
   * Get cached metadata
   */
  private async getCachedMetadata(contentId: string): Promise<MetadataCache | null> {
    return this.cache.get(contentId) || null;
  }

  /**
   * Check if cache entry is expired
   */
  private isCacheExpired(cacheEntry: MetadataCache): boolean {
    return new Date() > cacheEntry.expiryDate;
  }

  /**
   * Update cache statistics
   */
  private updateCacheStats(contentId: string): void {
    const cached = this.cache.get(contentId);
    if (cached) {
      cached.hits++;
      cached.lastAccessed = new Date();
    }
  }

  /**
   * Clean expired and least used cache entries
   */
  private async cleanCache(): Promise<void> {
    const now = new Date();
    const entries = Array.from(this.cache.entries());
    
    // Remove expired entries
    const validEntries = entries.filter(([, entry]) => now <= entry.expiryDate);
    
    // If still too many, remove least recently used
    if (validEntries.length > this.config.maxCacheSize) {
      validEntries.sort(([, a], [, b]) => a.lastAccessed.getTime() - b.lastAccessed.getTime());
      const keepEntries = validEntries.slice(-this.config.maxCacheSize);
      
      this.cache.clear();
      keepEntries.forEach(([key, entry]) => {
        this.cache.set(key, entry);
      });
    } else {
      // Just remove expired entries
      this.cache.clear();
      validEntries.forEach(([key, entry]) => {
        this.cache.set(key, entry);
      });
    }

    await this.saveCache();
    loggingService.info(this.TAG, `Cache cleaned. Size: ${this.cache.size}`);
  }

  /**
   * Load cache from storage
   */
  private async loadCache(): Promise<void> {
    try {
      const cacheData = await AsyncStorage.getItem(this.METADATA_CACHE_KEY);
      if (cacheData) {
        const entries: [string, MetadataCache][] = JSON.parse(cacheData);
        entries.forEach(([key, entry]) => {
          // Convert date strings back to Date objects
          entry.expiryDate = new Date(entry.expiryDate);
          entry.lastAccessed = new Date(entry.lastAccessed);
          entry.metadata.lastUpdated = new Date(entry.metadata.lastUpdated);
          entry.metadata.dateAdded = new Date(entry.metadata.dateAdded);
          
          this.cache.set(key, entry);
        });
        
        loggingService.info(this.TAG, `Loaded ${this.cache.size} cache entries`);
      }
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to load cache', error);
    }
  }

  /**
   * Save cache to storage
   */
  private async saveCache(): Promise<void> {
    try {
      const entries = Array.from(this.cache.entries());
      await AsyncStorage.setItem(this.METADATA_CACHE_KEY, JSON.stringify(entries));
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to save cache', error);
    }
  }

  /**
   * Update user metadata
   */
  public async updateUserMetadata(
    contentId: string, 
    updates: Partial<Pick<EnhancedMetadata, 'personalRating' | 'notes' | 'favorited' | 'readingProgress'>>
  ): Promise<void> {
    try {
      const cached = this.cache.get(contentId);
      if (cached) {
        Object.assign(cached.metadata, updates);
        cached.metadata.lastAccessed = new Date();
        await this.saveCache();
      }
      
      loggingService.info(this.TAG, `Updated user metadata for ${contentId}`);
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to update user metadata', error);
      errorService.captureError(error as Error, ErrorType.DATA, ErrorSeverity.ERROR);
    }
  }

  /**
   * Get cache statistics
   */
  public getCacheStats(): { size: number; hitRate: number; totalHits: number } {
    const entries = Array.from(this.cache.values());
    const totalHits = entries.reduce((sum, entry) => sum + entry.hits, 0);
    const totalRequests = entries.length;
    const hitRate = totalRequests > 0 ? totalHits / totalRequests : 0;

    return {
      size: this.cache.size,
      hitRate,
      totalHits,
    };
  }

  /**
   * Clear all cached metadata
   */
  public async clearCache(): Promise<void> {
    this.cache.clear();
    await AsyncStorage.removeItem(this.METADATA_CACHE_KEY);
    loggingService.info(this.TAG, 'Cache cleared');
  }

  /**
   * Export metadata for backup
   */
  public async exportMetadata(): Promise<string> {
    const data = {
      cache: Array.from(this.cache.entries()),
      sources: this.sources,
      config: this.config,
      timestamp: new Date().toISOString(),
    };
    
    return JSON.stringify(data, null, 2);
  }

  /**
   * Import metadata from backup
   */
  public async importMetadata(data: string): Promise<void> {
    try {
      const parsed = JSON.parse(data);
      
      // Import cache
      if (parsed.cache) {
        this.cache.clear();
        parsed.cache.forEach(([key, entry]: [string, any]) => {
          // Convert date strings back to Date objects
          entry.expiryDate = new Date(entry.expiryDate);
          entry.lastAccessed = new Date(entry.lastAccessed);
          entry.metadata.lastUpdated = new Date(entry.metadata.lastUpdated);
          entry.metadata.dateAdded = new Date(entry.metadata.dateAdded);
          
          this.cache.set(key, entry);
        });
      }
      
      // Import sources
      if (parsed.sources) {
        this.sources = parsed.sources;
      }
      
      // Import config
      if (parsed.config) {
        this.config = { ...this.config, ...parsed.config };
      }
      
      await this.saveCache();
      loggingService.info(this.TAG, 'Metadata imported successfully');
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to import metadata', error);
      throw error;
    }
  }
}

// Export singleton instance
export const metadataService = MetadataService.getInstance();