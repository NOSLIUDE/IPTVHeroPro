package com.iptvhero.pro.data.model

enum class ContentTab(val title: String) {
    LIVE("Ao Vivo"),
    MOVIES("Filmes"),
    SERIES("Séries")
}

data class CategoryItem(
    val id: String,
    val name: String
)

data class MediaItem(
    val id: String,
    val name: String,
    val categoryId: String,
    val categoryName: String,
    val streamUrl: String,
    val logoUrl: String = "",
    val contentType: ContentTab,
    val containerExtension: String = "mp4"
)

data class EpisodeItem(
    val id: String,
    val episodeNum: Int,
    val title: String,
    val streamUrl: String,
    val seasonNumber: Int
)

data class SeasonItem(
    val seasonNumber: Int,
    val name: String,
    val episodes: List<EpisodeItem>
)

data class XtreamAuthInfo(
    val expiryTimestampMs: Long,
    val maxConnections: Int,
    val status: String
)

data class BatchLogEntry(
    val timestamp: String,
    val message: String,
    val isError: Boolean = false
)
