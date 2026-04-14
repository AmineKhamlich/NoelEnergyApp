/**
 * FITXER: MyFirebaseMessagingService.kt
 * CAPA: Servei (service)
 *
 * Aquest fitxer implementa el servei de recepció de notificacions push (FCM)
 * de Google Firebase. S'utilitza per rebre alertes del servidor fins i tot
 * quan l'aplicació està tancada o en segon pla.
 *
 * És un complement al SignalRService: Firebase és el canal de notificació urgent
 * per a dispositius en repòs (quan SignalR no pot mantenir una connexió activa).
 *
 * Mètodes principals:
 * - 'onMessageReceived': S'executa quan arriba una nova notificació push del servidor.
 * - 'onNewToken': S'executa quan Firebase genera o refresca el token únic del dispositiu.
 * - 'mostrarNotificacio': Construeix i mostra la notificació visible a l'usuari.
 *
 * NOTA FUTURA: Cal implementar l'enviament del token FCM al backend per poder
 * enviar notificacions dirigides a dispositius concrets.
 */
package com.noel.energyapp.service

// Importació del gestor de notificacions del sistema Android
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import android.util.Log
// Importació de la biblioteca de compatibilitat per construir notificacions en versions antigues d'Android
import androidx.core.app.NotificationCompat
// Importació de Firebase Messaging per escoltar missatges push entrants
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
// Importació de la pantalla principal per navegar-hi quan l'usuari toca la notificació
import com.noel.energyapp.MainActivity
// Importació dels recursos gràfics de l'App (icones, etc.)
import com.noel.energyapp.R

// La classe hereda de FirebaseMessagingService per poder rebre missatges push en segon pla
class MyFirebaseMessagingService : FirebaseMessagingService() {

    // S'executa automàticament cada vegada que arriba un missatge push de Firebase Cloud Messaging
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Extreu el títol de la notificació del missatge rebut; usa un valor per defecte si no n'hi ha
        val title = remoteMessage.notification?.title ?: "Nova Alarma"
        // Extreu el cos del text de la notificació; usa un valor per defecte si falta
        val body = remoteMessage.notification?.body ?: "S'ha superat un límit de consum."
        // Crida la funció interna per mostrar la notificació a la barra de notificacions del dispositiu
        mostrarNotificacio(title, body)
    }

    /**
     * S'executa quan Firebase genera un nou token d'identificació per a aquest dispositiu.
     * Cada dispositiu té un token únic que el servidor usa per saber on enviar les notificacions.
     * En el futur, aquest token s'hauria d'enviar al backend per mantenir-lo actualitzat.
     */
    override fun onNewToken(token: String) {
        super.onNewToken(token) // Crida la implementació base obligatòria de Firebase
        // Registra el nou token al Logcat per a depuració i verificació
        Log.d("FCM_TOKEN", "Nou token generat: $token")
        // TODO (futur): Enviar el token al backend perquè pugui dirigir les notificacions a aquest dispositiu
        // Exemple pendent: RetrofitClient.instance.saveFcmToken("Bearer $savedToken", token)
    }

    // Construeix i mostra una notificació al sistema Android amb el títol i el cos especificats
    private fun mostrarNotificacio(title: String, body: String) {
        // ID del canal de notificació (obligatori a Android 8+ per classificar les notificacions)
        val channelId = "alarmes_scada"

        // Obté el servei de notificacions del sistema Android per poder enviar i gestionar notificacions
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // A Android 8 (Oreo) i versions posteriors és obligatori crear el canal primer
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,                          // ID únic del canal (ha de coincidir amb el de la notificació)
                "Alarmes SCADA",                    // Nom visible per l'usuari a Configuració → Notificacions
                NotificationManager.IMPORTANCE_HIGH // Prioritat alta: sona, vibra i apareix com a popup
            ).apply {
                // Descripció visible a la configuració de notificacions del dispositiu
                description = "Notificacions d'alarmes i límits de consum del sistema SCADA"
                // Activa la vibració per defecte per a les notificacions d'aquest canal
                enableVibration(true)
            }
            // Registra el canal al sistema (si ja existeix, no fa res)
            notificationManager.createNotificationChannel(channel)
        }

        // Crea la intent que obrirà la MainActivity quan l'usuari toqui la notificació
        val intent = Intent(this, MainActivity::class.java).apply {
            // FLAG_ACTIVITY_CLEAR_TOP: tanca les pantalles al damunt i va a la pantalla principal
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        }
        // Crea un PendingIntent (intent diferida) que el sistema pot executar en nom de l'App
        val pendingIntent = PendingIntent.getActivity(
            this,                        // Context del servei
            0,                           // Codi de petició (0 perquè n'hi ha una sola)
            intent,                      // La intent que s'executarà en tocar la notificació
            PendingIntent.FLAG_IMMUTABLE // FLAG de seguretat: evita que es modifiqui externament
        )

        // Construeix la notificació amb el títol, el cos i l'acció en tocar-la
        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.ic_launcher_foreground) // Icona petita a la barra d'estat
            .setContentTitle(title)                          // Títol gran de la notificació
            .setContentText(body)                            // Cos del text de la notificació
            .setAutoCancel(true)                             // Es tanca automàticament en tocar-la
            .setPriority(NotificationCompat.PRIORITY_HIGH)   // Alta prioritat per a versió pre-Oreo
            .setContentIntent(pendingIntent)                  // Acció en tocar: obre la MainActivity
            .build()                                         // Construeix l'objecte Notification final

        // Mostra la notificació al dispositiu amb un ID únic basat en el timestamp actual
        // L'ús del timestamp com a ID evita que cada nova notificació sobreescrigui l'anterior
        notificationManager.notify(System.currentTimeMillis().toInt(), notification)
    }
}