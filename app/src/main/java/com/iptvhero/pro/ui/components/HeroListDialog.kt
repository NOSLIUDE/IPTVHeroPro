package com.iptvhero.pro.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.iptvhero.pro.data.model.BatchLogEntry
import com.iptvhero.pro.ui.theme.*

@Composable
fun HeroListDialog(
    isRunning: Boolean,
    progress: Float,
    logs: List<BatchLogEntry>,
    onStartBatch: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var remoteUrl by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = { if (!isRunning) onDismiss() },
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.8f).fillMaxHeight(0.8f),
            shape = RoundedCornerShape(14.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, HeroBlue)
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "LISTA DE HERÓIS - IMPORTAÇÃO EM LOTE", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                    IconButton(onClick = onDismiss, enabled = !isRunning) {
                        Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextPrimary)
                    }
                }

                Text(
                    text = "Insira a URL de um arquivo remoto (.txt) contendo uma URL de lista M3U ou Xtream (get.php) por linha:",
                    color = TextSecondary,
                    fontSize = 13.sp
                )

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = remoteUrl,
                        onValueChange = { remoteUrl = it },
                        label = { Text("https://exemplo.com/lista_herois.txt") },
                        singleLine = true,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { if (remoteUrl.isNotBlank()) onStartBatch(remoteUrl) },
                        enabled = !isRunning && remoteUrl.isNotBlank(),
                        colors = ButtonDefaults.buttonColors(containerColor = HeroRed)
                    ) {
                        Text(if (isRunning) "Processando..." else "Iniciar Lote")
                    }
                }

                if (isRunning || progress > 0f) {
                    LinearProgressIndicator(
                        progress = { progress },
                        color = HeroBlue,
                        trackColor = DarkSurfaceVariant,
                        modifier = Modifier.fillMaxWidth().height(8.dp)
                    )
                }

                Text("Logs de Execução:", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 13.sp)

                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth().background(DarkBackground, RoundedCornerShape(8.dp)).padding(10.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(logs) { entry ->
                        Text(
                            text = "[${entry.timestamp}] ${entry.message}",
                            color = if (entry.isError) HeroRed else StatusGreen,
                            fontFamily = FontFamily.Monospace,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }
    }
}
