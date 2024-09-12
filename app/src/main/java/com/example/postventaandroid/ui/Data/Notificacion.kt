package com.example.postventaandroid.ui.Data

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.TaskStackBuilder
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.postventaandroid.MainActivity
import com.example.postventaandroid.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext


//Este es el de notificaciones de 1 minuto de intervalo
class Notificacion : BroadcastReceiver() {

    //Puede ser eliminado despues,
    // es para pruebas de fechas validaciones y demas
    private var listGestiones: ArrayList<GestionData> = arrayListOf()
    private var fechasAnadidas: ArrayList<Timestamp> = arrayListOf()

    companion object {
        const val NOTIFICATION_ID = 1
        const val NOTIFICATION_ID2 = 1
        private const val MAX_NOTIFICATIONS = 5
        private const val CHANNEL_ID = "alertasChannel"
        private const val SECONDCHANNEL_ID = "gestionChannel"
    }

    //Primera instancia (no borrar por eso se deja comentada)
    /*override fun onReceive(context: Context, intent: Intent?) {
        if (intent?.action == "com.example.postventaandroid.ACTION_START_ALARM") {
            // Aquí se ejecuta la lógica para manejar las notificaciones
            val pendingResult = goAsync()
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch {
                val arrayListNotificacion = fetchDataFromSqlServer(context)
                handleNotifications(context, arrayListNotificacion)
            }

        }
    }*/

    override fun onReceive(context: Context, intent: Intent) = goAsync {
        if (intent?.action == "com.example.postventaandroid.ACTION_START_ALARM") {
            // Aquí se ejecuta la lógica para manejar las notificaciones
            val pendingResult = goAsync()
            @OptIn(DelicateCoroutinesApi::class)
            GlobalScope.launch {
                val arrayListNotificacion = fetchDataFromSqlServer(context)
                val arrayDeadline = getGestionData(context)
                handleNotifications(context, arrayListNotificacion)
                handleNotificationsDeadline(context, arrayDeadline)
            }
        }
    }

    fun BroadcastReceiver.goAsync(
        context: CoroutineContext = EmptyCoroutineContext,
        block: suspend CoroutineScope.() -> Unit
    ) {
        val pendingResult = goAsync()
        @OptIn(DelicateCoroutinesApi::class) // Must run globally; there's no teardown callback.
        GlobalScope.launch(context) {
            try {
                block()
            } finally {
                pendingResult.finish()
            }
        }
    }

    // Manejador de notificaciones de alertas recibidas
    private fun handleNotifications(context: Context, notifications: ArrayList<NotificacionData>) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Crear canal de notificación si es necesario (para Android O (Oreo) y superiores)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channelAlertas = NotificationChannel(
                CHANNEL_ID,
                "Alertas por Supervisor",
                NotificationManager.IMPORTANCE_LOW
            )

            notificationManager.createNotificationChannel(channelAlertas)
        }

        // Mostrar cada notificación secuencialmente
        notifications.forEachIndexed { index, notificationData ->
            val notificationId = NOTIFICATION_ID + index

            // Create an Intent for the activity you want to start.
            val resultIntent = Intent(context, MainActivity::class.java)
            // Create the TaskStackBuilder.
            val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
                // Add the intent, which inflates the back stack.
                addNextIntentWithParentStack(resultIntent)
                // Get the PendingIntent containing the entire back stack.
                getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }

            val notification = NotificationCompat.Builder(context, CHANNEL_ID)
                .setSmallIcon(R.mipmap.codelco_icon)                                // Reemplaza el icono que representa en la notificacion
                .setContentTitle("ALERTA: Sector ${notificationData.sector}")       //Titulo de notificacion
                .setContentText("Nivel de la Alerta: ${notificationData.nivelAlerta}")  //Contenido del texto
                .setStyle(NotificationCompat.BigTextStyle().bigText("Descripción: ${notificationData.descripcion}")) //Para descripcion
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setContentIntent(resultPendingIntent)  // Establece el Intentpending para abrir MiPerfilFragment
                .setAutoCancel(true)  // Cancelar la notificación al tocar
                .build()

            notificationManager.notify(notificationId, notification)
        }
    }

    // Manejador de notificaciones de alertas a vencer
    private fun handleNotificationsDeadline(context: Context, notifications: ArrayList<GestionData>) {
        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        //Devolvemos el total de gestiones que venceran pronto
        val totalDeadline = notifications.count()
        if(totalDeadline >= 1){

            // Crear canal de notificación si es necesario (para Android O (Oreo) y superiores)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                val channelGestiones = NotificationChannel(
                    SECONDCHANNEL_ID,
                    "Vencimiento Gestiones",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                notificationManager.createNotificationChannel(channelGestiones)
            }

            // Mostrar cada notificación secuencialmente
            val notificationId = NOTIFICATION_ID2

            // Create an Intent for the activity you want to start.
            val resultIntent = Intent(context, MainActivity::class.java)
            // Create the TaskStackBuilder.
            val resultPendingIntent: PendingIntent? = TaskStackBuilder.create(context).run {
                // Add the intent, which inflates the back stack.
                addNextIntentWithParentStack(resultIntent)
                // Get the PendingIntent containing the entire back stack.
                getPendingIntent(0,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE)
            }

            val notification = NotificationCompat.Builder(context, SECONDCHANNEL_ID)
                .setSmallIcon(R.mipmap.codelco_icon)                                // Reemplaza el icono que representa en la notificacion
                .setContentTitle("Tienes $totalDeadline tickets por vencer ")       //Titulo de notificacion
                //.setContentText("Fecha vencimiento:")  //Contenido del texto
                //.setStyle(NotificationCompat.BigTextStyle().bigText("Descripción: ")) //Para descripcion
                .setPriority(NotificationCompat.PRIORITY_MAX)
                .setContentIntent(resultPendingIntent)  // Establece el Intentpending para abrir MiPerfilFragment
                .setAutoCancel(true)  // Cancelar la notificación al tocar
                .build()

            notificationManager.notify(notificationId, notification)
        }else{
            println("Nada que mostrar")
        }


    }

    // Obtener datos desde el servidor SQL
    suspend private fun fetchDataFromSqlServer(context: Context): ArrayList<NotificacionData> {
        return withContext(Dispatchers.IO){
            val sessionManager = SessionManager(context)
            val supervisor = sessionManager.fetchUser()?.nombre

            val query = "[dbo].[_1_NotificationWorker] ?"

            val datasetArrayList = arrayListOf<NotificacionData>()
            try {
                val connection = ConnSQL().dbConn() ?: throw SQLException("Error connecting to database")
                val statement = connection.prepareStatement(query).apply {
                    setString(1, supervisor)
                }
                val resultSet = statement.executeQuery()

                while (resultSet.next()) {
                    val hallazgoData = resultSet.toNotificacionData()
                    datasetArrayList.add(hallazgoData)
                }
                resultSet.close()
                statement.close()
                connection.close()

            } catch (e: SQLException) {
                e.printStackTrace()
                // manejo de error específico de SQL
            } catch (e: Exception) {
                e.printStackTrace()
                // manejo de error general
            }
            return@withContext datasetArrayList
        }
    }

    private fun ResultSet.toNotificacionData(): NotificacionData {
        return NotificacionData(
            getString(1),  // Sector
            getString(2),  // Nivel de Alerta
            getString(3)   // Descripción
        )
    }


    suspend private fun getGestionData(context: Context): ArrayList<GestionData> {
        return withContext(Dispatchers.IO) {
            val sessionManager = SessionManager(context)
            val cargo = sessionManager.fetchUser()?.cargoTrabajador

            val datasetArrayList = arrayListOf<GestionData>()
            try {
                if (cargo == "Supervisor" && cargo.isNotEmpty()){
                    val query = "[dbo].[_1_ConteosDeadline]"
                    val statement = ConnSQL().dbConn()?.prepareStatement(query)
                    val resu = statement?.executeQuery()

                    while (resu!!.next()) {
                        datasetArrayList.add(resu.toGestionData())
                    }

                }else{
                    println("No existe perfil supervisor")
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } catch (x: SQLException) {
                x.printStackTrace()
            }
            return@withContext datasetArrayList
        }
    }

    private fun ResultSet.toGestionData(): GestionData {
        return GestionData(
            getInt(1),      //ID de la gestion
            getTimestamp(2),   //Fecha inicial de la gestion
            getString(3),   //Control de riesgo
            getTimestamp(4),   //Tiempo final asignado(deadline)
            getString(5),   //Responsable
            getString(6),   //Detalles
            getInt(7),      //ID del hallazgo asignado a esta gestion
            getString(8),    //trae informacion sobre si es Deadline derivado o no aplica, y hrs asignadas
            getString(9)
        )
    }


}

// Clase para los datos de notificación
data class NotificacionData(
    val sector: String,
    val nivelAlerta: String,
    val descripcion: String
)
