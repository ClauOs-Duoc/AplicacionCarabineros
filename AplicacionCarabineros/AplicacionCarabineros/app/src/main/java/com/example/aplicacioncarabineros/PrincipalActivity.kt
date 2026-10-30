package com.example.aplicacioncarabineros

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aplicacioncarabineros.data.AlumnoRepository
import com.example.aplicacioncarabineros.data.AppDatabase
import com.example.aplicacioncarabineros.data.DocumentoRepository
import com.example.aplicacioncarabineros.data.GrupoRepository
import com.example.aplicacioncarabineros.data.SesionManager
import com.example.aplicacioncarabineros.data.UsuarioRepository
import com.example.aplicacioncarabineros.model.Usuario
import com.example.aplicacioncarabineros.ui.componentes.BarraInferior
import com.example.aplicacioncarabineros.ui.componentes.BarraSuperior
import com.example.aplicacioncarabineros.ui.componentes.Navegacion
import com.example.aplicacioncarabineros.ui.componentes.obtenerUsuarioOSalir
import com.example.aplicacioncarabineros.ui.theme.AplicacionCarabinerosTheme
import kotlinx.coroutines.flow.Flow

/** VISTA DE BIENVENIDA (después de iniciar sesión) con los accesos rápidos */
class PrincipalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val usuarioId = obtenerUsuarioOSalir() ?: return // sin sesión -> bienvenida

        val db = AppDatabase.getInstance(this)
        val nombre = SesionManager(this).obtenerNombre()
        // Los contadores se crean una vez y se actualizan solos (Flow)
        val totalAlumnos = AlumnoRepository(db.alumnoDao()).contar()
        val totalGrupos = GrupoRepository(db.grupoDao(), db.alumnoDao()).contar()
        val totalDocumentos = DocumentoRepository(db.documentoDao()).contar(usuarioId)
        // El nombre se observa: si el usuario lo edita, el saludo cambia solo
        val flujoUsuario = UsuarioRepository(db.usuarioDao()).observar(usuarioId)

        setContent {
            AplicacionCarabinerosTheme {
                PantallaPrincipal(nombre, flujoUsuario, totalAlumnos, totalGrupos, totalDocumentos)
            }
        }
    }
}

/** Datos de cada acceso rápido */
data class AccesoRapido(
    val titulo: String,
    val icono: ImageVector,
    val cantidad: Int?,           // null = no muestra número
    val habilitado: Boolean = true,
    val accion: () -> Unit
)

@Composable
fun PantallaPrincipal(
    nombre: String,
    flujoUsuario: Flow<Usuario?>,
    totalAlumnos: Flow<Int>,
    totalGrupos: Flow<Int>,
    totalDocumentos: Flow<Int>
) {
    val context = LocalContext.current
    val usuario by flujoUsuario.collectAsState(initial = null)
    val alumnos by totalAlumnos.collectAsState(initial = 0)
    val grupos by totalGrupos.collectAsState(initial = 0)
    val documentos by totalDocumentos.collectAsState(initial = 0)

    val accesos = listOf(
        AccesoRapido("Alumnos", Icons.Filled.Person, alumnos) {
            Navegacion.irASeccion(context, ListaAlumnosActivity::class.java)
        },
        AccesoRapido("Grupos", Icons.Filled.Groups, grupos) {
            Navegacion.irASeccion(context, ListaGruposActivity::class.java)
        },
        AccesoRapido("Documentos", Icons.Filled.Description, documentos) {
            Navegacion.irASeccion(context, ListaDocumentosActivity::class.java)
        },
        AccesoRapido("Cargar documento", Icons.Filled.UploadFile, null) {
            Navegacion.abrir(context, CargarDocumentoActivity::class.java)
        },
        AccesoRapido("Mi perfil", Icons.Filled.School, null) {
            Navegacion.irASeccion(context, PerfilActivity::class.java)
        },
        AccesoRapido("Rúbricas", Icons.Filled.Quiz, null, habilitado = false) {
            Toast.makeText(context, "Rúbricas: próximamente", Toast.LENGTH_SHORT).show()
        }
    )

    // ANIMACIÓN de entrada: el contenido aparece deslizándose desde abajo
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }

    Scaffold(
        topBar = { BarraSuperior("Inicio") },
        bottomBar = { BarraInferior("Inicio") }
    ) { innerPadding ->
        AnimatedVisibility(
            visibleState = visible,
            enter = fadeIn() + slideInVertically(initialOffsetY = { it / 6 }),
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Column(Modifier.padding(20.dp)) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        // Escudo de Carabineros (res/drawable-nodpi/escudo_carabineros.png)
                        Image(
                            painter = painterResource(id = R.drawable.escudo_carabineros),
                            contentDescription = null,
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text(
                                "Bienvenido, ${usuario?.nombreUsuario ?: nombre}",
                                style = MaterialTheme.typography.titleLarge,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Text(
                                "¿Qué quieres hacer hoy?",
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
                Text("Accesos rápidos", style = MaterialTheme.typography.titleMedium)
                Spacer(Modifier.height(8.dp))

                LazyVerticalGrid(
                    columns = GridCells.Fixed(2),
                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    modifier = Modifier.fillMaxSize()
                ) {
                    items(accesos) { acceso -> TarjetaAcceso(acceso) }
                }
            }
        }
    }
}

@Composable
fun TarjetaAcceso(acceso: AccesoRapido) {
    ElevatedCard(
        onClick = acceso.accion,
        colors = CardDefaults.elevatedCardColors(
            containerColor = if (acceso.habilitado) MaterialTheme.colorScheme.surface
            else MaterialTheme.colorScheme.surfaceVariant
        ),
        modifier = Modifier
            .fillMaxWidth()
            .height(130.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                acceso.icono,
                contentDescription = null,
                tint = if (acceso.habilitado) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(36.dp)
            )
            Spacer(Modifier.height(6.dp))
            Text(acceso.titulo, style = MaterialTheme.typography.titleSmall, textAlign = TextAlign.Center)
            if (acceso.cantidad != null) {
                // ANIMACIÓN: el número cambia con un fundido
                AnimatedContent(targetState = acceso.cantidad, label = "contador") { cantidad ->
                    Text(
                        "$cantidad",
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (!acceso.habilitado) {
                Text("Próximamente", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}
