package com.tcm.app.ui.screens.ocr

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.tcm.app.ui.components.*
import com.tcm.app.ui.viewmodel.OcrViewModel
import com.tcm.app.utils.ImageUtils
import java.io.File

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OcrScreen(
    viewModel: OcrViewModel,
    onBack: () -> Unit,
    onSaveSuccess: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    var capturedBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var showAddHerbDialog by remember { mutableStateOf(false) }
    var editingHerbIndex by remember { mutableStateOf<Int?>(null) }
    var showPermissionDeniedDialog by remember { mutableStateOf(false) }
    var cameraPhotoUri by remember { mutableStateOf<Uri?>(null) }

    // 相机权限状态
    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    // 权限请求启动器
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        hasCameraPermission = isGranted
        if (!isGranted) {
            showPermissionDeniedDialog = true
        }
    }

    // Camera launcher - 使用TakePicture+FileProvider以支持全分辨率拍照
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicture()
    ) { success ->
        if (success) {
            cameraPhotoUri?.let { uri ->
                val bitmap = ImageUtils.uriToBitmap(context, uri)
                bitmap?.let { bmp ->
                    capturedBitmap = bmp
                    viewModel.performOcr(bmp)
                }
            }
        }
    }

    // Gallery launcher
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            val bitmap = ImageUtils.uriToBitmap(context, it)
            bitmap?.let { bmp ->
                capturedBitmap = bmp
                viewModel.performOcr(bmp)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("识别药方") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> {
                LoadingIndicator(
                    message = "正在识别...",
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            uiState.error != null -> {
                ErrorMessage(
                    message = uiState.error!!,
                    onRetry = { capturedBitmap?.let { viewModel.performOcr(it) } },
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                )
            }
            uiState.editedPrescription != null -> {
                // OCR Result Editor
                OcrResultEditor(
                    viewModel = viewModel,
                    onSave = { viewModel.savePrescription(onSaveSuccess) },
                    onAddHerb = { showAddHerbDialog = true },
                    onEditHerb = { index ->
                        editingHerbIndex = index
                        showAddHerbDialog = true
                    },
                    onDeleteHerb = { index -> viewModel.removeHerb(index) },
                    modifier = Modifier.padding(padding)
                )
            }
            else -> {
                // Initial State - Camera/Gallery options
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(padding)
                        .padding(16.dp),
                    verticalArrangement = Arrangement.Center,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    FilledTonalButton(
                        onClick = {
                            if (hasCameraPermission) {
                                val photoFile = File(context.cacheDir, "camera_${System.currentTimeMillis()}.jpg")
                                val uri = FileProvider.getUriForFile(
                                    context,
                                    "${context.packageName}.fileprovider",
                                    photoFile
                                )
                                cameraPhotoUri = uri
                                cameraLauncher.launch(uri)
                            } else {
                                permissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("拍照识别")
                        }
                    }
                    
                    Spacer(modifier = Modifier.height(16.dp))
                    
                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                Icons.Default.PhotoLibrary,
                                contentDescription = null,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(8.dp))
                            Text("从相册选择")
                        }
                    }
                }
            }
        }
    }

    // Permission Denied Dialog
    if (showPermissionDeniedDialog) {
        AlertDialog(
            onDismissRequest = { showPermissionDeniedDialog = false },
            title = { Text("需要相机权限") },
            text = { Text("拍照识别功能需要访问相机权限。请在设置中开启权限后重试。") },
            confirmButton = {
                TextButton(onClick = { showPermissionDeniedDialog = false }) {
                    Text("确定")
                }
            }
        )
    }

    // Add/Edit Herb Dialog
    if (showAddHerbDialog) {
        val currentHerbs = uiState.editedPrescription?.herbs ?: emptyList()
        val editingHerb = editingHerbIndex?.let { currentHerbs.getOrNull(it) }
        
        HerbEditDialog(
            herb = editingHerb,
            onDismiss = { 
                showAddHerbDialog = false
                editingHerbIndex = null
            },
            onConfirm = { herb ->
                if (editingHerbIndex != null) {
                    viewModel.updateHerb(editingHerbIndex!!, herb)
                } else {
                    viewModel.addHerb(herb)
                }
                showAddHerbDialog = false
                editingHerbIndex = null
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun OcrResultEditor(
    viewModel: OcrViewModel,
    onSave: () -> Unit,
    onAddHerb: () -> Unit,
    onEditHerb: (Int) -> Unit,
    onDeleteHerb: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val state by viewModel.uiState.collectAsState()
    val prescription = state.editedPrescription!!

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        item {
            // Confidence Score
            if (state.ocrResult?.confidence != null && state.ocrResult!!.confidence > 0) {
                Text(
                    text = "识别置信度: ${(state.ocrResult!!.confidence * 100).toInt()}%",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Prescription Name
            OutlinedTextField(
                value = prescription.name,
                onValueChange = { viewModel.updatePrescriptionName(it) },
                label = { Text("方剂名称") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("如：四君子汤") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Patient Name - 新增患者名称输入
            OutlinedTextField(
                value = prescription.patientName,
                onValueChange = { viewModel.updatePatientName(it) },
                label = { Text("患者姓名") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("如：张三") }
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Herbs Section
            Text(
                text = "药材清单",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        itemsIndexed(prescription.herbs) { index, herb ->
            HerbItem(
                name = herb.name,
                dosage = herb.dosage,
                preparation = herb.preparation,
                onEdit = { onEditHerb(index) },
                onDelete = { onDeleteHerb(index) }
            )
        }

        item {
            TextButton(
                onClick = onAddHerb,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(4.dp))
                Text("添加药材")
            }
            
            Spacer(modifier = Modifier.height(16.dp))

            // Usage Instructions
            Text(
                text = "用法用量",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = prescription.usage.decoctionMethod,
                onValueChange = { 
                    viewModel.updateUsageInstruction(prescription.usage.copy(decoctionMethod = it))
                },
                label = { Text("煎煮方法") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 2
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = prescription.usage.frequency,
                onValueChange = { 
                    viewModel.updateUsageInstruction(prescription.usage.copy(frequency = it))
                },
                label = { Text("服用频次") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = prescription.usage.dosagePerTime,
                onValueChange = { 
                    viewModel.updateUsageInstruction(prescription.usage.copy(dosagePerTime = it))
                },
                label = { Text("每次用量") },
                modifier = Modifier.fillMaxWidth()
            )
            
            Spacer(modifier = Modifier.height(16.dp))

            // Indications
            OutlinedTextField(
                value = prescription.description,
                onValueChange = { viewModel.updateDescription(it) },
                label = { Text("主治功效") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            
            Spacer(modifier = Modifier.height(24.dp))

            // Save Button
            Button(
                onClick = onSave,
                modifier = Modifier.fillMaxWidth(),
                enabled = prescription.herbs.isNotEmpty() && prescription.name.isNotBlank()
            ) {
                Icon(Icons.Default.Save, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("保存到方剂库")
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Composable
private fun HerbEditDialog(
    herb: OcrViewModel.EditableHerb?,
    onDismiss: () -> Unit,
    onConfirm: (OcrViewModel.EditableHerb) -> Unit
) {
    var name by remember { mutableStateOf(herb?.name ?: "") }
    var dosage by remember { mutableStateOf(herb?.dosage ?: "") }
    var preparation by remember { mutableStateOf(herb?.preparation ?: "") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(if (herb == null) "添加药材" else "编辑药材") },
        text = {
            Column {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("药材名称 *") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = dosage,
                    onValueChange = { dosage = it },
                    label = { Text("剂量") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("如：10g") }
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = preparation,
                    onValueChange = { preparation = it },
                    label = { Text("炮制") },
                    modifier = Modifier.fillMaxWidth(),
                    placeholder = { Text("如：炙、炒、生") }
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = {
                    if (name.isNotBlank()) {
                        onConfirm(OcrViewModel.EditableHerb(name, dosage, preparation))
                    }
                },
                enabled = name.isNotBlank()
            ) {
                Text("确定")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}
