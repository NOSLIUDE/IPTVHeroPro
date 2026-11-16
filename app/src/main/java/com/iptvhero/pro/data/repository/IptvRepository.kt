package com.iptvhero.pro.data.repository

import com.iptvhero.pro.data.local.ChannelDao
import com.iptvhero.pro.data.local.ProfileDao
import com.iptvhero.pro.data.local.ChannelEntity
import com.iptvhero.pro.data.local.ProfileEntity
import kotlinx.coroutines.flow.Flow

data class ParsedPlaylist(
    val profileName: String,
    val items: List<PlaylistItem>
)

data class PlaylistItem(
    val title: String,
    val url: String
)

object PlaylistParser {
    fun parseBatchTxtFile(fileContent: String): List<ParsedPlaylist> {
        val result = mutableListOf<ParsedPlaylist>()
        val lines = fileContent.lines()
        
        var currentProfileName = "Lista Importada 1"
        var currentItems = mutableListOf<PlaylistItem>()
        var listCounter = 1

        for (rawLine in lines) {
            val line = rawLine.trim()
            if (line.isEmpty()) continue

            if (isPlaylistHeader(line)) {
                if (currentItems.isNotEmpty()) {
                    result.add(ParsedPlaylist(currentProfileName, currentItems.toList()))
                    currentItems.clear()
                }
                currentProfileName = cleanHeaderName(line) ?: "Lista ${++listCounter}"
                continue
            }

            val parsedItem = parseLineToPlaylistItem(line)
            if (parsedItem != null) {
                currentItems.add(parsedItem)
            }
        }

        if (currentItems.isNotEmpty()) {
            result.add(ParsedPlaylist(currentProfileName, currentItems.toList()))
        }

        return result
    }

    private fun isPlaylistHeader(line: String): Boolean {
        return (line.startsWith("[") && line.endsWith("]")) || 
               (line.startsWith("#") && !line.contains("http") && line.length > 3) ||
               line.startsWith("---") || 
               line.startsWith("===")
    }

    private fun cleanHeaderName(line: String): String? {
        return line.removeSurrounding("[", "]")
                   .removePrefix("#")
                   .removePrefix("---")
                   .removeSuffix("---")
                   .trim()
                   .takeIf { it.isNotEmpty() }
    }

    private fun parseLineToPlaylistItem(line: String): PlaylistItem? {
        val parts = line.split(",", limit = 2)
        return if (parts.size == 2) {
            PlaylistItem(title = parts[0].trim(), url = parts[1].trim())
        } else if (line.startsWith("http://") || line.startsWith("https://")) {
            PlaylistItem(title = "Canal Sem Nome", url = line)
        } else {
            null
        }
    }
}

class IptvRepository(
    private val profileDao: ProfileDao,
    private val channelDao: ChannelDao
) {
    val allProfiles: Flow<List<ProfileEntity>> = profileDao.getAllProfiles()

    suspend fun getActiveProfile(): ProfileEntity? = profileDao.getActiveProfile()

    suspend fun setActiveProfile(profileId: Long) {
        profileDao.setActiveProfile(profileId)
    }

    suspend fun importBatchTxt(fileContent: String) {
        val parsedLists = PlaylistParser.parseBatchTxtFile(fileContent)

        for (playlist in parsedLists) {
            val newProfile = ProfileEntity(name = playlist.profileName)
            val profileId = profileDao.insertProfile(newProfile) 

            val channelEntities = playlist.items.map { item ->
                ChannelEntity(
                    profileId = profileId,
                    title = item.title,
                    url = item.url
                )
            }
            channelDao.insertChannels(channelEntities)
        }
    }
}
