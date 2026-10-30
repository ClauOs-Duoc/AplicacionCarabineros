package com.example.aplicacioncarabineros

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Key
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockReset
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.aplicacioncarabineros.data.AppDatabase
import com.example.aplicacioncarabineros.data.SesionManager
import com.example.aplicacioncarabineros.data.UsuarioRepository
import com.example.aplicacioncarabineros.ui.componentes.BarraSuperior
import com.example.aplicacioncarabineros.ui.componentes.CampoTexto
import com.example.aplicacioncarabineros.ui.componentes.obtenerUsuarioOSalir
import com.example.aplicacioncarabineros.ui.theme.AplicacionCarabinerosTheme
import com.example.aplicacioncarabineros.util.Validaciones
import kotlinx.coroutines.launch

/** EDITAR DATOS DE USUARIO: nombre, correo y (opcional) contraseña */
class EditarUsuarioActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val usuarioId = obtenerUsuarioOSalir() ?: return
        val repository = UsuarioRepository(AppDatabase.getInstance(this).usuarioDao())
        val sesion = SesionManager(this)
        setContent {
            AplicacionCarabinerosTheme {
                PantallaEditarUsuario(usuarioId, repository, sesion)
            }
        }
    }
}

@Composable
fun PantallaEditarUsuario(usuarioId: Long, repository: UsuarioRepository, sesion: SesionManager) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var nombreUsuario by rememberSaveable { mutableStateOf("") }
    var correo by rememberSaveable { mutableStateOf("") }
    var nuevaContrasena by rememberSaveable { mutableStateOf("") }
    var repetirContrasena by rememberSaveable { mutableStateOf("") }
    var contrasenaActual by rememberSaveable { mutableStateOf("") }
    var datosCargados by rememberSaveable { mutableStateOf(false) }
    var intentoEnviar by rememberSaveable { mutableStateOf(false) }
    var errorGeneral by remember { mutableStateOf<String?>(null) }
    var guardando by remember { mutableStateOf(false) }

    // Carga los datos actuales UNA vez (si se gira el teléfono, no los vuelve a pisar)
    LaunchedEffect(usuarioId) {
        if (!datosCargados) {
            repository.buscarPorId(usuarioId)?.let { u ->
                nombreUsuario = u.nombreUsuario
                correo = u.correo
            }
            datosCargados = true
        }
    }

    val errorNombre = if (intentoEnviar) Validaciones.validarNombreUsuario(nombreUsuario) else null
    val errorCorreo = if (intentoEnviar) Validaciones.validarCorreo(correo) else null
    val errorNueva = if (intentoEnviar) Validaciones.validarNuevaContrasena(nuevaContrasena) else null
    val errorRepetir = if (intentoEnviar && nuevaContrasena.isNotEmpty())
        Validaciones.validarRepetirContrasena(nuevaContrasena, repetirContrasena) else null
    val errorActual = if (intentoEnviar) Validaciones.validarContrasenaLogin(contrasenaActual) else null

    Scaffold(
        topBar = { BarraSuperior(titulo = "Editar datos", mostrarVolver = true, mostrarMenu = false) }
    ) { innerPadding ->
        if (!datosCargados) {
            Box(Modifier.fillMaxSize().padding(innerPadding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
        } else {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .imePadding()
                    .verticalScroll(rememberScrollState())
                    .padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Datos de la cuenta", style = MaterialTheme.typography.titleMedium)
                CampoTexto(
                    valor = nombreUsuario,
                    onCambio = { nombreUsuario = it },
                    etiqueta = "Nombre de usuario",
                    icono = Icons.Filled.Person,
                    error = errorNombre
                )
                CampoTexto(
                    valor = correo,
                    onCambio = { correo = it; errorGeneral = null },
                    etiqueta = "Correo electrónico",
                    icono = Icons.Filled.Email,
                    error = errorCorreo,
                    tipoTeclado = KeyboardType.Email
                )

                Text("Cambiar contraseña (opcional)", style = MaterialTheme.typography.titleMedium)
                CampoTexto(
                    valor = nuevaContrasena,
                    onCambio = { nuevaContrasena = it },
                    etiqueta = "Nueva contraseña",
                    icono = Icons.Filled.Lock,
                    error = errorNueva,
                    esPassword = true,
                    tipoTeclado = KeyboardType.Password
                )
                IndicadorSeguridadContrasena(nuevaContrasena)
                // ANIMACIÓN: "repetir" solo aparece si se escribe una contraseña nueva
                AnimatedVisibility(
                    visible = nuevaContrasena.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    CampoTexto(
                        valor = repetirContrasena,
                        onCambio = { repetirContrasena = it },
                        etiqueta = "Repetir nueva contraseña",
                        icono = Icons.Filled.LockReset,
                        error = errorRepetir,
                        esPassword = true,
                        tipoTeclado = KeyboardType.Password
                    )
                }

                Text("Confirma con tu contraseña actual", style = MaterialTheme.typography.titleMedium)
                CampoTexto(
                    valor = contrasenaActual,
                    onCambio = { contrasenaActual = it; errorGeneral = null },
                    etiqueta = "Contraseña actual",
                    icono = Icons.Filled.Key,
                    error = errorActual,
                    esPassword = true,
                    tipoTeclado = KeyboardType.Password,
                    accionTeclado = ImeAction.Done
                )

                AnimatedVisibility(
                    visible = errorGeneral != null,
                    enter = fadeIn() + expandVertically(),
                    exit = fadeOut() + shrinkVertically()
                ) {
                    Card(
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Filled.ErrorOutline, null, tint = MaterialTheme.colorScheme.onErrorContainer)
                            Spacer(Modifier.width(8.dp))
                            Text(errorGeneral ?: "", color = MaterialTheme.colorScheme.onErrorContainer)
                        }
                    }
                }

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = { (context as? Activity)?.finish() },
                        modifier = Modifier.weight(1f)
                    ) { Text("Cancelar") }

                    Button(
                        onClick = {
                            intentoEnviar = true
                            val repetirOk = nuevaContrasena.isEmpty() ||
                                    Validaciones.validarRepetirContrasena(nuevaContrasena, repetirContrasena) == null
                            val valido = Validaciones.validarNombreUsuario(nombreUsuario) == null &&
                                    Validaciones.validarCorreo(correo) == null &&
                                    Validaciones.validarNuevaContrasena(nuevaContrasena) == null &&
                                    repetirOk &&
                                    Validaciones.validarContrasenaLogin(contrasenaActual) == null
                            if (valido) {
                                guardando = true
                                scope.launch {
                                    val error = repository.actualizarDatos(
                                        usuarioId, nombreUsuario, correo, contrasenaActual, nuevaContrasena
                                    )
                                    guardando = false
                                    if (error == null) {
                                        sesion.iniciarSesion(usuarioId, nombreUsuario.trim()) // actualiza el nombre guardado
                                        Toast.makeText(context, "Datos actualizados", Toast.LENGTH_SHORT).show()
                                        (context as? Activity)?.finish()
                                    } else {
                                        errorGeneral = error
                                    }
                                }
                            }
                        },
                        enabled = !guardando,
                        modifier = Modifier.weight(1f)
                    ) {
                        if (guardando) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(20.dp),
                                strokeWidth = 2.dp,
                                color = MaterialTheme.colorScheme.onPrimary
                            )
                        } else {
                            Text("Guardar")
                        }
                    }
                }
            }
        }
    }
}
