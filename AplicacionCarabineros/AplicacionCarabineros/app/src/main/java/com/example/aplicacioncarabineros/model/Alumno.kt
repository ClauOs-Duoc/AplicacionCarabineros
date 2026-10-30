package com.example.aplicacioncarabineros.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla "alumnos". Mantiene los campos originales del proyecto:
 * nombreCompleto, rut, telefono y correo.
 * Se agrega grupoId: el grupo al que pertenece (null = sin grupo).
 * El RUT es único: no se puede registrar dos veces el mismo alumno.
 */
@Entity(
    tableName = "alumnos",
    foreignKeys = [
        ForeignKey(
            entity = Grupo::class,
            parentColumns = ["id"],
            childColumns = ["grupoId"],
            onDelete = ForeignKey.SET_NULL // si se borra el grupo, el alumno queda sin grupo
        )
    ],
    indices = [Index(value = ["rut"], unique = true), Index("grupoId")]
)
data class Alumno(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombreCompleto: String,
    val rut: String,
    val telefono: String,
    val correo: String,
    val grupoId: Long? = null
)
