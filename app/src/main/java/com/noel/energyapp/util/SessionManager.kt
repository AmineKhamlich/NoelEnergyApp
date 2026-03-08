package com.noel.energyapp.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {
    // Nom de l'arxiu de SharedPreferences / preferencies que es guardarà al mobil
    private val prefs: SharedPreferences =
        context.getSharedPreferences("NoelEnergyPrefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_ID = "user_id"
        const val USER_TOKEN = "user_token"
        const val USER_NAME = "user_name"
        const val USER_ROLE = "user_role"
        const val ASSIGNED_PLANTS = "assigned_plants"
        const val MUST_CHANGE_PASSWORD = "must_change_password"
    }

    /**
     * Guarda TOTA la informació del login de cop.
     * NOU: Ara rep userId i mustChangePassword
     */
    fun saveUserData(userId: Int, token: String, name: String, role: String, assignedPlants: String?, mustChangePassword: Boolean) {
        prefs.edit {
            putInt(USER_ID, userId)
            putString(USER_TOKEN, token)
            putString(USER_NAME, name)
            putString(USER_ROLE, role)
            putString(ASSIGNED_PLANTS, assignedPlants) // Corregit per utilitzar la constant
            putBoolean(MUST_CHANGE_PASSWORD, mustChangePassword)
        }
    }

    // Funció clau: Quan l'usuari canvia la contrasenya amb èxit, li traiem l'obligació
    fun clearMustChangePasswordFlag() {
        prefs.edit { putBoolean(MUST_CHANGE_PASSWORD, false) }
    }

    // Funció per obtenir l'ID
    fun fetchUserId(): Int = prefs.getInt(USER_ID, -1)

    // Funció per obtenir el token
    fun fetchAuthToken(): String? {
        return prefs.getString(USER_TOKEN, null)
    }

    // Funció per obtenir el nom
    fun fetchUserName(): String? {
        return prefs.getString(USER_NAME, null)
    }

    // Funció per obtenir el rol
    fun fetchUserRole(): String? {
        return prefs.getString(USER_ROLE, null)
    }

    // Funció per obtenir si ha canviat la contrasenya
    fun fetchMustChangePassword(): Boolean = prefs.getBoolean(MUST_CHANGE_PASSWORD, false)

    /**
     * Recupera el text de plantes assignades ("1,3,4") i el converteix en una llista
     * real de números [1, 3, 4] perquè Kotlin pugui treballar-hi fàcilment.
     */
    fun fetchAssignedPlants(): List<Int> {
        val idsString = prefs.getString(ASSIGNED_PLANTS, "") ?: ""
        if (idsString.isEmpty()) return emptyList()

        // Separem per comes, eliminem espais en blanc i convertim a Enter
        return idsString.split(",").mapNotNull { it.trim().toIntOrNull() }
    }

    // Funció per fer logout (esborrar tot)
    fun clearUserData() {
        prefs.edit {
            clear()
        }
    }
}