package com.example.aplicacioncarabineros.util

import java.security.MessageDigest

/**
 * Convierte la contraseña en un "hash" SHA-256.
 * Es irreversible: aunque alguien vea la base de datos, no puede leer la contraseña.
 * Para verificar el login se compara el hash de lo que escribe el usuario con el guardado.
 */
object Seguridad {
    fun hash(texto: String): String {
        val bytes = MessageDigest.getInstance("SHA-256").digest(texto.toByteArray())
        return bytes.joinToString("") { "%02x".format(it) }
    }
}
