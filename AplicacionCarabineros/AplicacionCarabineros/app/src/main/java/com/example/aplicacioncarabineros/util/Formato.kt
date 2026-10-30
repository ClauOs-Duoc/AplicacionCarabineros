package com.example.aplicacioncarabineros.util

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/** Funciones para mostrar datos de forma legible */
object Formato {

    fun fecha(millis: Long): String =
        SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.forLanguageTag("es-CL")).format(Date(millis))

    fun tamano(bytes: Long): String = when {
        bytes >= 1024 * 1024 -> String.format(Locale.US, "%.1f MB", bytes / (1024.0 * 1024.0))
        bytes >= 1024 -> String.format(Locale.US, "%.0f KB", bytes / 1024.0)
        else -> "$bytes B"
    }
}
