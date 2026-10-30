package com.example.aplicacioncarabineros

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import com.example.aplicacioncarabineros.data.SesionManager
import com.example.aplicacioncarabineros.ui.componentes.Navegacion
import com.example.aplicacioncarabineros.ui.theme.AplicacionCarabinerosTheme
import com.example.aplicacioncarabineros.ui.theme.VerdeCarabineros
import kotlinx.coroutines.delay

/**
 * PANTALLA DE BIENVENIDA.
 * 1. Muestra el logo y el nombre con animación.
 * 2. Si ya hay una sesión guardada, entra directo a la vista de bienvenida (Principal).
 * 3. Si no, aparecen los dos botones del proyecto original: Iniciar sesión y Registrarse.
 */
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val haySesion = SesionManager(this).haySesion()
        setContent {
            AplicacionCarabinerosTheme {
                PantallaInicio(haySesion)
            }
        }
    }
}

@Composable
fun PantallaInicio(haySesion: Boolean) {
    val context = LocalContext.current

    // ANIMACIÓN 1: el logo crece con rebote (Animatable + spring)
    val escala = remember { Animatable(0.3f) }

    // ANIMACIÓN 2: el texto sube a su lugar (animateIntOffsetAsState, como DesplazamientoActivity de clase)
    var textoEnPosicion by remember { mutableStateOf(false) }
    val distancia = with(LocalDensity.current) { 60.dp.roundToPx() }
    val desplazamiento by animateIntOffsetAsState(
        targetValue = if (textoEnPosicion) IntOffset.Zero else IntOffset(x = 0, y = distancia),
        animationSpec = tween(durationMillis = 700),
        label = "Desplazamiento del título"
    )
    val opacidadTexto by animateFloatAsState(
        targetValue = if (textoEnPosicion) 1f else 0f,
        animationSpec = tween(durationMillis = 700),
        label = "Opacidad del título"
    )

    // ANIMACIÓN 3: los botones aparecen al final (AnimatedVisibility)
    var mostrarBotones by rememberSaveable { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        textoEnPosicion = true
        escala.animateTo(
            targetValue = 1f,
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
        delay(400)
        if (haySesion) {
            Navegacion.irAPrincipalLimpiando(context)   // ya había iniciado sesión
        } else {
            mostrarBotones = true
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(VerdeCarabineros) // mismo verde del logo: la imagen se funde con el fondo
            .padding(50.dp),
        verticalArrangement = Arrangement.spacedBy(15.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Logo oficial (res/drawable-nodpi/logo_carabineros.png)
        Image(
            painter = painterResource(id = R.drawable.logo_carabineros),
            contentDescription = "Logo Carabineros de Chile",
            modifier = Modifier
                .fillMaxWidth()
                .graphicsLayer {
                    scaleX = escala.value
                    scaleY = escala.value
                }
        )
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .offset { desplazamiento }
                .graphicsLayer { alpha = opacidadTexto }
        ) {
            Text(
                "Aplicación Carabineros",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )
            Text(
                "Alumnos · Grupos · Documentos",
                color = Color.White.copy(alpha = 0.85f)
            )
        }

        Spacer(Modifier.height(20.dp))

        AnimatedVisibility(
            visible = mostrarBotones,
            enter = fadeIn() + expandVertically(),
            exit = fadeOut()
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(15.dp)) {
                Button(
                    onClick = { Navegacion.abrir(context, InicioSesionActivity::class.java) },
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color.White,
                        contentColor = VerdeCarabineros
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Iniciar sesión")
                }
                OutlinedButton(
                    onClick = { Navegacion.abrir(context, RegistroActivity::class.java) },
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = Color.White
                    ),
                    border = BorderStroke(1.dp, Color.White),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Registrarse")
                }
            }
        }
    }
}
