     package com.iptvhero.pro.ui.components

import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import com.iptvhero.pro.data.local.SourceType // <--- IMPORT OBRIGATÓRIO PARA RESOLVER O ERRO

@Composable
fun ProfileManagerDialog(
    onDismiss: () -> Unit,
    onAddProfile: (name: String, url: String, sourceType: SourceType) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var url by remember { mutableStateOf("") }
    var sourceType by remember { mutableStateOf(SourceType.URL) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Adicionar Novo Perfil") },
        text = {
            Column(modifier = Modifier.fillMaxWidth().padding(8.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nome da Lista") },
                    modifier = Modifier.fillMaxWidth()
                )
                OutlinedTextField(
                    value = url,
                    onValueChange = { url = it },
                    label = { Text("URL ou Caminho") },
                    modifier = Modifier.fillMaxWidth()
                )
            }
        },
        confirmButton = {
            Button(onClick = {
                if (name.isNotBlank()) {
                    onAddProfile(name, url, sourceType)
                    onDismiss()
                }
            }) {
                Text("Adicionar")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancelar")
            }
        }
    )
}
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
