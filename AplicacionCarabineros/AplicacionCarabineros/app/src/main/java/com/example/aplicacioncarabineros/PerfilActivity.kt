package com.example.aplicacioncarabineros

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacioncarabineros.data.AppDatabase
import com.example.aplicacioncarabineros.data.DocumentoRepository
import com.example.aplicacioncarabineros.data.UsuarioRepository
import com.example.aplicacioncarabineros.model.Usuario
import com.example.aplicacioncarabineros.ui.componentes.BarraInferior
import com.example.aplicacioncarabineros.ui.componentes.BarraSuperior
import com.example.aplicacioncarabineros.ui.componentes.DatoConTitulo
import com.example.aplicacioncarabineros.ui.componentes.DialogoCerrarSesion
import com.example.aplicacioncarabineros.ui.componentes.Navegacion
import com.example.aplicacioncarabineros.ui.componentes.obtenerUsuarioOSalir
import com.example.aplicacioncarabineros.ui.theme.AplicacionCarabinerosTheme
import com.example.aplicacioncarabineros.util.Formato
import kotlinx.coroutines.flow.Flow

/** PERFIL del usuario conectado (se abre desde el menú ☰) */
class PerfilActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val usuarioId = obtenerUsuarioOSalir() ?: return
        val db = AppDatabase.getInstance(this)
        val flujoUsuario = UsuarioRepository(db.usuarioDao()).observar(usuarioId)
        val flujoDocumentos = DocumentoRepository(db.documentoDao()).contar(usuarioId)
        setContent {
            AplicacionCarabinerosTheme {
                PantallaPerfil(flujoUsuario, flujoDocumentos)
            }
        }
    }
}

@Composable
fun PantallaPerfil(flujoUsuario: Flow<Usuario?>, flujoDocumentos: Flow<Int>) {
    val context = LocalContext.current
    val usuario by flujoUsuario.collectAsState(initial = null)
    val totalDocumentos by flujoDocumentos.collectAsState(initial = 0)
    var mostrarCerrarSesion by remember { mutableStateOf(false) }

    // ANIMACIÓN: el avatar aparece con rebote
    val escalaAvatar = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        escalaAvatar.animateTo(1f, spring(Spring.DampingRatioMediumBouncy, Spring.StiffnessLow))
    }

    Scaffold(
        topBar = { BarraSuperior("Mi perfil") },
        bottomBar = { BarraInferior("Perfil") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            val u = usuario
            val iniciales = u?.nombreUsuario
                ?.split(" ")
                ?.filter { it.isNotBlank() }
                ?.take(2)
                ?.joinToString("") { it.first().uppercase() }
                ?: "?"

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .size(110.dp)
                    .scale(escalaAvatar.value)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primary)
            ) {
                Text(iniciales, fontSize = 40.sp, color = MaterialTheme.colorScheme.onPrimary)
            }
            Text(u?.nombreUsuario ?: "Cargando...", style = MaterialTheme.typography.headlineSmall)

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    DatoConTitulo("Correo electrónico", u?.correo ?: "-")
                    DatoConTitulo("Documentos cargados", "$totalDocumentos")
                    DatoConTitulo("Miembro desde", u?.let { Formato.fecha(it.fechaRegistro) } ?: "-")
                }
            }

            Button(
                onClick = { Navegacion.abrir(context, EditarUsuarioActivity::class.java) },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.Filled.Edit, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Editar datos de usuario")
            }
            Button(
                onClick = { mostrarCerrarSesion = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Cerrar sesión")
            }
        }
    }

    if (mostrarCerrarSesion) {
        DialogoCerrarSesion(onCerrar = { mostrarCerrarSesion = false })
    }
}
