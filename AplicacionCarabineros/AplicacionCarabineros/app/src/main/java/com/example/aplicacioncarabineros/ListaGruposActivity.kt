package com.example.aplicacioncarabineros

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.aplicacioncarabineros.data.AppDatabase
import com.example.aplicacioncarabineros.data.GrupoRepository
import com.example.aplicacioncarabineros.model.GrupoConIntegrantes
import com.example.aplicacioncarabineros.ui.componentes.BarraInferior
import com.example.aplicacioncarabineros.ui.componentes.BarraSuperior
import com.example.aplicacioncarabineros.ui.componentes.Navegacion
import com.example.aplicacioncarabineros.ui.componentes.obtenerUsuarioOSalir
import com.example.aplicacioncarabineros.ui.theme.AplicacionCarabinerosTheme
import kotlinx.coroutines.flow.Flow

/** LISTA DE GRUPOS (diseño original: lista + botón largo "+" abajo) */
class ListaGruposActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        obtenerUsuarioOSalir() ?: return
        val db = AppDatabase.getInstance(this)
        val flujoGrupos = GrupoRepository(db.grupoDao(), db.alumnoDao()).obtenerGrupos()
        setContent {
            AplicacionCarabinerosTheme {
                PantallaListaGrupos(flujoGrupos)
            }
        }
    }
}

@Composable
fun PantallaListaGrupos(flujoGrupos: Flow<List<GrupoConIntegrantes>>) {
    val context = LocalContext.current
    val grupos by flujoGrupos.collectAsState(initial = null)

    Scaffold(
        topBar = { BarraSuperior("Grupos") },
        bottomBar = { BarraInferior("Grupos") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            val lista = grupos
            val estado = when {
                lista == null -> "cargando"
                lista.isEmpty() -> "vacio"
                else -> "lista"
            }
            Crossfade(targetState = estado, label = "estadoGrupos", modifier = Modifier.weight(1f)) { e ->
                when (e) {
                    "cargando" -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    "vacio" -> MensajeVacio("Aún no hay grupos", "Toca el botón + para crear uno con 3 alumnos")
                    else -> LazyColumn(modifier = Modifier.fillMaxSize()) {
                        items(lista ?: emptyList(), key = { it.grupo.id }) { grupo ->
                            GrupoCard(grupo, Modifier.animateItem())
                        }
                    }
                }
            }
            // botón largo con el símbolo + sobre la barra inferior para agregar grupos
            Button(
                onClick = { Navegacion.abrir(context, AgregarGrupoActivity::class.java) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = "Agregar grupo")
                Spacer(Modifier.width(6.dp))
                Text("Agregar grupo")
            }
        }
    }
}

/**
 * Tarjeta del grupo. Los integrantes se muestran/ocultan con la animación
 * de expansión y contracción vista en clase (ExpansionActivity).
 */
@Composable
fun GrupoCard(item: GrupoConIntegrantes, modifier: Modifier = Modifier) {
    var expandida by rememberSaveable(item.grupo.id) { mutableStateOf(false) }

    Card(
        onClick = { expandida = !expandida }, // ! invierte el valor: true/false
        modifier = modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Column(Modifier.weight(1f)) {
                    Text(text = "Grupo ${item.grupo.numero}", fontSize = 20.sp)
                    Text("${item.integrantes.size} integrantes")
                }
                Icon(
                    if (expandida) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                    contentDescription = if (expandida) "Ver menos" else "Ver más"
                )
            }
            AnimatedVisibility(
                visible = expandida,
                enter = expandVertically(animationSpec = tween(600), expandFrom = Alignment.Top),
                exit = shrinkVertically(animationSpec = tween(600), shrinkTowards = Alignment.Top)
            ) {
                Column(Modifier.padding(top = 8.dp)) {
                    item.integrantes.forEach { alumno ->
                        Text("• ${alumno.nombreCompleto}")
                    }
                }
            }
        }
    }
}
