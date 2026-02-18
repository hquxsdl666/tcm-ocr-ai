package com.tcm.app.ui.screens.search

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SearchScreen(
    viewModel: PrescriptionViewModel,
    onBack: () -> Unit,
    onNavigateToDetail: (Long) -> Unit
) {
    val searchQuery by viewModel.searchQuery.collectAsState()
    val searchResults by viewModel.searchResults.collectAsState()
    val allPrescriptions by viewModel.allPrescriptions.collectAsState()
    val herbNames by viewModel.getAllHerbNames().collectAsState()
    val symptoms by viewModel.getAllSymptoms().collectAsState()

    var selectedFilter by remember { mutableStateOf(SearchFilter.ALL) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("搜索方剂") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Search Bar
            OutlinedTextField(
                value = searchQuery,
                onValueChange = { viewModel.updateSearchQuery(it) },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text("搜索方剂名、药材、症状...") },
                leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
                trailingIcon = {
                    if (searchQuery.isNotEmpty()) {
                        IconButton(onClick = { viewModel.updateSearchQuery("") }) {
                            Icon(Icons.Default.Clear, contentDescription = "清除")
                        }
                    }
                },
                singleLine = true
            )

            // Quick Filters
            if (searchQuery.isEmpty()) {
                Text(
                    text = "常用药材",
                    style = MaterialTheme.typography.titleSmall,
                    modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                )
                
                FlowRow(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    herbNames.take(10).forEach { herb ->
                        AssistChip(
                            onClick = { viewModel.updateSearchQuery(herb) },
                            label = { Text(herb) }
                        )
                    }
                }

                if (symptoms.isNotEmpty()) {
                    Text(
                        text = "症状标签",
                        style = MaterialTheme.typography.titleSmall,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                    )
                    
                    FlowRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        symptoms.take(10).forEach { symptom ->
                            FilterChip(
                                selected = searchQuery == symptom,
                                onClick = { viewModel.updateSearchQuery(symptom) },
                                label = { Text(symptom) }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))
                
                Text(
                    text = "所有方剂 (${allPrescriptions.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
                
                LazyColumn(
                    modifier = Modifier.fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(allPrescriptions) { item ->
                        val herbSummary = item.herbs.joinToString(" ") { it.name + it.dosage }
                        PrescriptionCard(
                            prescription = item.prescription,
                            herbSummary = herbSummary.ifEmpty { "暂无药材信息" },
                            onClick = { onNavigateToDetail(item.prescription.id) }
                        )
                    }
                }
            } else {
                // Search Results
                Text(
                    text = "搜索结果 (${searchResults.size})",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )

                if (searchResults.isEmpty()) {
                    EmptyState(
                        title = "未找到相关方剂",
                        message = "尝试其他关键词搜索",
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxWidth(),
                        contentPadding = PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(searchResults) { item ->
                            val herbSummary = item.herbs.joinToString(" ") { it.name + it.dosage }
                            PrescriptionCard(
                                prescription = item.prescription,
                                herbSummary = herbSummary.ifEmpty { "暂无药材信息" },
                                onClick = { onNavigateToDetail(item.prescription.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}

enum class SearchFilter {
    ALL, NAME, HERB, SYMPTOM
}
