package com.iptvhero.pro.ui.navigation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.iptvhero.pro.ui.components.VideoPlayerView
import com.iptvhero.pro.ui.screens.MainScreen
import com.iptvhero.pro.ui.viewmodel.MainViewModel

object Routes {
    const val CHANNEL_LIST = "channel_list"
    const val PLAYER = "player"
}

@Composable
fun AppNavigation(viewModel: MainViewModel) {
    val navController = rememberNavController()
    val uiState by viewModel.uiState.collectAsState()

    NavHost(
        navController = navController,
        startDestination = Routes.CHANNEL_LIST
    ) {
        composable(Routes.CHANNEL_LIST) {
            MainScreen(
                viewModel = viewModel,
                onNavigateToFullPlayer = {
                    navController.navigate(Routes.PLAYER)
                }
            )
        }

        composable(Routes.PLAYER) {
            Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
                VideoPlayerView(
                    streamUrl = uiState.currentStreamUrl,
                    okHttpClient = viewModel.okHttpClient,
                    onPlayerError = {},
                    modifier = Modifier.fillMaxSize()
                )
                IconButton(
                    onClick = { navController.popBackStack() },
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(16.dp)
                        .background(Color.Black.copy(alpha = 0.6f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar para lista",
                        tint = Color.White
                    )
                }
            }
        }
    }
}
