package com.example.aplicacioncarabineros.util

/**
 * Reglas de validación de TODOS los formularios.
 * Cada función devuelve null si el dato es válido, o el mensaje de error.
 * Es Kotlin puro (sin Android) para poder probarla con JUnit.
 */
object Validaciones {

    private val REGEX_CORREO = Regex("^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
    private val REGEX_NOMBRE = Regex("^[A-Za-zÁÉÍÓÚáéíóúÑñÜü ]+$")
    private val REGEX_NOMBRE_USUARIO = Regex("^[A-Za-z0-9ÁÉÍÓÚáéíóúÑñÜü ._-]+$")
    // Celular chileno: 9 dígitos que empiezan con 9, opcionalmente con +56 delante
    private val REGEX_TELEFONO = Regex("^(\\+?56)?9\\d{8}$")

    fun validarNombreUsuario(nombre: String): String? = when {
        nombre.isBlank() -> "El nombre de usuario es obligatorio"
        nombre.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        nombre.trim().length > 30 -> "Máximo 30 caracteres"
        !REGEX_NOMBRE_USUARIO.matches(nombre.trim()) -> "Usa letras, números, espacios, punto o guion"
        else -> null
    }

    fun validarNombreCompleto(nombre: String): String? = when {
        nombre.isBlank() -> "El nombre completo es obligatorio"
        !REGEX_NOMBRE.matches(nombre.trim()) -> "Solo se permiten letras y espacios"
        nombre.trim().split(" ").filter { it.isNotBlank() }.size < 2 -> "Escribe nombre y apellido"
        else -> null
    }

    fun validarCorreo(correo: String): String? = when {
        correo.isBlank() -> "El correo es obligatorio"
        !REGEX_CORREO.matches(correo.trim()) -> "Formato de correo no válido (ej: nombre@correo.cl)"
        else -> null
    }

    fun validarContrasena(contrasena: String): String? = when {
        contrasena.isEmpty() -> "La contraseña es obligatoria"
        contrasena.length < 8 -> "Debe tener al menos 8 caracteres"
        !contrasena.any { it.isUpperCase() } -> "Debe incluir al menos una mayúscula"
        !contrasena.any { it.isDigit() } -> "Debe incluir al menos un número"
        else -> null
    }

    fun validarRepetirContrasena(contrasena: String, repetir: String): String? = when {
        repetir.isEmpty() -> "Debes repetir la contraseña"
        contrasena != repetir -> "Las contraseñas no coinciden"
        else -> null
    }

    /** En el login solo se exige que no esté vacía */
    fun validarContrasenaLogin(contrasena: String): String? =
        if (contrasena.isEmpty()) "La contraseña es obligatoria" else null

    /** Editar datos: la nueva contraseña es opcional; si se escribe, debe cumplir las reglas */
    fun validarNuevaContrasena(nueva: String): String? =
        if (nueva.isEmpty()) null else validarContrasena(nueva)

    /**
     * RUT chileno con dígito verificador (algoritmo módulo 11).
     * Acepta 12.345.678-5, 12345678-5 o 123456785.
     */
    fun validarRut(rut: String): String? {
        if (rut.isBlank()) return "El RUT es obligatorio"
        val limpio = rut.replace(".", "").replace("-", "").trim().uppercase()
        if (limpio.length < 8 || limpio.length > 9) return "Largo de RUT no válido"
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.last()
        if (!cuerpo.all { it.isDigit() }) return "El RUT solo puede tener números antes del guion"
        if (!(dv.isDigit() || dv == 'K')) return "El dígito verificador debe ser un número o K"
        return if (calcularDigitoVerificador(cuerpo) == dv) null else "RUT no válido (dígito verificador incorrecto)"
    }

    /** Calcula el dígito verificador: multiplica por 2,3,4,5,6,7 desde la derecha */
    fun calcularDigitoVerificador(cuerpo: String): Char {
        var suma = 0
        var multiplicador = 2
        for (digito in cuerpo.reversed()) {
            suma += digito.digitToInt() * multiplicador
            multiplicador = if (multiplicador == 7) 2 else multiplicador + 1
        }
        return when (val resultado = 11 - (suma % 11)) {
            11 -> '0'
            10 -> 'K'
            else -> resultado.digitToChar()
        }
    }

    /** Deja el RUT siempre igual: 12.345.678-5 (así no se repite escrito distinto) */
    fun formatearRut(rut: String): String {
        val limpio = rut.replace(".", "").replace("-", "").trim().uppercase()
        if (limpio.length < 2) return rut.trim()
        val cuerpo = limpio.dropLast(1)
        val dv = limpio.last()
        val conPuntos = cuerpo.reversed().chunked(3).joinToString(".").reversed()
        return "$conPuntos-$dv"
    }

    fun validarTelefono(telefono: String): String? {
        if (telefono.isBlank()) return "El teléfono es obligatorio"
        val limpio = telefono.replace(" ", "")
        return if (REGEX_TELEFONO.matches(limpio)) null
        else "Celular no válido (ej: +56 9 1234 5678)"
    }

    fun validarTitulo(titulo: String): String? = when {
        titulo.isBlank() -> "El título es obligatorio"
        titulo.trim().length < 3 -> "Debe tener al menos 3 caracteres"
        titulo.trim().length > 60 -> "Máximo 60 caracteres"
        else -> null
    }
}
