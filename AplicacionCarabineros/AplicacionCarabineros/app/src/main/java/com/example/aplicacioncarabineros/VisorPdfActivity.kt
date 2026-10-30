package com.example.aplicacioncarabineros

import android.graphics.Bitmap
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.example.aplicacioncarabineros.ui.componentes.BarraSuperior
import com.example.aplicacioncarabineros.ui.theme.AplicacionCarabinerosTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

/**
 * VISUALIZAR documentos PDF dentro de la app.
 * Usa PdfRenderer, una herramienta NATIVA de Android que convierte cada página en una imagen.
 */
class VisorPdfActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val ruta = intent.getStringExtra("ruta") ?: ""
        val titulo = intent.getStringExtra("titulo") ?: "Documento"

        setContent {
            AplicacionCarabinerosTheme {
                PantallaVisorPdf(ruta, titulo)
            }
        }
    }
}

/**
 * Abre el PDF y dibuja las páginas UNA POR UNA, solo cuando se necesitan
 * (así no se llena la memoria con PDFs largos).
 */
class LectorPdf(archivo: File) {
    private val descriptor = ParcelFileDescriptor.open(archivo, ParcelFileDescriptor.MODE_READ_ONLY)
    private val renderer = PdfRenderer(descriptor)
    private val candado = Any() // PdfRenderer solo puede dibujar una página a la vez
    private var cerrado = false

    val totalPaginas: Int = renderer.pageCount

    /** Dibuja una página en segundo plano (Dispatchers.IO) y la devuelve como imagen */
    suspend fun dibujarPagina(indice: Int, anchoPx: Int): ImageBitmap? =
        withContext(Dispatchers.IO) {
            synchronized(candado) {
                if (cerrado) return@synchronized null
                val pagina = renderer.openPage(indice)
                val alto = (anchoPx.toFloat() / pagina.width * pagina.height).toInt()
                val bitmap = Bitmap.createBitmap(anchoPx, alto, Bitmap.Config.ARGB_8888)
                bitmap.eraseColor(android.graphics.Color.WHITE) // fondo blanco
                pagina.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
                pagina.close()
                bitmap.asImageBitmap()
            }
        }

    /** Libera el archivo (espera a que termine la página que se esté dibujando) */
    fun cerrar() {
        synchronized(candado) {
            if (!cerrado) {
                cerrado = true
                renderer.close()
                descriptor.close()
            }
        }
    }
}

@Composable
fun PantallaVisorPdf(ruta: String, titulo: String) {
    // Se abre el PDF una sola vez (null si el archivo no existe o está dañado)
    val lector = remember(ruta) {
        try {
            LectorPdf(File(ruta))
        } catch (e: Exception) {
            null
        }
    }

    // Al salir de la pantalla se libera el archivo
    DisposableEffect(lector) {
        onDispose { lector?.cerrar() }
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = titulo, mostrarVolver = true, mostrarMenu = false) }
    ) { innerPadding ->
        if (lector == null) {
            Box(
                Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentAlignment = Alignment.Center
            ) {
                Text("No se pudo abrir el PDF. El archivo puede estar dañado.")
            }
        } else {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding),
                contentPadding = PaddingValues(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(lector.totalPaginas) { indice ->
                    PaginaPdf(lector, indice)
                }
            }
        }
    }
}

@Composable
fun PaginaPdf(lector: LectorPdf, indice: Int) {
    // produceState: dibuja la página en segundo plano y la muestra cuando está lista
    val imagen by produceState<ImageBitmap?>(initialValue = null, lector, indice) {
        value = lector.dibujarPagina(indice, anchoPx = 1000)
    }

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ElevatedCard(modifier = Modifier.fillMaxWidth()) {
            val img = imagen
            if (img == null) {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(420.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                Image(
                    bitmap = img,
                    contentDescription = "Página ${indice + 1}",
                    contentScale = ContentScale.FillWidth,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
        Text(
            "Página ${indice + 1} de ${lector.totalPaginas}",
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
