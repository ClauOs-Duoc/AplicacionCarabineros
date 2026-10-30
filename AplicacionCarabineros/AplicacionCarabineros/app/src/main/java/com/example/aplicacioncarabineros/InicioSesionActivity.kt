package com.example.aplicacioncarabineros

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.ErrorOutline
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.aplicacioncarabineros.data.AppDatabase
import com.example.aplicacioncarabineros.data.SesionManager
import com.example.aplicacioncarabineros.data.UsuarioRepository
import com.example.aplicacioncarabineros.ui.componentes.CampoTexto
import com.example.aplicacioncarabineros.ui.componentes.Navegacion
import com.example.aplicacioncarabineros.ui.theme.AplicacionCarabinerosTheme
import com.example.aplicacioncarabineros.util.Validaciones
import kotlinx.coroutines.launch

/** INICIAR SESIÓN */
class InicioSesionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = UsuarioRepository(AppDatabase.getInstance(this).usuarioDao())
        val sesion = SesionManager(this)
        // Si viene desde el registro, llega el correo para no escribirlo de nuevo
        val correoInicial = intent.getStringExtra("correo") ?: ""
        setContent {
            AplicacionCarabinerosTheme {
                PantallaInicioSesion(repository, sesion, correoInicial)
            }
        }
    }
}

@Composable
fun PantallaInicioSesion(repository: UsuarioRepository, sesion: SesionManager, correoInicial: String) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // rememberSaveable: lo escrito no se pierde al girar el teléfono
    var correo by rememberSaveable { mutableStateOf(correoInicial) }
    var contrasena by rememberSaveable { mutableStateOf("") }
    var intentoEnviar by rememberSaveable { mutableStateOf(false) }
    var errorGeneral by remember { mutableStateOf<String?>(null) }
    var cargando by remember { mutableStateOf(false) }

    val errorCorreo = if (intentoEnviar) Validaciones.validarCorreo(correo) else null
    val errorContrasena = if (intentoEnviar) Validaciones.validarContrasenaLogin(contrasena) else null

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
            Text("Iniciar sesión", style = MaterialTheme.typography.headlineMedium)

            CampoTexto(
                valor = correo,
                onCambio = { correo = it; errorGeneral = null },
                etiqueta = "Correo electrónico",
                icono = Icons.Filled.Email,
                error = errorCorreo,
                tipoTeclado = KeyboardType.Email
            )
            CampoTexto(
                valor = contrasena,
                onCambio = { contrasena = it; errorGeneral = null },
                etiqueta = "Contraseña",
                icono = Icons.Filled.Lock,
                error = errorContrasena,
                esPassword = true,
                tipoTeclado = KeyboardType.Password,
                accionTeclado = ImeAction.Done
            )

            // ANIMACIÓN: el mensaje de error aparece/desaparece
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
                        Icon(
                            Icons.Filled.ErrorOutline,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onErrorContainer
                        )
                        Spacer(Modifier.width(8.dp))
                        Text(errorGeneral ?: "", color = MaterialTheme.colorScheme.onErrorContainer)
                    }
                }
            }

            Button(
                onClick = {
                    intentoEnviar = true
                    val valido = Validaciones.validarCorreo(correo) == null &&
                            Validaciones.validarContrasenaLogin(contrasena) == null
                    if (valido) {
                        cargando = true
                        scope.launch {
                            val usuario = repository.iniciarSesion(correo, contrasena)
                            cargando = false
                            if (usuario != null) {
                                sesion.iniciarSesion(usuario.id, usuario.nombreUsuario)
                                Toast.makeText(context, "Inicio de sesión exitoso", Toast.LENGTH_SHORT).show()
                                Navegacion.irAPrincipalLimpiando(context)
                            } else {
                                errorGeneral = "Correo o contraseña incorrectos"
                            }
                        }
                    }
                },
                enabled = !cargando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (cargando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Iniciar sesión")
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
