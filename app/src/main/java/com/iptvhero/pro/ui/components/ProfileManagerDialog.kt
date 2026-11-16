package com.iptvhero.pro.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iptvhero.pro.data.local.ProfileEntity
import com.iptvhero.pro.data.local.SourceType

@Composable
fun ProfileManagerDialog(
    profiles: List<ProfileEntity> = emptyList(),
    activeProfileId: Long? = null,
    onDismiss: () -> Unit,
    onActivateProfile: (Long) -> Unit,
    onDeleteProfile: (Long) -> Unit,
    onImportContent: (String) -> Unit // Alinhado para receber o conteúdo bruto do TXT
) {
    var showAddSection by remember { mutableStateOf(false) }
    var rawTxtContent by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Gerenciar Perfis e Listas") },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(max = 400.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (!showAddSection) {
                    Button(
                        onClick = { showAddSection = true },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Importar Nova Lista (TXT)")
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Perfis Salvos no Banco:", style = MaterialTheme.typography.titleSmall)

                    if (profiles.isEmpty()) {
                        Text("Nenhum perfil cadastrado.", style = MaterialTheme.typography.bodyMedium)
                    } else {
                        LazyColumn(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f, fill = false),
                            verticalArrangement = Arrangement.spacedBy(4.dp)
                        ) {
                            items(profiles) { profile ->
                                Card(
                                    modifier = Modifier.fillMaxWidth(),
                                    colors = CardDefaults.cardColors(
                                        containerColor = if (profile.id == activeProfileId)
                                            MaterialTheme.colorScheme.primaryContainer
                                        else
                                            MaterialTheme.colorScheme.surfaceVariant
                                    )
                                ) {
                                    Row(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(8.dp),
                                        horizontalArrangement = Arrangement.SpaceBetween,
                                        verticalAlignment = Alignment.CenterVertically
                                    ) {
                                        Column(modifier = Modifier.weight(1f)) {
                                            Text(text = profile.name, style = MaterialTheme.typography.bodyLarge)
                                        }
                                        Row {
                                            if (profile.id != activeProfileId) {
                                                TextButton(onClick = { onActivateProfile(profile.id) }) {
                                                    Text("Ativar")
                                                }
                                            }
                                            TextButton(onClick = { onDeleteProfile(profile.id) }) {
                                                Text("Excluir", color = MaterialTheme.colorScheme.error)
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                } else {
                    Text("Cole o conteúdo da lista TXT abaixo:", style = MaterialTheme.typography.titleSmall)
                    OutlinedTextField(
                        value = rawTxtContent,
                        onValueChange = { rawTxtContent = it },
                        label = { Text("Conteúdo do arquivo .txt / M3U") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp),
                        maxLines = 10
                    )
                }
            }
        },
        confirmButton = {
            if (showAddSection) {
                Button(onClick = {
                    if (rawTxtContent.isNotBlank()) {
                        onImportContent(rawTxtContent) // Executa o parser e salva no Room
                        showAddSection = false
                        rawTxtContent = ""
                    }
                }) {
                    Text("Processar e Salvar")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Fechar")
                }
            }
        },
        dismissButton = {
            if (showAddSection) {
                TextButton(onClick = { showAddSection = false }) {
                    Text("Voltar")
                }
            } else {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
            }
        }
    )
}
