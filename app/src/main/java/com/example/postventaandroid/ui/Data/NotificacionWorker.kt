package com.example.postventaandroid.ui.Data

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class NotificacionWorker(context: Context, params: WorkerParameters) :
    CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        return withContext(Dispatchers.IO) {
            try {
                val notificacion = Notificacion()
                notificacion.onReceive(applicationContext, null!!)
                Result.success()
            } catch (e: Exception) {
                Result.failure()
            }
        }
    }
}