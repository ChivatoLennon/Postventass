package com.example.postventaandroid.ui.Data

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.RemoteViews
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.postventaandroid.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

//Sservicio de notificaciones de 1 minuto intervalo
class FcmService : FirebaseMessagingService(){

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d(TAG, "Token actualizado: $token")
        //Envia el token al servidor

    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d(TAG, "Message data: ${remoteMessage.data}")
        // Muestra la notificacion push

        val title = remoteMessage.notification!!.title
        val message = remoteMessage.notification!!.body

        val notificationLayout = RemoteViews(packageName, R.layout.notification_layout)
        notificationLayout.setTextViewText(R.id.notification_title, title)
        notificationLayout.setTextViewText(R.id.notification_message, message)

        /*val dismissIntent = Intent(this, NotificationActionReceiver::class.java)
        dismissIntent.action = "DISMISS"
        val dismissPendingIntent = PendingIntent.getBroadcast(this, 0, dismissIntent, PendingIntent.FLAG_UPDATE_CURRENT)*/

        val notificationBuilder = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.logo_codelco)
            .setStyle(NotificationCompat.DecoratedCustomViewStyle())
            .setCustomContentView(notificationLayout)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .build()

        // Display custom push notification

    }

    override fun onDeletedMessages() {
        super.onDeletedMessages()
        Log.d(TAG, "Device not registered")
    }

    override fun onSendError(messageId: String, exception: Exception) {
        super.onSendError(messageId, exception)
        Log.d(TAG, "Invalid server key: $exception")
        Log.d(TAG, "Network error: $exception")
    }

    private fun crearCanalNotificacion(){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            val canalImportancia = NotificationManager.IMPORTANCE_HIGH
            val canal = NotificationChannel(CHANNEL_NUM_ID, CHANNEL_ID, canalImportancia)
            val a = Context.NOTIFICATION_SERVICE

            val manager = getSystemService(a) as NotificationManager
            manager.createNotificationChannel(canal)

        }
    }

    private fun crearNotificacion(){
        val notificacion = NotificationCompat.Builder(this, CHANNEL_NUM_ID).also {
            it.setContentTitle("Notificacion")
            it.setContentText("Cuerpo de la notificacion")
            it.setSmallIcon(R.drawable.logo_codelco)
            it.priority = NotificationCompat.PRIORITY_HIGH
            it.setAutoCancel(true)
        }.build()

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
        notificationManager.notify(NOTIFICATION_ID, notificacion)
    }


    companion object{
        private const val TAG = "MyFirebaseMessagingServicee"
        private const val CHANNEL_ID = "my_channel_id_01"
        private const val CHANNEL_NUM_ID = "1"
        private const val NOTIFICATION_ID = 0
    }
}