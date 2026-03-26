package com.noel.energyapp.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.noel.energyapp.MainActivity
import com.noel.energyapp.R

class MyFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val title = remoteMessage.notification?.title ?: "Nova Alarma"
        val body = remoteMessage.notification?.body ?: "S'ha superat un límit de consum."
        mostrarNotificacio(title, body)
    }

    /**
     * S'executa quan Firebase genera un nou token per al dispositiu.
     * Necessari per poder enviar notificacions dirigides al dispositiu concret.
     * En el futur, aquí s'hauria d'enviar el token al servidor (WConsumsAPI).
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM_TOKEN", "Nou token generat: $token")
        // TODO (futur): Enviar este token al backend via Retrofit
        // aixi el servidor sap a quin dispositiu enviar les push notifications
        // Exemple: RetrofitClient.instance.saveFcmToken("Bearer $savedToken", token)
    }

    private fun mostrarNotificacio(title: String, body: String) {
        val channelId = "alarmes_scada"

        // 1. Obtenim el NotificationManager correctament (des del propi Service/Context)
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // 2. Creem el canal PRIMER (obligatori Android 8+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Alarmes SCADA",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notificacions d'alarmes i límits de consum del sistema SCADA"
                enableVibration(true)
            }
            notificationManager.createNotificationChannel(channel)
        }

        // 3. PendingIntent que obre la MainActivity en tocar la notificació
        val intent = Intent(this, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent, PendingIntent.FLAG_IMMUTABLE
        )

        // 4. Construïm la notificació
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .build()

        // 5. Enviem la notificació
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}