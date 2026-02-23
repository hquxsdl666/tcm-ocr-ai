package com.tcm.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.tcm.app.data.local.dao.HerbDao
import com.tcm.app.data.local.dao.PrescriptionDao
import com.tcm.app.data.local.entity.Herb
import com.tcm.app.data.local.entity.Prescription
import com.tcm.app.data.repository.PrescriptionRepository
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import java.util.*

class StatisticsViewModel(
    private val prescriptionDao: PrescriptionDao,
    private val herbDao: HerbDao,
    private val prescriptionRepository: PrescriptionRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(StatisticsUiState())
    val uiState: StateFlow<StatisticsUiState> = _uiState.asStateFlow()

    // 基础统计数据流
    val prescriptionCount: StateFlow<Int> = prescriptionDao.getPrescriptionCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val aiGeneratedCount: StateFlow<Int> = prescriptionDao.getAiGeneratedCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val totalHerbCount: StateFlow<Int> = herbDao.getTotalHerbCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    val uniqueHerbCount: StateFlow<Int> = herbDao.getUniqueHerbCountFlow()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 0)

    init {
        loadStatistics()
    }

    fun loadStatistics() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            
            try {
                // 获取最常用的药材
                val topHerbs = herbDao.getTopHerbs(10)
                
                // 获取最近的方剂
                val recentPrescriptions = prescriptionDao.getRecentPrescriptions(5)
                
                // 计算本周新增
                val calendar = Calendar.getInstance()
                calendar.add(Calendar.DAY_OF_YEAR, -7)
                val weeklyCount = prescriptionDao.getPrescriptionCountSince(calendar.timeInMillis)
                
                // 计算本月新增
                calendar.timeInMillis = System.currentTimeMillis()
                calendar.add(Calendar.MONTH, -1)
                val monthlyCount = prescriptionDao.getPrescriptionCountSince(calendar.timeInMillis)
                
                // 获取所有药材用于分析
                val allHerbs = herbDao.getAllHerbs()
                val herbFrequency = allHerbs.groupingBy { it.name }.eachCount()
                    .toList().sortedByDescending { it.second }.take(10)
                
                _uiState.update {
                    it.copy(
                        isLoading = false,
                        topHerbs = topHerbs,
                        recentPrescriptions = recentPrescriptions,
                        weeklyNewCount = weeklyCount,
                        monthlyNewCount = monthlyCount,
                        herbFrequency = herbFrequency
                    )
                }
            } catch (e: Exception) {
                _uiState.update { it.copy(isLoading = false, error = e.message) }
            }
        }
    }

    data class StatisticsUiState(
        val isLoading: Boolean = false,
        val topHerbs: List<HerbDao.HerbCount> = emptyList(),
        val recentPrescriptions: List<Prescription> = emptyList(),
        val weeklyNewCount: Int = 0,
        val monthlyNewCount: Int = 0,
        val herbFrequency: List<Pair<String, Int>> = emptyList(),
        val error: String? = null
    )
}
