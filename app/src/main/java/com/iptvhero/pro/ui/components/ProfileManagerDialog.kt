package com.iptvhero.pro.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.GroupAdd
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.iptvhero.pro.data.local.ExpiryStatus
import com.iptvhero.pro.data.local.ProfileEntity
import com.iptvhero.pro.data.local.SourceType
import com.iptvhero.pro.ui.theme.*

@Composable
fun ProfileManagerDialog(
    profiles: List<ProfileEntity>,
    onActivateProfile: (ProfileEntity) -> Unit,
    onDeleteProfile: (ProfileEntity) -> Unit,
    onAddProfile: (String, SourceType, String, String, String) -> Unit,
    onOpenHeroBatch: () -> Unit,
    onDismiss: () -> Unit
) {
    var heroName by remember { mutableStateOf("") }
    var selectedType by remember { mutableStateOf(SourceType.XTREAM) }
    var server by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxWidth(0.92f).fillMaxHeight(0.88f),
            shape = RoundedCornerShape(14.dp),
            color = DarkSurface,
            border = BorderStroke(1.dp, DarkSurfaceVariant)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "GERENCIAMENTO DE PERFIS (HERÓIS)", color = TextPrimary, fontSize = 18.sp, fontWeight = FontWeight.Bold)
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Button(
                            onClick = {
                                onDismiss()
                                onOpenHeroBatch()
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HeroBlue)
                        ) {
                            Icon(Icons.Default.GroupAdd, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lista de Heróis (Lote)")
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Fechar", tint = TextPrimary)
                        }
                    }
                }

                HorizontalDivider(modifier = Modifier.padding(vertical = 8.dp), color = DarkSurfaceVariant)

                Row(modifier = Modifier.fillMaxSize()) {
                    Column(
                        modifier = Modifier.weight(0.42f).padding(end = 12.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text("Adicionar Novo Perfil", color = HeroRed, fontWeight = FontWeight.Bold)

                        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                            SourceType.entries.forEach { type ->
                                FilterChip(
                                    selected = selectedType == type,
                                    onClick = { selectedType = type },
                                    label = { Text(type.name) },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = HeroRed,
                                        selectedLabelColor = Color.White
                                    )
                                )
                            }
                        }

                        OutlinedTextField(
                            value = heroName,
                            onValueChange = { heroName = it },
                            label = { Text("Nome do Herói") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        OutlinedTextField(
                            value = server,
                            onValueChange = { server = it },
                            label = {
                                Text(
                                    if (selectedType == SourceType.XTREAM) "Servidor (http://dominio:porta)"
                                    else "URL da Lista M3U / TXT"
                                )
                            },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )

                        if (selectedType == SourceType.XTREAM) {
                            OutlinedTextField(
                                value = username,
                                onValueChange = { username = it },
                                label = { Text("Usuário") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                            OutlinedTextField(
                                value = password,
                                onValueChange = { password = it },
                                label = { Text("Senha") },
                                singleLine = true,
                                modifier = Modifier.fillMaxWidth()
                            )
                        }

                        Button(
                            onClick = {
                                if (server.isNotBlank()) {
                                    onAddProfile(heroName, selectedType, server, username, password)
                                }
                            },
                            colors = ButtonDefaults.buttonColors(containerColor = HeroRed),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Salvar e Ativar Perfil", fontWeight = FontWeight.Bold)
                        }
                    }

                    Column(
                        modifier = Modifier.weight(0.58f).padding(start = 12.dp)
                    ) {
                        Text(
                            text = "Perfis Cadastrados (${profiles.size})",
                            color = TextSecondary,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(bottom = 8.dp)
                        )

                        if (profiles.isEmpty()) {
                            Text("Nenhum perfil salvo.", color = TextSecondary)
                        } else {
                            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                items(profiles, key = { it.id }) { profile ->
                                    val status = profile.getExpiryStatus()
                                    val statusColor = when (status) {
                                        ExpiryStatus.VALID, ExpiryStatus.UNLIMITED -> StatusGreen
                                        ExpiryStatus.EXPIRING_SOON -> StatusAmber
                                        ExpiryStatus.EXPIRED -> HeroRed
                                    }
                                    val statusLabel = when (status) {
                                        ExpiryStatus.VALID -> "VÁLIDO"
                                        ExpiryStatus.UNLIMITED -> "ATIVO"
                                        ExpiryStatus.EXPIRING_SOON -> "EXPIRA EM BREVE"
                                        ExpiryStatus.EXPIRED -> "EXPIRADO"
                                    }

                                    Surface(
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (profile.isActive) Color(0xFF1F283B) else Color(0xFF1B1F2A),
                                        border = if (profile.isActive) BorderStroke(1.5.dp, HeroBlue) else null,
                                        modifier = Modifier.fillMaxWidth()
                                    ) {
                                        Row(
                                            modifier = Modifier.fillMaxWidth().padding(12.dp),
                                            verticalAlignment = Alignment.CenterVertically,
                                            horizontalArrangement = Arrangement.SpaceBetween
                                        ) {
                                            Column(modifier = Modifier.weight(1f)) {
                                                Row(verticalAlignment = Alignment.CenterVertically) {
                                                    Text(text = profile.heroName, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                                                    Spacer(modifier = Modifier.width(8.dp))
                                                    Text(
                                                        text = statusLabel,
                                                        color = Color.Black,
                                                        fontSize = 10.sp,
                                                        fontWeight = FontWeight.Bold,
                                                        modifier = Modifier.background(statusColor, RoundedCornerShape(4.dp)).padding(horizontal = 6.dp, vertical = 2.dp)
                                                    )
                                                }
                                                Text(
                                                    text = "Tipo: ${profile.sourceType} | Validade: ${profile.getFormattedExpiry()} | Max Conn: ${profile.maxConnections}",
                                                    color = TextSecondary,
                                                    fontSize = 12.sp
                                                )
                                                Text(
                                                    text = profile.getContentCountsSummary(),
                                                    color = HeroBlue,
                                                    fontSize = 12.sp,
                                                    fontWeight = FontWeight.Medium
                                                )
                                            }

                                            Row(verticalAlignment = Alignment.CenterVertically) {
                                                Button(
                                                    onClick = { onActivateProfile(profile) },
                                                    colors = ButtonDefaults.buttonColors(containerColor = if (profile.isActive) StatusGreen else HeroBlue)
                                                ) {
                                                    Icon(Icons.Default.CheckCircle, contentDescription = null)
                                                    Spacer(modifier = Modifier.width(4.dp))
                                                    Text(if (profile.isActive) "Ativo" else "Ativar")
                                                }
                                                Spacer(modifier = Modifier.width(6.dp))
                                                IconButton(onClick = { onDeleteProfile(profile) }) {
                                                    Icon(imageVector = Icons.Default.Delete, contentDescription = "Excluir", tint = HeroRed)
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
    }
}
