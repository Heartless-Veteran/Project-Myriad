// Core type definitions for Project Myriad

export interface MangaChapter {
  id: string;
  title: string;
  chapterNumber: number;
  pages: string[];
  readProgress: number;
  isRead: boolean;
  /**
   * ISO 8601 date string representing when the chapter was added.
   * Example: "2024-06-10T12:34:56.789Z"
   */
  dateAdded: string;
  // Phase 2 enhancements
  downloadStatus?: 'pending' | 'downloading' | 'completed' | 'error';
  size?: number;
  quality?: string;
  sourceUrl?: string;
  lastRead?: string;
}

export interface Manga {
  id: string;
  title: string;
  author: string;
  description: string;
  coverImage: string;
  chapters: MangaChapter[];
  genres: string[];
  status: 'ongoing' | 'completed' | 'hiatus';
  rating: number;
  tags: string[];
  // Phase 2 enhancements
  alternativeTitles?: string[];
  originalLanguage?: string;
  publicationYear?: number;
  contentRating?: 'G' | 'PG' | 'PG-13' | 'R' | 'NC-17';
  themes?: string[];
  demographics?: string[];
  source?: string;
  sourceUrl?: string;
  lastUpdated?: string;
  totalSize?: number;
  favorited?: boolean;
  personalRating?: number;
  notes?: string;
  readingProgress?: number;
}

export interface AnimeEpisode {
  id: string;
  title: string;
  episodeNumber: number;
  duration: number;
  watchProgress: number;
  isWatched: boolean;
  videoUrl?: string;
  localPath?: string;
  /**
   * ISO 8601 date string representing when the episode was added.
   * Example: "2024-06-10T12:34:56.789Z"
   */
  dateAdded: string;
  // Phase 2 enhancements
  downloadStatus?: 'pending' | 'downloading' | 'completed' | 'error';
  size?: number;
  quality?: string;
  subtitles?: SubtitleTrack[];
  streamingUrls?: string[];
  lastWatched?: string;
}

export interface SubtitleTrack {
  language: string;
  url: string;
  format: string;
  label: string;
}

export interface Anime {
  id: string;
  title: string;
  description: string;
  coverImage: string;
  episodes: AnimeEpisode[];
  genres: string[];
  status: 'ongoing' | 'completed' | 'upcoming';
  rating: number;
  studio: string;
  tags: string[];
  // Phase 2 enhancements
  alternativeTitles?: string[];
  originalLanguage?: string;
  airYear?: number;
  contentRating?: 'G' | 'PG' | 'PG-13' | 'R' | 'NC-17';
  themes?: string[];
  demographics?: string[];
  source?: string;
  sourceUrl?: string;
  lastUpdated?: string;
  totalSize?: number;
  favorited?: boolean;
  personalRating?: number;
  notes?: string;
  watchProgress?: number;
}

export interface User {
  id: string;
  username: string;
  preferences: UserPreferences;
  history: UserHistory;
  // Phase 2 enhancements
  profile: UserProfile;
  statistics: UserStatistics;
}

export interface UserProfile {
  avatar?: string;
  displayName?: string;
  bio?: string;
  location?: string;
  joinDate: string;
  lastActive: string;
}

export interface UserStatistics {
  mangaRead: number;
  animeWatched: number;
  totalReadingTime: number; // minutes
  totalWatchingTime: number; // minutes
  favoriteGenres: string[];
  readingStreak: number; // days
  achievementsUnlocked: string[];
}

export interface UserPreferences {
  language: string;
  themes: string[];
  autoTranslate: boolean;
  offlineMode: boolean;
  genres: string[];
  // Phase 2 enhancements
  readingMode: 'single' | 'double' | 'webtoon' | 'continuous' | 'fit-width' | 'fit-height';
  readingDirection: 'ltr' | 'rtl' | 'vertical';
  videoQuality: 'auto' | 'low' | 'medium' | 'high' | 'ultra';
  autoDownload: boolean;
  notifications: NotificationSettings;
  privacy: PrivacySettings;
}

export interface NotificationSettings {
  newChapters: boolean;
  newEpisodes: boolean;
  recommendations: boolean;
  system: boolean;
}

export interface PrivacySettings {
  shareReadingHistory: boolean;
  showOnlineStatus: boolean;
  allowRecommendations: boolean;
}

export interface UserHistory {
  mangaRead: string[];
  animeWatched: string[];
  searchHistory: string[];
  recommendations: string[];
  // Phase 2 enhancements
  recentlyViewed: RecentItem[];
  bookmarks: Bookmark[];
  downloadHistory: DownloadItem[];
}

export interface RecentItem {
  contentId: string;
  contentType: 'manga' | 'anime';
  title: string;
  coverImage: string;
  lastAccessedAt: string;
  progress: number;
}

export interface Bookmark {
  id: string;
  contentId: string;
  contentType: 'manga' | 'anime';
  title: string;
  chapterId?: string;
  episodeId?: string;
  note?: string;
  createdAt: string;
}

export interface DownloadItem {
  id: string;
  contentId: string;
  contentType: 'manga' | 'anime';
  title: string;
  status: 'pending' | 'downloading' | 'completed' | 'paused' | 'error';
  progress: number;
  size: number;
  downloadedSize: number;
  createdAt: string;
  completedAt?: string;
}

// AI Core Types
export interface AITranslation {
  originalText: string;
  translatedText: string;
  sourceLanguage: string;
  targetLanguage: string;
  confidence: number;
  boundingBoxes: BoundingBox[];
  /**
   * ISO 8601 date string representing when the translation was created.
   * Example: "2024-06-10T12:34:56.789Z"
   */
  timestamp: string;
  // Phase 2 enhancements
  cached: boolean;
  processingTime: number;
  method: 'online' | 'offline' | 'hybrid';
  context?: string;
}

export interface BoundingBox {
  x: number;
  y: number;
  width: number;
  height: number;
  // Phase 2 enhancements
  confidence?: number;
  text?: string;
}

// Phase 2: Enhanced AI Types
export interface ArtStyleAnalysis {
  style: string;
  confidence: number;
  characteristics: string[];
  colorPalette: string[];
  genre: string;
  era: string;
  similarWorks: string[];
}

export interface ContentRecommendation {
  contentId: string;
  title: string;
  score: number;
  reasons: string[];
  similarity: number;
  type: 'manga' | 'anime';
  coverImage?: string;
  genres?: string[];
}

export interface MetadataAnalysis {
  title?: string;
  author?: string;
  genres?: string[];
  year?: number;
  description?: string;
  confidence: number;
  source: string;
  extractionMethod: string;
}

// Phase 2: Smart Caching Types
export interface CacheEntry {
  key: string;
  data: any;
  size: number;
  createdAt: Date;
  lastAccessed: Date;
  expiresAt: Date;
  accessCount: number;
  tags: string[];
}

export interface CacheStats {
  totalSize: number;
  entryCount: number;
  hitRate: number;
  missRate: number;
  averageAccessTime: number;
}

// Phase 2: Source Extension Types
export interface ContentSource {
  id: string;
  name: string;
  version: string;
  description: string;
  baseUrl: string;
  enabled: boolean;
  requiresAuth: boolean;
  rateLimit: number;
  priority: number;
  supportedTypes: ContentType[];
  capabilities: SourceCapabilities;
}

export interface SourceCapabilities {
  search: boolean;
  browse: boolean;
  download: boolean;
  stream: boolean;
  chapters: boolean;
  episodes: boolean;
  metadata: boolean;
}

export enum ContentType {
  MANGA = 'manga',
  ANIME = 'anime',
  LIGHT_NOVEL = 'light_novel',
  WEB_NOVEL = 'web_novel',
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
}
}

export interface ArtStyleMatch {
  item: Manga | Anime;
  similarity: number;
  artStyle: string;
  matchingCharacteristics: string[];
}

export interface AIRecommendation {
  item: Manga | Anime;
  score: number;
  reason: string;
  confidence: number;
}

// Browser/Source Types
export interface Source {
  id: string;
  name: string;
  baseUrl: string;
  type: 'manga' | 'anime' | 'both';
  isEnabled: boolean;
  config: SourceConfig;
}

export interface SourceConfig {
  apiKey?: string;
  rateLimit: number;
  searchEndpoint: string;
  streamEndpoint?: string;
  headers: Record<string, string>;
}

export interface SearchResult {
  id: string;
  title: string;
  description: string;
  coverImage: string;
  source: string;
  type: 'manga' | 'anime';
  genres: string[];
  status: string;
  rating?: number;
}

// File System Types
export interface ImportTask {
  id: string;
  files: string[];
  type: 'manga' | 'anime';
  status: 'pending' | 'processing' | 'completed' | 'failed';
  progress: number;
  errors: string[];
  /**
   * ISO 8601 date string representing when the task was created.
   * Example: "2024-06-10T12:34:56.789Z"
   */
  createdAt: string;
  /**
   * ISO 8601 date string representing when the task was completed.
   * Example: "2024-06-10T12:34:56.789Z"
   */
  completedAt?: string;
}

export interface LibraryStats {
  totalManga: number;
  totalAnime: number;
  totalSize: number;
  /**
   * ISO 8601 date string representing when the library was last updated.
   * Example: "2024-06-10T12:34:56.789Z"
   */
  lastUpdated: string;
  recentlyAdded: (Manga | Anime)[];
}

// Error Types
export interface AppError {
  id: string;
  type: 'network' | 'storage' | 'ai' | 'ui' | 'unknown';
  severity: 'low' | 'medium' | 'high' | 'critical';
  message: string;
  details?: any;
  /**
   * ISO 8601 date string representing when the error occurred.
   * Example: "2024-06-10T12:34:56.789Z"
   */
  timestamp: string;
  resolved: boolean;
}

// Settings Types
export interface AppSettings {
  theme: 'light' | 'dark' | 'auto';
  language: string;
  autoTranslate: boolean;
  offlineMode: boolean;
  aiSettings: AISettings;
  storageSettings: StorageSettings;
  sourceSettings: SourceSettings;
}

export interface AISettings {
  enableOCR: boolean;
  defaultSourceLanguage: string;
  defaultTargetLanguage: string;
  enableRecommendations: boolean;
  enableArtStyleMatching: boolean;
  offlineMode: boolean;
}

export interface StorageSettings {
  maxCacheSize: number;
  autoCleanup: boolean;
  compressionLevel: number;
  backupEnabled: boolean;
}

export interface SourceSettings {
  enabledSources: string[];
  defaultSource: string;
  rateLimit: number;
  autoRefresh: boolean;
}

// Collections System Types
export interface Collection {
  id: string;
  name: string;
  description?: string;
  coverImage?: string;
  contentIds: string[];
  contentType: 'manga' | 'anime' | 'mixed';
  isDefault: boolean;
  createdAt: string;
  updatedAt: string;
  sortOrder: number;
  tags?: string[];
}

export interface CollectionItem {
  collectionId: string;
  contentId: string;
  contentType: 'manga' | 'anime';
  addedAt: string;
  position: number;
}

// Reading Statistics Types
export interface ReadingSession {
  id: string;
  contentId: string;
  contentType: 'manga' | 'anime';
  chapterId?: string;
  episodeId?: string;
  startTime: string;
  endTime: string;
  pagesRead?: number;
  duration: number; // minutes
  completed: boolean;
}

export interface ReadingStatistics {
  totalReadingSessions: number;
  totalReadingTime: number; // minutes
  totalWatchingTime: number; // minutes
  averageSessionTime: number; // minutes
  streakDays: number;
  longestStreak: number;
  booksCompleted: number;
  chaptersRead: number;
  episodesWatched: number;
  favoriteGenres: Array<{ genre: string; count: number }>;
  readingTimeByDay: Array<{ date: string; minutes: number }>;
  completedThisWeek: number;
  completedThisMonth: number;
  completedThisYear: number;
  lastUpdated: string;
}

export interface DailyReadingGoal {
  targetMinutes: number;
  currentMinutes: number;
  date: string;
  achieved: boolean;
}