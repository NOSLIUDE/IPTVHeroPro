package com.iptvhero.pro.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptvhero.pro.data.model.ContentTab
import com.iptvhero.pro.ui.components.*
import com.iptvhero.pro.ui.theme.*
import com.iptvhero.pro.ui.viewmodel.MainViewModel

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToFullPlayer: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val profiles by viewModel.profiles.collectAsState()
    val activeProfile by viewModel.activeProfile.collectAsState()

    val categories = viewModel.getFilteredCategories()
    val mediaItems = viewModel.getFilteredItems()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(DarkBackground)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp && event.key == Key.Back && uiState.isFullscreen) {
                    viewModel.setFullscreen(false)
                    true
                } else {
                    false
                }
            }
    ) {
        if (!uiState.isFullscreen) {
            HeaderBar(
                activeHeroName = activeProfile?.heroName ?: "Sem Perfil Ativo",
                proxyActive = uiState.proxyUrl.isNotBlank(),
                isFullscreen = uiState.isFullscreen,
                onToggleFullscreen = { viewModel.toggleFullscreen() },
                onOpenProxy = { viewModel.toggleProxyDialog(true) },
                onOpenProfiles = { viewModel.toggleProfileDialog(true) }
            )
        }

        Row(modifier = Modifier.weight(1f).fillMaxWidth()) {
            if (!uiState.isFullscreen) {
                Column(
                    modifier = Modifier
                        .weight(0.36f)
                        .fillMaxHeight()
                        .background(DarkSurface)
                        .padding(8.dp)
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        ContentTab.entries.forEach { tab ->
                            val selected = uiState.selectedTab == tab
                            var focused by remember { mutableStateOf(false) }
                            Surface(
                                onClick = { viewModel.selectTab(tab) },
                                shape = RoundedCornerShape(8.dp),
                                color = if (selected) HeroRed else DarkSurfaceVariant,
                                border = if (focused) BorderStroke(2.dp, HeroBlue) else null,
                                modifier = Modifier
                                    .weight(1f)
                                    .onFocusChanged { focused = it.isFocused }
                                    .focusable()
                            ) {
                                Row(
                                    modifier = Modifier.padding(vertical = 8.dp, horizontal = 4.dp),
                                    horizontalArrangement = Arrangement.Center,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    val icon = when (tab) {
                                        ContentTab.LIVE -> Icons.Default.LiveTv
                                        ContentTab.MOVIES -> Icons.Default.Movie
                                        ContentTab.SERIES -> Icons.Default.Tv
                                    }
                                    Icon(
                                        imageVector = icon,
                                        contentDescription = null,
                                        tint = Color.White,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text(
                                        text = tab.title,
                                        color = Color.White,
                                        fontSize = 12.sp,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedTextField(
                        value = uiState.searchQuery,
                        onValueChange = { viewModel.updateSearchQuery(it) },
                        placeholder = { Text("Buscar canal...", fontSize = 12.sp) },
                        leadingIcon = { Icon(Icons.Default.Search, contentDescription = null, modifier = Modifier.size(18.dp)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    if (categories.isNotEmpty()) {
                        LazyRow(
                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                            contentPadding = PaddingValues(vertical = 4.dp)
                        ) {
                            items(categories, key = { it.id }) { cat ->
                                val isSelected = uiState.selectedCategoryId == cat.id
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { viewModel.selectCategory(cat.id) },
                                    label = { Text(cat.name, fontSize = 11.sp) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = HeroBlue,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(6.dp))

                    if (uiState.isLoadingContent) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            CircularProgressIndicator(color = HeroRed)
                        }
                    } else if (mediaItems.isEmpty()) {
                        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                            Text(
                                text = "Nenhum item encontrado nesta categoria.",
                                color = TextSecondary,
                                fontSize = 13.sp
                            )
                        }
                    } else {
                        LazyColumn(
                            verticalArrangement = Arrangement.spacedBy(6.dp),
                            modifier = Modifier.fillMaxSize()
                        ) {
                            items(mediaItems, key = { "${it.id}_${it.name}" }) { item ->
                                var isFocused by remember { mutableStateOf(false) }
                                Surface(
                                    onClick = { viewModel.onMediaItemClicked(item) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isFocused) DarkSurfaceVariant else Color(0xFF1B202C),
                                    border = if (isFocused) BorderStroke(2.dp, HeroBlue) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .onFocusChanged { isFocused = it.isFocused }
                                        .focusable()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 10.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(
                                                text = item.name,
                                                color = TextPrimary,
                                                fontSize = 14.sp,
                                                fontWeight = FontWeight.Medium,
                                                maxLines = 1,
                                                overflow = TextOverflow.Ellipsis
                                            )
                                            Text(
                                                text = item.categoryName,
                                                color = TextSecondary,
                                                fontSize = 11.sp,
                                                maxLines = 1
                                            )
                                        }
                                        if (item.contentType == ContentTab.SERIES) {
                                            Text(
                                                text = "EPISÓDIOS",
                                                color = HeroBlue,
                                                fontSize = 10.sp,
                                                fontWeight = FontWeight.Bold
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            Column(
                modifier = Modifier.weight(if (uiState.isFullscreen) 1f else 0.64f).fillMaxHeight()
            ) {
                Box(modifier = Modifier.weight(1f).fillMaxWidth()) {
                    VideoPlayerView(
                        streamUrl = uiState.currentStreamUrl,
                        okHttpClient = viewModel.okHttpClient,
                        onPlayerError = {},
                        modifier = Modifier.fillMaxSize()
                    )

                    if (uiState.isFullscreen) {
                        IconButton(
                            onClick = { viewModel.toggleFullscreen() },
                            modifier = Modifier
                                .align(Alignment.TopEnd)
                                .padding(12.dp)
                                .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                        ) {
                            Icon(
                                imageVector = Icons.Default.FullscreenExit,
                                contentDescription = "Sair da Tela Cheia",
                                tint = Color.White
                            )
                        }
                    }

                    uiState.activeSeriesItem?.let { series ->
                        SeriesOverlayPanel(
                            seriesTitle = series.name,
                            seasons = uiState.seriesSeasons,
                            isLoading = uiState.isLoadingSeries,
                            onPlayEpisode = { episode ->
                                viewModel.playStream(
                                    url = episode.streamUrl,
                                    title = "${series.name} - T${episode.seasonNumber}E${episode.episodeNum}: ${episode.title}"
                                )
                            },
                            onClose = { viewModel.closeSeriesOverlay() }
                        )
                    }
                }

                if (!uiState.isFullscreen) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .background(DarkSurfaceVariant)
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = uiState.statusMessage,
                            color = TextPrimary,
                            fontSize = 12.sp,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis,
                            modifier = Modifier.weight(1f)
                        )
                        if (uiState.currentStreamUrl != null) {
                            Text(
                                text = "ABRIR PLAYER DEDICADO",
                                color = HeroBlue,
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier
                                    .clickable { onNavigateToFullPlayer() }
                                    .padding(start = 12.dp)
                            )
                        }
                    }
                }
            }
        }
    }

    if (uiState.showProfileDialog) {
        ProfileManagerDialog(
            profiles = profiles,
            onActivateProfile = { viewModel.activateProfile(it) },
            onDeleteProfile = { viewModel.deleteProfile(it) },
            onAddProfile = { name, type, srv, usr, pwd ->
                viewModel.addManualProfile(name, type, srv, usr, pwd)
            },
            onOpenHeroBatch = { viewModel.toggleHeroBatchDialog(true) },
            onDismiss = { viewModel.toggleProfileDialog(false) }
        )
    }

    if (uiState.showHeroBatchDialog) {
        HeroListDialog(
            isRunning = uiState.isBatchRunning,
            progress = uiState.batchProgress,
            logs = uiState.batchLogs,
            onStartBatch = { viewModel.processHeroBatchList(it) },
            onDismiss = { viewModel.toggleHeroBatchDialog(false) }
        )
    }

    if (uiState.showProxyDialog) {
        ProxyConfigDialog(
            currentProxy = uiState.proxyUrl,
            onSave = { viewModel.updateProxyUrl(it) },
            onDismiss = { viewModel.toggleProxyDialog(false) }
        )
    }
}

@Composable
private fun HeaderBar(
    activeHeroName: String,
    proxyActive: Boolean,
    isFullscreen: Boolean,
    onToggleFullscreen: () -> Unit,
    onOpenProxy: () -> Unit,
    onOpenProfiles: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(Color(0xFF11141C))
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(text = "IPTV HERO", color = HeroRed, fontSize = 20.sp, fontWeight = FontWeight.Black)
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "PRO",
                color = Color.White,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.background(HeroBlue, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(text = "Perfil: $activeHeroName", color = TextSecondary, fontSize = 13.sp)
        }

        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Button(
                onClick = onOpenProxy,
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (proxyActive) StatusGreen.copy(alpha = 0.2f) else DarkSurfaceVariant
                ),
                border = BorderStroke(1.dp, if (proxyActive) StatusGreen else TextSecondary)
            ) {
                Icon(
                    imageVector = Icons.Default.VpnLock,
                    contentDescription = null,
                    tint = if (proxyActive) StatusGreen else TextSecondary,
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = if (proxyActive) "Proxy: ON" else "Proxy: OFF",
                    color = if (proxyActive) StatusGreen else TextPrimary,
                    fontSize = 12.sp
                )
            }

            Button(
                onClick = onOpenProfiles,
                colors = ButtonDefaults.buttonColors(containerColor = HeroRed)
            ) {
                Icon(Icons.Default.AccountCircle, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Perfis", fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }

            IconButton(onClick = onToggleFullscreen) {
                Icon(
                    imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                    contentDescription = "Tela Cheia",
                    tint = TextPrimary
                )
            }
        }
    }
}

@Composable
private fun ProxyConfigDialog(
    currentProxy: String,
    onSave: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf(currentProxy) }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = DarkSurface,
        title = { Text("Configuração de Proxy HTTP", color = TextPrimary) },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    text = "Todas as chamadas Xtream, downloads M3U e streams de vídeo passarão por este proxy.",
                    color = TextSecondary,
                    fontSize = 13.sp
                )
                OutlinedTextField(
                    value = text,
                    onValueChange = { text = it },
                    label = { Text("https://seu-proxy.com/?url=") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onSave(text) },
                colors = ButtonDefaults.buttonColors(containerColor = HeroBlue)
            ) {
                Text("Salvar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar", color = TextSecondary)
            }
        }
    )
}
