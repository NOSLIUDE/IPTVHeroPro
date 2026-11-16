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

data class MainUiState(
    val currentStreamUrl: String? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val successMessage: String? = null
)

class MainViewModel(
    private val repository: IptvRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    val okHttpClient: OkHttpClient = OkHttpClient.Builder().build()

    val profiles: Flow<List<ProfileEntity>> = repository.allProfiles

    var activeProfile by mutableStateOf<ProfileEntity?>(null)
        private set

    init {
        loadActiveProfile()
    }

    fun loadActiveProfile() {
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

    // FUNÇÃO RESPONSÁVEL POR EXECUTAR O PARSER E SALVAR NO ROOM
    fun importBatchTxt(fileContent: String) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, errorMessage = null) }
            try {
                repository.importBatchTxt(fileContent)
                loadActiveProfile() // Atualiza o perfil ativo após importar
                _uiState.update { it.copy(isLoading = false, successMessage = "Lista importada com sucesso!") }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, errorMessage = "Erro ao importar: ${e.localizedMessage}") }
            }
        }
    }

    fun setStreamUrl(url: String?) {
        _uiState.update { it.copy(currentStreamUrl = url) }
    }
}
