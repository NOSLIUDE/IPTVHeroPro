package com.iptvhero.pro.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.iptvhero.pro.ui.components.ProfileManagerDialog
import com.iptvhero.pro.ui.viewmodel.MainViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    viewModel: MainViewModel,
    onNavigateToFullPlayer: () -> Unit = {}
) {
    val profiles by viewModel.profiles.collectAsState(initial = emptyList())
    val activeProfile = viewModel.activeProfile
    
    val displayTitle = activeProfile?.name ?: "IPTV Hero Pro"
    var showProfileDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(displayTitle) },
                actions = {
                    TextButton(onClick = { showProfileDialog = true }) {
                        Text("Perfis")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Text(
                    text = if (activeProfile != null) 
                        "Perfil ativo: ${activeProfile?.name}" 
                    else 
                        "Nenhum perfil selecionado",
                    style = MaterialTheme.typography.bodyLarge
                )
                
                Button(onClick = { showProfileDialog = true }) {
                    Text("Gerenciar / Importar Listas")
                }

                Button(onClick = onNavigateToFullPlayer) {
                    Text("Abrir Player de Vídeo")
                }
            }
        }
    }

    if (showProfileDialog) {
        ProfileManagerDialog(
            profiles = profiles,
            activeProfileId = activeProfile?.id,
            onDismiss = { showProfileDialog = false },
            onActivateProfile = { profileId ->
                viewModel.selectProfile(profileId)
            },
            onDeleteProfile = { profileId ->
                // Ação de exclusão se necessário
            },
            onImportContent = { txtContent ->
                viewModel.importBatchTxt(txtContent)
                showProfileDialog = false
            }
        )
    }
}
