package com.example.aplicacioncarabineros

import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.aplicacioncarabineros.data.AlumnoRepository
import com.example.aplicacioncarabineros.data.AppDatabase
import com.example.aplicacioncarabineros.model.AlumnoConGrupo
import com.example.aplicacioncarabineros.ui.componentes.BarraSuperior
import com.example.aplicacioncarabineros.ui.componentes.DatoConTitulo
import com.example.aplicacioncarabineros.ui.componentes.obtenerUsuarioOSalir
import com.example.aplicacioncarabineros.ui.theme.AplicacionCarabinerosTheme

/** DATOS DEL ALUMNO + funciones nativas: llamar y enviar correo */
class DetalleAlumnoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        obtenerUsuarioOSalir() ?: return
        // recibir el id desde la lista de alumnos
        val alumnoId = intent.getLongExtra("alumnoId", -1L)
        val repository = AlumnoRepository(AppDatabase.getInstance(this).alumnoDao())
        setContent {
            AplicacionCarabinerosTheme {
                PantallaDetalleAlumno(alumnoId, repository)
            }
        }
    }
}

@Composable
fun PantallaDetalleAlumno(alumnoId: Long, repository: AlumnoRepository) {
    val context = LocalContext.current
    // produceState: busca el alumno en Room y lo guarda como estado
    val datos by produceState<AlumnoConGrupo?>(initialValue = null, alumnoId) {
        value = repository.buscarPorId(alumnoId)
    }
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Datos del alumno", mostrarVolver = true) }
    ) { innerPadding ->
        val d = datos
        if (d == null) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            // ANIMACIÓN de entrada (AnimatedVisibility, como en clase)
            AnimatedVisibility(
                visibleState = visible,
                enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 }),
                modifier = Modifier.padding(innerPadding)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(
                            modifier = Modifier.padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            DatoConTitulo("Nombre completo", d.alumno.nombreCompleto)
                            DatoConTitulo("RUT", d.alumno.rut)
                            DatoConTitulo("Teléfono de contacto", d.alumno.telefono)
                            DatoConTitulo("Correo electrónico", d.alumno.correo)
                            DatoConTitulo("Grupo", d.numeroGrupo?.let { "Grupo $it" } ?: "Sin grupo")
                        }
                    }
                    // FUNCIÓN NATIVA: abre la app de teléfono con el número marcado (ACTION_DIAL)
                    Button(
                        onClick = { marcarTelefono(context, d.alumno.telefono) },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Icon(Icons.Filled.Phone, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Llamar")
                    }
                    // FUNCIÓN NATIVA: abre la app de correo con el destinatario puesto (ACTION_SENDTO)
                    OutlinedButton(
                        onClick = { enviarCorreo(context, d.alumno.correo) },
                        modifier = Modifier.fillMaxWidth().height(50.dp)
                    ) {
                        Icon(Icons.Filled.Email, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Enviar correo")
                    }
                }
            }
        }
    }
}

/**
 * Igual que EjemploMarcarNumeroTelefono de clase: ACTION_DIAL no necesita permiso,
 * solo abre el marcador; la persona decide si llama.
 */
fun marcarTelefono(context: Context, telefono: String) {
    val intent = Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", telefono.replace(" ", ""), null))
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No hay una aplicación de teléfono disponible", Toast.LENGTH_SHORT).show()
    }
}

fun enviarCorreo(context: Context, correo: String) {
    val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$correo"))
    try {
        context.startActivity(intent)
    } catch (e: ActivityNotFoundException) {
        Toast.makeText(context, "No hay una aplicación de correo disponible", Toast.LENGTH_SHORT).show()
    }
}
