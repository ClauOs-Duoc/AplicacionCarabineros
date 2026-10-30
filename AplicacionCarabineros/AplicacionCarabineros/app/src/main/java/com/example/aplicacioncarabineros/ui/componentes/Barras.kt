// Aquí viven las dos barras que usan todas las páginas (archivo original del proyecto, mejorado).
// BarraInferior recibe el nombre de la página actual para marcar su icono
// y para no abrirla otra vez si ya estás en ella.
package com.example.aplicacioncarabineros.ui.componentes

import android.app.Activity
import android.widget.Toast
import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Quiz
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.aplicacioncarabineros.EditarUsuarioActivity
import com.example.aplicacioncarabineros.ListaAlumnosActivity
import com.example.aplicacioncarabineros.ListaDocumentosActivity
import com.example.aplicacioncarabineros.ListaGruposActivity
import com.example.aplicacioncarabineros.PerfilActivity

/**
 * BARRA SUPERIOR.
 * - mostrarVolver = true: flecha para volver (pantallas de detalle y formularios).
 * - mostrarMenu = true: botón ☰ con MENÚ desplegable y botón de cerrar sesión.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BarraSuperior(
    titulo: String,
    mostrarVolver: Boolean = false,
    mostrarMenu: Boolean = true
) {
    val context = LocalContext.current
    var menuAbierto by remember { mutableStateOf(false) }
    // controla si se muestra o no la alerta de cerrar sesión
    var mostrarCerrarSesion by remember { mutableStateOf(false) }
    var mostrarAcercaDe by remember { mutableStateOf(false) }

    CenterAlignedTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            titleContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            navigationIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer,
            actionIconContentColor = MaterialTheme.colorScheme.onPrimaryContainer
        ),
        title = {
            Text(titulo, maxLines = 1, overflow = TextOverflow.Ellipsis)
        },
        navigationIcon = {
            if (mostrarVolver) {
                IconButton(onClick = { (context as? Activity)?.finish() }) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                }
            } else if (mostrarMenu) {
                // Box: para que el menú aparezca pegado al botón ☰
                Box {
                    IconButton(onClick = { menuAbierto = true }) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menú")
                    }
                    DropdownMenu(
                        expanded = menuAbierto,
                        onDismissRequest = { menuAbierto = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Mi perfil") },
                            leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                            onClick = {
                                menuAbierto = false
                                Navegacion.irASeccion(context, PerfilActivity::class.java)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Editar datos de usuario") },
                            leadingIcon = { Icon(Icons.Filled.Edit, contentDescription = null) },
                            onClick = {
                                menuAbierto = false
                                Navegacion.abrir(context, EditarUsuarioActivity::class.java)
                            }
                        )
                        HorizontalDivider()
                        DropdownMenuItem(
                            text = { Text("Acerca de") },
                            leadingIcon = { Icon(Icons.Filled.Info, contentDescription = null) },
                            onClick = {
                                menuAbierto = false
                                mostrarAcercaDe = true
                            }
                        )
                    }
                }
            }
        },
        actions = {
            if (mostrarMenu) {
                IconButton(onClick = { mostrarCerrarSesion = true }) {
                    Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Cerrar sesión")
                }
            }
        }
    )

    if (mostrarCerrarSesion) {
        DialogoCerrarSesion(onCerrar = { mostrarCerrarSesion = false })
    }

    if (mostrarAcercaDe) {
        AlertDialog(
            onDismissRequest = { mostrarAcercaDe = false },
            icon = { Icon(Icons.Filled.Info, contentDescription = null) },
            title = { Text("Aplicación Carabineros") },
            text = {
                Text(
                    "Versión 1.0\n\nGestión de alumnos, grupos y documentos (PDF y Word). " +
                        "Rúbricas: próximamente.\n\nKotlin, Jetpack Compose, Material 3 y Room."
                )
            },
            confirmButton = {
                TextButton(onClick = { mostrarAcercaDe = false }) { Text("Entendido") }
            }
        )
    }
}

/** Alerta de cerrar sesión (la del proyecto original). Se reutiliza también en Perfil. */
@Composable
fun DialogoCerrarSesion(onCerrar: () -> Unit) {
    val context = LocalContext.current
    AlertDialog(
        icon = { Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null) },
        title = { Text("Cerrar sesión") },
        text = { Text("¿Seguro que deseas cerrar sesión?") },
        confirmButton = {
            Button(onClick = {
                onCerrar()
                Navegacion.irACerrarSesion(context) // vista de cierre de sesión
            }) {
                Text("Cerrar sesión")
            }
        },
        dismissButton = {
            TextButton(onClick = onCerrar) { Text("Cancelar") }
        },
        onDismissRequest = onCerrar
    )
}

/**
 * BARRA INFERIOR con los 5 botones del proyecto original:
 * Inicio, Alumnos, Grupos, Rúbricas (próximamente) y Documentos.
 */
@Composable
fun BarraInferior(actual: String) {
    val context = LocalContext.current

    BottomAppBar(
        actions = {
            ItemBarra(Icons.Filled.Home, "Inicio", actual == "Inicio", Modifier.weight(1f)) {
                if (actual != "Inicio") Navegacion.irAPrincipal(context)
            }
            ItemBarra(Icons.Filled.Person, "Alumnos", actual == "Alumnos", Modifier.weight(1f)) {
                if (actual != "Alumnos") Navegacion.irASeccion(context, ListaAlumnosActivity::class.java)
            }
            ItemBarra(Icons.Filled.Groups, "Grupos", actual == "Grupos", Modifier.weight(1f)) {
                if (actual != "Grupos") Navegacion.irASeccion(context, ListaGruposActivity::class.java)
            }
            // Antes "Pendiente": queda reservado para las rúbricas
            ItemBarra(Icons.Filled.Quiz, "Rúbricas", actual == "Rúbricas", Modifier.weight(1f)) {
                Toast.makeText(context, "Rúbricas: próximamente", Toast.LENGTH_SHORT).show()
            }
            ItemBarra(Icons.Filled.Description, "Documentos", actual == "Documentos", Modifier.weight(1f)) {
                if (actual != "Documentos") Navegacion.irASeccion(context, ListaDocumentosActivity::class.java)
            }
        }
    )
}

@Composable
private fun ItemBarra(
    icono: ImageVector,
    texto: String,
    seleccionado: Boolean,
    modifier: Modifier,
    onClick: () -> Unit
) {
    // ANIMACIÓN (como en CambiarColorActivity de clase): el color cambia suave
    val color by animateColorAsState(
        targetValue = if (seleccionado) MaterialTheme.colorScheme.primary
        else MaterialTheme.colorScheme.onSurfaceVariant,
        label = "colorItemBarra"
    )
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = modifier
            .clickable(onClick = onClick)
            .padding(vertical = 6.dp)
    ) {
        Icon(icono, contentDescription = texto, tint = color)
        Text(
            texto,
            style = MaterialTheme.typography.labelSmall,
            color = color,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
