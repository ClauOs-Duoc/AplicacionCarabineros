package com.example.aplicacioncarabineros

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.aplicacioncarabineros.data.SesionManager
import com.example.aplicacioncarabineros.ui.componentes.Navegacion
import com.example.aplicacioncarabineros.ui.theme.AplicacionCarabinerosTheme
import kotlinx.coroutines.delay

/**
 * VISTA DE CIERRE DE SESIÓN.
 * Se abre después de confirmar en la alerta "¿Seguro que deseas cerrar sesión?".
 * Borra la sesión, muestra la despedida y vuelve a la pantalla de bienvenida.
 */
class CerrarSesionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val sesion = SesionManager(this)
        val nombre = sesion.obtenerNombre()   // se lee ANTES de borrar la sesión
        sesion.cerrarSesion()
        setContent {
            AplicacionCarabinerosTheme {
                PantallaCerrarSesion(nombre)
            }
        }
    }
}

@Composable
fun PantallaCerrarSesion(nombre: String) {
    val context = LocalContext.current

    // El botón atrás no debe volver a pantallas privadas
    BackHandler { Navegacion.irABienvenidaLimpiando(context) }

    // A los 2 segundos vuelve sola a la bienvenida
    LaunchedEffect(Unit) {
        delay(2000)
        Navegacion.irABienvenidaLimpiando(context)
    }

    // ANIMACIÓN de entrada: aparece con fundido y zoom
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }

    Scaffold { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentAlignment = Alignment.Center
        ) {
            AnimatedVisibility(
                visibleState = visible,
                enter = fadeIn(tween(500)) + scaleIn(initialScale = 0.7f, animationSpec = tween(500))
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.padding(30.dp)
                ) {
                    Icon(
                        Icons.Filled.CheckCircle,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(110.dp)
                    )
                    Spacer(Modifier.height(16.dp))
                    Text("Sesión cerrada", style = MaterialTheme.typography.headlineSmall)
                    Text(
                        if (nombre.isNotBlank()) "¡Hasta pronto, $nombre!" else "¡Hasta pronto!",
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(24.dp))
                    LinearProgressIndicator(modifier = Modifier.fillMaxWidth(0.6f))
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = { Navegacion.irABienvenidaLimpiando(context) }) {
                        Text("Volver ahora")
                    }
                }
            }
        }
    }
}
