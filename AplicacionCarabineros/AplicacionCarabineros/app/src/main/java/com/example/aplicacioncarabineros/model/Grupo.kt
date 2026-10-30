package com.example.aplicacioncarabineros.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla "grupos".
 * Antes el grupo guardaba una List<Alumno>. En una base de datos relacional
 * la relación se guarda al revés: cada alumno tiene el id de su grupo (grupoId).
 * Para leer un grupo con sus integrantes se usa GrupoConIntegrantes.
 */
@Entity(
    tableName = "grupos",
    indices = [Index(value = ["numero"], unique = true)]
)
data class Grupo(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val numero: Int
)
