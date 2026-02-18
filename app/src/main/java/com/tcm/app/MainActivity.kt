package com.tcm.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.tcm.app.ui.screens.ai.AiChatScreen
import com.tcm.app.ui.screens.home.HomeScreen
import com.tcm.app.ui.screens.ocr.OcrScreen
import com.tcm.app.ui.screens.prescription.PrescriptionDetailScreen
import com.tcm.app.ui.screens.search.SearchScreen
import com.tcm.app.ui.theme.TcmOcrAiTheme
import com.tcm.app.ui.viewmodel.AiViewModel
import com.tcm.app.ui.viewmodel.OcrViewModel
import com.tcm.app.ui.viewmodel.PrescriptionViewModel

class MainActivity : ComponentActivity() {

    private val app by lazy { application as TcmApplication }

    private val prescriptionViewModel: PrescriptionViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return PrescriptionViewModel(app.prescriptionRepository) as T
            }
        }
    }

    private val ocrViewModel: OcrViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return OcrViewModel(app.aiRepository, app.prescriptionRepository) as T
            }
        }
    }

    private val aiViewModel: AiViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return AiViewModel(app.aiRepository, app.prescriptionRepository) as T
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        setContent {
            TcmOcrAiTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    TcmNavHost(
                        navController = navController,
                        prescriptionViewModel = prescriptionViewModel,
                        ocrViewModel = ocrViewModel,
                        aiViewModel = aiViewModel
                    )
                }
            }
        }
    }
}

@Composable
fun TcmNavHost(
    navController: NavHostController,
    prescriptionViewModel: PrescriptionViewModel,
    ocrViewModel: OcrViewModel,
    aiViewModel: AiViewModel
) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable("home") {
            HomeScreen(
                viewModel = prescriptionViewModel,
                onNavigateToOcr = { 
                    ocrViewModel.clearResult()
                    navController.navigate("ocr") 
                },
                onNavigateToDetail = { id ->
                    navController.navigate("detail/$id")
                },
                onNavigateToSearch = { navController.navigate("search") },
                onNavigateToAi = { navController.navigate("ai") },
                onNavigateToSettings = { navController.navigate("settings") }
            )
        }
        
        composable("ocr") {
            OcrScreen(
                viewModel = ocrViewModel,
                onBack = { navController.popBackStack() },
                onSaveSuccess = { 
                    ocrViewModel.clearResult()
                    navController.popBackStack() 
                }
            )
        }
        
        composable("search") {
            SearchScreen(
                viewModel = prescriptionViewModel,
                onBack = { navController.popBackStack() },
                onNavigateToDetail = { id ->
                    navController.navigate("detail/$id")
                }
            )
        }
        
        composable(
            "detail/{prescriptionId}",
            arguments = listOf(navArgument("prescriptionId") { type = NavType.LongType })
        ) { backStackEntry ->
            val prescriptionId = backStackEntry.arguments?.getLong("prescriptionId") ?: 0L
            PrescriptionDetailScreen(
                prescriptionId = prescriptionId,
                viewModel = prescriptionViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("ai") {
            AiChatScreen(
                viewModel = aiViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        
        composable("settings") {
            // Settings screen placeholder
            androidx.compose.material3.Scaffold(
                topBar = {
                    androidx.compose.material3.TopAppBar(
                        title = { androidx.compose.material3.Text("设置") },
                        navigationIcon = {
                            androidx.compose.material3.IconButton(onClick = { navController.popBackStack() }) {
                                androidx.compose.material3.Icon(
                                    androidx.compose.material.icons.Icons.Default.ArrowBack,
                                    contentDescription = "返回"
                                )
                            }
                        }
                    )
                }
            ) { padding ->
                androidx.compose.foundation.layout.Column(
                    modifier = androidx.compose.ui.Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp)
                ) {
                    androidx.compose.material3.Text(
                        text = "设置",
                        style = androidx.compose.material3.MaterialTheme.typography.headlineSmall
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(16.dp))
                    androidx.compose.material3.Text(
                        text = "请前往 AI助手 页面设置 DeepSeek API Key",
                        style = androidx.compose.material3.MaterialTheme.typography.bodyMedium
                    )
                    androidx.compose.foundation.layout.Spacer(modifier = androidx.compose.ui.Modifier.height(8.dp))
                    androidx.compose.material3.Text(
                        text = "获取方式：\n1. 访问 platform.deepseek.com\n2. 注册并创建 API Key\n3. 新用户有10元免费额度",
                        style = androidx.compose.material3.MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
