package com.example.aplicacioncarabineros

import com.example.aplicacioncarabineros.util.Validaciones
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

/**
 * Pruebas unitarias de las validaciones.
 * Ejecutar: clic derecho sobre esta clase > Run 'ValidacionesTest'
 */
class ValidacionesTest {

    @Test fun correoValido() = assertNull(Validaciones.validarCorreo("ana@correo.cl"))
    @Test fun correoSinArroba() = assertNotNull(Validaciones.validarCorreo("anacorreo.cl"))
    @Test fun contrasenaCorta() = assertNotNull(Validaciones.validarContrasena("Ab1"))
    @Test fun contrasenaValida() = assertNull(Validaciones.validarContrasena("Clave1234"))
    @Test fun contrasenasDistintas() = assertNotNull(Validaciones.validarRepetirContrasena("Clave1234", "Clave12345"))
    @Test fun rutValido() = assertNull(Validaciones.validarRut("11.111.111-1"))
    @Test fun rutValidoConK() = assertNull(Validaciones.validarRut("10.000.013-K"))
    @Test fun rutDigitoIncorrecto() = assertNotNull(Validaciones.validarRut("11.111.111-2"))
    @Test fun formatoRut() = assertEquals("12.345.678-5", Validaciones.formatearRut("123456785"))
    @Test fun telefonoValido() = assertNull(Validaciones.validarTelefono("+56 9 1111 1111"))
    @Test fun telefonoFijoNoValido() = assertNotNull(Validaciones.validarTelefono("22345678"))
    @Test fun nombreSinApellido() = assertNotNull(Validaciones.validarNombreCompleto("Camila"))
    @Test fun nombreCompletoValido() = assertNull(Validaciones.validarNombreCompleto("Camila Soto Rojas"))
}
