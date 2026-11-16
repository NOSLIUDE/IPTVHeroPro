package com.iptvhero.pro

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.lifecycle.viewmodel.compose.viewModel
import com.iptvhero.pro.data.local.AppDatabase // Ajuste para o pacote real do seu banco
import com.iptvhero.pro.data.repository.IptvRepository
import com.iptvhero.pro.ui.navigation.AppNavigation
import com.iptvhero.pro.ui.viewmodel.MainViewModel
import com.iptvhero.pro.ui.viewmodel.MainViewModelFactory
import com.iptvhero.pro.ui.theme.IPTVHeroProTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 1. Inicializa o banco de dados Room com segurança
        val database = AppDatabase.getDatabase(applicationContext)
        
        // 2. Instancia o repositório injetando os DAOs
        val repository = IptvRepository(
            profileDao = database.profileDao(),
            channelDao = database.channelDao()
        )
        
        // 3. Cria a Factory passando o repositório
        val factory = MainViewModelFactory(repository)

        setContent {
            IPTVHeroProTheme {
                // 4. Obtém o ViewModel utilizando a Factory customizada
                val viewModel: MainViewModel = viewModel(factory = factory)
                
                // Passa o ViewModel para o sistema de navegação
                AppNavigation(viewModel = viewModel)
            }
        }
    }
}
