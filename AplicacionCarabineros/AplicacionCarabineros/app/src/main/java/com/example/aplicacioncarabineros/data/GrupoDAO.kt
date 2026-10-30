package com.example.aplicacioncarabineros.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Transaction
import com.example.aplicacioncarabineros.model.Grupo
import com.example.aplicacioncarabineros.model.GrupoConIntegrantes
import kotlinx.coroutines.flow.Flow

/**
 * Consultas SQL de la tabla grupos.
 * Es una "abstract class" (no interface) para poder escribir crearGrupo,
 * una función con cuerpo que hace varias operaciones en una sola transacción.
 */
@Dao
abstract class GrupoDAO {

    @Insert
    abstract suspend fun insertar(grupo: Grupo): Long

    // @Transaction: Room lee el grupo y sus integrantes de forma consistente
    @Transaction
    @Query("SELECT * FROM grupos ORDER BY numero")
    abstract fun listarConIntegrantes(): Flow<List<GrupoConIntegrantes>>

    // Número más alto usado (0 si no hay grupos)
    @Query("SELECT COALESCE(MAX(numero), 0) FROM grupos")
    abstract suspend fun numeroMaximo(): Int

    // Asigna el grupo a varios alumnos a la vez
    @Query("UPDATE alumnos SET grupoId = :grupoId WHERE id IN (:idsAlumnos)")
    abstract suspend fun asignarAlumnos(idsAlumnos: List<Long>, grupoId: Long)

    @Query("SELECT COUNT(*) FROM grupos")
    abstract fun contar(): Flow<Int>

    /**
     * Crea el grupo y le asigna los alumnos.
     * @Transaction = todo o nada: si algo falla, no queda un grupo vacío a medias.
     * Devuelve el número del grupo creado.
     */
    @Transaction
    open suspend fun crearGrupo(idsAlumnos: List<Long>): Int {
        val numero = numeroMaximo() + 1   // nunca se repite, aunque se borren grupos
        val grupoId = insertar(Grupo(numero = numero))
        asignarAlumnos(idsAlumnos, grupoId)
        return numero
    }
}
