package com.example.aplicacioncarabineros.model

import androidx.room.Embedded
import androidx.room.Relation

/**
 * Un grupo junto con la lista de sus integrantes.
 * @Relation le dice a Room: "busca en alumnos los que tengan grupoId = id de este grupo".
 * Reemplaza al antiguo Grupo(numero, integrantes: List<Alumno>).
 */
data class GrupoConIntegrantes(
    @Embedded val grupo: Grupo,
    @Relation(parentColumn = "id", entityColumn = "grupoId")
    val integrantes: List<Alumno>
)

/**
 * Un alumno junto con el número de su grupo (null si no tiene).
 * Se llena con un LEFT JOIN en AlumnoDAO.
 */
data class AlumnoConGrupo(
    @Embedded val alumno: Alumno,
    val numeroGrupo: Int?
)
