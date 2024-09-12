package com.example.postventaandroid.ui.Data

import android.Manifest
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.postventaandroid.MainActivity
import com.example.postventaandroid.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


// Clase que extiende FirebaseMessagingService para manejar los mensajes de Firebase Cloud Messaging (FCM)
//Servicio de firebase para conectar y recibir notificaciones en tiempo real
class MyFirebaseMessagingService : FirebaseMessagingService() {

    // Método para crear el canal de notificación en dispositivos con Android Oreo (API 26) y superior
    companion object {
        private const val TAG = "MyFirebaseMessagingServicee" // Tag para logs
        const val CHANNEL_ID = "my_channel_id_01" // ID del canal de notificación
        const val CHANNEL_NAME = "My Notification Channel" // Nombre del canal de notificación
        const val CHANNEL_DESCRIPTION = "Channel for my app notifications" // Descripción del canal de notificación
        private const val NOTIFICATION_ID = 0 // ID de la notificación
    }

    // Este método se llama cuando se actualiza el token de FCM
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        if (token.isNotEmpty()){
            Log.d(TAG, "Token actualizado: $token")
        }else{
            Log.d(TAG, "Token vacio no hay nada error: $token")
        }
        // Aquí puedes enviar el nuevo token a tu servidor si es necesario

    }

    /*override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        Looper.prepare()

        Handler().post{
            Toast.makeText(baseContext, message.notification?.title, Toast.LENGTH_SHORT).show()
        }

        Looper.loop()
    }*/

    // Este método se llama cuando se recibe un mensaje FCM
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Message data: ${remoteMessage.data}")
        // Procesa el mensaje recibido y muestra una notificación push

        try {
            // Obtén el título y el cuerpo de la notificación del mensaje
            val title = remoteMessage.notification?.title ?: "Título predeterminado"
            val message = remoteMessage.notification?.body ?: "Mensaje predeterminado"

            // Configura la vista personalizada de la notificación
            val notificationLayout = RemoteViews(packageName, R.layout.notification_layout)
            notificationLayout.setTextViewText(R.id.notification_title, title)
            notificationLayout.setTextViewText(R.id.notification_message, message)

            // Crear un Intent que dirija a la actividad deseada al hacer clic en la notificación
            val intent = Intent(this, MainActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

            // Construye la notificación con el canal de notificación, icono, vista personalizada y prioridad
            val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.logo_codelco) // Icono de la notificación
                .setStyle(NotificationCompat.DecoratedCustomViewStyle()) // Estilo de vista personalizada
                .setCustomContentView(notificationLayout) // Vista personalizada
                .setPriority(NotificationCompat.PRIORITY_DEFAULT) // Prioridad de la notificación
                .setContentIntent(pendingIntent)  // Configurar el PendingIntent
                .setAutoCancel(true)  // Cancelar la notificación al hacer clic
                .build()

            // Muestra la notificación usando NotificationManagerCompat
            val notificationManager = NotificationManagerCompat.from(this)
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                // TODO: Consider calling
                //    ActivityCompat#requestPermissions
                // here to request the missing permissions, and then overriding
                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                //                                          int[] grantResults)
                // to handle the case where the user grants the permission. See the documentation
                // for ActivityCompat#requestPermissions for more details.
                return
            }
            notificationManager.notify(NOTIFICATION_ID, notificationBuilder)

        } catch (x: Exception) {
            // Maneja cualquier excepción que ocurra durante la construcción o visualización de la notificación
            x.printStackTrace()
        }
    }



    // Este método se llama cuando se eliminan mensajes de FCM
    override fun onDeletedMessages() {
        super.onDeletedMessages()
        Log.d(TAG, "Device not registered")
    }

    // Este método se llama cuando hay un error al enviar un mensaje
    override fun onSendError(messageId: String, exception: Exception) {
        super.onSendError(messageId, exception)
        Log.d(TAG, "Invalid server key: $exception")
        Log.d(TAG, "Network error: $exception")
    }



}