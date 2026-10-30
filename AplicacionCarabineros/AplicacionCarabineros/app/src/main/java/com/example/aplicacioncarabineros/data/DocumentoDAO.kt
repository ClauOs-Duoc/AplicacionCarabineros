package com.example.aplicacioncarabineros.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import com.example.aplicacioncarabineros.model.Documento
import kotlinx.coroutines.flow.Flow

/** Operaciones SQL sobre la tabla documentos */
@Dao
interface DocumentoDAO {

    @Insert
    suspend fun insertar(documento: Documento): Long

    // Flow = la lista se actualiza SOLA en pantalla cada vez que la tabla cambia
    @Query("SELECT * FROM documentos WHERE usuarioId = :usuarioId ORDER BY fechaCarga DESC")
    fun listarPorUsuario(usuarioId: Long): Flow<List<Documento>>

    @Query("SELECT * FROM documentos WHERE id = :id LIMIT 1")
    suspend fun buscarPorId(id: Long): Documento?

    @Query("SELECT COUNT(*) FROM documentos WHERE usuarioId = :usuarioId")
    fun contarPorUsuario(usuarioId: Long): Flow<Int>

    @Delete
    suspend fun eliminar(documento: Documento)
}
