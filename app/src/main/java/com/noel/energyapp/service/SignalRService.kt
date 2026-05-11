/**
 * FITXER: SignalRService.kt
 * CAPA: Servei (service)
 *
 * Aquest fitxer implementa un Foreground Service d'Android que manté una connexió
 * persistent amb el servidor a través del protocol SignalR (WebSocket/Long Polling).
 *
 * El servei s'inicia en segon pla quan l'usuari fa login i segueix actiu fins que
 * l'usuari tanca la sessió. La seva única responsabilitat és escoltar el hub SignalR
 * del backend i mostrar una notificació emergent al dispositiu cada vegada que
 * el servidor informa d'una nova incidència.
 *
 * Per garantir que Android no el mati per manca de memòria, utilitza el mode
 * Foreground (amb una notificació de servei actiu a la barra d'estat) i el
 * flag START_STICKY (que el reinicia automàticament si el sistema el tanqués).
 *
 * El filtratge per plantes s'aplica en rebre cada notificació: si l'usuari
 * autenticat no té accés a la planta de l'alarma, es descarta silenciosament.
 */
package com.noel.energyapp.service

// Importació de les classes del sistema per gestionar notificacions i serveis
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import android.util.Log
// Importació de la biblioteca de compatibilitat per construir notificacions correctament
import androidx.core.app.NotificationCompat
// Importació de les classes de SignalR per establir la connexió al hub del servidor
import com.microsoft.signalr.HubConnection
import com.microsoft.signalr.HubConnectionBuilder
import com.microsoft.signalr.TransportEnum
// Importació de les classes pròpies de l'App
import com.noel.energyapp.MainActivity
import com.noel.energyapp.network.RetrofitClient
import com.noel.energyapp.util.SessionManager
// Importació de Kotlin Coroutines per executar crides de xarxa fora del fil principal
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

// Hereda de 'Service' per poder executar-se permanentment en segon pla
class SignalRService : Service() {

    // Objecte que representa la connexió al hub de SignalR del servidor
    private var hubConnection: HubConnection? = null

    // ID del canal de notificació per al servei de fons (icona persistent a la barra d'estat)
    private val CHANNEL_ID = "NoelSignalRChannel"

    // ID del canal de notificació per a les alarmes crítiques emergents (alta prioritat)
    private val ALARM_CHANNEL_ID = "NoelAlarmsChannel"

    // S'executa quan el servei es crea per primera vegada
    override fun onCreate() {
        super.onCreate() // Crida la implementació base del Service
        // Crea els canals de notificació necessaris abans d'enviar cap notificació
        createNotificationChannels()
    }

    // S'executa cada vegada que algú crida 'startService()' o 'startForegroundService()'
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        // Construeix la notificació persistent que mostra que el servei d'alarmes és actiu
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle("Sistema d'Alarmes Actiu")        // Títol de la notificació persistent
            .setContentText("Escoltant incidències en segon pla...") // Subtítol informatiu
            .setSmallIcon(android.R.drawable.ic_dialog_alert)  // Icona del sistema d'alerta
            .build()                                            // Construeix l'objecte Notification

        // A Android 14+ cal especificar el tipus de servei en primer pla
        if (Build.VERSION.SDK_INT >= 34) {
            // Inicia el Foreground Service amb el tipus DATA_SYNC per compatibilitat amb Android 14+
            startForeground(1001, notification, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        } else {
            // En versions anteriors d'Android, l'inici és més senzill
            startForeground(1001, notification)
        }

        // Un cop el Foreground Service és actiu, inicia la connexió SignalR
        startSignalR()

        // START_STICKY indica a Android que si mata el servei per memòria, l'ha de reiniciar
        return START_STICKY
    }

    // Estableix la connexió amb el hub de SignalR del backend i s'inscriu als events
    private fun startSignalR() {
        if (hubConnection != null) {
            Log.d("SignalR", "Connexio SignalR ja inicialitzada.")
            return
        }

        // Crea el SessionManager per accedir al token JWT guardat localment al dispositiu
        val sessionManager = SessionManager(applicationContext)

        // Construeix la connexió al hub SignalR del servidor backend
        hubConnection = HubConnectionBuilder.create("http://172.20.1.46/api/hubs/notificacions")
            // Long Polling és el mètode de transport escollit per major compatibilitat de xarxa
            .withTransport(TransportEnum.LONG_POLLING)
            .build() // Construeix l'objecte de connexió (encara no es connecta)

        // Registra un escoltador per a l'event "RebreNotificacio" que emet el servidor
        // Cada vegada que el servidor crida hubContext.Clients.All.SendAsync("RebreNotificacio", id),
        // aquesta lambda s'executa amb l'ID de la incidència
        hubConnection?.on("RebreNotificacio", { idIncidencia: Int ->
            Log.d("SignalR", "NOVA INCIDÈNCIA REBUDA: $idIncidencia")

            // La lògica de filtratge s'executa en un coroutine d'IO (no bloqueja la UI)
            CoroutineScope(Dispatchers.IO).launch {
                // Obté el token JWT de la sessió actual
                val token = sessionManager.fetchAuthToken()

                if (token.isNullOrEmpty()) {
                    // Si no hi ha sessió activa, mostra la notificació a tothom (fallback segur)
                    mostrarNotificacioCritica(idIncidencia)
                } else {
                    // Si hi ha sessió, filtra l'alarma per les plantes de l'usuari actual
                    try {
                        val response = RetrofitClient.instance.getNotificacioIncidencia("Bearer $token", idIncidencia)
                        val notificacio = response.body()

                        if (response.isSuccessful && notificacio != null) {
                            mostrarNotificacioCritica(
                                id = idIncidencia,
                                title = notificacio.titol.ifBlank { "Nova Incidencia Detectada" },
                                body = notificacio.missatge.ifBlank { "S'ha enregistrat l'alarma #$idIncidencia. Obre per revisar-la." }
                            )
                        } else if (response.code() == 404) {
                            Log.d("SignalR", "Ignorada. L'alarma $idIncidencia no es de cap planta meva.")
                        } else {
                            mostrarNotificacioCritica(idIncidencia)
                        }
                    } catch (e: Exception) {
                        Log.e("SignalR", "Error filtrant notificació", e)
                        // En cas d'error de xarxa al filtrar, es mostra igualment per no perdre l'avisos
                        mostrarNotificacioCritica(idIncidencia)
                    }
                }
            }
        }, Int::class.java) // Indica a SignalR que el paràmetre de l'event és de tipus Int

        // Inicia la connexió al servidor en un coroutine d'IO (és una operació bloquejant)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                // blockingAwait() espera fins que la connexió s'estableix o llança una excepció
                hubConnection?.start()?.blockingAwait()
            } catch (e: Exception) {
                // Registra l'error al Logcat sense esfondrar el servei
                Log.e("SignalR", "Error iniciant el websocket", e)
                hubConnection = null
            }
        }
    }

    // Construeix i mostra una notificació d'alta prioritat per a una incidència nova
    private fun mostrarNotificacioCritica(
        id: Int,
        title: String = "Nova Incidencia Detectada",
        body: String = "S'ha enregistrat l'alarma #$id. Obre per revisar-la."
    ) {
        // Crea la intent per navegar a la MainActivity quan l'usuari toqui la notificació
        val intent = Intent(this, MainActivity::class.java).apply {
            // Tanca totes les activitats al damunt i reinicia la pila de navegació
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        // Crea un PendingIntent immutable (segur) que el sistema pot executar en tocar la notificació
        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)

        // Construeix la notificació emergent amb tots els seus atributs
        val builder = NotificationCompat.Builder(this, ALARM_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.stat_sys_warning) // Icona d'advertència del sistema
            .setContentTitle(title)                            // Titol en negreta de la notificacio
            .setContentText(body)                              // Cos de la notificacio
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_MAX)     // Prioritat màxima: apareix com a popup (Heads-Up)
            .setDefaults(Notification.DEFAULT_ALL)             // Activa so, vibració i llumeta per defecte
            .setContentIntent(pendingIntent)                   // Acció en tocar: obre la MainActivity
            .setAutoCancel(true)                               // Es tanca automàticament en tocar-la

        // Obté el NotificationManager del sistema per enviar la notificació
        val notificationManager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        // Envía la notificació usant l'ID de la incidència com a identificador de la notificació
        notificationManager.notify(id, builder.build())
    }

    // Crea els dos canals de notificació necessaris per al funcionament del servei
    private fun createNotificationChannels() {
        // Obté el gestor de notificacions del sistema
        val manager = getSystemService(NotificationManager::class.java)

        // Canal de baixa importància per a la notificació persistent del servei actiu
        val fgChannel = NotificationChannel(
            CHANNEL_ID,                          // ID únic del canal
            "Servei de fons d'Alarmes",          // Nom visible a la configuració del dispositiu
            NotificationManager.IMPORTANCE_LOW    // Baixa importància: sense so ni popup
        )

        // Canal d'alta importància per a les alarmes crítiques emergents
        val alarmChannel = NotificationChannel(
            ALARM_CHANNEL_ID,                    // ID únic del canal d'alarmes
            "Alarmes Crítiques",                 // Nom visible a la configuració del dispositiu
            NotificationManager.IMPORTANCE_HIGH  // Alta importància: amb so, vibració i popup
        ).apply {
            description = "Avisos d'emergència en comptadors" // Descripció visible a la configuració
            enableVibration(true) // Activa la vibració per defecte per a totes les notificacions d'aquest canal
        }

        // Registra els dos canals al sistema Android (si ja existeixen, no fa res)
        manager?.createNotificationChannel(fgChannel)
        manager?.createNotificationChannel(alarmChannel)
    }

    // S'executa quan el sistema destrueix el servei (logout o tancament de l'App)
    override fun onDestroy() {
        // Atura la connexió SignalR de forma neta per alliberar recursos del servidor
        hubConnection?.stop()
        hubConnection = null
        super.onDestroy() // Crida la implementació base del Service
    }

    // Requerit per la interfície Service però no s'utilitza (el servei no és un Bound Service)
    override fun onBind(intent: Intent): IBinder? {
        return null // Retorna null perquè cap altra classe es lliga a aquest servei
    }
}
