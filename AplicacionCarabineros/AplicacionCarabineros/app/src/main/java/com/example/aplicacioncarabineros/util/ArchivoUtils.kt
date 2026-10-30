package com.example.aplicacioncarabineros.util

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.OpenableColumns
import androidx.core.content.FileProvider
import com.example.aplicacioncarabineros.model.Documento
import java.io.File
import java.io.IOException
import java.util.UUID

/**
 * Manejo de archivos nativos (PDF y Word).
 *
 * 1. El usuario elige un archivo con el selector de archivos de Android.
 * 2. Android nos entrega una "Uri" (una dirección temporal al archivo).
 * 3. Copiamos el archivo a la memoria INTERNA de la app (context.filesDir),
 *    así queda guardado aunque el original se borre del teléfono.
 */
object ArchivoUtils {

    /** Tamaño máximo permitido: 10 MB */
    const val TAMANO_MAXIMO = 10L * 1024 * 1024

    /** Tipos de archivo que se pueden seleccionar (PDF, Word .doc y Word .docx) */
    val TIPOS_PERMITIDOS = arrayOf(
        "application/pdf",
        "application/msword",
        "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
    )

    data class InfoArchivo(
        val nombre: String,
        val tamanoBytes: Long,
        val mimeType: String,
        val tipo: String,       // Documento.TIPO_PDF o Documento.TIPO_WORD
        val extension: String   // pdf, doc o docx
    )

    /** Lee nombre, tamaño y tipo del archivo seleccionado */
    fun obtenerInfo(context: Context, uri: Uri): InfoArchivo {
        var nombre = "documento"
        var tamano = 0L

        context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            val indiceNombre = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            val indiceTamano = cursor.getColumnIndex(OpenableColumns.SIZE)
            if (cursor.moveToFirst()) {
                if (indiceNombre >= 0) nombre = cursor.getString(indiceNombre) ?: nombre
                if (indiceTamano >= 0 && !cursor.isNull(indiceTamano)) tamano = cursor.getLong(indiceTamano)
            }
        }

        val mime = context.contentResolver.getType(uri) ?: ""
        val extensionNombre = nombre.substringAfterLast('.', "").lowercase()

        val tipo = if (mime == "application/pdf" || extensionNombre == "pdf") {
            Documento.TIPO_PDF
        } else {
            Documento.TIPO_WORD
        }

        val extension = when {
            tipo == Documento.TIPO_PDF -> "pdf"
            extensionNombre == "doc" || mime == "application/msword" -> "doc"
            else -> "docx"
        }

        val mimeFinal = mime.ifBlank {
            when (extension) {
                "pdf" -> TIPOS_PERMITIDOS[0]
                "doc" -> TIPOS_PERMITIDOS[1]
                else -> TIPOS_PERMITIDOS[2]
            }
        }

        return InfoArchivo(nombre, tamano, mimeFinal, tipo, extension)
    }

    /** Valida tipo y tamaño. Devuelve null si está todo bien o el mensaje de error. */
    fun validarArchivo(info: InfoArchivo?): String? {
        if (info == null) return "Debes seleccionar un archivo PDF o Word"
        val extensionValida = info.nombre.substringAfterLast('.', "").lowercase() in listOf("pdf", "doc", "docx")
        return when {
            info.mimeType !in TIPOS_PERMITIDOS && !extensionValida -> "Solo se permiten archivos PDF o Word"
            info.tamanoBytes > TAMANO_MAXIMO -> "El archivo supera el máximo de 10 MB"
            else -> null
        }
    }

    /** Copia el archivo a /data/data/<app>/files/documentos/ con un nombre único */
    fun copiarAMemoriaInterna(context: Context, uri: Uri, extension: String): File {
        val carpeta = File(context.filesDir, "documentos")
        if (!carpeta.exists()) carpeta.mkdirs()

        val destino = File(carpeta, "${UUID.randomUUID()}.$extension")

        val entrada = context.contentResolver.openInputStream(uri)
            ?: throw IOException("No se pudo leer el archivo seleccionado")

        entrada.use { input ->
            destino.outputStream().use { output -> input.copyTo(output) }
        }
        return destino
    }

    /**
     * Abre el documento con otra app del teléfono (Word, Google Docs, lector PDF...).
     * Se usa un FileProvider para compartir el archivo de forma segura.
     * Devuelve false si no hay ninguna app instalada que pueda abrirlo.
     */
    fun abrirConAppExterna(context: Context, documento: Documento): Boolean {
        val archivo = File(documento.rutaArchivo)
        if (!archivo.exists()) return false

        val uri = FileProvider.getUriForFile(
            context,
            "${context.packageName}.fileprovider",
            archivo
        )

        val intent = Intent(Intent.ACTION_VIEW).apply {
            setDataAndType(uri, documento.mimeType)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }

        return try {
            context.startActivity(intent)
            true
        } catch (e: ActivityNotFoundException) {
            false
        }
    }
}
