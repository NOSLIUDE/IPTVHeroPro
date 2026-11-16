package com.example.iptvheropro.ui.viewmodel

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.iptvheropro.data.repository.PlaylistRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class PlaylistViewModel(
    private val repository: PlaylistRepository
) : ViewModel() {

    // Estado reativo para controlar o indicador de progresso na UI
    var isLoading by mutableStateOf(false)
        private set

    // Estado reativo para disparar mensagens de sucesso ou erro (Toast)
    var importMessage by mutableStateOf<String?>(null)
        private set

    /**
     * Recebe o conteúdo bruto do arquivo TXT e dispara o parsing e salvamento no Room.
     */
    fun importBatchFile(fileContent: String) {
        viewModelScope.launch(Dispatchers.IO) {
            isLoading = true
            try {
                // Chama a função do repositório que faz o parse e insere os perfis no Room
                repository.importBatchTxt(fileContent)
                importMessage = "Listas importadas e perfis criados com sucesso!"
            } catch (e: Exception) {
                importMessage = "Erro ao importar: ${e.localizedMessage ?: "Erro desconhecido"}"
            } finally {
                isLoading = false
            }
        }
    }

    /**
     * Limpa a mensagem de feedback para evitar disparos repetidos no Toast.
     */
    fun clearMessage() {
        importMessage = null
    }
}
