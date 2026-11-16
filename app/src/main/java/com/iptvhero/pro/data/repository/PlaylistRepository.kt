package com.iptvhero.pro.data.repository

import com.iptvhero.pro.data.local.ChannelDao
import com.iptvhero.pro.data.local.ProfileDao
import com.iptvhero.pro.data.local.ProfileEntity
import kotlinx.coroutines.flow.Flow

/**
 * Classe mantida para compatibilidade com códigos antigos que referenciam PlaylistRepository,
 * delegando toda a operação diretamente para o IptvRepository unificado.
 */
class PlaylistRepository(
    profileDao: ProfileDao,
    channelDao: ChannelDao
) {
    private val delegate = IptvRepository(profileDao, channelDao)

    val allProfiles: Flow<List<ProfileEntity>> = delegate.allProfiles

    suspend fun getActiveProfile(): ProfileEntity? = delegate.getActiveProfile()

    suspend fun setActiveProfile(profileId: Long) {
        delegate.setActiveProfile(profileId)
    }

    suspend fun importBatchTxt(fileContent: String) {
        delegate.importBatchTxt(fileContent)
    }
}
