package com.example.aplicacioncarabineros

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.aplicacioncarabineros.data.AlumnoRepository
import com.example.aplicacioncarabineros.data.AppDatabase
import com.example.aplicacioncarabineros.ui.componentes.BarraSuperior
import com.example.aplicacioncarabineros.ui.componentes.CampoTexto
import com.example.aplicacioncarabineros.ui.componentes.obtenerUsuarioOSalir
import com.example.aplicacioncarabineros.ui.theme.AplicacionCarabinerosTheme
import com.example.aplicacioncarabineros.util.Validaciones
import kotlinx.coroutines.launch

/** AGREGAR ALUMNO (mismos campos y botones del proyecto original) */
class AgregarAlumnoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        obtenerUsuarioOSalir() ?: return
        val repository = AlumnoRepository(AppDatabase.getInstance(this).alumnoDao())
        setContent {
            AplicacionCarabinerosTheme {
                PantallaAgregarAlumno(repository)
            }
        }
    }
}

@Composable
fun PantallaAgregarAlumno(repository: AlumnoRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nombreCompleto by rememberSaveable { mutableStateOf("") }
    var rut by rememberSaveable { mutableStateOf("") }
    var telefono by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var intentoEnviar by rememberSaveable { mutableStateOf(false) }
    var errorRutRepetido by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }

    val errorNombre = if (intentoEnviar) Validaciones.validarNombreCompleto(nombreCompleto) else null
    val errorRut = if (intentoEnviar) Validaciones.validarRut(rut) ?: errorRutRepetido else errorRutRepetido
    val errorTelefono = if (intentoEnviar) Validaciones.validarTelefono(telefono) else null
    val errorCorreo = if (intentoEnviar) Validaciones.validarCorreo(correo) else null

    Scaffold(
        topBar = { BarraSuperior(titulo = "Agregar alumno", mostrarVolver = true, mostrarMenu = false) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .padding(20.dp)
        ) {
            // campos del formulario (ocupan el espacio disponible)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                CampoTexto(
                    valor = nombreCompleto,
                    onCambio = { nombreCompleto = it },
                    etiqueta = "Nombre completo",
                    icono = Icons.Filled.Person,
                    error = errorNombre
                )
                CampoTexto(
                    valor = rut,
                    onCambio = { rut = it; errorRutRepetido = null },
                    etiqueta = "RUT (ej: 12.345.678-5)",
                    icono = Icons.Filled.Badge,
                    error = errorRut
                )
                CampoTexto(
                    valor = telefono,
                    onCambio = { telefono = it },
                    etiqueta = "Teléfono de contacto",
                    icono = Icons.Filled.Phone,
                    error = errorTelefono,
                    tipoTeclado = KeyboardType.Phone
                )
                CampoTexto(
                    valor = correo,
                    onCambio = { correo = it },
                    etiqueta = "Correo electrónico",
                    icono = Icons.Filled.Email,
                    error = errorCorreo,
                    tipoTeclado = KeyboardType.Email,
                    accionTeclado = ImeAction.Done
                )
            }
            // botones en la parte inferior
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { (context as? Activity)?.finish() }, // vuelve a la lista sin guardar nada
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        intentoEnviar = true
                        val valido = Validaciones.validarNombreCompleto(nombreCompleto) == null &&
                                Validaciones.validarRut(rut) == null &&
                                Validaciones.validarTelefono(telefono) == null &&
                                Validaciones.validarCorreo(correo) == null
                        if (valido) {
                            guardando = true
                            scope.launch {
                                val agregado = repository.agregarAlumno(nombreCompleto, rut, telefono, correo)
                                guardando = false
                                if (agregado) {
                                    Toast.makeText(context, "Alumno agregado correctamente", Toast.LENGTH_SHORT).show()
                                    (context as? Activity)?.finish() // vuelve a la lista
                                } else {
                                    errorRutRepetido = "Ya existe un alumno con este RUT"
                                }
                            }
                        }
                    },
                    enabled = !guardando,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Aceptar")
                }
            }
        }
    }
}
