import AsyncStorage from '@react-native-async-storage/async-storage';
import { Collection, CollectionItem, Manga, Anime } from '../types';
import { LoggingService } from './LoggingService';

const loggingService = LoggingService.getInstance();

export class CollectionService {
  private static instance: CollectionService;
  private readonly TAG = 'CollectionService';
  private readonly COLLECTIONS_KEY = '@project_myriad_collections';
  private readonly COLLECTION_ITEMS_KEY = '@project_myriad_collection_items';

  private constructor() {}

  public static getInstance(): CollectionService {
    if (!CollectionService.instance) {
      CollectionService.instance = new CollectionService();
    }
    return CollectionService.instance;
  }

  /**
   * Get all collections
   */
  async getCollections(): Promise<Collection[]> {
    try {
      const collections = await AsyncStorage.getItem(this.COLLECTIONS_KEY);
      if (!collections) {
        return this.createDefaultCollections();
      }
      return JSON.parse(collections);
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to get collections', error);
      return this.createDefaultCollections();
    }
  }

  /**
   * Create default collections (Favorites, Currently Reading, etc.)
   */
  private async createDefaultCollections(): Promise<Collection[]> {
    const defaultCollections: Collection[] = [
      {
        id: 'favorites',
        name: 'Favorites',
        description: 'Your favorite manga and anime',
        contentIds: [],
        contentType: 'mixed',
        isDefault: true,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        sortOrder: 0,
      },
      {
        id: 'currently_reading',
        name: 'Currently Reading',
        description: 'Content you are currently reading/watching',
        contentIds: [],
        contentType: 'mixed',
        isDefault: true,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        sortOrder: 1,
      },
      {
        id: 'want_to_read',
        name: 'Want to Read',
        description: 'Content you plan to read/watch later',
        contentIds: [],
        contentType: 'mixed',
        isDefault: true,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
        sortOrder: 2,
      },
    ];

    await this.saveCollections(defaultCollections);
    return defaultCollections;
  }

  /**
   * Create a new collection
   */
  async createCollection(collection: Omit<Collection, 'id' | 'createdAt' | 'updatedAt'>): Promise<Collection> {
    try {
      const collections = await this.getCollections();
      const newCollection: Collection = {
        ...collection,
        id: `collection_${Date.now()}_${Math.random().toString(36).substr(2, 9)}`,
        createdAt: new Date().toISOString(),
        updatedAt: new Date().toISOString(),
      };

      collections.push(newCollection);
      await this.saveCollections(collections);

      loggingService.info(this.TAG, `Created collection: ${newCollection.name}`);
      return newCollection;
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to create collection', error);
      throw error;
    }
  }

  /**
   * Update an existing collection
   */
  async updateCollection(collectionId: string, updates: Partial<Collection>): Promise<Collection> {
    try {
      const collections = await this.getCollections();
      const index = collections.findIndex(c => c.id === collectionId);
      
      if (index === -1) {
        throw new Error(`Collection with ID ${collectionId} not found`);
      }

      collections[index] = {
        ...collections[index],
        ...updates,
        updatedAt: new Date().toISOString(),
      };

      await this.saveCollections(collections);
      loggingService.info(this.TAG, `Updated collection: ${collections[index].name}`);
      return collections[index];
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to update collection', error);
      throw error;
    }
  }

  /**
   * Delete a collection (cannot delete default collections)
   */
  async deleteCollection(collectionId: string): Promise<void> {
    try {
      const collections = await this.getCollections();
      const collection = collections.find(c => c.id === collectionId);

      if (!collection) {
        throw new Error(`Collection with ID ${collectionId} not found`);
      }

      if (collection.isDefault) {
        throw new Error('Cannot delete default collections');
      }

      const filteredCollections = collections.filter(c => c.id !== collectionId);
      await this.saveCollections(filteredCollections);

      // Remove all items from this collection
      await this.removeAllItemsFromCollection(collectionId);

      loggingService.info(this.TAG, `Deleted collection: ${collection.name}`);
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to delete collection', error);
      throw error;
    }
  }

  /**
   * Add content to a collection
   */
  async addToCollection(collectionId: string, contentId: string, contentType: 'manga' | 'anime'): Promise<void> {
    try {
      const collections = await this.getCollections();
      const collection = collections.find(c => c.id === collectionId);

      if (!collection) {
        throw new Error(`Collection with ID ${collectionId} not found`);
      }

      // Check if content is already in collection
      if (collection.contentIds.includes(contentId)) {
        return; // Already in collection
      }

      // Add to collection
      collection.contentIds.push(contentId);
      collection.updatedAt = new Date().toISOString();

      // Update collection type if needed
      if (collection.contentType !== 'mixed' && collection.contentType !== contentType) {
        collection.contentType = 'mixed';
      }

      await this.saveCollections(collections);

      // Add collection item entry
      const collectionItems = await this.getCollectionItems();
      const newItem: CollectionItem = {
        collectionId,
        contentId,
        contentType,
        addedAt: new Date().toISOString(),
        position: collection.contentIds.length - 1,
      };

      collectionItems.push(newItem);
      await this.saveCollectionItems(collectionItems);

      loggingService.info(this.TAG, `Added content ${contentId} to collection ${collection.name}`);
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to add to collection', error);
      throw error;
    }
  }

  /**
   * Remove content from a collection
   */
  async removeFromCollection(collectionId: string, contentId: string): Promise<void> {
    try {
      const collections = await this.getCollections();
      const collection = collections.find(c => c.id === collectionId);

      if (!collection) {
        throw new Error(`Collection with ID ${collectionId} not found`);
      }

      collection.contentIds = collection.contentIds.filter(id => id !== contentId);
      collection.updatedAt = new Date().toISOString();

      await this.saveCollections(collections);

      // Remove collection item entry
      const collectionItems = await this.getCollectionItems();
      const filteredItems = collectionItems.filter(
        item => !(item.collectionId === collectionId && item.contentId === contentId)
      );
      await this.saveCollectionItems(filteredItems);

      loggingService.info(this.TAG, `Removed content ${contentId} from collection ${collection.name}`);
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to remove from collection', error);
      throw error;
    }
  }

  /**
   * Get content in a collection
   */
  async getCollectionContent(collectionId: string, library: (Manga | Anime)[]): Promise<(Manga | Anime)[]> {
    try {
      const collection = await this.getCollectionById(collectionId);
      if (!collection) {
        return [];
      }

      return library.filter(item => collection.contentIds.includes(item.id));
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to get collection content', error);
      return [];
    }
  }

  /**
   * Get a collection by ID
   */
  async getCollectionById(collectionId: string): Promise<Collection | null> {
    try {
      const collections = await this.getCollections();
      return collections.find(c => c.id === collectionId) || null;
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to get collection by ID', error);
      return null;
    }
  }

  /**
   * Get collections that contain specific content
   */
  async getCollectionsForContent(contentId: string): Promise<Collection[]> {
    try {
      const collections = await this.getCollections();
      return collections.filter(c => c.contentIds.includes(contentId));
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to get collections for content', error);
      return [];
    }
  }

  // Private helper methods
  private async saveCollections(collections: Collection[]): Promise<void> {
    await AsyncStorage.setItem(this.COLLECTIONS_KEY, JSON.stringify(collections));
  }

  private async getCollectionItems(): Promise<CollectionItem[]> {
    try {
      const items = await AsyncStorage.getItem(this.COLLECTION_ITEMS_KEY);
      return items ? JSON.parse(items) : [];
    } catch (error) {
      loggingService.error(this.TAG, 'Failed to get collection items', error);
      return [];
    }
  }

  private async saveCollectionItems(items: CollectionItem[]): Promise<void> {
    await AsyncStorage.setItem(this.COLLECTION_ITEMS_KEY, JSON.stringify(items));
  }

  private async removeAllItemsFromCollection(collectionId: string): Promise<void> {
    const collectionItems = await this.getCollectionItems();
    const filteredItems = collectionItems.filter(item => item.collectionId !== collectionId);
    await this.saveCollectionItems(filteredItems);
  }
}