package com.tcm.app.ui.screens.ai

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.tcm.app.ui.components.*
import com.tcm.app.ui.viewmodel.AiViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AiChatScreen(
    viewModel: AiViewModel,
    onBack: () -> Unit
) {
    val chatState by viewModel.chatState.collectAsState()
    val chatHistory by viewModel.chatHistory.collectAsState()
    val recommendationState by viewModel.recommendationState.collectAsState()
    val apiKey by viewModel.apiKeyState.collectAsState()
    
    var messageText by remember { mutableStateOf("") }
    var showRecommendationDialog by remember { mutableStateOf(false) }
    var showApiKeyDialog by remember { mutableStateOf(apiKey.isEmpty()) }
    
    val listState = rememberLazyListState()
    val coroutineScope = rememberCoroutineScope()

    // Auto-scroll to bottom when new messages arrive
    LaunchedEffect(chatHistory.size) {
        if (chatHistory.isNotEmpty()) {
            coroutineScope.launch {
                listState.animateScrollToItem(chatHistory.size - 1)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("AI中医助手") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showRecommendationDialog = true }) {
                        Icon(Icons.Default.AutoFixHigh, contentDescription = "AI开方")
                    }
                    IconButton(onClick = { viewModel.clearChat() }) {
                        Icon(Icons.Default.DeleteSweep, contentDescription = "清空对话")
                    }
                    IconButton(onClick = { showApiKeyDialog = true }) {
                        Icon(Icons.Default.Key, contentDescription = "API设置")
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
            // Chat Messages
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                if (chatHistory.isEmpty()) {
                    EmptyState(
                        title = "开始与AI中医助手对话",
                        message = "可以询问方剂解析、病症分析等问题\n或点击右上角AI开方功能",
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        state = listState,
                        contentPadding = PaddingValues(vertical = 8.dp)
                    ) {
                        items(chatHistory) { message ->
                            ChatMessageItem(
                                content = message.content,
                                isUser = message.role == "user"
                            )
                        }
                    }
                }

                // Loading indicator
                if (chatState.isLoading) {
                    Surface(
                        modifier = Modifier.align(Alignment.BottomCenter),
                        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
                    ) {
                        TypingIndicator(modifier = Modifier.padding(8.dp))
                    }
                }
            }

            // Error message
            chatState.error?.let { error ->
                Surface(
                    color = MaterialTheme.colorScheme.errorContainer,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.padding(8.dp),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }

            // Input Area
            Surface(
                tonalElevation = 3.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    OutlinedTextField(
                        value = messageText,
                        onValueChange = { messageText = it },
                        placeholder = { Text("输入问题...") },
                        modifier = Modifier.weight(1f),
                        maxLines = 4,
                        enabled = !chatState.isLoading
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    IconButton(
                        onClick = {
                            if (messageText.isNotBlank()) {
                                viewModel.sendMessage(messageText)
                                messageText = ""
                            }
                        },
                        enabled = messageText.isNotBlank() && !chatState.isLoading
                    ) {
                        Icon(Icons.Default.Send, contentDescription = "发送")
                    }
                }
            }
        }
    }

    // Recommendation Dialog
    if (showRecommendationDialog) {
        PrescriptionRecommendationDialog(
            onDismiss = { showRecommendationDialog = false },
            onSubmit = { symptoms, constitution, ageGender ->
                viewModel.getPrescriptionRecommendation(symptoms, constitution, ageGender)
                showRecommendationDialog = false
            },
            recommendationState = recommendationState,
            onClearRecommendation = { viewModel.clearRecommendation() }
        )
    }

    // API Key Dialog
    if (showApiKeyDialog) {
        ApiKeyDialog(
            currentKey = apiKey,
            onDismiss = { showApiKeyDialog = false },
            onSave = { key ->
                viewModel.saveApiKey(key)
                showApiKeyDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PrescriptionRecommendationDialog(
    onDismiss: () -> Unit,
    onSubmit: (String, String, String) -> Unit,
    recommendationState: AiViewModel.RecommendationUiState,
    onClearRecommendation: () -> Unit
) {
    var symptoms by remember { mutableStateOf("") }
    var constitution by remember { mutableStateOf("平和质") }
    var ageGender by remember { mutableStateOf("") }
    
    val constitutions = listOf("平和质", "气虚质", "阳虚质", "阴虚质", "痰湿质", "湿热质", "血瘀质", "气郁质", "特禀质")
    
    var expanded by remember { mutableStateOf(false) }

    AlertDialog(
        onDismissRequest = {
            onClearRecommendation()
            onDismiss()
        },
        title = { Text("AI智能开方") },
        text = {
            Column {
                if (recommendationState.recommendation != null) {
                    // Show recommendation result
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .heightIn(max = 400.dp),
                        color = MaterialTheme.colorScheme.surfaceVariant,
                        shape = MaterialTheme.shapes.medium
                    ) {
                        LazyColumn(modifier = Modifier.padding(12.dp)) {
                            item {
                                Text(
                                    text = recommendationState.recommendation,
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }
                        }
                    }
                } else if (recommendationState.isLoading) {
                    LoadingIndicator(message = "AI正在分析中...")
                } else {
                    // Input form
                    OutlinedTextField(
                        value = symptoms,
                        onValueChange = { symptoms = it },
                        label = { Text("主要症状 *") },
                        modifier = Modifier.fillMaxWidth(),
                        minLines = 3,
                        placeholder = { Text("请详细描述患者的症状...") }
                    )
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    ExposedDropdownMenuBox(
                        expanded = expanded,
                        onExpandedChange = { expanded = it }
                    ) {
                        OutlinedTextField(
                            value = constitution,
                            onValueChange = {},
                            readOnly = true,
                            label = { Text("体质类型") },
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
                            modifier = Modifier.menuAnchor().fillMaxWidth()
                        )
                        ExposedDropdownMenu(
                            expanded = expanded,
                            onDismissRequest = { expanded = false }
                        ) {
                            constitutions.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option) },
                                    onClick = {
                                        constitution = option
                                        expanded = false
                                    }
                                )
                            }
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(12.dp))
                    
                    OutlinedTextField(
                        value = ageGender,
                        onValueChange = { ageGender = it },
                        label = { Text("年龄性别") },
                        modifier = Modifier.fillMaxWidth(),
                        placeholder = { Text("如：35岁 女") }
                    )
                    
                    if (recommendationState.error != null) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = recommendationState.error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }
        },
        confirmButton = {
            if (recommendationState.recommendation != null) {
                TextButton(onClick = {
                    onClearRecommendation()
                    onDismiss()
                }) {
                    Text("关闭")
                }
            } else if (!recommendationState.isLoading) {
                TextButton(
                    onClick = { onSubmit(symptoms, constitution, ageGender) },
                    enabled = symptoms.isNotBlank()
                ) {
                    Text("获取推荐")
                }
            }
        },
        dismissButton = {
            if (!recommendationState.isLoading) {
                TextButton(onClick = {
                    onClearRecommendation()
                    onDismiss()
                }) {
                    Text("取消")
                }
            }
        }
    )
}

@Composable
private fun ApiKeyDialog(
    currentKey: String,
    onDismiss: () -> Unit,
    onSave: (String) -> Unit
) {
    var apiKey by remember { mutableStateOf(currentKey) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("设置 DeepSeek API Key") },
        text = {
            Column {
                Text(
                    text = "请输入您的 DeepSeek API Key。\n\n获取方式：\n1. 访问 platform.deepseek.com\n2. 注册并创建 API Key\n3. 新用户有10元免费额度",
                    style = MaterialTheme.typography.bodySmall
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = apiKey,
                    onValueChange = { apiKey = it },
                    label = { Text("API Key") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(apiKey) },
                enabled = apiKey.isNotBlank()
            ) {
                Text("保存")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
