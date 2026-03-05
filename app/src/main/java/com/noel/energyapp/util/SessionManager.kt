package com.noel.energyapp.util

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

class SessionManager(context: Context) {
    // Nom de l'arxiu de SharedPreferences / preferencies que es guardarà al mobil
    private val prefs: SharedPreferences =
        context.getSharedPreferences("NoelEnergyPrefs", Context.MODE_PRIVATE)

    companion object {
        const val USER_TOKEN = "user_token"
        const val USER_NAME = "user_name"
        const val USER_ROLE = "user_role"
    }

    // Funció per guardar el token i dades de l'usuari
    fun saveUserData(token: String, name: String, role: String) {
        prefs.edit {
            putString(USER_TOKEN, token)
            putString(USER_NAME, name)
            putString(USER_ROLE, role)
        }
    }

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

    // Funció per fer logout (esborrar tot)
    fun clearUserData() {
        prefs.edit {
            clear()
        }
    }
}