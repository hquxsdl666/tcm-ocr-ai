package com.tcm.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcm.app.data.local.entity.*
import com.tcm.app.data.repository.PrescriptionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.Date

class PrescriptionViewModel(
    private val repository: PrescriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(PrescriptionUiState())
    val uiState: StateFlow<PrescriptionUiState> = _uiState.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val allPrescriptions: StateFlow<List<PrescriptionRepository.PrescriptionWithDetails>> = repository.getAllPrescriptions()
        .map { prescriptions ->
            prescriptions.map { p ->
                PrescriptionRepository.PrescriptionWithDetails(
                    prescription = p,
                    herbs = emptyList(),
                    usageInstruction = null,
                    symptoms = emptyList()
                )
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    val searchResults: StateFlow<List<PrescriptionRepository.PrescriptionWithDetails>> = _searchQuery
        .debounce(300)
        .flatMapLatest { query ->
            if (query.isBlank()) {
                flowOf(emptyList())
            } else {
                repository.searchPrescriptions(query)
            }
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun loadPrescriptionDetail(id: Long) {
        viewModelScope.launch {
            repository.getPrescriptionWithDetails(id)
                .collect { details ->
                    _uiState.update { it.copy(selectedPrescription = details) }
                }
        }
    }

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun deletePrescription(id: Long, onSuccess: () -> Unit = {}) {
        viewModelScope.launch {
            repository.deletePrescriptionById(id)
            onSuccess()
        }
    }

    fun updatePrescription(
        prescription: Prescription,
        herbs: List<Herb>,
        usageInstruction: UsageInstruction?,
        onSuccess: () -> Unit = {}
    ) {
        viewModelScope.launch {
            repository.updatePrescription(
                prescription.copy(updatedAt = Date()),
                herbs,
                usageInstruction
            )
            onSuccess()
        }
    }

    fun getAllHerbNames(): StateFlow<List<String>> = flow {
        emit(repository.getHerbNames())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    fun getAllSymptoms(): StateFlow<List<String>> = flow {
        emit(repository.getSymptoms())
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    data class PrescriptionUiState(
        val selectedPrescription: PrescriptionRepository.PrescriptionWithDetails? = null,
        val isLoading: Boolean = false,
        val error: String? = null
    )
}
