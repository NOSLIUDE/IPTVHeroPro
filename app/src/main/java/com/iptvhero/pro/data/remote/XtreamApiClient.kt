package com.iptvhero.pro.data.remote

import com.iptvhero.pro.data.model.CategoryItem
import com.iptvhero.pro.data.model.ContentTab
import com.iptvhero.pro.data.model.EpisodeItem
import com.iptvhero.pro.data.model.MediaItem
import com.iptvhero.pro.data.model.SeasonItem
import com.iptvhero.pro.data.model.XtreamAuthInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

class XtreamApiClient(private val client: OkHttpClient) {

    private fun normalizeServer(server: String): String {
        var clean = server.trim().removeSuffix("/")
        if (!clean.startsWith("http://") && !clean.startsWith("https://")) {
            clean = "http://$clean"
        }
        return clean
    }

    private suspend fun executeGet(url: String): String = withContext(Dispatchers.IO) {
        val request = Request.Builder()
            .url(url)
            .header("User-Agent", "IPTVHeroPro/1.0")
            .build()
        client.newCall(request).execute().use { response ->
            if (!response.isSuccessful) {
                throw Exception("Erro HTTP ${response.code}: ${response.message}")
            }
            response.body?.string() ?: throw Exception("Resposta vazia do servidor")
        }
    }

    suspend fun downloadRawText(url: String): String = executeGet(url)

    suspend fun authenticate(server: String, username: String, password: String): XtreamAuthInfo {
        val baseUrl = normalizeServer(server)
        val url = "$baseUrl/player_api.php?username=$username&password=$password"
        val jsonStr = executeGet(url)
        val root = JSONObject(jsonStr)
        val userInfo = root.optJSONObject("user_info")
            ?: throw Exception("Credenciais inválidas ou resposta inesperada")

        val auth = userInfo.optInt("auth", 0)
        if (auth == 0) {
            throw Exception("Falha na autenticação Xtream (auth=0)")
        }

        val expStr = userInfo.optString("exp_date", "0")
        val expSeconds = expStr.toLongOrNull() ?: 0L
        val maxConn = userInfo.optString("max_connections", "1").toIntOrNull() ?: 1
        val status = userInfo.optString("status", "Active")

        return XtreamAuthInfo(
            expiryTimestampMs = if (expSeconds > 0) expSeconds * 1000L else 0L,
            maxConnections = maxConn,
            status = status
        )
    }

    suspend fun getCategories(
        server: String,
        username: String,
        password: String,
        tab: ContentTab
    ): List<CategoryItem> {
        val baseUrl = normalizeServer(server)
        val action = when (tab) {
            ContentTab.LIVE -> "get_live_categories"
            ContentTab.MOVIES -> "get_vod_categories"
            ContentTab.SERIES -> "get_series_categories"
        }
        val url = "$baseUrl/player_api.php?username=$username&password=$password&action=$action"
        val jsonStr = executeGet(url)
        val array = JSONArray(jsonStr)
        val result = mutableListOf(CategoryItem("ALL", "Todas as Categorias"))
        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val id = obj.optString("category_id")
            val name = obj.optString("category_name", "Sem Nome")
            if (id.isNotEmpty()) {
                result.add(CategoryItem(id, name))
            }
        }
        return result
    }

    suspend fun getStreams(
        server: String,
        username: String,
        password: String,
        tab: ContentTab,
        categoriesMap: Map<String, String>
    ): List<MediaItem> {
        val baseUrl = normalizeServer(server)
        val action = when (tab) {
            ContentTab.LIVE -> "get_live_streams"
            ContentTab.MOVIES -> "get_vod_streams"
            ContentTab.SERIES -> "get_series"
        }
        val url = "$baseUrl/player_api.php?username=$username&password=$password&action=$action"
        val jsonStr = executeGet(url)
        val array = JSONArray(jsonStr)
        val result = mutableListOf<MediaItem>()

        for (i in 0 until array.length()) {
            val obj = array.getJSONObject(i)
            val catId = obj.optString("category_id", "")
            val catName = categoriesMap[catId] ?: "Geral"
            val name = obj.optString("name", "Sem Título")

            when (tab) {
                ContentTab.LIVE -> {
                    val streamId = obj.optString("stream_id")
                    val icon = obj.optString("stream_icon", "")
                    val streamUrl = "$baseUrl/live/$username/$password/$streamId.m3u8"
                    result.add(
                        MediaItem(
                            id = streamId,
                            name = name,
                            categoryId = catId,
                            categoryName = catName,
                            streamUrl = streamUrl,
                            logoUrl = icon,
                            contentType = ContentTab.LIVE
                        )
                    )
                }
                ContentTab.MOVIES -> {
                    val streamId = obj.optString("stream_id")
                    val ext = obj.optString("container_extension", "mp4").ifEmpty { "mp4" }
                    val icon = obj.optString("stream_icon", "")
                    val streamUrl = "$baseUrl/movie/$username/$password/$streamId.$ext"
                    result.add(
                        MediaItem(
                            id = streamId,
                            name = name,
                            categoryId = catId,
                            categoryName = catName,
                            streamUrl = streamUrl,
                            logoUrl = icon,
                            contentType = ContentTab.MOVIES,
                            containerExtension = ext
                        )
                    )
                }
                ContentTab.SERIES -> {
                    val seriesId = obj.optString("series_id")
                    val cover = obj.optString("cover", "")
                    result.add(
                        MediaItem(
                            id = seriesId,
                            name = name,
                            categoryId = catId,
                            categoryName = catName,
                            streamUrl = "",
                            logoUrl = cover,
                            contentType = ContentTab.SERIES
                        )
                    )
                }
            }
        }
        return result
    }

    suspend fun getSeriesInfo(
        server: String,
        username: String,
        password: String,
        seriesId: String
    ): List<SeasonItem> {
        val baseUrl = normalizeServer(server)
        val url = "$baseUrl/player_api.php?username=$username&password=$password&action=get_series_info&series_id=$seriesId"
        val jsonStr = executeGet(url)
        val root = JSONObject(jsonStr)
        val episodesObj = root.optJSONObject("episodes") ?: return emptyList()

        val seasons = mutableListOf<SeasonItem>()
        val keys = episodesObj.keys()
        while (keys.hasNext()) {
            val seasonKey = keys.next()
            val seasonNum = seasonKey.toIntOrNull() ?: 1
            val epArray = episodesObj.optJSONArray(seasonKey) ?: continue
            val epList = mutableListOf<EpisodeItem>()

            for (i in 0 until epArray.length()) {
                val epObj = epArray.getJSONObject(i)
                val epId = epObj.optString("id")
                val epNum = epObj.optInt("episode_num", i + 1)
                val title = epObj.optString("title", "Episódio $epNum")
                val ext = epObj.optString("container_extension", "mp4").ifEmpty { "mp4" }
                val streamUrl = "$baseUrl/series/$username/$password/$epId.$ext"

                epList.add(
                    EpisodeItem(
                        id = epId,
                        episodeNum = epNum,
                        title = title,
                        streamUrl = streamUrl,
                        seasonNumber = seasonNum
                    )
                )
            }
            if (epList.isNotEmpty()) {
                seasons.add(
                    SeasonItem(
                        seasonNumber = seasonNum,
                        name = "Temporada $seasonNum (${epList.size} eps)",
                        episodes = epList.sortedBy { it.episodeNum }
                    )
                )
            }
        }
        return seasons.sortedBy { it.seasonNumber }
    }
}
