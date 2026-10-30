package com.example.aplicacioncarabineros

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacioncarabineros.data.AlumnoRepository
import com.example.aplicacioncarabineros.data.AppDatabase
import com.example.aplicacioncarabineros.model.AlumnoConGrupo
import com.example.aplicacioncarabineros.ui.componentes.BarraInferior
import com.example.aplicacioncarabineros.ui.componentes.BarraSuperior
import com.example.aplicacioncarabineros.ui.componentes.Navegacion
import com.example.aplicacioncarabineros.ui.componentes.obtenerUsuarioOSalir
import com.example.aplicacioncarabineros.ui.theme.AplicacionCarabinerosTheme
import kotlinx.coroutines.flow.Flow

/** LISTA DE ALUMNOS (diseño original: lista + botón largo "+" abajo) */
class ListaAlumnosActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        obtenerUsuarioOSalir() ?: return
        val repository = AlumnoRepository(AppDatabase.getInstance(this).alumnoDao())
        val flujoAlumnos = repository.obtenerAlumnos()
        setContent {
            AplicacionCarabinerosTheme {
                PantallaListaAlumnos(flujoAlumnos)
            }
        }
    }
}

@Composable
fun PantallaListaAlumnos(flujoAlumnos: Flow<List<AlumnoConGrupo>>) {
    val context = LocalContext.current
    // La lista viene de Room y se actualiza sola (null = cargando)
    val alumnos by flujoAlumnos.collectAsState(initial = null)

    Scaffold(
        topBar = { BarraSuperior("Alumnos") },
        bottomBar = { BarraInferior("Alumnos") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            val lista = alumnos
            val estado = when {
                lista == null -> "cargando"
                lista.isEmpty() -> "vacio"
                else -> "lista"
            }
            // ANIMACIÓN: fundido entre cargando / vacío / lista
            Crossfade(targetState = estado, label = "estadoAlumnos", modifier = Modifier.weight(1f)) { e ->
                when (e) {
                    "cargando" -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    "vacio" -> MensajeVacio(
                        "Aún no hay alumnos",
                        "Toca el botón + para agregar el primero"
                    )
                    else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(lista ?: emptyList(), key = { it.alumno.id }) { item ->
                            AlumnoCard(item, Modifier.animateItem())
                        }
                    }
                }
            }
            // botón largo con el símbolo + sobre la barra inferior para agregar un alumno
            Button(
                onClick = { Navegacion.abrir(context, AgregarAlumnoActivity::class.java) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Agregar alumno")
                Spacer(Modifier.width(6.dp))
                Text("Agregar alumno")
            }
        }
    }
}

@Composable
fun AlumnoCard(item: AlumnoConGrupo, modifier: Modifier = Modifier) {
    val context = LocalContext.current
    val alumno = item.alumno
    Card(
        onClick = {
            // manda el id del alumno a la pantalla de detalle
            Navegacion.abrir(context, DetalleAlumnoActivity::class.java) {
                putExtra("alumnoId", alumno.id)
            }
        },
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Text(text = alumno.nombreCompleto, fontSize = 20.sp)
                Text("RUT: ${alumno.rut}")
            }
            AssistChip(
                onClick = {},
                label = { Text(item.numeroGrupo?.let { "Grupo $it" } ?: "Sin grupo") }
            )
        }
    }
}

/** Mensaje para listas vacías (se usa en alumnos, grupos y documentos) */
@Composable
fun MensajeVacio(titulo: String, detalle: String) {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.PersonSearch,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(72.dp)
        )
        Spacer(Modifier.height(8.dp))
        Text(titulo, style = MaterialTheme.typography.titleMedium)
        Text(detalle, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
