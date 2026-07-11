package com.anchor.procurement

import android.app.Application
import com.anchor.procurement.data.AppDatabase
import com.anchor.procurement.data.Repository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch

class AnchorApp : Application() {
    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    lateinit var repository: Repository
        private set

    override fun onCreate() {
        super.onCreate()
        val db = AppDatabase.get(this)
        repository = Repository(db)
        applicationScope.launch { repository.ensureSeeded() }
    }
}
