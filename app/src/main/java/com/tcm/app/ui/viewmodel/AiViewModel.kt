package com.tcm.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcm.app.data.local.entity.ChatHistory
import com.tcm.app.data.local.entity.Prescription
import com.tcm.app.data.remote.model.ChatMessage
import com.tcm.app.data.repository.AiRepository
import com.tcm.app.data.repository.PrescriptionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch

class AiViewModel(
    private val aiRepository: AiRepository,
    private val prescriptionRepository: PrescriptionRepository
) : ViewModel() {

    private val _chatState = MutableStateFlow(ChatUiState())
    val chatState: StateFlow<ChatUiState> = _chatState.asStateFlow()

    private val _recommendationState = MutableStateFlow(RecommendationUiState())
    val recommendationState: StateFlow<RecommendationUiState> = _recommendationState.asStateFlow()

    private val _apiKeyState = MutableStateFlow("")
    val apiKeyState: StateFlow<String> = _apiKeyState.asStateFlow()

    val chatHistory: StateFlow<List<ChatHistory>> = aiRepository.getChatHistory()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        _apiKeyState.value = aiRepository.getSavedApiKey()
    }

    fun sendMessage(message: String) {
        viewModelScope.launch {
            _chatState.update { it.copy(isLoading = true, error = null) }
            
            // Save user message
            aiRepository.saveChatMessage(role = "user", content = message)
            
            // Get recent context
            val recentHistory = chatHistory.value.takeLast(10)
            val contextMessages = recentHistory.map { 
                ChatMessage(role = it.role, content = it.content)
            } + ChatMessage(role = "user", content = message)
            
            // Get AI response with prescription library context
            val prescriptionLibrary = prescriptionRepository.getAllPrescriptions().first().take(20)
            
            aiRepository.getAiResponse(contextMessages, prescriptionLibrary)
                .onSuccess { response ->
                    aiRepository.saveChatMessage(role = "assistant", content = response)
                    _chatState.update { it.copy(isLoading = false) }
                }
                .onFailure { error ->
                    _chatState.update { it.copy(isLoading = false, error = error.message) }
                }
        }
    }

    fun getPrescriptionRecommendation(
        symptoms: String,
        constitution: String,
        ageGender: String
    ) {
        viewModelScope.launch {
            _recommendationState.update { it.copy(isLoading = true, error = null) }
            
            val prescriptionLibrary = prescriptionRepository.getAllPrescriptions().first()
            
            aiRepository.recommendPrescription(
                symptoms = symptoms,
                constitution = constitution,
                ageGender = ageGender,
                prescriptionLibrary = prescriptionLibrary
            )
                .onSuccess { response ->
                    _recommendationState.update { 
                        it.copy(isLoading = false, recommendation = response)
                    }
                }
                .onFailure { error ->
                    _recommendationState.update { 
                        it.copy(isLoading = false, error = error.message)
                    }
                }
        }
    }

    fun saveApiKey(apiKey: String) {
        viewModelScope.launch {
            aiRepository.saveApiKey(apiKey)
            _apiKeyState.value = apiKey
        }
    }

    fun clearChat() {
        viewModelScope.launch {
            aiRepository.clearChatHistory()
        }
    }

    fun clearRecommendation() {
        _recommendationState.value = RecommendationUiState()
    }

    data class ChatUiState(
        val isLoading: Boolean = false,
        val error: String? = null
    )

    data class RecommendationUiState(
        val isLoading: Boolean = false,
        val recommendation: String? = null,
        val error: String? = null
    )
}
