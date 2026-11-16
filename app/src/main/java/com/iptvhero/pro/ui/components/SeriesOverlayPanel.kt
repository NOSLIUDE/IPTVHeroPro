package com.iptvhero.pro.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.key.*
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.iptvhero.pro.data.model.EpisodeItem
import com.iptvhero.pro.data.model.SeasonItem
import com.iptvhero.pro.ui.theme.*

@Composable
fun SeriesOverlayPanel(
    seriesTitle: String,
    seasons: List<SeasonItem>,
    isLoading: Boolean,
    onPlayEpisode: (EpisodeItem) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedSeasonIndex by remember(seasons) { mutableIntStateOf(0) }
    val firstSeasonFocus = remember { FocusRequester() }
    val firstEpisodeFocus = remember { FocusRequester() }

    LaunchedEffect(seasons) {
        if (seasons.isNotEmpty()) {
            runCatching { firstSeasonFocus.requestFocus() }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.88f))
            .padding(16.dp)
            .onKeyEvent { event ->
                if (event.type == KeyEventType.KeyUp && (event.key == Key.Back || event.key == Key.Escape)) {
                    onClose()
                    true
                } else {
                    false
                }
            }
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(DarkSurface, RoundedCornerShape(12.dp))
                .border(1.dp, HeroBlue.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
                .padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(text = "SÉRIE SELECIONADA", color = HeroRed, fontSize = 11.sp, fontWeight = FontWeight.Bold)
                    Text(text = seriesTitle, color = TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold, maxLines = 1, overflow = TextOverflow.Ellipsis)
                }
                IconButton(onClick = onClose) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Fechar painel", tint = TextPrimary)
                }
            }

            HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DarkSurfaceVariant)

            if (isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = HeroRed)
                }
            } else if (seasons.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Nenhuma temporada disponível para esta série.", color = TextSecondary)
                }
            } else {
                Row(modifier = Modifier.fillMaxSize()) {
                    Column(modifier = Modifier.weight(0.35f).fillMaxHeight().padding(end = 8.dp)) {
                        Text(text = "TEMPORADAS", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxSize()) {
                            items(seasons.size) { index ->
                                val season = seasons[index]
                                var isFocused by remember { mutableStateOf(false) }
                                val isSelected = index == selectedSeasonIndex

                                Surface(
                                    onClick = {
                                        selectedSeasonIndex = index
                                        runCatching { firstEpisodeFocus.requestFocus() }
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    color = when {
                                        isSelected -> HeroRed
                                        isFocused -> DarkSurfaceVariant
                                        else -> Color(0xFF1B202C)
                                    },
                                    border = if (isFocused) BorderStroke(2.dp, HeroBlue) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(if (index == 0) Modifier.focusRequester(firstSeasonFocus) else Modifier)
                                        .onFocusChanged {
                                            isFocused = it.isFocused
                                            if (it.isFocused) selectedSeasonIndex = index
                                        }
                                        .onKeyEvent { event ->
                                            if (event.type == KeyEventType.KeyDown && event.key == Key.DirectionRight) {
                                                runCatching { firstEpisodeFocus.requestFocus() }
                                                true
                                            } else {
                                                false
                                            }
                                        }
                                        .focusable()
                                ) {
                                    Text(
                                        text = season.name,
                                        color = TextPrimary,
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                        fontSize = 14.sp,
                                        modifier = Modifier.padding(12.dp)
                                    )
                                }
                            }
                        }
                    }

                    Box(modifier = Modifier.width(1.dp).fillMaxHeight().background(DarkSurfaceVariant))

                    Column(modifier = Modifier.weight(0.65f).fillMaxHeight().padding(start = 12.dp)) {
                        val currentEpisodes = seasons.getOrNull(selectedSeasonIndex)?.episodes ?: emptyList()
                        Text(text = "EPISÓDIOS (${currentEpisodes.size})", color = TextSecondary, fontSize = 12.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(bottom = 8.dp))
                        LazyColumn(verticalArrangement = Arrangement.spacedBy(6.dp), modifier = Modifier.fillMaxSize()) {
                            items(currentEpisodes.size) { epIdx ->
                                val episode = currentEpisodes[epIdx]
                                var isFocused by remember { mutableStateOf(false) }

                                Surface(
                                    onClick = { onPlayEpisode(episode) },
                                    shape = RoundedCornerShape(8.dp),
                                    color = if (isFocused) DarkSurfaceVariant else Color(0xFF1B202C),
                                    border = if (isFocused) BorderStroke(2.dp, HeroBlue) else null,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .then(if (epIdx == 0) Modifier.focusRequester(firstEpisodeFocus) else Modifier)
                                        .onFocusChanged { isFocused = it.isFocused }
                                        .onKeyEvent { event ->
                                            if (event.type == KeyEventType.KeyDown && event.key == Key.DirectionLeft) {
                                                runCatching { firstSeasonFocus.requestFocus() }
                                                true
                                            } else {
                                                false
                                            }
                                        }
                                        .focusable()
                                ) {
                                    Row(
                                        modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 10.dp),
                                        verticalAlignment = Alignment.CenterVertically,
                                        horizontalArrangement = Arrangement.SpaceBetween
                                    ) {
                                        Text(
                                            text = "E${episode.episodeNum.toString().padStart(2, '0')} - ${episode.title}",
                                            color = TextPrimary,
                                            fontSize = 14.sp,
                                            maxLines = 1,
                                            overflow = TextOverflow.Ellipsis,
                                            modifier = Modifier.weight(1f)
                                        )
                                        Button(
                                            onClick = { onPlayEpisode(episode) },
                                            colors = ButtonDefaults.buttonColors(containerColor = HeroBlue),
                                            modifier = Modifier.height(32.dp)
                                        ) {
                                            Icon(imageVector = Icons.Default.PlayArrow, contentDescription = null, modifier = Modifier.width(16.dp))
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text("Assistir", fontSize = 12.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
