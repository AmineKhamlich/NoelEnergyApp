package com.noel.energyapp.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

/**
 * GESTOR DE SESSIÓ (SessionManager)
 * Aquesta classe gestiona les "SharedPreferences", que és una petita base de dades local
 * dins del telèfon on guardem el Token i les dades de l'usuari perquè no hagi de fer
 * login cada vegada que obre l'aplicació.
 */
class SessionManager(context: Context) {

    // Nom de l'arxiu de preferències que es guardarà de forma privada al dispositiu
    private val prefs: SharedPreferences =
        context.getSharedPreferences("NoelEnergyPrefs", Context.MODE_PRIVATE)

    companion object {
        // Claus constants per evitar errors de picat en el codi
        const val USER_ID = "user_id"
        const val USER_TOKEN = "user_token"
        const val USER_NAME = "user_name"           // Nick de l'usuari (ex: admin)
        const val USER_REAL_NAME = "user_real_name" // Nom real (ex: Joan Petit) - NOU!
        const val USER_ROLE = "user_role"
        const val ASSIGNED_PLANTS = "assigned_plants"
        const val MUST_CHANGE_PASSWORD = "must_change_password"
    }

    /**
     * Guarda TOTA la informació del login de cop.
     * Es crida just després d'un login correcte contra l'API.
     */
    fun saveUserData(
        userId: Int,
        token: String,
        name: String,
        realName: String, // NOU: Passem el nom real
        role: String,
        assignedPlants: String?,
        mustChangePassword: Boolean
    ) {
        prefs.edit {
            putInt(USER_ID, userId)
            putString(USER_TOKEN, token)
            putString(USER_NAME, name)
            putString(USER_REAL_NAME, realName) // Guardem el nom real per a la salutació
            putString(USER_ROLE, role)
            putString(ASSIGNED_PLANTS, assignedPlants)
            putBoolean(MUST_CHANGE_PASSWORD, mustChangePassword)
        }
    }

    /**
     * Actualitza l'estat de la contrasenya.
     * Quan l'usuari completa el canvi obligatori, posem aquest flag a 'false'
     * perquè l'App el deixi passar al Dashboard.
     */
    fun clearMustChangePasswordFlag() {
        prefs.edit { putBoolean(MUST_CHANGE_PASSWORD, false) }
    }

    // --- GETTERS: Recuperació de dades guardades ---

    /** Retorna l'ID numèric de l'usuari de la base de dades */
    fun fetchUserId(): Int = prefs.getInt(USER_ID, -1)

    /** Retorna el Token JWT per adjuntar-lo a les capçaleres de les crides API */
    fun fetchAuthToken(): String? = prefs.getString(USER_TOKEN, null)

    /** Retorna el nick de l'usuari (nom d'usuari de login) */
    fun fetchUserName(): String? = prefs.getString(USER_NAME, null)

    /** Retorna el Nom i Cognom reals per a la salutació del Dashboard */
    fun fetchUserRealName(): String? = prefs.getString(USER_REAL_NAME, null)

    /** Retorna el Rol (ADMIN, SUPERVISOR, TÈCNIC) per gestionar permisos de pantalles */
    fun fetchUserRole(): String? = prefs.getString(USER_ROLE, null)

    /** Retorna si l'usuari té pendent el canvi de contrasenya obligatori */
    fun fetchMustChangePassword(): Boolean = prefs.getBoolean(MUST_CHANGE_PASSWORD, false)

    /**
     * Recupera el text de plantes assignades (ex: "1,3,4") i el converteix en una
     * llista real de números [1, 3, 4] perquè el filtre del Dashboard pugui
     * comparar-ho fàcilment amb els IDs de les plantes que vénen de l'API.
     */
    fun fetchAssignedPlants(): List<Int> {
        val idsString = prefs.getString(ASSIGNED_PLANTS, "") ?: ""
        if (idsString.isEmpty()) return emptyList()

        // Separem per comes, netegem espais i convertim cada ID a Enter
        return idsString.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    /**
     * Esborra TOTA la informació de la sessió.
     * S'utilitza quan l'usuari clica "Tancar Sessió" per seguretat.
     */
    fun clearUserData() {
        prefs.edit {
            clear()
        }
    }
}