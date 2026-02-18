package com.tcm.app.data.repository

import com.tcm.app.data.local.dao.*
import com.tcm.app.data.local.entity.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class PrescriptionRepository(
    private val prescriptionDao: PrescriptionDao,
    private val herbDao: HerbDao,
    private val usageInstructionDao: UsageInstructionDao,
    private val symptomDao: SymptomDao
) {
    fun getAllPrescriptions(): Flow<List<Prescription>> = prescriptionDao.getAllPrescriptions()

    fun searchPrescriptions(query: String): Flow<List<PrescriptionWithDetails>> = flow {
        val byName = prescriptionDao.searchByNameOrDescription(query).first()
        val byHerb = prescriptionDao.searchByHerbName(query).first()
        val bySymptom = symptomDao.searchBySymptom(query).first()
        
        val allResults = (byName + byHerb + bySymptom).distinctBy { it.id }
        
        val withDetails = allResults.map { prescription ->
            PrescriptionWithDetails(
                prescription = prescription,
                herbs = herbDao.getHerbsByPrescriptionIdSync(prescription.id),
                usageInstruction = usageInstructionDao.getUsageInstructionByPrescriptionIdSync(prescription.id),
                symptoms = symptomDao.getSymptomsByPrescriptionIdSync(prescription.id)
            )
        }
        emit(withDetails)
    }

    fun getPrescriptionWithDetails(id: Long): Flow<PrescriptionWithDetails?> = flow {
        val prescription = prescriptionDao.getPrescriptionById(id)
        if (prescription != null) {
            emit(PrescriptionWithDetails(
                prescription = prescription,
                herbs = herbDao.getHerbsByPrescriptionIdSync(id),
                usageInstruction = usageInstructionDao.getUsageInstructionByPrescriptionIdSync(id),
                symptoms = symptomDao.getSymptomsByPrescriptionIdSync(id)
            ))
        } else {
            emit(null)
        }
    }

    suspend fun savePrescription(
        prescription: Prescription,
        herbs: List<Herb>,
        usageInstruction: UsageInstruction?,
        symptoms: List<Symptom> = emptyList()
    ): Long {
        val prescriptionId = prescriptionDao.insertPrescription(prescription)
        
        // Save herbs with correct prescription ID
        val herbsWithId = herbs.map { it.copy(prescriptionId = prescriptionId) }
        herbDao.insertHerbs(herbsWithId)
        
        // Save usage instruction
        usageInstruction?.let {
            usageInstructionDao.insertUsageInstruction(it.copy(prescriptionId = prescriptionId))
        }
        
        // Save symptoms
        if (symptoms.isNotEmpty()) {
            val symptomsWithId = symptoms.map { it.copy(prescriptionId = prescriptionId) }
            symptomDao.insertSymptoms(symptomsWithId)
        }
        
        return prescriptionId
    }

    suspend fun updatePrescription(
        prescription: Prescription,
        herbs: List<Herb>,
        usageInstruction: UsageInstruction?
    ) {
        prescriptionDao.updatePrescription(prescription)
        
        // Update herbs - delete old and insert new
        herbDao.deleteHerbsByPrescriptionId(prescription.id)
        herbDao.insertHerbs(herbs)
        
        // Update usage instruction
        usageInstructionDao.deleteUsageInstructionByPrescriptionId(prescription.id)
        usageInstruction?.let {
            usageInstructionDao.insertUsageInstruction(it)
        }
    }

    suspend fun deletePrescription(prescription: Prescription) {
        prescriptionDao.deletePrescription(prescription)
    }

    suspend fun deletePrescriptionById(id: Long) {
        prescriptionDao.deletePrescriptionById(id)
    }

    suspend fun getHerbNames(): List<String> = herbDao.getAllHerbNames()

    suspend fun getSymptoms(): List<String> = symptomDao.getAllSymptoms()

    data class PrescriptionWithDetails(
        val prescription: Prescription,
        val herbs: List<Herb>,
        val usageInstruction: UsageInstruction?,
        val symptoms: List<Symptom> = emptyList()
    )
}
