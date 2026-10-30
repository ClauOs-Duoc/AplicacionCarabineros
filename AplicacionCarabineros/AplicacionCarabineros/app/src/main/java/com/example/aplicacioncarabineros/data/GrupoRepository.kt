package com.example.aplicacioncarabineros.data

import com.example.aplicacioncarabineros.model.Alumno
import com.example.aplicacioncarabineros.model.GrupoConIntegrantes
import kotlinx.coroutines.flow.Flow

/** Repositorio de grupos (mismas funciones que el original, ahora con Room) */
class GrupoRepository(private val grupoDao: GrupoDAO, private val alumnoDao: AlumnoDAO) {

    fun obtenerGrupos(): Flow<List<GrupoConIntegrantes>> = grupoDao.listarConIntegrantes()

    // alumnos que no están en ningún grupo
    fun obtenerAlumnosSinGrupo(): Flow<List<Alumno>> = alumnoDao.listarSinGrupo()

    fun contar(): Flow<Int> = grupoDao.contar()

    /** Crea el grupo con los alumnos elegidos y devuelve su número */
    suspend fun agregarGrupo(integrantes: List<Alumno>): Int =
        grupoDao.crearGrupo(integrantes.map { it.id })
}
