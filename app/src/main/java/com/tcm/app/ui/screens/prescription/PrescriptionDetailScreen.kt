package com.tcm.app.ui.screens.prescription

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tcm.app.ui.components.*
import com.tcm.app.ui.viewmodel.PrescriptionViewModel
import java.text.SimpleDateFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PrescriptionDetailScreen(
    prescriptionId: Long,
    viewModel: PrescriptionViewModel,
    onBack: () -> Unit,
    onEdit: () -> Unit = {}
) {
    val uiState by viewModel.uiState.collectAsState()
    
    LaunchedEffect(prescriptionId) {
        viewModel.loadPrescriptionDetail(prescriptionId)
    }

    val detail = uiState.selectedPrescription

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("方剂详情") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    detail?.let {
                        IconButton(onClick = {
                            viewModel.deletePrescription(prescriptionId, onBack)
                        }) {
                            Icon(Icons.Default.Delete, contentDescription = "删除")
                        }
                    }
                }
            )
        }
    ) { padding ->
        if (detail == null) {
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
                    .padding(16.dp)
            ) {
                item {
                    // Header
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = detail.prescription.name.ifEmpty { "未命名方剂" },
                                style = MaterialTheme.typography.headlineSmall
                            )
                            if (detail.prescription.isAiGenerated) {
                                Spacer(modifier = Modifier.height(4.dp))
                                AssistChip(
                                    onClick = { },
                                    label = { Text("AI生成") },
                                    colors = AssistChipDefaults.assistChipColors(
                                        containerColor = MaterialTheme.colorScheme.secondaryContainer
                                    )
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(8.dp))
                    
                    Text(
                        text = "创建于: ${SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(detail.prescription.createdAt)}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.outline
                    )
                    
                    if (detail.prescription.confidenceScore > 0) {
                        Text(
                            text = "识别置信度: ${(detail.prescription.confidenceScore * 100).toInt()}%",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                    
                    Spacer(modifier = Modifier.height(24.dp))
                }

                // Herbs Section
                item {
                    Text(
                        text = "药材组成",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    HorizontalDivider()
                    Spacer(modifier = Modifier.height(8.dp))
                }

                if (detail.herbs.isEmpty()) {
                    item {
                        Text(
                            text = "暂无药材信息",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.outline
                        )
                    }
                } else {
                    items(detail.herbs.sortedBy { it.sequence }) { herb ->
                        HerbItem(
                            name = herb.name,
                            dosage = herb.dosage,
                            preparation = herb.preparation
                        )
                    }
                }

                item {
                    Spacer(modifier = Modifier.height(24.dp))

                    // Usage Instructions
                    detail.usageInstruction?.let { usage ->
                        if (usage.decoctionMethod.isNotEmpty() || usage.frequency.isNotEmpty()) {
                            Text(
                                text = "用法用量",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            HorizontalDivider()
                            Spacer(modifier = Modifier.height(8.dp))

                            if (usage.decoctionMethod.isNotEmpty()) {
                                InfoCard(
                                    title = "煎煮方法",
                                    content = usage.decoctionMethod
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            if (usage.frequency.isNotEmpty()) {
                                InfoCard(
                                    title = "服用频次",
                                    content = usage.frequency
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            if (usage.dosagePerTime.isNotEmpty()) {
                                InfoCard(
                                    title = "每次用量",
                                    content = usage.dosagePerTime
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                            }
                            
                            if (usage.precautions.isNotEmpty()) {
                                InfoCard(
                                    title = "注意事项",
                                    content = usage.precautions
                                )
                            }
                            
                            Spacer(modifier = Modifier.height(24.dp))
                        }
                    }

                    // Description / Indications
                    if (detail.prescription.description.isNotEmpty()) {
                        Text(
                            text = "主治功效",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        HorizontalDivider()
                        Spacer(modifier = Modifier.height(8.dp))

                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Text(
                                text = detail.prescription.description,
                                modifier = Modifier.padding(16.dp),
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        
                        Spacer(modifier = Modifier.height(24.dp))
                    }

                    // Symptoms Tags
                    if (detail.symptoms.isNotEmpty()) {
                        Text(
                            text = "适用症状",
                            style = MaterialTheme.typography.titleLarge,
                            color = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            detail.symptoms.forEach { symptom ->
                                InputChip(
                                    selected = false,
                                    onClick = { },
                                    label = { Text(symptom.symptom) }
                                )
                            }
                        }
                        
                        Spacer(modifier = Modifier.height(32.dp))
                    }

                    // Actions
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        OutlinedButton(
                            onClick = onBack,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text("返回")
                        }
                        Button(
                            onClick = { /* Share functionality */ },
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(Icons.Default.Share, contentDescription = null)
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("分享")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(32.dp))
                }
            }
        }
    }
}
