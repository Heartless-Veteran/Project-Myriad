package com.heartlessveteran.myriad.domain.ai

import com.heartlessveteran.myriad.domain.models.Result
import kotlinx.coroutines.flow.Flow
import java.io.File

/**
 * AI Core - Intelligent Features Engine
 * 
 * Provides modular AI capabilities for enhancing the manga/anime experience:
 * - OCR Translation: Real-time text extraction and translation
 * - Art Style Matching: Computer vision for style categorization
 * - AI-Powered Recommendations: Intelligent content suggestions
 * - Natural Language Search: NLP query parsing and understanding
 * - Metadata Extraction: AI-powered cover analysis and metadata extraction
 */
interface AICore {
    
    /**
     * Initialize AI services and models
     */
    suspend fun initialize(): Result<Unit>
    
    /**
     * Get available AI features and their status
     */
    suspend fun getAvailableFeatures(): Result<List<AIFeature>>
    
    /**
     * Enable or disable an AI feature
     */
    suspend fun setFeatureEnabled(feature: AIFeatureType, enabled: Boolean): Result<Unit>
}

/**
 * OCR Translation Service for real-time manga translation
 */
interface OCRTranslationService {
    
    /**
     * Extract and translate text from an image
     */
    suspend fun translateImage(
        imageFile: File,
        sourceLanguage: String = "auto",
        targetLanguage: String = "en",
        options: OCROptions = OCROptions()
    ): Result<TranslationResult>
    
    /**
     * Extract text without translation
     */
    suspend fun extractText(
        imageFile: File,
        language: String = "auto",
        options: OCROptions = OCROptions()
    ): Result<TextExtractionResult>
    
    /**
     * Translate extracted text
     */
    suspend fun translateText(
        text: String,
        sourceLanguage: String,
        targetLanguage: String
    ): Result<String>
    
    /**
     * Get supported languages
     */
    suspend fun getSupportedLanguages(): Result<List<Language>>
    
    /**
     * Preprocess image for better OCR accuracy
     */
    suspend fun preprocessImage(
        imageFile: File,
        options: PreprocessingOptions
    ): Result<File>
}

/**
 * Art Style Analysis Service for content categorization
 */
interface ArtStyleAnalysisService {
    
    /**
     * Analyze art style of manga cover or page
     */
    suspend fun analyzeArtStyle(imageFile: File): Result<ArtStyleResult>
    
    /**
     * Compare art styles between images
     */
    suspend fun compareArtStyles(
        image1: File,
        image2: File
    ): Result<StyleSimilarity>
    
    /**
     * Find similar art styles in library
     */
    suspend fun findSimilarStyles(
        referenceImage: File,
        threshold: Float = 0.8f
    ): Result<List<StyleMatch>>
    
    /**
     * Categorize content by art style
     */
    suspend fun categorizeByStyle(
        images: List<File>
    ): Result<Map<ArtStyleCategory, List<File>>>
}

/**
 * AI-Powered Recommendation Engine
 */
interface RecommendationService {
    
    /**
     * Get personalized recommendations
     */
    suspend fun getRecommendations(
        userId: String,
        count: Int = 10,
        preferences: RecommendationPreferences = RecommendationPreferences()
    ): Result<List<Recommendation>>
    
    /**
     * Get similar content recommendations
     */
    suspend fun getSimilarContent(
        contentId: String,
        count: Int = 5
    ): Result<List<Recommendation>>
    
    /**
     * Update user preferences based on behavior
     */
    suspend fun updateUserBehavior(
        userId: String,
        behavior: UserBehavior
    ): Result<Unit>
    
    /**
     * Get trending content
     */
    suspend fun getTrendingContent(
        timeframe: TrendingTimeframe = TrendingTimeframe.WEEK
    ): Result<List<Recommendation>>
    
    /**
     * Train recommendation model with user data
     */
    suspend fun trainModel(
        userData: List<UserBehavior>
    ): Result<ModelTrainingResult>
}

/**
 * Natural Language Processing Service
 */
interface NLPService {
    
    /**
     * Parse natural language search query
     */
    suspend fun parseSearchQuery(query: String): Result<ParsedQuery>
    
    /**
     * Extract entities from text (character names, places, etc.)
     */
    suspend fun extractEntities(text: String): Result<List<Entity>>
    
    /**
     * Analyze sentiment of text
     */
    suspend fun analyzeSentiment(text: String): Result<SentimentResult>
    
    /**
     * Generate summary of long text
     */
    suspend fun summarizeText(
        text: String,
        maxLength: Int = 200
    ): Result<String>
    
    /**
     * Classify text into categories
     */
    suspend fun classifyText(
        text: String,
        categories: List<String>
    ): Result<ClassificationResult>
}

/**
 * Metadata Extraction Service using AI
 */
interface MetadataExtractionService {
    
    /**
     * Extract metadata from cover image
     */
    suspend fun extractFromCover(coverImage: File): Result<ExtractedMetadata>
    
    /**
     * Extract metadata from title image/text
     */
    suspend fun extractFromTitle(titleImage: File): Result<TitleMetadata>
    
    /**
     * Identify characters from image
     */
    suspend fun identifyCharacters(image: File): Result<List<Character>>
    
    /**
     * Extract genre information from visual elements
     */
    suspend fun extractGenres(images: List<File>): Result<List<Genre>>
    
    /**
     * Analyze content rating from visual content
     */
    suspend fun analyzeContentRating(images: List<File>): Result<ContentRating>
}

// Data classes and enums for AI Core

/**
 * AI Feature types
 */
enum class AIFeatureType {
    OCR_TRANSLATION,
    ART_STYLE_ANALYSIS,
    RECOMMENDATIONS,
    NLP_SEARCH,
    METADATA_EXTRACTION,
    CHARACTER_RECOGNITION,
    GENRE_CLASSIFICATION,
    CONTENT_RATING
}

/**
 * AI Feature status
 */
data class AIFeature(
    val type: AIFeatureType,
    val name: String,
    val description: String,
    val isAvailable: Boolean,
    val isEnabled: Boolean,
    val requiresNetwork: Boolean,
    val model: AIModel? = null
)

/**
 * AI Model information
 */
data class AIModel(
    val name: String,
    val version: String,
    val size: Long,
    val accuracy: Float,
    val isDownloaded: Boolean,
    val downloadUrl: String? = null
)

/**
 * OCR Options
 */
data class OCROptions(
    val preprocessImage: Boolean = true,
    val detectOrientation: Boolean = true,
    val preserveWhitespace: Boolean = false,
    val confidenceThreshold: Float = 0.8f,
    val language: String? = null
)

/**
 * Preprocessing options for images
 */
data class PreprocessingOptions(
    val denoise: Boolean = true,
    val enhanceContrast: Boolean = true,
    val adjustBrightness: Boolean = false,
    val deskew: Boolean = true,
    val cropWhitespace: Boolean = true
)

/**
 * Translation result
 */
data class TranslationResult(
    val originalText: List<TextRegion>,
    val translatedText: List<TextRegion>,
    val confidence: Float,
    val processingTime: Long,
    val sourceLanguage: String,
    val targetLanguage: String
)

/**
 * Text extraction result
 */
data class TextExtractionResult(
    val textRegions: List<TextRegion>,
    val confidence: Float,
    val detectedLanguage: String,
    val processingTime: Long
)

/**
 * Text region with coordinates
 */
data class TextRegion(
    val text: String,
    val boundingBox: BoundingBox,
    val confidence: Float,
    val language: String? = null
)

/**
 * Bounding box coordinates
 */
data class BoundingBox(
    val x: Int,
    val y: Int,
    val width: Int,
    val height: Int
)

/**
 * Supported language
 */
data class Language(
    val code: String,
    val name: String,
    val isSupported: Boolean
)

/**
 * Art style analysis result
 */
data class ArtStyleResult(
    val dominantStyle: ArtStyleCategory,
    val styleScores: Map<ArtStyleCategory, Float>,
    val colorPalette: List<String>,
    val visualElements: List<VisualElement>,
    val confidence: Float
)

/**
 * Art style categories
 */
enum class ArtStyleCategory {
    SHOUNEN,
    SHOUJO,
    SEINEN,
    JOSEI,
    REALISTIC,
    CHIBI,
    MINIMALIST,
    DETAILED,
    WESTERN,
    TRADITIONAL,
    DIGITAL,
    WATERCOLOR,
    VINTAGE,
    MODERN
}

/**
 * Visual elements detected in art
 */
data class VisualElement(
    val type: VisualElementType,
    val confidence: Float,
    val description: String
)

/**
 * Visual element types
 */
enum class VisualElementType {
    CHARACTER,
    BACKGROUND,
    TEXT_BUBBLE,
    SOUND_EFFECT,
    PANEL_BORDER,
    ACTION_LINE,
    EMOTION_SYMBOL
}

/**
 * Style similarity result
 */
data class StyleSimilarity(
    val similarity: Float,
    val matchingElements: List<VisualElement>,
    val differences: List<String>
)

/**
 * Style match for search results
 */
data class StyleMatch(
    val itemId: String,
    val similarity: Float,
    val matchingStyles: List<ArtStyleCategory>
)

/**
 * Recommendation
 */
data class Recommendation(
    val itemId: String,
    val title: String,
    val score: Float,
    val reason: RecommendationReason,
    val metadata: Map<String, Any> = emptyMap()
)

/**
 * Recommendation reasons
 */
enum class RecommendationReason {
    SIMILAR_GENRE,
    SIMILAR_AUTHOR,
    SIMILAR_ART_STYLE,
    USER_BEHAVIOR,
    POPULAR_TRENDING,
    CONTENT_BASED,
    COLLABORATIVE_FILTERING
}

/**
 * Recommendation preferences
 */
data class RecommendationPreferences(
    val preferredGenres: List<String> = emptyList(),
    val excludedGenres: List<String> = emptyList(),
    val preferredLanguages: List<String> = emptyList(),
    val contentTypes: Set<com.heartlessveteran.myriad.domain.vault.MediaType> = emptySet(),
    val includeNSFW: Boolean = false,
    val noveltyFactor: Float = 0.5f // 0 = only familiar content, 1 = only novel content
)

/**
 * User behavior for recommendations
 */
data class UserBehavior(
    val userId: String,
    val action: UserAction,
    val itemId: String,
    val timestamp: Long,
    val rating: Float? = null,
    val readingTime: Long? = null,
    val completionPercentage: Float? = null
)

/**
 * User actions
 */
enum class UserAction {
    VIEW,
    READ,
    FAVORITE,
    RATE,
    SHARE,
    DOWNLOAD,
    BOOKMARK,
    COMPLETE,
    DROP
}

/**
 * Trending timeframes
 */
enum class TrendingTimeframe {
    DAY,
    WEEK,
    MONTH,
    YEAR,
    ALL_TIME
}

/**
 * Model training result
 */
data class ModelTrainingResult(
    val accuracy: Float,
    val trainingTime: Long,
    val dataSize: Int,
    val modelVersion: String
)

/**
 * Parsed search query
 */
data class ParsedQuery(
    val intent: SearchIntent,
    val entities: List<Entity>,
    val filters: Map<String, String>,
    val originalQuery: String,
    val confidence: Float
)

/**
 * Search intents
 */
enum class SearchIntent {
    FIND_CONTENT,
    DISCOVER_SIMILAR,
    GET_RECOMMENDATIONS,
    FILTER_LIBRARY,
    FIND_CHARACTER,
    FIND_AUTHOR,
    FIND_GENRE
}

/**
 * Named entities
 */
data class Entity(
    val text: String,
    val type: EntityType,
    val confidence: Float,
    val startIndex: Int,
    val endIndex: Int
)

/**
 * Entity types
 */
enum class EntityType {
    TITLE,
    AUTHOR,
    CHARACTER,
    GENRE,
    YEAR,
    PUBLISHER,
    SERIES,
    LANGUAGE
}

/**
 * Sentiment analysis result
 */
data class SentimentResult(
    val sentiment: Sentiment,
    val confidence: Float,
    val scores: Map<Sentiment, Float>
)

/**
 * Sentiment types
 */
enum class Sentiment {
    POSITIVE,
    NEGATIVE,
    NEUTRAL,
    MIXED
}

/**
 * Text classification result
 */
data class ClassificationResult(
    val category: String,
    val confidence: Float,
    val scores: Map<String, Float>
)

/**
 * Extracted metadata from AI analysis
 */
data class ExtractedMetadata(
    val title: String? = null,
    val author: String? = null,
    val genres: List<String> = emptyList(),
    val characters: List<String> = emptyList(),
    val year: Int? = null,
    val language: String? = null,
    val description: String? = null,
    val artStyle: ArtStyleCategory? = null,
    val contentRating: ContentRating? = null,
    val confidence: Float
)

/**
 * Title metadata extraction
 */
data class TitleMetadata(
    val title: String,
    val subtitle: String? = null,
    val series: String? = null,
    val volume: Int? = null,
    val chapter: Float? = null,
    val confidence: Float
)

/**
 * Character information
 */
data class Character(
    val name: String,
    val confidence: Float,
    val description: String? = null,
    val role: CharacterRole? = null
)

/**
 * Character roles
 */
enum class CharacterRole {
    MAIN_CHARACTER,
    SUPPORTING_CHARACTER,
    ANTAGONIST,
    BACKGROUND_CHARACTER
}

/**
 * Genre information
 */
data class Genre(
    val name: String,
    val confidence: Float,
    val subgenres: List<String> = emptyList()
)

/**
 * Content rating analysis
 */
data class ContentRating(
    val rating: Rating,
    val confidence: Float,
    val reasons: List<String> = emptyList()
)

/**
 * Content ratings
 */
enum class Rating {
    G,      // General Audiences
    PG,     // Parental Guidance
    PG_13,  // Parents Strongly Cautioned
    R,      // Restricted
    NC_17,  // Adults Only
    UNRATED
}