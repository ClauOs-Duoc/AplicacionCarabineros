package com.example.aplicacioncarabineros.data

import com.example.aplicacioncarabineros.model.Alumno
import com.example.aplicacioncarabineros.model.AlumnoConGrupo
import com.example.aplicacioncarabineros.util.Validaciones
import kotlinx.coroutines.flow.Flow

/** Repositorio de alumnos (antes leía de BaseDatosSimulada, ahora de Room) */
class AlumnoRepository(private val dao: AlumnoDAO) {

    // Flow: la lista se actualiza sola cuando se agrega un alumno
    fun obtenerAlumnos(): Flow<List<AlumnoConGrupo>> = dao.listarConGrupo()

    fun contar(): Flow<Int> = dao.contar()

    suspend fun buscarPorId(id: Long): AlumnoConGrupo? = dao.buscarConGrupo(id)

    /** Devuelve false si ya existe un alumno con ese RUT */
    suspend fun agregarAlumno(nombreCompleto: String, rut: String, telefono: String, correo: String): Boolean {
        val rutFormateado = Validaciones.formatearRut(rut)
        if (dao.buscarPorRut(rutFormateado) != null) return false
        dao.insertar(
            Alumno(
                nombreCompleto = nombreCompleto.trim(),
                rut = rutFormateado,
                telefono = telefono.trim(),
                correo = correo.trim().lowercase()
            )
        )
        return true
    }
}
