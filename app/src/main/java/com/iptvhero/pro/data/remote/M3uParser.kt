package com.iptvhero.pro.data.remote

import com.iptvhero.pro.data.model.CategoryItem
import com.iptvhero.pro.data.model.ContentTab
import com.iptvhero.pro.data.model.MediaItem
import java.util.UUID

data class ParsedPlaylist(
    val liveItems: List<MediaItem>,
    val movieItems: List<MediaItem>,
    val seriesItems: List<MediaItem>,
    val liveCategories: List<CategoryItem>,
    val movieCategories: List<CategoryItem>,
    val seriesCategories: List<CategoryItem>
)

object M3uParser {
    private val groupTitleRegex = Regex("group-title=\"([^\"]*)\"")
    private val tvgLogoRegex = Regex("tvg-logo=\"([^\"]*)\"")

    fun parse(rawContent: String): ParsedPlaylist {
        val liveList = mutableListOf<MediaItem>()
        val movieList = mutableListOf<MediaItem>()
        val seriesList = mutableListOf<MediaItem>()

        val lines = rawContent.lines()
        var currentName = ""
        var currentGroup = "Geral"
        var currentLogo = ""

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue

            if (line.startsWith("#EXTINF:", ignoreCase = true)) {
                currentGroup = groupTitleRegex.find(line)?.groupValues?.getOrNull(1)
                    ?.takeIf { it.isNotBlank() } ?: "Geral"
                currentLogo = tvgLogoRegex.find(line)?.groupValues?.getOrNull(1) ?: ""
                val commaIdx = line.lastIndexOf(',')
                currentName = if (commaIdx != -1 && commaIdx < line.length - 1) {
                    line.substring(commaIdx + 1).trim()
                } else {
                    "Canal Desconhecido"
                }
            } else if (!line.startsWith("#")) {
                var streamUrl = line
                var itemName = currentName
                var groupName = currentGroup

                if (itemName.isEmpty() && line.contains(",") && !line.startsWith("http", ignoreCase = true)) {
                    val parts = line.split(",", limit = 2)
                    itemName = parts[0].trim()
                    streamUrl = parts[1].trim()
                    groupName = "Lista TXT"
                } else if (itemName.isEmpty()) {
                    itemName = streamUrl.substringAfterLast('/').substringBefore('?').ifEmpty { "Stream" }
                    groupName = "Lista Direta"
                }

                if (streamUrl.startsWith("http", ignoreCase = true)) {
                    val lowerUrl = streamUrl.lowercase()
                    val lowerGroup = groupName.lowercase()
                    val type = when {
                        lowerUrl.contains("/series/") || lowerGroup.contains("serie") || lowerGroup.contains("série") -> ContentTab.SERIES
                        lowerUrl.contains("/movie/") || lowerUrl.endsWith(".mp4") || lowerUrl.endsWith(".mkv") || lowerUrl.endsWith(".avi") || lowerGroup.contains("filme") || lowerGroup.contains("vod") -> ContentTab.MOVIES
                        else -> ContentTab.LIVE
                    }

                    val item = MediaItem(
                        id = UUID.randomUUID().toString(),
                        name = itemName,
                        categoryId = groupName,
                        categoryName = groupName,
                        streamUrl = streamUrl,
                        logoUrl = currentLogo,
                        contentType = type
                    )

                    when (type) {
                        ContentTab.LIVE -> liveList.add(item)
                        ContentTab.MOVIES -> movieList.add(item)
                        ContentTab.SERIES -> seriesList.add(item)
                    }
                }

                currentName = ""
                currentGroup = "Geral"
                currentLogo = ""
            }
        }

        return ParsedPlaylist(
            liveItems = liveList,
            movieItems = movieList,
            seriesItems = seriesList,
            liveCategories = buildCategories(liveList),
            movieCategories = buildCategories(movieList),
            seriesCategories = buildCategories(seriesList)
        )
    }

    private fun buildCategories(items: List<MediaItem>): List<CategoryItem> {
        val cats = mutableListOf(CategoryItem("ALL", "Todas as Categorias (${items.size})"))
        items.map { it.categoryName }.distinct().forEach { catName ->
            cats.add(CategoryItem(catName, catName))
        }
        return cats
    }
}
