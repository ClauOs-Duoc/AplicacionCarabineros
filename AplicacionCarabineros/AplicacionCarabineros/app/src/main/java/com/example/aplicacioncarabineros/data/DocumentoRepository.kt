package com.example.aplicacioncarabineros.data

import android.content.Context
import android.net.Uri
import com.example.aplicacioncarabineros.model.Documento
import com.example.aplicacioncarabineros.util.ArchivoUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.io.File

/**
 * Repositorio de documentos.
 * Se encarga de DOS cosas a la vez: el archivo físico y el registro en la base de datos.
 */
class DocumentoRepository(private val dao: DocumentoDAO) {

    fun listar(usuarioId: Long): Flow<List<Documento>> = dao.listarPorUsuario(usuarioId)

    fun contar(usuarioId: Long): Flow<Int> = dao.contarPorUsuario(usuarioId)

    suspend fun buscarPorId(id: Long): Documento? = dao.buscarPorId(id)

    /**
     * CARGAR: copia el archivo elegido a la memoria interna de la app
     * y guarda sus datos en la tabla documentos.
     */
    suspend fun guardar(context: Context, uri: Uri, titulo: String, usuarioId: Long) {
        withContext(Dispatchers.IO) { // Trabajo pesado fuera del hilo de la interfaz
            val info = ArchivoUtils.obtenerInfo(context, uri)
            val archivo = ArchivoUtils.copiarAMemoriaInterna(context, uri, info.extension)

            dao.insertar(
                Documento(
                    usuarioId = usuarioId,
                    titulo = titulo.trim(),
                    nombreOriginal = info.nombre,
                    tipo = info.tipo,
                    mimeType = info.mimeType,
                    rutaArchivo = archivo.absolutePath,
                    tamanoBytes = info.tamanoBytes
                )
            )
        }
    }

    /** ELIMINAR: borra el archivo físico y luego el registro de la base de datos. */
    suspend fun eliminar(documento: Documento) {
        withContext(Dispatchers.IO) {
            File(documento.rutaArchivo).delete()
            dao.eliminar(documento)
        }
    }
}
