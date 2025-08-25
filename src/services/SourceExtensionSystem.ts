/**
 * Source Extension System
 * Phase 2: Core Features - Extensible source system framework
 * 
 * Provides a framework for integrating multiple content sources
 * with plugin architecture, authentication, and unified search
 */

import AsyncStorage from '@react-native-async-storage/async-storage';
import axios, { AxiosInstance, AxiosRequestConfig } from 'axios';
import { loggingService } from './LoggingService';
import { errorService, ErrorType, ErrorSeverity } from './ErrorService';
import { smartCacheService, CachePriority } from './CacheService';
import { Manga, Anime, MangaChapter, AnimeEpisode } from '../types';

// Source interfaces
export interface ContentSource {
  id: string;
  name: string;
  version: string;
  description: string;
  baseUrl: string;
  supportedTypes: ContentType[];
  requiresAuth: boolean;
  rateLimit: number; // requests per minute
  priority: number; // lower = higher priority
  enabled: boolean;
  config: SourceConfig;
  capabilities: SourceCapabilities;
  metadata: SourceMetadata;
}

export interface SourceConfig {
  apiKey?: string;
  username?: string;
  password?: string;
  userAgent?: string;
  timeout: number;
  retryAttempts: number;
  customHeaders?: Record<string, string>;
  proxy?: ProxyConfig;
}

export interface ProxyConfig {
  enabled: boolean;
  host: string;
  port: number;
  username?: string;
  password?: string;
}

export interface SourceCapabilities {
  search: boolean;
  browse: boolean;
  download: boolean;
  stream: boolean;
  chapters: boolean;
  episodes: boolean;
  metadata: boolean;
  favorites: boolean;
  ratings: boolean;
  comments: boolean;
}

export interface SourceMetadata {
  author: string;
  website: string;
  language: string;
  nsfw: boolean;
  installDate: Date;
  lastUpdate: Date;
  updateUrl?: string;
}

export enum ContentType {
  MANGA = 'manga',
  ANIME = 'anime',
  LIGHT_NOVEL = 'light_novel',
  WEB_NOVEL = 'web_novel',
}

// Search and browsing interfaces
export interface SearchQuery {
  query: string;
  type?: ContentType;
  genres?: string[];
  status?: string;
  year?: number;
  rating?: number;
  sortBy?: SortOption;
  page?: number;
  limit?: number;
}

export enum SortOption {
  RELEVANCE = 'relevance',
  TITLE = 'title',
  RATING = 'rating',
  UPDATED = 'updated',
  VIEWS = 'views',
  CHAPTERS = 'chapters',
  YEAR = 'year',
}

export interface SearchResult {
  id: string;
  sourceId: string;
  title: string;
  description: string;
  coverUrl: string;
  url: string;
  type: ContentType;
  genres: string[];
  status: string;
  rating: number;
  chapters?: number;
  episodes?: number;
  year?: number;
  author?: string;
  studio?: string;
}

export interface BrowseOptions {
  type: ContentType;
  category?: string;
  genres?: string[];
  status?: string;
  sortBy?: SortOption;
  page?: number;
  limit?: number;
}

export interface SourceResponse<T> {
  success: boolean;
  data?: T;
  error?: string;
  hasMore?: boolean;
  totalCount?: number;
  page?: number;
}

// Authentication interfaces
export interface AuthCredentials {
  username?: string;
  password?: string;
  apiKey?: string;
  token?: string;
  refreshToken?: string;
  expiresAt?: Date;
}

export interface AuthResult {
  success: boolean;
  credentials?: AuthCredentials;
  error?: string;
  requiresCaptcha?: boolean;
  captchaUrl?: string;
}

// Source plugin interface
export interface SourcePlugin {
  source: ContentSource;
  authenticate?(credentials: AuthCredentials): Promise<AuthResult>;
  search(query: SearchQuery): Promise<SourceResponse<SearchResult[]>>;
  browse?(options: BrowseOptions): Promise<SourceResponse<SearchResult[]>>;
  getContent?(contentId: string, sourceUrl: string): Promise<SourceResponse<Manga | Anime>>;
  getChapters?(mangaId: string, sourceUrl: string): Promise<SourceResponse<MangaChapter[]>>;
  getEpisodes?(animeId: string, sourceUrl: string): Promise<SourceResponse<AnimeEpisode[]>>;
  getStreamingUrls?(episodeId: string, sourceUrl: string): Promise<SourceResponse<string[]>>;
  getChapterPages?(chapterId: string, sourceUrl: string): Promise<SourceResponse<string[]>>;
}

// Source manager events
export interface SourceManagerEvents {
  sourceAdded: (source: ContentSource) => void;
  sourceRemoved: (sourceId: string) => void;
  sourceUpdated: (source: ContentSource) => void;
  searchStarted: (query: SearchQuery, sources: string[]) => void;
  searchCompleted: (results: SearchResult[], totalTime: number) => void;
  authenticationRequired: (sourceId: string) => void;
  rateLimitHit: (sourceId: string, retryAfter: number) => void;
}

export class SourceExtensionSystem {
  private static instance: SourceExtensionSystem;
  private readonly TAG = 'SourceExtensionSystem';
  
  // Registered sources and plugins
  private sources: Map<string, ContentSource> = new Map();
  private plugins: Map<string, SourcePlugin> = new Map();
  private httpClients: Map<string, AxiosInstance> = new Map();
  
  // Rate limiting
  private rateLimiters: Map<string, { lastRequest: number; requestCount: number }> = new Map();
  
  // Event handlers
  private eventHandlers: Partial<SourceManagerEvents> = {};
  
  // Configuration
  private config = {
    maxConcurrentRequests: 5,
    defaultTimeout: 30000,
    cacheEnabled: true,
    cacheDuration: 5 * 60 * 1000, // 5 minutes
    retryDelay: 1000,
    rateLimitBuffer: 1000, // 1 second buffer
  };
  
  // Storage keys
  private readonly SOURCES_KEY = '@ProjectMyriad:Sources';
  private readonly SOURCE_CONFIGS_KEY = '@ProjectMyriad:SourceConfigs';

  private constructor() {
    this.initializeDefaultSources();
    this.loadSources();
  }

  public static getInstance(): SourceExtensionSystem {
    if (!SourceExtensionSystem.instance) {
      SourceExtensionSystem.instance = new SourceExtensionSystem();
    }
    return SourceExtensionSystem.instance;
  }

  /**
   * Register a new source plugin
   */
  public async registerSource(plugin: SourcePlugin): Promise<void> {
    try {
      const source = plugin.source;
      
      // Validate source
      this.validateSource(source);
      
      // Register source and plugin
      this.sources.set(source.id, source);
      this.plugins.set(source.id, plugin);
      
      // Create HTTP client for source
      this.createHttpClient(source);
      
      // Initialize rate limiter
      this.rateLimiters.set(source.id, { lastRequest: 0, requestCount: 0 });
      
      // Save to storage
      await this.saveSources();
      
      // Emit event
      this.eventHandlers.sourceAdded?.(source);
      
      loggingService.info(this.TAG, `Registered source: ${source.name} (${source.id})`);
      
    } catch (error) {
      loggingService.error(this.TAG, `Failed to register source: ${plugin.source.name}`, error);
      errorService.captureError(error as Error, ErrorType.SYSTEM, ErrorSeverity.ERROR);
      throw error;
    }
  }

  /**
   * Unregister a source
   */
  public async unregisterSource(sourceId: string): Promise<void> {
    try {
      this.sources.delete(sourceId);
      this.plugins.delete(sourceId);
      this.httpClients.delete(sourceId);
      this.rateLimiters.delete(sourceId);
      
      await this.saveSources();
      
      this.eventHandlers.sourceRemoved?.(sourceId);
      
      loggingService.info(this.TAG, `Unregistered source: ${sourceId}`);
      
    } catch (error) {
      loggingService.error(this.TAG, `Failed to unregister source: ${sourceId}`, error);
      throw error;
    }
  }

  /**
   * Update source configuration
   */
  public async updateSourceConfig(sourceId: string, config: Partial<SourceConfig>): Promise<void> {
    try {
      const source = this.sources.get(sourceId);
      if (!source) {
        throw new Error(`Source not found: ${sourceId}`);
      }
      
      // Update configuration
      source.config = { ...source.config, ...config };
      
      // Recreate HTTP client with new config
      this.createHttpClient(source);
      
      await this.saveSources();
      
      this.eventHandlers.sourceUpdated?.(source);
      
      loggingService.info(this.TAG, `Updated source config: ${sourceId}`);
      
    } catch (error) {
      loggingService.error(this.TAG, `Failed to update source config: ${sourceId}`, error);
      throw error;
    }
  }

  /**
   * Search across multiple sources
   */
  public async search(
    query: SearchQuery, 
    sourceIds?: string[]
  ): Promise<SourceResponse<SearchResult[]>> {
    const startTime = Date.now();
    
    try {
      // Get sources to search
      const sourcesToSearch = sourceIds 
        ? sourceIds.map(id => this.sources.get(id)).filter(Boolean) as ContentSource[]
        : Array.from(this.sources.values()).filter(s => s.enabled && s.capabilities.search);
      
      if (sourcesToSearch.length === 0) {
        throw new Error('No sources available for search');
      }
      
      // Check cache
      const cacheKey = this.generateSearchCacheKey(query, sourceIds);
      if (this.config.cacheEnabled) {
        const cached = await smartCacheService.get<SearchResult[]>(cacheKey);
        if (cached) {
          return {
            success: true,
            data: cached,
            totalCount: cached.length,
          };
        }
      }
      
      // Emit search started event
      this.eventHandlers.searchStarted?.(query, sourcesToSearch.map(s => s.id));
      
      // Execute searches in parallel with rate limiting
      const searchPromises = sourcesToSearch.map(source => 
        this.executeSearch(source, query)
      );
      
      const results = await Promise.allSettled(searchPromises);
      
      // Combine and deduplicate results
      const allResults: SearchResult[] = [];
      const seenTitles = new Set<string>();
      
      for (const result of results) {
        if (result.status === 'fulfilled' && result.value.success && result.value.data) {
          for (const item of result.value.data) {
            const titleKey = `${item.title.toLowerCase()}_${item.type}`;
            if (!seenTitles.has(titleKey)) {
              allResults.push(item);
              seenTitles.add(titleKey);
            }
          }
        }
      }
      
      // Sort by relevance and source priority
      allResults.sort((a, b) => {
        const sourceA = this.sources.get(a.sourceId);
        const sourceB = this.sources.get(b.sourceId);
        
        if (sourceA && sourceB) {
          return sourceA.priority - sourceB.priority;
        }
        
        return 0;
      });
      
      // Cache results
      if (this.config.cacheEnabled) {
        await smartCacheService.set(
          cacheKey,
          allResults,
          {
            ttl: this.config.cacheDuration,
            priority: CachePriority.NORMAL,
            tags: ['search', 'sources'],
          }
        );
      }
      
      const totalTime = Date.now() - startTime;
      
      // Emit search completed event
      this.eventHandlers.searchCompleted?.(allResults, totalTime);
      
      loggingService.info(this.TAG, `Search completed in ${totalTime}ms, found ${allResults.length} results`);
      
      return {
        success: true,
        data: allResults,
        totalCount: allResults.length,
      };
      
    } catch (error) {
      loggingService.error(this.TAG, 'Search failed', error);
      errorService.captureError(error as Error, ErrorType.DATA, ErrorSeverity.ERROR);
      
      return {
        success: false,
        error: (error as Error).message,
        data: [],
        totalCount: 0,
      };
    }
  }

  /**
   * Browse content from a specific source
   */
  public async browse(sourceId: string, options: BrowseOptions): Promise<SourceResponse<SearchResult[]>> {
    try {
      const source = this.sources.get(sourceId);
      const plugin = this.plugins.get(sourceId);
      
      if (!source || !plugin) {
        throw new Error(`Source not found or not registered: ${sourceId}`);
      }
      
      if (!source.enabled) {
        throw new Error(`Source is disabled: ${sourceId}`);
      }
      
      if (!source.capabilities.browse || !plugin.browse) {
        throw new Error(`Source does not support browsing: ${sourceId}`);
      }
      
      // Check rate limiting
      await this.enforceRateLimit(sourceId);
      
      // Execute browse request
      const result = await plugin.browse(options);
      
      loggingService.debug(this.TAG, `Browse completed for ${sourceId}: ${result.data?.length || 0} items`);
      
      return result;
      
    } catch (error) {
      loggingService.error(this.TAG, `Browse failed for ${sourceId}`, error);
      
      return {
        success: false,
        error: (error as Error).message,
        data: [],
      };
    }
  }

  /**
   * Get content details from a source
   */
  public async getContent(
    sourceId: string, 
    contentId: string, 
    sourceUrl: string
  ): Promise<SourceResponse<Manga | Anime>> {
    try {
      const plugin = this.plugins.get(sourceId);
      
      if (!plugin || !plugin.getContent) {
        throw new Error(`Source does not support content retrieval: ${sourceId}`);
      }
      
      await this.enforceRateLimit(sourceId);
      
      const result = await plugin.getContent(contentId, sourceUrl);
      
      return result;
      
    } catch (error) {
      loggingService.error(this.TAG, `Get content failed for ${sourceId}`, error);
      
      return {
        success: false,
        error: (error as Error).message,
      };
    }
  }

  /**
   * Authenticate with a source
   */
  public async authenticate(sourceId: string, credentials: AuthCredentials): Promise<AuthResult> {
    try {
      const source = this.sources.get(sourceId);
      const plugin = this.plugins.get(sourceId);
      
      if (!source || !plugin) {
        throw new Error(`Source not found: ${sourceId}`);
      }
      
      if (!source.requiresAuth || !plugin.authenticate) {
        throw new Error(`Source does not require authentication: ${sourceId}`);
      }
      
      const result = await plugin.authenticate(credentials);
      
      if (result.success && result.credentials) {
        // Update source config with credentials
        await this.updateSourceConfig(sourceId, {
          ...source.config,
          apiKey: result.credentials.apiKey,
          username: result.credentials.username,
          password: result.credentials.password,
        });
      }
      
      return result;
      
    } catch (error) {
      loggingService.error(this.TAG, `Authentication failed for ${sourceId}`, error);
      
      return {
        success: false,
        error: (error as Error).message,
      };
    }
  }

  /**
   * Get all registered sources
   */
  public getSources(): ContentSource[] {
    return Array.from(this.sources.values());
  }

  /**
   * Get enabled sources by type
   */
  public getSourcesByType(type: ContentType): ContentSource[] {
    return Array.from(this.sources.values())
      .filter(source => source.enabled && source.supportedTypes.includes(type));
  }

  /**
   * Enable/disable a source
   */
  public async toggleSource(sourceId: string, enabled: boolean): Promise<void> {
    const source = this.sources.get(sourceId);
    if (source) {
      source.enabled = enabled;
      await this.saveSources();
      
      loggingService.info(this.TAG, `Source ${sourceId} ${enabled ? 'enabled' : 'disabled'}`);
    }
  }

  /**
   * Set event handler
   */
  public on<K extends keyof SourceManagerEvents>(
    event: K, 
    handler: SourceManagerEvents[K]
  ): void {
    this.eventHandlers[event] = handler;
  }

  /**
   * Get source statistics
   */
  public getSourceStats(): Record<string, any> {
    const stats = {
      totalSources: this.sources.size,
      enabledSources: Array.from(this.sources.values()).filter(s => s.enabled).length,
      sourcesByType: {} as Record<ContentType, number>,
      authenticationRequired: Array.from(this.sources.values()).filter(s => s.requiresAuth).length,
    };

    // Count sources by type
    for (const type of Object.values(ContentType)) {
      stats.sourcesByType[type] = this.getSourcesByType(type).length;
    }

    return stats;
  }

  // Private methods

  private validateSource(source: ContentSource): void {
    if (!source.id || !source.name || !source.baseUrl) {
      throw new Error('Source missing required fields: id, name, baseUrl');
    }

    if (this.sources.has(source.id)) {
      throw new Error(`Source already registered: ${source.id}`);
    }

    if (!source.supportedTypes || source.supportedTypes.length === 0) {
      throw new Error('Source must support at least one content type');
    }
  }

  private createHttpClient(source: ContentSource): void {
    const config: AxiosRequestConfig = {
      baseURL: source.baseUrl,
      timeout: source.config.timeout,
      headers: {
        'User-Agent': source.config.userAgent || 'ProjectMyriad/1.0',
        ...source.config.customHeaders,
      },
    };

    // Add authentication headers if available
    if (source.config.apiKey) {
      config.headers!['Authorization'] = `Bearer ${source.config.apiKey}`;
    }

    const client = axios.create(config);

    // Add request interceptor for logging
    client.interceptors.request.use(
      (config) => {
        loggingService.debug(this.TAG, `${source.id}: ${config.method?.toUpperCase()} ${config.url}`);
        return config;
      },
      (error) => {
        loggingService.error(this.TAG, `${source.id}: Request error`, error);
        return Promise.reject(error);
      }
    );

    // Add response interceptor for error handling
    client.interceptors.response.use(
      (response) => response,
      (error) => {
        if (error.response?.status === 429) {
          this.eventHandlers.rateLimitHit?.(source.id, error.response.headers['retry-after'] || 60);
        }
        return Promise.reject(error);
      }
    );

    this.httpClients.set(source.id, client);
  }

  private async executeSearch(source: ContentSource, query: SearchQuery): Promise<SourceResponse<SearchResult[]>> {
    try {
      const plugin = this.plugins.get(source.id);
      if (!plugin) {
        throw new Error(`Plugin not found for source: ${source.id}`);
      }

      await this.enforceRateLimit(source.id);

      return await plugin.search(query);

    } catch (error) {
      loggingService.error(this.TAG, `Search failed for source ${source.id}`, error);
      
      return {
        success: false,
        error: (error as Error).message,
        data: [],
      };
    }
  }

  private async enforceRateLimit(sourceId: string): Promise<void> {
    const source = this.sources.get(sourceId);
    const limiter = this.rateLimiters.get(sourceId);
    
    if (!source || !limiter) {
      return;
    }

    const now = Date.now();
    const minInterval = (60 * 1000) / source.rateLimit; // ms per request
    const timeSinceLastRequest = now - limiter.lastRequest;

    if (timeSinceLastRequest < minInterval) {
      const waitTime = minInterval - timeSinceLastRequest + this.config.rateLimitBuffer;
      loggingService.debug(this.TAG, `Rate limiting ${sourceId}: waiting ${waitTime}ms`);
      await new Promise(resolve => setTimeout(resolve, waitTime));
    }

    limiter.lastRequest = Date.now();
    limiter.requestCount++;
  }

  private generateSearchCacheKey(query: SearchQuery, sourceIds?: string[]): string {
    const keyObject = {
      query: query.query,
      type: query.type,
      genres: query.genres?.sort(),
      sources: sourceIds?.sort(),
    };
    
    return `search_${JSON.stringify(keyObject)}`;
  }

  private initializeDefaultSources(): void {
    // This would initialize default sources
    loggingService.info(this.TAG, 'Initializing default sources...');
    
    // Example sources would be added here
    // For now, just log that we're ready
    loggingService.info(this.TAG, 'Source extension system ready');
  }

  private async loadSources(): Promise<void> {
    try {
      const sourcesData = await AsyncStorage.getItem(this.SOURCES_KEY);
      if (sourcesData) {
        const sources: ContentSource[] = JSON.parse(sourcesData);
        for (const source of sources) {
          this.sources.set(source.id, source);
          this.createHttpClient(source);
          this.rateLimiters.set(source.id, { lastRequest: 0, requestCount: 0 });
        }
      }
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to load sources', error);
    }
  }

  private async saveSources(): Promise<void> {
    try {
      const sources = Array.from(this.sources.values());
      await AsyncStorage.setItem(this.SOURCES_KEY, JSON.stringify(sources));
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to save sources', error);
    }
  }
}

// Export singleton instance
export const sourceExtensionSystem = SourceExtensionSystem.getInstance();