package com.heartlessveteran.myriad.domain.usecase

import com.heartlessveteran.myriad.domain.models.Result
import com.heartlessveteran.myriad.domain.repository.MangaRepository
import com.heartlessveteran.myriad.services.ContentRecommendation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Use case for generating AI-powered content recommendations
 */
@Singleton
class GetRecommendationsUseCase
    @Inject
    constructor(
        private val mangaRepository: MangaRepository,
    ) {
        /**
         * Get personalized recommendations based on user's library and reading history
         *
         * @param userId User identifier
         * @param limit Maximum number of recommendations to return
         * @return Flow of recommendations
         */
        operator fun invoke(
            userId: String = "default_user",
            limit: Int = 10,
        ): Flow<Result<List<ContentRecommendation>>> =
            flow {
                emit(Result.Loading)

                try {
                    // Get user's manga library to analyze preferences
                    mangaRepository.getAllManga().collect { userManga ->
                        // Analyze user preferences from their library
                        val userPreferences = analyzeUserPreferences(userManga)

                        // Generate recommendations based on preferences
                        val recommendations = generateRecommendations(userPreferences, limit)

                        emit(Result.Success(recommendations))
                    }
                } catch (e: Exception) {
                    emit(Result.Error(e))
                }
            }.catch { e ->
                emit(Result.Error(e as? Exception ?: Exception("Unknown error occurred")))
            }

        /**
         * Get recommendations based on specific genres
         *
         * @param genres List of preferred genres
         * @param limit Maximum number of recommendations
         * @return Flow of genre-based recommendations
         */
        fun getRecommendationsByGenres(
            genres: List<String>,
            limit: Int = 10,
        ): Flow<Result<List<ContentRecommendation>>> =
            flow {
                emit(Result.Loading)

                try {
                    val recommendations = generateGenreBasedRecommendations(genres, limit)
                    emit(Result.Success(recommendations))
                } catch (e: Exception) {
                    emit(Result.Error(e))
                }
            }.catch { e ->
                emit(Result.Error(e as? Exception ?: Exception("Unknown error occurred")))
            }

        /**
         * Get trending recommendations
         *
         * @param limit Maximum number of recommendations
         * @return Flow of trending recommendations
         */
        fun getTrendingRecommendations(limit: Int = 10): Flow<Result<List<ContentRecommendation>>> =
            flow {
                emit(Result.Loading)

                try {
                    val recommendations = generateTrendingRecommendations(limit)
                    emit(Result.Success(recommendations))
                } catch (e: Exception) {
                    emit(Result.Error(e))
                }
            }.catch { e ->
                emit(Result.Error(e as? Exception ?: Exception("Unknown error occurred")))
            }

        /**
         * Analyze user preferences from their manga library
         */
        private fun analyzeUserPreferences(
            userManga: List<com.heartlessveteran.myriad.domain.entities.Manga>,
        ): UserPreferences {
            val genreCounts = mutableMapOf<String, Int>()
            val authorCounts = mutableMapOf<String, Int>()
            var totalRating = 0.0
            var ratedCount = 0

            userManga.forEach { manga ->
                // Count genres (assuming genres are stored in description for now)
                extractGenresFromDescription(manga.description).forEach { genre ->
                    genreCounts[genre] = genreCounts.getOrDefault(genre, 0) + 1
                }

                // Count authors
                manga.author?.let { author ->
                    authorCounts[author] = authorCounts.getOrDefault(author, 0) + 1
                }

                // Calculate average rating if available
                if (manga.rating > 0.0f) {
                    totalRating += manga.rating
                    ratedCount++
                }
            }

            val preferredGenres =
                genreCounts.entries
                    .sortedByDescending { it.value }
                    .take(5)
                    .map { it.key }
            val preferredAuthors =
                authorCounts.entries
                    .sortedByDescending { it.value }
                    .take(3)
                    .map { it.key }
            val averageRating = if (ratedCount > 0) totalRating / ratedCount else 7.5

            return UserPreferences(
                preferredGenres = preferredGenres,
                preferredAuthors = preferredAuthors,
                averageRatingThreshold = averageRating,
                totalMangaCount = userManga.size,
            )
        }

        /**
         * Generate recommendations based on user preferences
         */
        private fun generateRecommendations(
            preferences: UserPreferences,
            limit: Int,
        ): List<ContentRecommendation> {
            val recommendations = mutableListOf<ContentRecommendation>()

            // Generate recommendations based on preferred genres
            preferences.preferredGenres.forEach { genre ->
                recommendations.addAll(
                    getRecommendationsForGenre(genre, limit / preferences.preferredGenres.size),
                )
            }

            // Add author-based recommendations
            preferences.preferredAuthors.forEach { author ->
                recommendations.addAll(
                    getRecommendationsForAuthor(author, 2),
                )
            }

            // Add trending recommendations if we don't have enough
            if (recommendations.size < limit) {
                recommendations.addAll(
                    generateTrendingRecommendations(limit - recommendations.size),
                )
            }

            return recommendations
                .distinctBy { it.contentId }
                .sortedByDescending { it.similarity }
                .take(limit)
        }

        /**
         * Generate genre-based recommendations
         */
        private fun generateGenreBasedRecommendations(
            genres: List<String>,
            limit: Int,
        ): List<ContentRecommendation> {
            val recommendations = mutableListOf<ContentRecommendation>()

            genres.forEach { genre ->
                recommendations.addAll(
                    getRecommendationsForGenre(genre, limit / genres.size),
                )
            }

            return recommendations
                .distinctBy { it.contentId }
                .sortedByDescending { it.similarity }
                .take(limit)
        }

        /**
         * Generate trending recommendations
         */
        private fun generateTrendingRecommendations(limit: Int): List<ContentRecommendation> {
            // Mock trending recommendations - in production this would use actual trending data
            return listOf(
                ContentRecommendation(
                    contentId = "trending_1",
                    title = "Jujutsu Kaisen",
                    similarity = 0.95f,
                    reason = "Currently trending worldwide",
                    genres = listOf("Action", "Supernatural", "School"),
                ),
                ContentRecommendation(
                    contentId = "trending_2",
                    title = "Chainsaw Man",
                    similarity = 0.93f,
                    reason = "Highly rated new release",
                    genres = listOf("Action", "Horror", "Supernatural"),
                ),
                ContentRecommendation(
                    contentId = "trending_3",
                    title = "Spy x Family",
                    similarity = 0.91f,
                    reason = "Popular wholesome series",
                    genres = listOf("Comedy", "Action", "Family"),
                ),
                ContentRecommendation(
                    contentId = "trending_4",
                    title = "My Hero Academia",
                    similarity = 0.89f,
                    reason = "Long-running popular series",
                    genres = listOf("Action", "School", "Superhero"),
                ),
                ContentRecommendation(
                    contentId = "trending_5",
                    title = "Demon Slayer",
                    similarity = 0.87f,
                    reason = "Award-winning animation",
                    genres = listOf("Action", "Historical", "Supernatural"),
                ),
            ).take(limit)
        }

        /**
         * Get recommendations for a specific genre
         */
        private fun getRecommendationsForGenre(
            genre: String,
            limit: Int,
        ): List<ContentRecommendation> =
            when (genre.lowercase()) {
                "action" ->
                    listOf(
                        ContentRecommendation(
                            "action_1",
                            "One Piece",
                            0.92f,
                            "Epic action adventure",
                            listOf("Action", "Adventure"),
                        ),
                        ContentRecommendation(
                            "action_2",
                            "Naruto",
                            0.88f,
                            "Classic ninja action",
                            listOf("Action", "Martial Arts"),
                        ),
                        ContentRecommendation(
                            "action_3",
                            "Dragon Ball",
                            0.85f,
                            "Legendary battle manga",
                            listOf("Action", "Fighting"),
                        ),
                    )
                "romance" ->
                    listOf(
                        ContentRecommendation(
                            "romance_1",
                            "Kaguya-sama",
                            0.90f,
                            "Romantic comedy masterpiece",
                            listOf("Romance", "Comedy"),
                        ),
                        ContentRecommendation(
                            "romance_2",
                            "Horimiya",
                            0.87f,
                            "Heartwarming school romance",
                            listOf("Romance", "School"),
                        ),
                        ContentRecommendation(
                            "romance_3",
                            "Your Name",
                            0.84f,
                            "Beautiful love story",
                            listOf("Romance", "Drama"),
                        ),
                    )
                "fantasy" ->
                    listOf(
                        ContentRecommendation(
                            "fantasy_1",
                            "Made in Abyss",
                            0.91f,
                            "Dark fantasy adventure",
                            listOf("Fantasy", "Adventure"),
                        ),
                        ContentRecommendation(
                            "fantasy_2",
                            "Re:Zero",
                            0.88f,
                            "Isekai fantasy drama",
                            listOf("Fantasy", "Drama"),
                        ),
                        ContentRecommendation(
                            "fantasy_3",
                            "Overlord",
                            0.85f,
                            "Dark fantasy strategy",
                            listOf("Fantasy", "Action"),
                        ),
                    )
                "slice of life" ->
                    listOf(
                        ContentRecommendation(
                            "slice_1",
                            "K-On!",
                            0.89f,
                            "Music and friendship",
                            listOf("Slice of Life", "Music"),
                        ),
                        ContentRecommendation(
                            "slice_2",
                            "Yuru Camp",
                            0.86f,
                            "Relaxing camping adventures",
                            listOf("Slice of Life", "Outdoor"),
                        ),
                        ContentRecommendation(
                            "slice_3",
                            "Lucky Star",
                            0.83f,
                            "Daily life comedy",
                            listOf("Slice of Life", "Comedy"),
                        ),
                    )
                else ->
                    listOf(
                        ContentRecommendation(
                            "general_1",
                            "Attack on Titan",
                            0.90f,
                            "Critically acclaimed series",
                            listOf("Action", "Drama"),
                        ),
                        ContentRecommendation(
                            "general_2",
                            "Death Note",
                            0.88f,
                            "Psychological thriller",
                            listOf("Thriller", "Supernatural"),
                        ),
                    )
            }.take(limit)

        /**
         * Get recommendations for a specific author
         */
        private fun getRecommendationsForAuthor(
            author: String,
            limit: Int,
        ): List<ContentRecommendation> {
            // Mock author-based recommendations
            return listOf(
                ContentRecommendation(
                    contentId = "author_${author.hashCode()}",
                    title = "New work by $author",
                    similarity = 0.94f,
                    reason = "From your favorite author",
                    genres = listOf("Various"),
                ),
            ).take(limit)
        }

        /**
         * Extract genres from manga description (simplified implementation)
         */
        private fun extractGenresFromDescription(description: String): List<String> {
            val genreKeywords =
                mapOf(
                    "action" to listOf("fight", "battle", "combat", "war", "martial"),
                    "romance" to listOf("love", "romance", "romantic", "relationship", "dating"),
                    "comedy" to listOf("funny", "comedy", "humor", "laugh", "comic"),
                    "drama" to listOf("drama", "emotional", "serious", "tragic", "family"),
                    "fantasy" to listOf("magic", "fantasy", "wizard", "dragon", "supernatural"),
                    "sci-fi" to listOf("science", "future", "space", "robot", "technology"),
                    "slice of life" to listOf("daily", "school", "life", "everyday", "peaceful"),
                    "horror" to listOf("horror", "scary", "ghost", "demon", "monster"),
                    "sports" to listOf("sport", "game", "competition", "team", "match"),
                )

            val genres = mutableListOf<String>()
            val lowerDescription = description.lowercase()

            genreKeywords.forEach { (genre, keywords) ->
                if (keywords.any { keyword -> lowerDescription.contains(keyword) }) {
                    genres.add(genre)
                }
            }

            return genres.ifEmpty { listOf("general") }
        }
    }

/**
 * Data class representing user preferences
 */
data class UserPreferences(
    val preferredGenres: List<String>,
    val preferredAuthors: List<String>,
    val averageRatingThreshold: Double,
    val totalMangaCount: Int,
)
