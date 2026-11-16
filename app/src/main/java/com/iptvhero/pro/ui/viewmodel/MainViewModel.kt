package com.iptvhero.pro.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvhero.pro.data.local.ProfileEntity
import com.iptvhero.pro.data.repository.IptvRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient

// Estado da UI consumido pela navegação e telas
data class MainUiState(
    val currentStreamUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null
)

class MainViewModel(
    private val repository: IptvRepository
) : ViewModel() {

    // Gerenciamento de Estado exigido pela UI e AppNavigation
    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    // Cliente HTTP para o Player / Streams
    val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()

    // Perfis do Room
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

    fun setStreamUrl(url: String?) {
        _uiState.update { it.copy(currentStreamUrl = url) }
    }
}
