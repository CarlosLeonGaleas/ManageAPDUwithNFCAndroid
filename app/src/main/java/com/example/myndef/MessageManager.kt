package com.example.myndef

import android.content.Context
import android.content.SharedPreferences

object MessageManager {
    private const val PREFS_NAME = "MessagePrefs"
    private const val KEY_MESSAGE = "lastMessage"
    private lateinit var sharedPreferences: SharedPreferences

    // Inicializar las preferencias
    fun initialize(context: Context) {
        sharedPreferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    // Función para establecer un nuevo mensaje
    fun setMessage(newMessage: String) {
        val editor = sharedPreferences.edit()
        editor.putString(KEY_MESSAGE, newMessage)
        editor.apply() // Guarda el mensaje en SharedPreferences
    }

    // Función para obtener el último mensaje almacenado
    fun getMessage(): String {
        // Retorna el mensaje guardado o el valor por defecto si no existe
        return sharedPreferences.getString(KEY_MESSAGE, "No se ha iniciado sesión") ?: "No se ha iniciado sesión"
    }
}
