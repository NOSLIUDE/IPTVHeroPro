package com.iptvhero.pro.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.iptvhero.pro.data.repository.PlaylistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val repository: PlaylistRepository
) : ViewModel() {

    var isLoading by mutableStateOf(false)
        private set

    var importMessage by mutableStateOf<String?>(null)
        private set

    fun importBatchFile(fileContent: String) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            try {
                repository.importBatchTxt(fileContent)
                importMessage = "Listas importadas e perfis criados com sucesso!"
            } catch (e: Exception) {
                importMessage = "Erro ao importar: ${e.localizedMessage ?: "Erro desconhecido"}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearMessage() {
        importMessage = null
    }
}
