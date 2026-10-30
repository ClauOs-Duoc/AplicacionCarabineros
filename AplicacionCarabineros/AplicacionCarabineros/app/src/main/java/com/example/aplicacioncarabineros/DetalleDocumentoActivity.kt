package com.example.aplicacioncarabineros

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.aplicacioncarabineros.data.AppDatabase
import com.example.aplicacioncarabineros.data.DocumentoRepository
import com.example.aplicacioncarabineros.model.Documento
import com.example.aplicacioncarabineros.ui.componentes.BarraSuperior
import com.example.aplicacioncarabineros.ui.componentes.IconoTipoDocumento
import com.example.aplicacioncarabineros.ui.componentes.Navegacion
import com.example.aplicacioncarabineros.ui.componentes.obtenerUsuarioOSalir
import com.example.aplicacioncarabineros.ui.theme.AplicacionCarabinerosTheme
import com.example.aplicacioncarabineros.util.ArchivoUtils
import com.example.aplicacioncarabineros.util.Formato
import kotlinx.coroutines.launch

/** DETALLE de un documento: visualizar o eliminar */
class DetalleDocumentoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val usuarioId = obtenerUsuarioOSalir() ?: return
        // Recibe el id enviado desde la lista (getLongExtra, como en "PasarDatos")
        val documentoId = intent.getLongExtra("documentoId", -1L)
        val repo = DocumentoRepository(AppDatabase.getInstance(this).documentoDao())

        setContent {
            AplicacionCarabinerosTheme {
                PantallaDetalle(documentoId, usuarioId, repo)
            }
        }
    }
}

@Composable
fun PantallaDetalle(documentoId: Long, usuarioId: Long, repo: DocumentoRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var documento by remember { mutableStateOf<Documento?>(null) }
    var cargado by remember { mutableStateOf(false) }
    var mostrarConfirmacion by remember { mutableStateOf(false) }

    // Se busca el documento en la BD al abrir la pantalla
    LaunchedEffect(documentoId) {
        val encontrado = repo.buscarPorId(documentoId)
        // Seguridad: solo se muestra si pertenece al usuario conectado
        documento = if (encontrado?.usuarioId == usuarioId) encontrado else null
        cargado = true
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Detalle del documento", mostrarVolver = true) }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val doc = documento
            when {
                !cargado -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                doc == null -> Text(
                    "El documento no existe o fue eliminado",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> ContenidoDetalle(
                    documento = doc,
                    onVisualizar = {
                        if (doc.tipo == Documento.TIPO_PDF) {
                            // PDF: se muestra DENTRO de la app con el visor nativo
                            Navegacion.abrir(context, VisorPdfActivity::class.java) {
                                putExtra("ruta", doc.rutaArchivo)
                                putExtra("titulo", doc.titulo)
                            }
                        } else {
                            // WORD: Android no trae visor de Word, se abre con otra app
                            abrirExterno(context, doc)
                        }
                    },
                    onAbrirExterno = { abrirExterno(context, doc) },
                    onEliminar = { mostrarConfirmacion = true }
                )
            }
        }
    }

    // Diálogo de confirmación antes de ELIMINAR
    val docAEliminar = documento
    if (mostrarConfirmacion && docAEliminar != null) {
        AlertDialog(
            onDismissRequest = { mostrarConfirmacion = false },
            icon = { Icon(Icons.Filled.Delete, contentDescription = null) },
            title = { Text("¿Eliminar documento?") },
            text = { Text("Se eliminará \"${docAEliminar.titulo}\" de forma permanente. Esta acción no se puede deshacer.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        mostrarConfirmacion = false
                        scope.launch {
                            repo.eliminar(docAEliminar)
                            Toast.makeText(context, "Documento eliminado", Toast.LENGTH_SHORT).show()
                            (context as? Activity)?.finish() // vuelve a la lista (se actualiza sola)
                        }
                    },
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error)
                ) { Text("Eliminar") }
            },
            dismissButton = {
                TextButton(onClick = { mostrarConfirmacion = false }) { Text("Cancelar") }
            }
        )
    }
}

private fun abrirExterno(context: android.content.Context, documento: Documento) {
    val abierto = ArchivoUtils.abrirConAppExterna(context, documento)
    if (!abierto) {
        Toast.makeText(
            context,
            "No hay una app instalada para abrir este archivo (instala Word o Google Docs)",
            Toast.LENGTH_LONG
        ).show()
    }
}

@Composable
fun ContenidoDetalle(
    documento: Documento,
    onVisualizar: () -> Unit,
    onAbrirExterno: () -> Unit,
    onEliminar: () -> Unit
) {
    val esPdf = documento.tipo == Documento.TIPO_PDF

    // ANIMACIÓN de entrada: el contenido aparece deslizándose hacia arriba
    val visible = remember { MutableTransitionState(false).apply { targetState = true } }

    AnimatedVisibility(
        visibleState = visible,
        enter = fadeIn() + slideInVertically(initialOffsetY = { it / 4 })
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            IconoTipoDocumento(esPdf = esPdf, tamano = 88)
            Text(
                documento.titulo,
                style = MaterialTheme.typography.headlineSmall,
                textAlign = TextAlign.Center
            )
            AssistChip(onClick = {}, label = { Text(if (esPdf) "Documento PDF" else "Documento Word") })

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    FilaDato("Archivo original", documento.nombreOriginal)
                    HorizontalDivider()
                    FilaDato("Tamaño", Formato.tamano(documento.tamanoBytes))
                    HorizontalDivider()
                    FilaDato("Fecha de carga", Formato.fecha(documento.fechaCarga))
                }
            }

            Spacer(Modifier.height(4.dp))

            Button(onClick = onVisualizar, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Icon(Icons.Filled.Visibility, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Visualizar")
            }

            OutlinedButton(onClick = onAbrirExterno, modifier = Modifier.fillMaxWidth().height(50.dp)) {
                Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Abrir con otra app")
            }

            Button(
                onClick = onEliminar,
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                ),
                modifier = Modifier.fillMaxWidth().height(50.dp)
            ) {
                Icon(Icons.Filled.Delete, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text("Eliminar documento")
            }
        }
    }
}

@Composable
fun FilaDato(etiqueta: String, valor: String) {
    Column {
        Text(
            etiqueta,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(valor, style = MaterialTheme.typography.bodyLarge)
    }
}
