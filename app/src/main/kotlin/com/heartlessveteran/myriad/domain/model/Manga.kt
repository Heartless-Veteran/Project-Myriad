package com.heartlessveteran.myriad.domain.model

/**
 * Domain model representing a single manga title.
 * This is a clean data class, free of any platform-specific dependencies.
 *
 * @param url The unique URL identifier for this manga on its source.
 * @param title The main title of the manga.
 * @param artist The artist of the manga.
 * @param author The author of the manga.
 * @param description A summary or description of the manga.
 * @param genre A list of genres associated with the manga.
 * @param status The publication status (e.g., "Ongoing", "Completed").
 * @param thumbnailUrl The URL for the manga's cover thumbnail image.
 */
data class Manga(
    val url: String,
    val title: String,
    val artist: String?,
    val author: String?,
    val description: String?,
    val genre: List<String>?,
    val status: String?,
    val thumbnailUrl: String,
)
