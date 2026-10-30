package com.example.aplicacioncarabineros.data

import android.content.Context

/**
 * Guarda la sesión del usuario con SharedPreferences (almacenamiento local clave-valor).
 * Así, si el usuario cierra la app y la vuelve a abrir, sigue con su sesión iniciada.
 */
class SesionManager(context: Context) {

    private val prefs = context.getSharedPreferences("sesion", Context.MODE_PRIVATE)

    fun iniciarSesion(usuarioId: Long, nombre: String) {
        prefs.edit()
            .putLong(CLAVE_ID, usuarioId)
            .putString(CLAVE_NOMBRE, nombre)
            .apply()
    }

    /** Devuelve el id del usuario conectado, o -1 si no hay sesión */
    fun obtenerUsuarioId(): Long = prefs.getLong(CLAVE_ID, -1L)

    fun obtenerNombre(): String = prefs.getString(CLAVE_NOMBRE, "") ?: ""

    fun haySesion(): Boolean = obtenerUsuarioId() != -1L

    fun cerrarSesion() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val CLAVE_ID = "usuario_id"
        private const val CLAVE_NOMBRE = "usuario_nombre"
    }
}
