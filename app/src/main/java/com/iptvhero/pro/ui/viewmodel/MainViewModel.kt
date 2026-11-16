package com.iptvhero.pro.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvhero.pro.data.local.ProfileEntity
import com.iptvhero.pro.data.repository.IptvRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch

class MainViewModel(
    private val repository: IptvRepository
) : ViewModel() {

    val profiles: Flow<List<ProfileEntity>> = repository.allProfiles

    var activeProfile by mutableStateOf<ProfileEntity?>(null)
        private set

    init {
        loadActiveProfile()
    }

    private fun loadActiveProfile() {
        viewModelScope.launch {
            activeProfile = repository.getActiveProfile()
        }
    }

    fun selectProfile(profileId: Long) {
        viewModelScope.launch {
            repository.setActiveProfile(profileId)
            loadActiveProfile()
        }
    }
}
