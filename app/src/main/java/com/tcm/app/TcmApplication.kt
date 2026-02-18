package com.tcm.app

import android.app.Application
import com.tcm.app.data.local.TcmDatabase
import com.tcm.app.data.remote.RetrofitClient
import com.tcm.app.data.repository.AiRepository
import com.tcm.app.data.repository.PrescriptionRepository

class TcmApplication : Application() {
    
    lateinit var database: TcmDatabase
        private set
    
    lateinit var prescriptionRepository: PrescriptionRepository
        private set
    
    lateinit var aiRepository: AiRepository
        private set

    override fun onCreate() {
        super.onCreate()
        
        // Initialize database
        database = TcmDatabase.getDatabase(this)
        
        // Initialize repositories
        prescriptionRepository = PrescriptionRepository(
            prescriptionDao = database.prescriptionDao(),
            herbDao = database.herbDao(),
            usageInstructionDao = database.usageInstructionDao(),
            symptomDao = database.symptomDao()
        )
        
        aiRepository = AiRepository(
            api = RetrofitClient.deepSeekApi,
            chatHistoryDao = database.chatHistoryDao(),
            context = this
        )
    }
}
