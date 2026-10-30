package com.example.aplicacioncarabineros

import android.app.Activity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.aplicacioncarabineros.data.AppDatabase
import com.example.aplicacioncarabineros.data.GrupoRepository
import com.example.aplicacioncarabineros.model.Alumno
import com.example.aplicacioncarabineros.ui.componentes.BarraSuperior
import com.example.aplicacioncarabineros.ui.componentes.obtenerUsuarioOSalir
import com.example.aplicacioncarabineros.ui.theme.AplicacionCarabinerosTheme
import kotlinx.coroutines.launch

/** AGREGAR GRUPO: elegir 3 alumnos sin grupo (lógica del proyecto original) */
class AgregarGrupoActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        obtenerUsuarioOSalir() ?: return
        val db = AppDatabase.getInstance(this)
        val repository = GrupoRepository(db.grupoDao(), db.alumnoDao())
        setContent {
            AplicacionCarabinerosTheme {
                PantallaAgregarGrupo(repository)
            }
        }
    }
}

@Composable
fun PantallaAgregarGrupo(repository: GrupoRepository) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()

    // solo los alumnos que todavía no tienen grupo (desde Room)
    val flujoSinGrupo = remember { repository.obtenerAlumnosSinGrupo() }
    val sinGrupo by flujoSinGrupo.collectAsState(initial = emptyList())

    // Se guardan los ids (Long) para que rememberSaveable pueda conservarlos al girar
    var id1 by rememberSaveable { mutableStateOf<Long?>(null) }
    var id2 by rememberSaveable { mutableStateOf<Long?>(null) }
    var id3 by rememberSaveable { mutableStateOf<Long?>(null) }
    var guardando by remember { mutableStateOf(false) }

    val alumno1 = sinGrupo.find { it.id == id1 }
    val alumno2 = sinGrupo.find { it.id == id2 }
    val alumno3 = sinGrupo.find { it.id == id3 }
    val seleccionados = listOfNotNull(alumno1, alumno2, alumno3)

    // para los alumnos sin grupo que no estén elegidos en otro selector
    fun opcionesPara(actual: Alumno?): List<Alumno> {
        return sinGrupo.filter { alumno -> alumno == actual || alumno !in seleccionados }
    }

    Scaffold(
        topBar = { BarraSuperior(titulo = "Agregar grupo", mostrarVolver = true, mostrarMenu = false) }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(20.dp)
        ) {
            // selectores (ocupan el espacio disponible)
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text("Selecciona 3 alumnos sin grupo", style = MaterialTheme.typography.titleMedium)
                AnimatedVisibility(visible = sinGrupo.size < 3) {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer)) {
                        Text(
                            "No hay suficientes alumnos sin grupo (se necesitan 3). Agrega más alumnos primero.",
                            modifier = Modifier.padding(12.dp),
                            color = MaterialTheme.colorScheme.onTertiaryContainer
                        )
                    }
                }
                SelectorAlumno("Seleccionar alumno 1", alumno1, opcionesPara(alumno1)) { id1 = it.id }
                SelectorAlumno("Seleccionar alumno 2", alumno2, opcionesPara(alumno2)) { id2 = it.id }
                SelectorAlumno("Seleccionar alumno 3", alumno3, opcionesPara(alumno3)) { id3 = it.id }
            }
            // botones en la parte inferior
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                OutlinedButton(
                    onClick = { (context as? Activity)?.finish() }, // vuelve a la lista sin guardar nada
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Cancelar")
                }
                Button(
                    onClick = {
                        val a1 = alumno1
                        val a2 = alumno2
                        val a3 = alumno3
                        if (a1 == null || a2 == null || a3 == null) {
                            Toast.makeText(context, "Debes elegir los 3 alumnos", Toast.LENGTH_SHORT).show()
                        } else {
                            guardando = true
                            scope.launch {
                                val numero = repository.agregarGrupo(listOf(a1, a2, a3))
                                Toast.makeText(context, "Grupo $numero creado correctamente", Toast.LENGTH_SHORT).show()
                                (context as? Activity)?.finish() // vuelve a la lista
                            }
                        }
                    },
                    enabled = !guardando,
                    modifier = Modifier.weight(1f)
                ) {
                    Text("Aceptar")
                }
            }
        }
    }
}

/** Selector desplegable de alumnos (componente original del proyecto) */
@Composable
fun SelectorAlumno(
    etiqueta: String,
    seleccionado: Alumno?,
    opciones: List<Alumno>,
    onSeleccion: (Alumno) -> Unit
) {
    var expandido by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedButton(
            onClick = { expandido = !expandido },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(seleccionado?.nombreCompleto ?: etiqueta, modifier = Modifier.weight(1f))
            Icon(Icons.Filled.ArrowDropDown, contentDescription = null)
        }
        DropdownMenu(
            expanded = expandido,
            onDismissRequest = { expandido = false }
        ) {
            if (opciones.isEmpty()) {
                DropdownMenuItem(text = { Text("No hay alumnos disponibles") }, onClick = { expandido = false })
            }
            opciones.forEach { alumno ->
                DropdownMenuItem(
                    text = { Text("${alumno.nombreCompleto} (${alumno.rut})") },
                    onClick = {
                        onSeleccion(alumno)
                        expandido = false
                    }
                )
            }
        }
    }
}
