package com.tcm.app.ui.viewmodel

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcm.app.data.local.entity.*
import com.tcm.app.data.remote.model.OcrResult
import com.tcm.app.data.repository.AiRepository
import com.tcm.app.data.repository.PrescriptionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

class OcrViewModel(
    private val aiRepository: AiRepository,
    private val prescriptionRepository: PrescriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(OcrUiState())
    val uiState: StateFlow<OcrUiState> = _uiState.asStateFlow()

    fun performOcr(bitmap: Bitmap) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, error = null) }
            
            aiRepository.performOcr(bitmap)
                .onSuccess { ocrResult ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            ocrResult = ocrResult,
                            editedPrescription = EditablePrescription.fromOcrResult(ocrResult)
                        )
                    }
                }
                .onFailure { error ->
                    _uiState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun updatePrescriptionName(name: String) {
        _uiState.update { 
            it.copy(editedPrescription = it.editedPrescription?.copy(name = name))
        }
    }

    fun updatePatientName(patientName: String) {
        _uiState.update { 
            it.copy(editedPrescription = it.editedPrescription?.copy(patientName = patientName))
        }
    }

    fun updateDescription(description: String) {
        _uiState.update { 
            it.copy(editedPrescription = it.editedPrescription?.copy(description = description))
        }
    }

    fun addHerb(herb: EditableHerb) {
        _uiState.update { state ->
            val currentHerbs = state.editedPrescription?.herbs ?: emptyList()
            state.copy(
                editedPrescription = state.editedPrescription?.copy(
                    herbs = currentHerbs + herb.copy(sequence = currentHerbs.size)
                )
            )
        }
    }

    fun updateHerb(index: Int, herb: EditableHerb) {
        _uiState.update { state ->
            val currentHerbs = state.editedPrescription?.herbs?.toMutableList() ?: return@update state
            if (index in currentHerbs.indices) {
                currentHerbs[index] = herb
                state.copy(editedPrescription = state.editedPrescription.copy(herbs = currentHerbs))
            } else state
        }
    }

    fun removeHerb(index: Int) {
        _uiState.update { state ->
            val currentHerbs = state.editedPrescription?.herbs?.toMutableList() ?: return@update state
            if (index in currentHerbs.indices) {
                currentHerbs.removeAt(index)
                // Re-sequence
                val resequenced = currentHerbs.mapIndexed { i, h -> h.copy(sequence = i) }
                state.copy(editedPrescription = state.editedPrescription.copy(herbs = resequenced))
            } else state
        }
    }

    fun updateUsageInstruction(usage: EditableUsage) {
        _uiState.update { 
            it.copy(editedPrescription = it.editedPrescription?.copy(usage = usage))
        }
    }

    fun savePrescription(onSuccess: () -> Unit) {
        viewModelScope.launch {
            val editable = _uiState.value.editedPrescription ?: return@launch
            
            val prescription = Prescription(
                name = editable.name,
                patientName = editable.patientName,  // 保存患者名称
                description = editable.description,
                isAiGenerated = false,
                confidenceScore = _uiState.value.ocrResult?.confidence ?: 0f,
                createdAt = Date(),
                updatedAt = Date()
            )
            
            val herbs = editable.herbs.mapIndexed { index, h ->
                Herb(
                    prescriptionId = 0, // Will be set by repository
                    name = h.name,
                    dosage = h.dosage,
                    preparation = h.preparation,
                    sequence = index
                )
            }
            
            val usageInstruction = UsageInstruction(
                prescriptionId = 0,
                decoctionMethod = editable.usage.decoctionMethod,
                frequency = editable.usage.frequency,
                dosagePerTime = editable.usage.dosagePerTime,
                precautions = editable.usage.precautions
            )
            
            prescriptionRepository.savePrescription(
                prescription = prescription,
                herbs = herbs,
                usageInstruction = usageInstruction
            )
            
            onSuccess()
        }
    }

    fun clearResult() {
        _uiState.update { OcrUiState() }
    }

    data class OcrUiState(
        val isLoading: Boolean = false,
        val ocrResult: OcrResult? = null,
        val editedPrescription: EditablePrescription? = null,
        val error: String? = null
    )

    data class EditablePrescription(
        val name: String = "",
        val patientName: String = "",  // 患者名称
        val description: String = "",
        val herbs: List<EditableHerb> = emptyList(),
        val usage: EditableUsage = EditableUsage(),
        val specialNotes: String = ""
    ) {
        companion object {
            fun fromOcrResult(ocrResult: OcrResult): EditablePrescription {
                return EditablePrescription(
                    name = ocrResult.prescriptionName,
                    patientName = ocrResult.patientName ?: "",  // 从OCR结果获取患者名称
                    description = ocrResult.indications,
                    herbs = ocrResult.herbs.mapIndexed { index, h ->
                        EditableHerb(
                            name = h.name,
                            dosage = h.dosage,
                            preparation = h.preparation,
                            sequence = index
                        )
                    },
                    usage = EditableUsage(
                        decoctionMethod = ocrResult.usage.decoctionMethod,
                        frequency = ocrResult.usage.frequency,
                        dosagePerTime = ocrResult.usage.dosagePerTime
                    ),
                    specialNotes = ocrResult.specialNotes
                )
            }
        }
    }

    data class EditableHerb(
        val name: String = "",
        val dosage: String = "",
        val preparation: String = "",
        val sequence: Int = 0
    )

    data class EditableUsage(
        val decoctionMethod: String = "",
        val frequency: String = "",
        val dosagePerTime: String = "",
        val precautions: String = ""
    )
}
