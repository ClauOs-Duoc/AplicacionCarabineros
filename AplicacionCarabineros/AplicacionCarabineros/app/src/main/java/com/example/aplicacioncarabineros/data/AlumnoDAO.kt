package com.example.aplicacioncarabineros.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.aplicacioncarabineros.model.Alumno
import com.example.aplicacioncarabineros.model.AlumnoConGrupo
import kotlinx.coroutines.flow.Flow

/** Consultas SQL de la tabla alumnos */
@Dao
interface AlumnoDAO {

    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertar(alumno: Alumno): Long

    // LEFT JOIN: trae todos los alumnos, tengan grupo o no, con el número de su grupo
    @Query(
        "SELECT alumnos.*, grupos.numero AS numeroGrupo FROM alumnos " +
        "LEFT JOIN grupos ON alumnos.grupoId = grupos.id " +
        "ORDER BY alumnos.nombreCompleto"
    )
    fun listarConGrupo(): Flow<List<AlumnoConGrupo>>

    @Query(
        "SELECT alumnos.*, grupos.numero AS numeroGrupo FROM alumnos " +
        "LEFT JOIN grupos ON alumnos.grupoId = grupos.id " +
        "WHERE alumnos.id = :id LIMIT 1"
    )
    suspend fun buscarConGrupo(id: Long): AlumnoConGrupo?

    @Query("SELECT * FROM alumnos WHERE rut = :rut LIMIT 1")
    suspend fun buscarPorRut(rut: String): Alumno?

    // Alumnos que todavía no están en ningún grupo
    @Query("SELECT * FROM alumnos WHERE grupoId IS NULL ORDER BY nombreCompleto")
    fun listarSinGrupo(): Flow<List<Alumno>>

    @Query("SELECT COUNT(*) FROM alumnos")
    fun contar(): Flow<Int>
}
