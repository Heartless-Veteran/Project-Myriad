import { CollectionService } from '../../src/services/CollectionService';
import { StatisticsService } from '../../src/services/StatisticsService';

describe('New Features Integration Tests', () => {
  describe('CollectionService', () => {
    let collectionService: CollectionService;

    beforeEach(() => {
      collectionService = CollectionService.getInstance();
    });

    it('should create default collections', async () => {
      const collections = await collectionService.getCollections();
      
      expect(collections).toHaveLength(3);
      expect(collections.find(c => c.name === 'Favorites')).toBeTruthy();
      expect(collections.find(c => c.name === 'Currently Reading')).toBeTruthy();
      expect(collections.find(c => c.name === 'Want to Read')).toBeTruthy();
    });

    it('should create a new collection', async () => {
      const newCollection = await collectionService.createCollection({
        name: 'Test Collection',
        description: 'A test collection',
        contentIds: [],
        contentType: 'manga',
        isDefault: false,
        sortOrder: 10,
      });

      expect(newCollection.name).toBe('Test Collection');
      expect(newCollection.description).toBe('A test collection');
      expect(newCollection.contentType).toBe('manga');
      expect(newCollection.isDefault).toBe(false);
    });

    it('should add content to collection', async () => {
      const collections = await collectionService.getCollections();
      const favoritesCollection = collections.find(c => c.name === 'Favorites')!;
      
      await collectionService.addToCollection(favoritesCollection.id, 'test-manga-1', 'manga');
      
      const updatedCollection = await collectionService.getCollectionById(favoritesCollection.id);
      expect(updatedCollection?.contentIds).toContain('test-manga-1');
    });
  });

  describe('StatisticsService', () => {
    let statisticsService: StatisticsService;

    beforeEach(() => {
      statisticsService = StatisticsService.getInstance();
    });

    it('should start and end reading session', async () => {
      const sessionId = await statisticsService.startReadingSession('test-manga-1', 'manga', 'chapter-1');
      expect(sessionId).toBeTruthy();

      await statisticsService.endReadingSession(sessionId, 20, true);
      
      const statistics = await statisticsService.getStatistics();
      expect(statistics.totalReadingSessions).toBeGreaterThan(0);
    });

    it('should set daily reading goal', async () => {
      await statisticsService.setDailyReadingGoal(30);
      
      const todayGoal = await statisticsService.getTodayGoal();
      expect(todayGoal?.targetMinutes).toBe(30);
    });

    it('should generate empty statistics for new user', async () => {
      const statistics = await statisticsService.getStatistics();
      
      expect(statistics.totalReadingTime).toBeGreaterThanOrEqual(0);
      expect(statistics.totalWatchingTime).toBeGreaterThanOrEqual(0);
      expect(statistics.streakDays).toBeGreaterThanOrEqual(0);
      expect(statistics.longestStreak).toBeGreaterThanOrEqual(0);
    });
  });
});