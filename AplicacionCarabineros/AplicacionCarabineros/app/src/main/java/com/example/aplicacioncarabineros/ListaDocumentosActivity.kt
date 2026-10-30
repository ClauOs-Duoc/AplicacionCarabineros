package com.example.aplicacioncarabineros

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.aplicacioncarabineros.data.AppDatabase
import com.example.aplicacioncarabineros.data.DocumentoRepository
import com.example.aplicacioncarabineros.model.Documento
import com.example.aplicacioncarabineros.ui.componentes.BarraInferior
import com.example.aplicacioncarabineros.ui.componentes.BarraSuperior
import com.example.aplicacioncarabineros.ui.componentes.IconoTipoDocumento
import com.example.aplicacioncarabineros.ui.componentes.Navegacion
import com.example.aplicacioncarabineros.ui.componentes.obtenerUsuarioOSalir
import com.example.aplicacioncarabineros.ui.theme.AplicacionCarabinerosTheme
import com.example.aplicacioncarabineros.util.Formato
import kotlinx.coroutines.flow.Flow

/** VER DOCUMENTOS: lista de documentos del usuario conectado (botón "Documentos" de la barra) */
class ListaDocumentosActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val usuarioId = obtenerUsuarioOSalir() ?: return
        val repo = DocumentoRepository(AppDatabase.getInstance(this).documentoDao())
        val flujoDocumentos = repo.listar(usuarioId) // se crea una sola vez
        setContent {
            AplicacionCarabinerosTheme {
                PantallaListaDocumentos(flujoDocumentos)
            }
        }
    }
}

@Composable
fun PantallaListaDocumentos(flujoDocumentos: Flow<List<Documento>>) {
    val context = LocalContext.current
    val documentos by flujoDocumentos.collectAsState(initial = null)
    var filtro by rememberSaveable { mutableStateOf("TODOS") }

    val lista = documentos
    val filtrados = when (filtro) {
        Documento.TIPO_PDF -> lista?.filter { it.tipo == Documento.TIPO_PDF }
        Documento.TIPO_WORD -> lista?.filter { it.tipo == Documento.TIPO_WORD }
        else -> lista
    }
    val estado = when {
        lista == null -> "cargando"
        filtrados.isNullOrEmpty() -> "vacio"
        else -> "lista"
    }

    Scaffold(
        topBar = { BarraSuperior("Documentos") },
        bottomBar = { BarraInferior("Documentos") }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            // Filtros (chips de Material 3)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(
                    "TODOS" to "Todos",
                    Documento.TIPO_PDF to "PDF",
                    Documento.TIPO_WORD to "Word"
                ).forEach { (valor, texto) ->
                    FilterChip(
                        selected = filtro == valor,
                        onClick = { filtro = valor },
                        label = { Text(texto) },
                        leadingIcon = if (filtro == valor) {
                            { Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(18.dp)) }
                        } else null
                    )
                }
            }

            Crossfade(targetState = estado, label = "estadoDocumentos", modifier = Modifier.weight(1f)) { e ->
                when (e) {
                    "cargando" -> Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                    "vacio" -> VacioDocumentos(hayFiltro = filtro != "TODOS")
                    else -> LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(10.dp),
                        contentPadding = PaddingValues(vertical = 12.dp),
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(filtrados ?: emptyList(), key = { it.id }) { documento ->
                            TarjetaDocumento(
                                documento = documento,
                                modifier = Modifier.animateItem(),
                                onClick = {
                                    Navegacion.abrir(context, DetalleDocumentoActivity::class.java) {
                                        putExtra("documentoId", documento.id)
                                    }
                                }
                            )
                        }
                    }
                }
            }

            // botón largo (mismo estilo que alumnos y grupos) para CARGAR documentos
            Button(
                onClick = { Navegacion.abrir(context, CargarDocumentoActivity::class.java) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(Icons.Filled.UploadFile, contentDescription = null)
                Spacer(Modifier.width(6.dp))
                Text("Cargar documento")
            }
        }
    }
}

@Composable
fun TarjetaDocumento(documento: Documento, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val esPdf = documento.tipo == Documento.TIPO_PDF
    ElevatedCard(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconoTipoDocumento(esPdf = esPdf)
            Spacer(Modifier.width(14.dp))
            Column(Modifier.weight(1f)) {
                Text(
                    documento.titulo,
                    style = MaterialTheme.typography.titleMedium,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${if (esPdf) "PDF" else "Word"} · ${Formato.tamano(documento.tamanoBytes)} · ${Formato.fecha(documento.fechaCarga)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = "Ver detalle")
        }
    }
}

@Composable
fun VacioDocumentos(hayFiltro: Boolean) {
    // ANIMACIÓN infinita: la carpeta "late"
    val transicion = rememberInfiniteTransition(label = "latido")
    val escala by transicion.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "escalaCarpeta"
    )
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Filled.FolderOpen,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(96.dp)
                .scale(escala)
        )
        Spacer(Modifier.height(12.dp))
        Text(
            if (hayFiltro) "No hay documentos de este tipo" else "Aún no tienes documentos",
            style = MaterialTheme.typography.titleMedium
        )
        Text(
            "Toca \"Cargar documento\" para subir un PDF o Word",
            textAlign = TextAlign.Center,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
