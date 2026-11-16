package com.iptvhero.pro.data.repository

import android.net.Uri
import com.iptvhero.pro.data.local.ProfileDao
import com.iptvhero.pro.data.local.ProfileEntity
import com.iptvhero.pro.data.local.SourceType
import com.iptvhero.pro.data.model.ContentTab
import com.iptvhero.pro.data.model.SeasonItem
import com.iptvhero.pro.data.remote.M3uParser
import com.iptvhero.pro.data.remote.ParsedPlaylist
import com.iptvhero.pro.data.remote.ProxyInterceptor
import com.iptvhero.pro.data.remote.XtreamApiClient
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import okhttp3.OkHttpClient
import java.util.concurrent.TimeUnit

class IptvRepository(private val profileDao: ProfileDao) {

    val proxyInterceptor = ProxyInterceptor()

    val okHttpClient: OkHttpClient = OkHttpClient.Builder()
        .addInterceptor(proxyInterceptor)
        .connectTimeout(20, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .followRedirects(true)
        .build()

    private val xtreamClient = XtreamApiClient(okHttpClient)

    val allProfiles: Flow<List<ProfileEntity>> = profileDao.getAllProfiles()
    val activeProfile: Flow<ProfileEntity?> = profileDao.getActiveProfile()

    fun setProxyUrl(url: String) {
        proxyInterceptor.proxyPrefix = url
    }

    suspend fun activateProfile(profileId: Long) {
        profileDao.setActiveProfile(profileId)
    }

    suspend fun deleteProfile(profile: ProfileEntity) {
        profileDao.deleteProfile(profile)
    }

    suspend fun addAndValidateProfile(
        heroName: String,
        sourceType: SourceType,
        server: String,
        username: String,
        password: String,
        setActive: Boolean = true
    ): Result<ProfileEntity> = runCatching {
        var expiry = 0L
        var maxConn = 1
        var liveCount = 0
        var vodCount = 0
        var seriesCount = 0

        if (sourceType == SourceType.XTREAM) {
            val authInfo = xtreamClient.authenticate(server, username, password)
            expiry = authInfo.expiryTimestampMs
            maxConn = authInfo.maxConnections
        } else {
            val raw = xtreamClient.downloadRawText(server)
            val parsed = M3uParser.parse(raw)
            liveCount = parsed.liveItems.size
            vodCount = parsed.movieItems.size
            seriesCount = parsed.seriesItems.size
        }

        val entity = ProfileEntity(
            heroName = heroName.ifBlank { "Herói #${System.currentTimeMillis() % 1000}" },
            sourceType = sourceType.name,
            server = server.trim(),
            username = username.trim(),
            password = password.trim(),
            expiryDate = expiry,
            maxConnections = maxConn,
            liveCount = liveCount,
            vodCount = vodCount,
            seriesCount = seriesCount,
            isActive = setActive
        )

        val newId = profileDao.insertProfile(entity)
        if (setActive) {
            profileDao.setActiveProfile(newId)
        }
        entity.copy(id = newId)
    }

    suspend fun loadProfileContent(profile: ProfileEntity): Result<ParsedPlaylist> = runCatching {
        if (profile.sourceType == SourceType.XTREAM.name) {
            coroutineScope {
                val liveCatDef = async { xtreamClient.getCategories(profile.server, profile.username, profile.password, ContentTab.LIVE) }
                val vodCatDef = async { xtreamClient.getCategories(profile.server, profile.username, profile.password, ContentTab.MOVIES) }
                val serCatDef = async { xtreamClient.getCategories(profile.server, profile.username, profile.password, ContentTab.SERIES) }

                val liveCats = liveCatDef.await()
                val vodCats = vodCatDef.await()
                val serCats = serCatDef.await()

                val liveMap = liveCats.associate { it.id to it.name }
                val vodMap = vodCats.associate { it.id to it.name }
                val serMap = serCats.associate { it.id to it.name }

                val liveStreamsDef = async { xtreamClient.getStreams(profile.server, profile.username, profile.password, ContentTab.LIVE, liveMap) }
                val vodStreamsDef = async { xtreamClient.getStreams(profile.server, profile.username, profile.password, ContentTab.MOVIES, vodMap) }
                val serStreamsDef = async { xtreamClient.getStreams(profile.server, profile.username, profile.password, ContentTab.SERIES, serMap) }

                val liveItems = liveStreamsDef.await()
                val vodItems = vodStreamsDef.await()
                val serItems = serStreamsDef.await()

                profileDao.updateCounts(profile.id, liveItems.size, vodItems.size, serItems.size)

                ParsedPlaylist(
                    liveItems = liveItems,
                    movieItems = vodItems,
                    seriesItems = serItems,
                    liveCategories = liveCats,
                    movieCategories = vodCats,
                    seriesCategories = serCats
                )
            }
        } else {
            val raw = xtreamClient.downloadRawText(profile.server)
            val parsed = M3uParser.parse(raw)
            profileDao.updateCounts(
                profile.id,
                parsed.liveItems.size,
                parsed.movieItems.size,
                parsed.seriesItems.size
            )
            parsed
        }
    }

    suspend fun fetchSeriesSeasons(profile: ProfileEntity, seriesId: String): Result<List<SeasonItem>> = runCatching {
        if (profile.sourceType == SourceType.XTREAM.name) {
            xtreamClient.getSeriesInfo(profile.server, profile.username, profile.password, seriesId)
        } else {
            emptyList()
        }
    }

    suspend fun downloadRemoteHeroLines(listUrl: String): Result<List<String>> = runCatching {
        val raw = xtreamClient.downloadRawText(listUrl.trim())
        raw.lines()
            .map { it.trim() }
            .filter { it.isNotEmpty() && !it.startsWith("#") && it.startsWith("http", ignoreCase = true) }
    }

    fun parseLineToProfileParams(lineUrl: String, index: Int): Triple<SourceType, String, Pair<String, String>> {
        val uri = Uri.parse(lineUrl)
        val user = uri.getQueryParameter("username")
        val pass = uri.getQueryParameter("password")
        return if (!user.isNullOrEmpty() && !pass.isNullOrEmpty()) {
            val baseServer = "${uri.scheme}://${uri.authority}"
            Triple(SourceType.XTREAM, baseServer, user to pass)
        } else {
            val type = if (lineUrl.lowercase().endsWith(".txt")) SourceType.TXT else SourceType.M3U
            Triple(type, lineUrl, "" to "")
        }
    }
}
