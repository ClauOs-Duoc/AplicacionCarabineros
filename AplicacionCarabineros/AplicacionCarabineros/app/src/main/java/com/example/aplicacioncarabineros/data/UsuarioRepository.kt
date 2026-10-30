package com.example.aplicacioncarabineros.data

import com.example.aplicacioncarabineros.model.Usuario
import com.example.aplicacioncarabineros.util.Seguridad
import kotlinx.coroutines.flow.Flow

/**
 * Repositorio de usuarios. Mantiene las funciones originales
 * (registrarUsuario, iniciarSesion) pero ahora usa Room en vez de la base simulada.
 */
class UsuarioRepository(private val dao: UsuarioDAO) {

    /** Devuelve false si el correo ya está registrado */
    suspend fun registrarUsuario(nombreUsuario: String, correo: String, contrasena: String): Boolean {
        val correoLimpio = correo.trim().lowercase()
        if (dao.buscarPorCorreo(correoLimpio) != null) return false
        dao.insertar(
            Usuario(
                nombreUsuario = nombreUsuario.trim(),
                correo = correoLimpio,
                contrasenaHash = Seguridad.hash(contrasena)
            )
        )
        return true
    }

    /** Antes devolvía Boolean; ahora devuelve el Usuario (o null) para guardar la sesión */
    suspend fun iniciarSesion(correo: String, contrasena: String): Usuario? {
        val usuario = dao.buscarPorCorreo(correo.trim().lowercase()) ?: return null
        return if (usuario.contrasenaHash == Seguridad.hash(contrasena)) usuario else null
    }

    suspend fun buscarPorId(id: Long): Usuario? = dao.buscarPorId(id)

    fun observar(id: Long): Flow<Usuario?> = dao.observarPorId(id)

    /**
     * EDITAR DATOS DE USUARIO.
     * Exige la contraseña actual por seguridad.
     * Si nuevaContrasena está vacía, se mantiene la anterior.
     * Devuelve null si todo salió bien, o el mensaje de error.
     */
    suspend fun actualizarDatos(
        usuarioId: Long,
        nombreUsuario: String,
        correo: String,
        contrasenaActual: String,
        nuevaContrasena: String
    ): String? {
        val usuario = dao.buscarPorId(usuarioId) ?: return "No se encontró el usuario"
        if (usuario.contrasenaHash != Seguridad.hash(contrasenaActual)) {
            return "La contraseña actual no es correcta"
        }
        val correoLimpio = correo.trim().lowercase()
        val otro = dao.buscarPorCorreo(correoLimpio)
        if (otro != null && otro.id != usuarioId) return "Ese correo ya está en uso por otra cuenta"

        dao.actualizar(
            usuario.copy(   // copy: crea una copia cambiando solo estos campos
                nombreUsuario = nombreUsuario.trim(),
                correo = correoLimpio,
                contrasenaHash = if (nuevaContrasena.isBlank()) usuario.contrasenaHash
                                 else Seguridad.hash(nuevaContrasena)
            )
        )
        return null
    }
}
