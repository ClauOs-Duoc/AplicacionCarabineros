package com.example.aplicacioncarabineros

import android.app.Activity
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Title
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.aplicacioncarabineros.data.AppDatabase
import com.example.aplicacioncarabineros.data.DocumentoRepository
import com.example.aplicacioncarabineros.model.Documento
import com.example.aplicacioncarabineros.ui.componentes.BarraSuperior
import com.example.aplicacioncarabineros.ui.componentes.IconoTipoDocumento
import com.example.aplicacioncarabineros.ui.componentes.CampoTexto
import com.example.aplicacioncarabineros.ui.componentes.obtenerUsuarioOSalir
import com.example.aplicacioncarabineros.ui.theme.AplicacionCarabinerosTheme
import com.example.aplicacioncarabineros.util.ArchivoUtils
import com.example.aplicacioncarabineros.util.Formato
import com.example.aplicacioncarabineros.util.Validaciones
import kotlinx.coroutines.launch

/** CARGAR DOCUMENTOS (PDF o Word) */
class CargarDocumentoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val usuarioId = obtenerUsuarioOSalir() ?: return
        val repo = DocumentoRepository(AppDatabase.getInstance(this).documentoDao())

        setContent {
            AplicacionCarabinerosTheme {
                PantallaCargarDocumento(repo, usuarioId)
            }
        }
    }
}

@Composable
fun PantallaCargarDocumento(repo: DocumentoRepository, usuarioId: Long) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    var uriSeleccionada by rememberSaveable { mutableStateOf<Uri?>(null) }
    var titulo by rememberSaveable { mutableStateOf("") }
    var intentoEnviar by rememberSaveable { mutableStateOf(false) }
    var guardando by remember { mutableStateOf(false) }

    // Datos del archivo elegido (nombre, tamaño, tipo). Se recalcula si cambia la Uri.
    val info = remember(uriSeleccionada) {
        uriSeleccionada?.let { ArchivoUtils.obtenerInfo(context, it) }
    }

    // RECURSO NATIVO: abre el selector de archivos de Android filtrando PDF y Word
    val selectorArchivos = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri ->
        if (uri != null) {
            uriSeleccionada = uri
            // Si el título está vacío, se sugiere el nombre del archivo sin extensión
            if (titulo.isBlank()) {
                titulo = ArchivoUtils.obtenerInfo(context, uri).nombre.substringBeforeLast('.')
            }
        }
    }

    // El error de tipo/tamaño se muestra apenas se elige; el de "falta archivo" al enviar
    val errorArchivo = if (info != null || intentoEnviar) ArchivoUtils.validarArchivo(info) else null
    val errorTitulo = if (intentoEnviar) Validaciones.validarTitulo(titulo) else null

    Scaffold(
        topBar = { BarraSuperior(titulo = "Cargar documento", mostrarVolver = true) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Text("1. Selecciona el archivo", style = MaterialTheme.typography.titleMedium)

            OutlinedCard(
                onClick = { selectorArchivos.launch(ArchivoUtils.TIPOS_PERMITIDOS) },
                modifier = Modifier.fillMaxWidth()
            ) {
                // ANIMACIÓN: cambia con transición entre "sin archivo" y "archivo elegido"
                AnimatedContent(targetState = info, label = "zonaArchivo") { archivo ->
                    if (archivo == null) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(28.dp)
                        ) {
                            Icon(
                                Icons.Filled.CloudUpload,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(56.dp)
                            )
                            Spacer(Modifier.height(8.dp))
                            Text(
                                "Toca aquí para elegir un PDF o Word",
                                style = MaterialTheme.typography.titleSmall,
                                textAlign = TextAlign.Center
                            )
                            Text(
                                "Formatos: .pdf, .doc, .docx · Máximo 10 MB",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    } else {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp)
                        ) {
                            IconoTipoDocumento(esPdf = archivo.tipo == Documento.TIPO_PDF)
                            Spacer(Modifier.width(12.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    archivo.nombre,
                                    style = MaterialTheme.typography.titleSmall,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    "${Formato.tamano(archivo.tamanoBytes)} · Toca para cambiar",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }

            AnimatedVisibility(visible = errorArchivo != null) {
                Text(
                    errorArchivo ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall
                )
            }

            Text("2. Escribe un título", style = MaterialTheme.typography.titleMedium)

            CampoTexto(
                valor = titulo,
                onCambio = { titulo = it },
                etiqueta = "Título del documento",
                icono = Icons.Filled.Title,
                error = errorTitulo
            )

            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    intentoEnviar = true
                    val uri = uriSeleccionada
                    val valido = uri != null &&
                            ArchivoUtils.validarArchivo(info) == null &&
                            Validaciones.validarTitulo(titulo) == null

                    if (valido && uri != null) {
                        guardando = true
                        scope.launch {
                            try {
                                repo.guardar(context, uri, titulo, usuarioId)
                                Toast.makeText(context, "Documento cargado correctamente", Toast.LENGTH_SHORT).show()
                                (context as? Activity)?.finish() // vuelve a la lista
                            } catch (e: Exception) {
                                guardando = false
                                Toast.makeText(context, "Error al cargar: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                },
                enabled = !guardando,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                if (guardando) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Icon(Icons.Filled.Save, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Guardar documento")
                }
            }
        }
    }
}
