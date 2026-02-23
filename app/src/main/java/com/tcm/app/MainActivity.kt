package com.tcm.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
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
import com.tcm.app.ui.screens.statistics.StatisticsScreen
import com.tcm.app.ui.theme.TcmOcrAiTheme
import com.tcm.app.ui.viewmodel.AiViewModel
import com.tcm.app.ui.viewmodel.OcrViewModel
import com.tcm.app.ui.viewmodel.PrescriptionViewModel
import com.tcm.app.ui.viewmodel.StatisticsViewModel

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

    private val statisticsViewModel: StatisticsViewModel by viewModels {
        object : ViewModelProvider.Factory {
            @Suppress("UNCHECKED_CAST")
            override fun <T : ViewModel> create(modelClass: Class<T>): T {
                return StatisticsViewModel(
                    app.database.prescriptionDao(),
                    app.database.herbDao(),
                    app.prescriptionRepository
                ) as T
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
                        aiViewModel = aiViewModel,
                        statisticsViewModel = statisticsViewModel
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
    aiViewModel: AiViewModel,
    statisticsViewModel: StatisticsViewModel
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
                onNavigateToSettings = { navController.navigate("settings") },
                onNavigateToStatistics = { navController.navigate("statistics") }  // 新增统计导航
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

        // 新增统计页面路由
        composable("statistics") {
            StatisticsScreen(
                viewModel = statisticsViewModel,
                onBack = { navController.popBackStack() }
            )
        }
        
        @OptIn(ExperimentalMaterial3Api::class)
        composable("settings") {
            Scaffold(
                topBar = {
                    TopAppBar(
                        title = { Text("设置") },
                        navigationIcon = {
                            IconButton(onClick = { navController.popBackStack() }) {
                                Icon(
                                    Icons.AutoMirrored.Filled.ArrowBack,
                                    contentDescription = "返回"
                                )
                            }
                        }
                    )
                }
            ) { paddingValues ->
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    Text(
                        text = "设置",
                        style = MaterialTheme.typography.headlineSmall
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "请前往 AI助手 页面设置 MiniMax API Key",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "获取方式：\n1. 访问 platform.minimaxi.com\n2. 注册并创建 API Key\n3. 对话使用 MiniMax-M2.5，识图使用 MiniMax-Text-01",
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}
