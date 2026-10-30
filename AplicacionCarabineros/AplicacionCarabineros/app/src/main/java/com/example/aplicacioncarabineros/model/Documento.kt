package com.example.aplicacioncarabineros.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla "documentos".
 * Cada documento pertenece a un usuario (usuarioId).
 * Si se borra el usuario, se borran sus documentos (CASCADE).
 *
 * El archivo físico (PDF o Word) se copia a la memoria interna de la app
 * y aquí solo se guarda la RUTA donde quedó.
 */
@Entity(
    tableName = "documentos",
    foreignKeys = [
        ForeignKey(
            entity = Usuario::class,
            parentColumns = ["id"],
            childColumns = ["usuarioId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("usuarioId")]
)
data class Documento(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val usuarioId: Long,
    val titulo: String,          // Título que escribe el usuario
    val nombreOriginal: String,  // Nombre real del archivo (ej. rubrica.pdf)
    val tipo: String,            // "PDF" o "WORD"
    val mimeType: String,
    val rutaArchivo: String,     // Ruta del archivo dentro de la app
    val tamanoBytes: Long,
    val fechaCarga: Long = System.currentTimeMillis()
) {
    companion object {
        const val TIPO_PDF = "PDF"
        const val TIPO_WORD = "WORD"
    }
}
