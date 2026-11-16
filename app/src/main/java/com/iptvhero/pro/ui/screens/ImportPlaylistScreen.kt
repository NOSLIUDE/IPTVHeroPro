package com.iptvhero.pro.ui.screens

import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.iptvhero.pro.ui.viewmodel.PlaylistViewModel

@Composable
fun ImportPlaylistScreen(
    viewModel: PlaylistViewModel
) {
    val context = LocalContext.current
    val isLoading = viewModel.isLoading
    val importMessage = viewModel.importMessage

    LaunchedEffect(importMessage) {
        importMessage?.let { message ->
            Toast.makeText(context, message, Toast.LENGTH_LONG).show()
            viewModel.clearMessage()
        }
    }

    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let { selectedUri ->
            try {
                context.contentResolver.openInputStream(selectedUri)?.use { inputStream ->
                    val fileContent = inputStream.bufferedReader().readText()
                    viewModel.importBatchFile(fileContent)
                }
            } catch (e: Exception) {
                Toast.makeText(context, "Erro ao abrir o arquivo: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        if (isLoading) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                CircularProgressIndicator()
                Text(
                    text = "Processando lotes e criando perfis...",
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        } else {
            Button(
                onClick = { filePickerLauncher.launch("text/plain") },
                modifier = Modifier.fillMaxWidth(0.8f)
            ) {
                Text(text = "Importar Lista TXT em Lote")
            }
        }
    }
}
