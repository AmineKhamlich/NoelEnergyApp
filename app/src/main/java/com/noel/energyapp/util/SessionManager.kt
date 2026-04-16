/**
 * FITXER: SessionManager.kt
 * CAPA: Utilitats (util)
 *
 * Aquest fitxer implementa el gestor de sessió de l'aplicació, que s'encarrega
 * de persistir i recuperar les dades de l'usuari autenticat al dispositiu local.
 *
 * Utilitza SharedPreferences (una petita base de dades clau-valor del sistema Android)
 * per guardar el token JWT, les dades del perfil, les plantes assignades i les
 * preferències d'interfície. D'aquesta manera, l'usuari no ha de tornar a iniciar
 * sessió cada vegada que obre l'aplicació.
 *
 * Totes les claus de SharedPreferences estan definides com a constants al
 * companion object, cosa que evita errors tipogràfics i facilita el manteniment.
 *
 * Quan l'usuari tanca la sessió, el mètode 'clearUserData()' esborra tot el
 * contingut de les preferències per garantir la seguretat.
 */
package com.noel.energyapp.util

// Importació del Context d'Android necessari per accedir a les SharedPreferences
import android.content.Context
// Importació de la interfície SharedPreferences per llegir i escriure preferències persistents
import android.content.SharedPreferences
// Importació de l'extensió 'edit' de la biblioteca de compatibilitat per a SharedPreferences
import androidx.core.content.edit
// Importació per obtenir l'hora actual del dispositiu
import java.time.LocalTime
import java.time.format.DateTimeFormatter

// Classe que encapsula tota la lògica de persistència de la sessió de l'usuari
// Rep un 'context' de l'activitat o servei que l'instancia
class SessionManager(context: Context) {

    // Instancia les SharedPreferences amb un nom de fitxer privat exclusiu de l'App
    // MODE_PRIVATE garanteix que cap altra aplicació pot llegir aquest fitxer
    private val prefs: SharedPreferences =
        context.getSharedPreferences("NoelEnergyPrefs", Context.MODE_PRIVATE)

    // Companion object: conté les constants de les claus de SharedPreferences
    // Declares com a 'const val' per a màxim rendiment en temps de compilació
    companion object {
        const val USER_ID = "user_id"                   // Clau per a l'ID numèric de l'usuari
        const val USER_TOKEN = "user_token"              // Clau per al token JWT d'autenticació
        const val USER_NAME = "user_name"                // Clau per al nick d'usuari (ex: "joanpetit")
        const val USER_REAL_NAME = "user_real_name"      // Clau per al nom real (ex: "Joan Petit")
        const val USER_ROLE = "user_role"                // Clau per al rol textual (ex: "TECNIC")
        const val USER_ROLE_ID = "user_role_id"          // Clau per a l'ID numèric del rol (1, 2, 3)
        const val ASSIGNED_PLANTS = "assigned_plants"    // Clau per als IDs de plantes (ex: "1,3,7")
        const val MUST_CHANGE_PASSWORD = "must_change_password" // Clau per al flag de canvi obligatori

        // Constants per a les preferències d'interfície d'usuari
        const val THEME_PREFERENCE = "theme_preference"  // Tema visual: "LIGHT", "DARK" o "AUTO"
        const val ANIMATIONS_ENABLED = "animations_enabled" // Animacions actives: true o false
        const val DEFAULT_PLANT_ID = "default_plant_id"  // ID de la planta per defecte del Dashboard

        // Clau per guardar les hores de tancament forçat de sessió (ex: "22:00,06:00")
        const val FORCED_LOGOUT_TIMES = "forced_logout_times"
    }

    /**
     * Guarda en un bloc atòmic totes les dades rebudes de l'API en un login correcte.
     * S'utilitza just després de rebre la resposta exitosa del servidor.
     */
    fun saveUserData(
        userId: Int,             // ID únic de l'usuari a la base de dades del servidor
        token: String,           // Token JWT per autenticar les peticions posteriors
        name: String,            // Nick de login de l'usuari
        realName: String,        // Nom i cognom reals per mostrar al Dashboard
        role: String,            // Rol textual (ex: "ADMIN", "TECNIC")
        roleId: Int,             // ID numèric del rol per comparar permisos
        assignedPlants: String?, // Text de plantes assignades (ex: "1,3,7"), pot ser null
        mustChangePassword: Boolean // Indica si l'usuari ha de canviar la contrasenya en el proper login
    ) {
        // 'edit' aplica tots els canvis de cop de forma efficiente i atòmica
        prefs.edit {
            putInt(USER_ID, userId)                        // Guarda l'ID numèric de l'usuari
            putString(USER_TOKEN, token)                    // Guarda el token JWT
            putString(USER_NAME, name)                     // Guarda el nick d'usuari
            putString(USER_REAL_NAME, realName)            // Guarda el nom real
            putString(USER_ROLE, role)                     // Guarda el rol textual
            putInt(USER_ROLE_ID, roleId)                   // Guarda l'ID numèric del rol
            putString(ASSIGNED_PLANTS, assignedPlants)     // Guarda els IDs de plantes com a text
            putBoolean(MUST_CHANGE_PASSWORD, mustChangePassword) // Guarda el flag de canvi obligatori
        }
    }

    /**
     * Elimina el flag de canvi de contrasenya obligatori un cop l'usuari ha completat el canvi.
     * Permet que el NavHost enviï l'usuari al Dashboard en el proper inici de l'App.
     */
    fun clearMustChangePasswordFlag() {
        // Actualitza únicament el flag de canvi obligatori a 'false' sense tocar la resta de dades
        prefs.edit { putBoolean(MUST_CHANGE_PASSWORD, false) }
    }

    // --- GETTERS: Recuperació de les dades guardades ---

    /** Retorna l'ID numèric de l'usuari; retorna -1 si no hi ha sessió activa */
    fun fetchUserId(): Int = prefs.getInt(USER_ID, -1)

    /** Retorna el Token JWT per adjuntar-lo com a capçalera a les peticions API; null si no hi ha sessió */
    fun fetchAuthToken(): String? = prefs.getString(USER_TOKEN, null)

    /** Retorna el nick d'usuari de login; null si no hi ha sessió */
    fun fetchUserName(): String? = prefs.getString(USER_NAME, null)

    /** Retorna el Nom i Cognom reals per mostrar a la salutació del Dashboard; null si no hi ha sessió */
    fun fetchUserRealName(): String? = prefs.getString(USER_REAL_NAME, null)

    /** Retorna el rol textual de l'usuari (ex: "ADMIN") per controlar la visibilitat de pantalles; null si no hi ha sessió */
    fun fetchUserRole(): String? = prefs.getString(USER_ROLE, null)

    /** Retorna l'ID numèric del rol (1=ADMIN, 2=SUPERVISOR, 3=TECNIC) per a comparació de permisos; -1 si no hi ha sessió */
    fun fetchUserRoleId(): Int = prefs.getInt(USER_ROLE_ID, -1)

    /** Retorna true si l'usuari té el canvi de contrasenya pendent; false per defecte */
    fun fetchMustChangePassword(): Boolean = prefs.getBoolean(MUST_CHANGE_PASSWORD, false)

    /**
     * Recupera la llista d'IDs de plantes assignades com a llista d'enters.
     * El text guardat (ex: "1,3,7") es separa per comes i cada part es converteix a Int.
     * Retorna una llista buida si no hi ha plantes assignades o si la sessió no existeix.
     */
    fun fetchAssignedPlants(): List<Int> {
        // Obté el text de plantes guardat; retorna string buit si no existeix
        val idsString = prefs.getString(ASSIGNED_PLANTS, "") ?: ""
        // Si el text és buit, retorna una llista buida directament
        if (idsString.isEmpty()) return emptyList()
        // Separa per comes, elimina espais i converteix a Int, descartant els que no siguin vàlids
        return idsString.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    // --- Preferències d'interfície d'usuari ---

    /** Guarda la preferència de tema visual escollida per l'usuari ("LIGHT", "DARK" o "AUTO") */
    fun saveThemePreference(theme: String) {
        prefs.edit { putString(THEME_PREFERENCE, theme) }
    }

    /** Recupera la preferència de tema; retorna "AUTO" per defecte (segueix el sistema) */
    fun fetchThemePreference(): String = prefs.getString(THEME_PREFERENCE, "AUTO") ?: "AUTO"

    /** Guarda si les animacions de la UI estan activades per l'usuari */
    fun saveAnimationsEnabled(enabled: Boolean) {
        prefs.edit { putBoolean(ANIMATIONS_ENABLED, enabled) }
    }

    /** Recupera si les animacions estan activades; true per defecte */
    fun fetchAnimationsEnabled(): Boolean = prefs.getBoolean(ANIMATIONS_ENABLED, true)

    /** Guarda l'ID de la planta per defecte que l'usuari ha seleccionat */
    fun saveDefaultPlant(plantaId: Int) {
        prefs.edit { putInt(DEFAULT_PLANT_ID, plantaId) }
    }

    /** Recupera l'ID de la planta per defecte; -1 si no n'hi ha cap seleccionada */
    fun fetchDefaultPlant(): Int = prefs.getInt(DEFAULT_PLANT_ID, -1)

    /**
     * Esborra totes les dades persistides de la sessió.
     * S'utilitza quan l'usuari clica "Tancar Sessió" per garantir que no queden
     * dades sensibles al dispositiu i que el proper inici de l'App mostrarà el Login.
     */
    fun clearUserData() {
        prefs.edit {
            // Conservem les preferències d'interfície i les hores de logout (no les esborrem al logout)
            val theme = prefs.getString(THEME_PREFERENCE, "AUTO")
            val animations = prefs.getBoolean(ANIMATIONS_ENABLED, true)
            val logoutTimes = prefs.getString(FORCED_LOGOUT_TIMES, "")
            clear() // Elimina TOTES les claus i valors del fitxer de preferències
            // Restaurem les preferències que volem conservar entre sessions
            putString(THEME_PREFERENCE, theme)
            putBoolean(ANIMATIONS_ENABLED, animations)
            putString(FORCED_LOGOUT_TIMES, logoutTimes)
        }
    }

    // --- Hores de tancament forçat de sessió ---

    /**
     * Guarda la llista d'hores de tancament forçat.
     * Les hores es guarden com a text separat per comes (ex: "22:00,06:00").
     */
    fun saveForcedLogoutTimes(times: Set<String>) {
        // Uneix la llista d'hores amb comes i la guarda com a string únic
        prefs.edit { putString(FORCED_LOGOUT_TIMES, times.joinToString(",")) }
    }

    /**
     * Recupera la llista d'hores de tancament forçat com a conjunt de Strings (ex: {"22:00", "06:00"}).
     * Retorna un conjunt buit si no n'hi ha cap configurada.
     */
    fun fetchForcedLogoutTimes(): Set<String> {
        val raw = prefs.getString(FORCED_LOGOUT_TIMES, "") ?: ""
        // Si el text és buit no hi ha hores configurades
        if (raw.isBlank()) return emptySet()
        // Separa per comes, neteja espais i retorna com a Set per evitar duplicats
        return raw.split(",").map { it.trim() }.filter { it.isNotBlank() }.toSet()
    }

    /**
     * Comprova si l'hora actual coincideix amb alguna de les hores de logout forçat configurades.
     * La comparació es fa per minut exacte (HH:mm).
     * Retorna 'true' si s'ha de forçar el tancament de sessió ara mateix.
     */
    fun shouldForceLogout(): Boolean {
        val times = fetchForcedLogoutTimes()
        if (times.isEmpty()) return false // Si no hi ha hores configurades, mai es forçarà
        // Obté l'hora actual del dispositiu en format "HH:mm"
        val currentTime = LocalTime.now().format(DateTimeFormatter.ofPattern("HH:mm"))
        // Comprova si l'hora actual (arrodonida al minut) coincideix amb alguna hora configurada
        return times.contains(currentTime)
    }
}