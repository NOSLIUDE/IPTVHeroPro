package com.iptvhero.pro.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.iptvhero.pro.data.local.AppDatabase
import com.iptvhero.pro.data.local.ProfileEntity
import com.iptvhero.pro.data.local.SourceType
import com.iptvhero.pro.data.model.BatchLogEntry
import com.iptvhero.pro.data.model.CategoryItem
import com.iptvhero.pro.data.model.ContentTab
import com.iptvhero.pro.data.model.MediaItem
import com.iptvhero.pro.data.model.SeasonItem
import com.iptvhero.pro.data.remote.ParsedPlaylist
import com.iptvhero.pro.data.repository.IptvRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import okhttp3.OkHttpClient
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class MainUiState(
    val selectedTab: ContentTab = ContentTab.LIVE,
    val selectedCategoryId: String = "ALL",
    val searchQuery: String = "",
    val playlist: ParsedPlaylist? = null,
    val isLoadingContent: Boolean = false,
    val statusMessage: String = "Pronto. Selecione ou adicione um perfil.",
    val currentStreamUrl: String? = null,
    val currentMediaTitle: String = "Nenhum canal selecionado",
    val isFullscreen: Boolean = false,
    val proxyUrl: String = "",
    val showProfileDialog: Boolean = false,
    val showHeroBatchDialog: Boolean = false,
    val showProxyDialog: Boolean = false,
    val activeSeriesItem: MediaItem? = null,
    val seriesSeasons: List<SeasonItem> = emptyList(),
    val isLoadingSeries: Boolean = false,
    val isBatchRunning: Boolean = false,
    val batchProgress: Float = 0f,
    val batchLogs: List<BatchLogEntry> = emptyList()
)

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application)
    private val repository = IptvRepository(database.profileDao())

    val okHttpClient: OkHttpClient get() = repository.okHttpClient

    val profiles: StateFlow<List<ProfileEntity>> = repository.allProfiles
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val activeProfile: StateFlow<ProfileEntity?> = repository.activeProfile
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _uiState = MutableStateFlow(MainUiState())
    val uiState: StateFlow<MainUiState> = _uiState.asStateFlow()

    private var lastLoadedProfileId: Long? = null

    init {
        viewModelScope.launch {
            activeProfile.collectLatest { profile ->
                if (profile != null && profile.id != lastLoadedProfileId) {
                    lastLoadedProfileId = profile.id
                    loadContentForProfile(profile)
                } else if (profile == null) {
                    lastLoadedProfileId = null
                    _uiState.update {
                        it.copy(
                            playlist = null,
                            statusMessage = "Nenhum perfil ativo. Abra o Menu de Perfis para adicionar."
                        )
                    }
                }
            }
        }
    }

    fun selectTab(tab: ContentTab) {
        _uiState.update {
            it.copy(
                selectedTab = tab,
                selectedCategoryId = "ALL",
                activeSeriesItem = null
            )
        }
    }

    fun selectCategory(categoryId: String) {
        _uiState.update { it.copy(selectedCategoryId = categoryId) }
    }

    fun updateSearchQuery(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
    }

    fun toggleFullscreen() {
        _uiState.update { it.copy(isFullscreen = !it.isFullscreen) }
    }

    fun setFullscreen(fullscreen: Boolean) {
        _uiState.update { it.copy(isFullscreen = fullscreen) }
    }

    fun toggleProfileDialog(show: Boolean) {
        _uiState.update { it.copy(showProfileDialog = show) }
    }

    fun toggleHeroBatchDialog(show: Boolean) {
        _uiState.update { it.copy(showHeroBatchDialog = show) }
    }

    fun toggleProxyDialog(show: Boolean) {
        _uiState.update { it.copy(showProxyDialog = show) }
    }

    fun updateProxyUrl(url: String) {
        repository.setProxyUrl(url)
        _uiState.update {
            it.copy(
                proxyUrl = url.trim(),
                showProxyDialog = false,
                statusMessage = if (url.isBlank()) "Proxy desativado" else "Proxy ativo: ${url.trim()}"
            )
        }
        activeProfile.value?.let { loadContentForProfile(it) }
    }

    fun activateProfile(profile: ProfileEntity) {
        viewModelScope.launch {
            lastLoadedProfileId = null
            repository.activateProfile(profile.id)
            _uiState.update { it.copy(showProfileDialog = false) }
        }
    }

    fun deleteProfile(profile: ProfileEntity) {
        viewModelScope.launch {
            repository.deleteProfile(profile)
        }
    }

    fun addManualProfile(
        heroName: String,
        sourceType: SourceType,
        server: String,
        username: String,
        password: String
    ) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingContent = true, statusMessage = "Validando novo perfil...") }
            val result = repository.addAndValidateProfile(
                heroName = heroName,
                sourceType = sourceType,
                server = server,
                username = username,
                password = password,
                setActive = true
            )
            result.onSuccess {
                _uiState.update {
                    it.copy(
                        isLoadingContent = false,
                        showProfileDialog = false,
                        statusMessage = "Perfil criado com sucesso!"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoadingContent = false,
                        statusMessage = "Erro ao validar perfil: ${err.message}"
                    )
                }
            }
        }
    }

    fun loadContentForProfile(profile: ProfileEntity) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isLoadingContent = true,
                    statusMessage = "Carregando listas de ${profile.heroName}...",
                    activeSeriesItem = null
                )
            }
            val result = repository.loadProfileContent(profile)
            result.onSuccess { parsed ->
                _uiState.update {
                    it.copy(
                        playlist = parsed,
                        isLoadingContent = false,
                        selectedCategoryId = "ALL",
                        statusMessage = "Carregado: ${parsed.liveItems.size} Ao Vivo, ${parsed.movieItems.size} Filmes, ${parsed.seriesItems.size} Séries"
                    )
                }
            }.onFailure { err ->
                _uiState.update {
                    it.copy(
                        isLoadingContent = false,
                        statusMessage = "Falha ao carregar conteúdo: ${err.message}"
                    )
                }
            }
        }
    }

    fun onMediaItemClicked(item: MediaItem) {
        if (item.contentType == ContentTab.SERIES && item.streamUrl.isEmpty()) {
            val profile = activeProfile.value ?: return
            viewModelScope.launch {
                _uiState.update {
                    it.copy(
                        activeSeriesItem = item,
                        seriesSeasons = emptyList(),
                        isLoadingSeries = true,
                        statusMessage = "Carregando temporadas de ${item.name}..."
                    )
                }
                val res = repository.fetchSeriesSeasons(profile, item.id)
                res.onSuccess { seasons ->
                    _uiState.update {
                        it.copy(
                            seriesSeasons = seasons,
                            isLoadingSeries = false,
                            statusMessage = "${item.name}: ${seasons.size} temporadas encontradas"
                        )
                    }
                }.onFailure { err ->
                    _uiState.update {
                        it.copy(
                            isLoadingSeries = false,
                            statusMessage = "Erro ao carregar episódios: ${err.message}"
                        )
                    }
                }
            }
        } else {
            playStream(item.streamUrl, item.name)
        }
    }

    fun playStream(url: String, title: String) {
        _uiState.update {
            it.copy(
                currentStreamUrl = url,
                currentMediaTitle = title,
                activeSeriesItem = null,
                statusMessage = "Reproduzindo: $title"
            )
        }
    }

    fun closeSeriesOverlay() {
        _uiState.update { it.copy(activeSeriesItem = null) }
    }

    fun processHeroBatchList(remoteListUrl: String) {
        viewModelScope.launch {
            _uiState.update {
                it.copy(
                    isBatchRunning = true,
                    batchProgress = 0f,
                    batchLogs = listOf(createLog("Baixando Lista de Heróis de: $remoteListUrl"))
                )
            }

            val linesResult = repository.downloadRemoteHeroLines(remoteListUrl)
            linesResult.onFailure { err ->
                appendBatchLog("Falha ao baixar arquivo remoto: ${err.message}", isError = true)
                _uiState.update { it.copy(isBatchRunning = false) }
                return@launch
            }

            val urls = linesResult.getOrDefault(emptyList())
            if (urls.isEmpty()) {
                appendBatchLog("Nenhuma URL válida encontrada no arquivo.", isError = true)
                _uiState.update { it.copy(isBatchRunning = false) }
                return@launch
            }

            appendBatchLog("${urls.size} fontes encontradas. Iniciando importação em lote...")

            urls.forEachIndexed { index, url ->
                val (type, server, creds) = repository.parseLineToProfileParams(url, index)
                val heroName = "Herói #${index + 1} (${type.name})"
                val res = repository.addAndValidateProfile(
                    heroName = heroName,
                    sourceType = type,
                    server = server,
                    username = creds.first,
                    password = creds.second,
                    setActive = (index == 0)
                )
                res.onSuccess { created ->
                    appendBatchLog("✔ $heroName importado (${created.getFormattedExpiry()})")
                }.onFailure { err ->
                    appendBatchLog("✖ Falha em $heroName: ${err.message}", isError = true)
                }

            
                val progress = (index + 1).toFloat() / urls.count().toFloat()

                _uiState.update { it.copy(batchProgress = progress) }
            }

            appendBatchLog("Processamento em lote concluído!")
            _uiState.update { it.copy(isBatchRunning = false) }
        }
    }

    private fun appendBatchLog(msg: String, isError: Boolean = false) {
        _uiState.update {
            it.copy(batchLogs = it.batchLogs + createLog(msg, isError))
        }
    }

    private fun createLog(msg: String, isError: Boolean = false): BatchLogEntry {
        val time = SimpleDateFormat("HH:mm:ss", Locale.getDefault()).format(Date())
        return BatchLogEntry(time, msg, isError)
    }

    fun getFilteredCategories(): List<CategoryItem> {
        val playlist = _uiState.value.playlist ?: return emptyList()
        return when (_uiState.value.selectedTab) {
            ContentTab.LIVE -> playlist.liveCategories
            ContentTab.MOVIES -> playlist.movieCategories
            ContentTab.SERIES -> playlist.seriesCategories
        }
    }

    fun getFilteredItems(): List<MediaItem> {
        val state = _uiState.value
        val playlist = state.playlist ?: return emptyList()
        val baseList = when (state.selectedTab) {
            ContentTab.LIVE -> playlist.liveItems
            ContentTab.MOVIES -> playlist.movieItems
            ContentTab.SERIES -> playlist.seriesItems
        }
        return baseList.filter { item ->
            val matchCat = state.selectedCategoryId == "ALL" || item.categoryId == state.selectedCategoryId
            val matchQuery = state.searchQuery.isBlank() || item.name.contains(state.searchQuery, ignoreCase = true)
            matchCat && matchQuery
        }
    }
}
