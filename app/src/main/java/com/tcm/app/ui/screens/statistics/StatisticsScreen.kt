package com.tcm.app.ui.screens.statistics

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.tcm.app.ui.components.LoadingIndicator
import com.tcm.app.ui.viewmodel.StatisticsViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    onBack: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val prescriptionCount by viewModel.prescriptionCount.collectAsState()
    val aiGeneratedCount by viewModel.aiGeneratedCount.collectAsState()
    val totalHerbCount by viewModel.totalHerbCount.collectAsState()
    val uniqueHerbCount by viewModel.uniqueHerbCount.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("数据统计") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.loadStatistics() }) {
                        Icon(Icons.Default.Refresh, contentDescription = "刷新")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading && uiState.topHerbs.isEmpty()) {
            LoadingIndicator(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 概览卡片
                item {
                    StatisticsOverviewCard(
                        totalPrescriptions = prescriptionCount,
                        aiGeneratedCount = aiGeneratedCount,
                        totalHerbs = totalHerbCount,
                        uniqueHerbs = uniqueHerbCount,
                        weeklyNew = uiState.weeklyNewCount,
                        monthlyNew = uiState.monthlyNewCount
                    )
                }

                // 常用药材统计
                if (uiState.topHerbs.isNotEmpty()) {
                    item {
                        TopHerbsCard(herbs = uiState.topHerbs)
                    }
                }

                // 药材使用频率图表
                if (uiState.herbFrequency.isNotEmpty()) {
                    item {
                        HerbFrequencyChart(herbFrequency = uiState.herbFrequency)
                    }
                }

                // 最近添加
                if (uiState.recentPrescriptions.isNotEmpty()) {
                    item {
                        RecentPrescriptionsCard(prescriptions = uiState.recentPrescriptions)
                    }
                }

                // 底部留白
                item {
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}

@Composable
private fun StatisticsOverviewCard(
    totalPrescriptions: Int,
    aiGeneratedCount: Int,
    totalHerbs: Int,
    uniqueHerbs: Int,
    weeklyNew: Int,
    monthlyNew: Int
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "数据概览",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 第一行：方剂统计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatisticItem(
                    icon = Icons.Default.Medication,
                    value = totalPrescriptions.toString(),
                    label = "总方剂",
                    color = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )
                StatisticItem(
                    icon = Icons.Default.AutoFixHigh,
                    value = aiGeneratedCount.toString(),
                    label = "AI生成",
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(8.dp))
            
            // 第二行：药材统计
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                StatisticItem(
                    icon = Icons.Default.Grass,
                    value = totalHerbs.toString(),
                    label = "药材总数",
                    color = Color(0xFF4CAF50),
                    modifier = Modifier.weight(1f)
                )
                StatisticItem(
                    icon = Icons.Default.Category,
                    value = uniqueHerbs.toString(),
                    label = "药材种类",
                    color = Color(0xFFFF9800),
                    modifier = Modifier.weight(1f)
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            Divider()
            Spacer(modifier = Modifier.height(16.dp))
            
            // 新增统计
            Text(
                text = "新增统计",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                NewItemBadge(
                    label = "本周新增",
                    count = weeklyNew,
                    modifier = Modifier.weight(1f)
                )
                NewItemBadge(
                    label = "本月新增",
                    count = monthlyNew,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatisticItem(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    value: String,
    label: String,
    color: Color,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = color.copy(alpha = 0.1f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = color,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = color
            )
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun NewItemBadge(
    label: String,
    count: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        color = if (count > 0) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(8.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = label,
                style = MaterialTheme.typography.bodyMedium
            )
            Badge(
                containerColor = if (count > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
            ) {
                Text(
                    text = "+$count",
                    color = if (count > 0) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
private fun TopHerbsCard(herbs: List<com.tcm.app.data.local.dao.HerbDao.HerbCount>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "常用药材 TOP10",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            herbs.forEachIndexed { index, herb ->
                HerbRankItem(
                    rank = index + 1,
                    name = herb.name,
                    count = herb.count,
                    maxCount = herbs.firstOrNull()?.count ?: 1
                )
                if (index < herbs.size - 1) {
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
    }
}

@Composable
private fun HerbRankItem(
    rank: Int,
    name: String,
    count: Int,
    maxCount: Int
) {
    val rankColor = when (rank) {
        1 -> Color(0xFFFFD700) // 金色
        2 -> Color(0xFFC0C0C0) // 银色
        3 -> Color(0xFFCD7F32) // 铜色
        else -> MaterialTheme.colorScheme.primary.copy(alpha = 0.5f)
    }
    
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 排名
            Surface(
                shape = RoundedCornerShape(4.dp),
                color = rankColor,
                modifier = Modifier.size(28.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = rank.toString(),
                        fontSize = 14.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (rank <= 3) Color.White else Color.Black
                    )
                }
            }
            
            // 药材名
            Text(
                text = name,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.width(80.dp)
            )
            
            // 进度条
            LinearProgressIndicator(
                progress = { count.toFloat() / maxCount },
                modifier = Modifier
                    .weight(1f)
                    .height(8.dp)
                    .clip(RoundedCornerShape(4.dp)),
                color = when (rank) {
                    1 -> Color(0xFFFFD700)
                    2 -> Color(0xFFC0C0C0)
                    3 -> Color(0xFFCD7F32)
                    else -> MaterialTheme.colorScheme.primary
                },
            )
            
            // 次数
            Text(
                text = "${count}次",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun HerbFrequencyChart(
    herbFrequency: List<Pair<String, Int>>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "药材使用频率",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(16.dp))
            
            // 简单的横向条形图
            val maxValue = herbFrequency.maxOfOrNull { it.second } ?: 1
            
            herbFrequency.take(8).forEach { (name, count) ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = name,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier.width(80.dp)
                    )
                    
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(20.dp)
                            .padding(horizontal = 8.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .fillMaxWidth(count.toFloat() / maxValue)
                                .clip(RoundedCornerShape(4.dp))
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                        )
                    }
                    
                    Text(
                        text = count.toString(),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.width(30.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun RecentPrescriptionsCard(
    prescriptions: List<com.tcm.app.data.local.entity.Prescription>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "最近添加",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
            
            Spacer(modifier = Modifier.height(12.dp))
            
            prescriptions.forEach { prescription ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = prescription.name.ifEmpty { "未命名方剂" },
                            style = MaterialTheme.typography.bodyLarge
                        )
                        if (prescription.patientName.isNotBlank()) {
                            Text(
                                text = "患者: ${prescription.patientName}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    }
                    
                    Text(
                        text = SimpleDateFormat("MM-dd", Locale.getDefault())
                            .format(prescription.createdAt),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                }
                Divider()
            }
        }
    }
}
