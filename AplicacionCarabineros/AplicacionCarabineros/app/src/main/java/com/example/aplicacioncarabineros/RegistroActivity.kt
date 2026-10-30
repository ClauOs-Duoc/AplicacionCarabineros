package com.example.aplicacioncarabineros

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.aplicacioncarabineros.data.AppDatabase
import com.example.aplicacioncarabineros.data.UsuarioRepository
import com.example.aplicacioncarabineros.ui.componentes.CampoTexto
import com.example.aplicacioncarabineros.ui.componentes.Navegacion
import com.example.aplicacioncarabineros.ui.theme.AplicacionCarabinerosTheme
import com.example.aplicacioncarabineros.util.Validaciones
import kotlinx.coroutines.launch

/** REGISTRO DE USUARIOS (mismos campos del proyecto original) */
class RegistroActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = UsuarioRepository(AppDatabase.getInstance(this).usuarioDao())
        setContent {
            AplicacionCarabinerosTheme {
                PantallaRegistro(repository)
            }
        }
    }
}

@Composable
fun PantallaRegistro(repository: UsuarioRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nombreUsuario by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var repetirContrasena by rememberSaveable { mutableStateOf("") }
    var intentoEnviar by rememberSaveable { mutableStateOf(false) }
    var errorCorreoRepetido by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }

    val errorNombre = if (intentoEnviar) Validaciones.validarNombreUsuario(nombreUsuario) else null
    val errorCorreo = if (intentoEnviar) Validaciones.validarCorreo(correo) ?: errorCorreoRepetido
    else errorCorreoRepetido
    val errorContrasena = if (intentoEnviar) Validaciones.validarContrasena(contrasena) else null
    val errorRepetir = if (intentoEnviar) Validaciones.validarRepetirContrasena(contrasena, repetirContrasena) else null

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .imePadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 40.dp, vertical = 30.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp, Alignment.CenterVertically),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Image(
                painter = painterResource(id = R.drawable.escudo_carabineros),
                contentDescription = "Escudo de Carabineros",
                modifier = Modifier.size(90.dp)
            )
            Text("Registro", style = MaterialTheme.typography.headlineMedium)

            CampoTexto(
                valor = nombreUsuario,
                onCambio = { nombreUsuario = it },
                etiqueta = "Nombre de usuario",
                icono = Icons.Filled.Person,
                error = errorNombre
            )
            CampoTexto(
                valor = correo,
                onCambio = { correo = it; errorCorreoRepetido = null },
                etiqueta = "Correo electrónico",
                icono = Icons.Filled.Email,
                error = errorCorreo,
                tipoTeclado = KeyboardType.Email
            )
            CampoTexto(
                valor = contrasena,
                onCambio = { contrasena = it },
                etiqueta = "Contraseña",
                icono = Icons.Filled.Lock,
                error = errorContrasena,
                esPassword = true,
                tipoTeclado = KeyboardType.Password
            )
            IndicadorSeguridadContrasena(contrasena)
            CampoTexto(
                valor = repetirContrasena,
                onCambio = { repetirContrasena = it },
                etiqueta = "Repetir contraseña",
                icono = Icons.Filled.LockReset,
                error = errorRepetir,
                esPassword = true,
                tipoTeclado = KeyboardType.Password,
                accionTeclado = ImeAction.Done
            )

            Button(
                onClick = {
                    intentoEnviar = true
                    val valido = Validaciones.validarNombreUsuario(nombreUsuario) == null &&
                            Validaciones.validarCorreo(correo) == null &&
                            Validaciones.validarContrasena(contrasena) == null &&
                            Validaciones.validarRepetirContrasena(contrasena, repetirContrasena) == null
                    if (valido) {
                        guardando = true
                        scope.launch {
                            val creado = repository.registrarUsuario(nombreUsuario, correo, contrasena)
                            guardando = false
                            if (creado) {
                                Toast.makeText(context, "Cuenta creada correctamente", Toast.LENGTH_SHORT).show()
                                // Abre el inicio de sesión con el correo ya escrito
                                Navegacion.abrir(context, InicioSesionActivity::class.java) {
                                    putExtra("correo", correo.trim().lowercase())
                                }
                                (context as? Activity)?.finish()
                            } else {
                                errorCorreoRepetido = "Ya existe una cuenta con este correo"
                            }
                        }
                    }
                },
                enabled = !guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Registrarse")
                }
            }
            OutlinedButton(
                onClick = { (context as? Activity)?.finish() }, // vuelve a la bienvenida sin hacer nada
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Cancelar")
            }
        }
    }
}

/** Barra que muestra qué tan segura es la contraseña (se usa en Registro y en Editar datos) */
@Composable
fun IndicadorSeguridadContrasena(contrasena: String) {
    val puntos = listOf(
        contrasena.length >= 8,
        contrasena.any { it.isUpperCase() },
        contrasena.any { it.isDigit() },
        contrasena.any { !it.isLetterOrDigit() }
    ).count { it }

    val progreso by animateFloatAsState(targetValue = puntos / 4f, label = "seguridadContrasena")

    val (texto, color) = when (puntos) {
        0, 1 -> "Débil" to MaterialTheme.colorScheme.error
        2, 3 -> "Media" to Color(0xFFF9A825)
        else -> "Fuerte" to Color(0xFF2E7D32)
    }

    AnimatedVisibility(visible = contrasena.isNotEmpty()) {
        Column(modifier = Modifier.fillMaxWidth()) {
            LinearProgressIndicator(
                progress = { progreso },
                color = color,
                modifier = Modifier.fillMaxWidth()
            )
            Text("Seguridad: $texto", style = MaterialTheme.typography.bodySmall, color = color)
        }
    }
}
