package com.example.aplicacioncarabineros.ui.componentes

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp

/** Ícono cuadrado del tipo de documento: rojo para PDF, verde para Word */
@Composable
fun IconoTipoDocumento(esPdf: Boolean, tamano: Int = 48) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(tamano.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (esPdf) MaterialTheme.colorScheme.errorContainer
                else MaterialTheme.colorScheme.primaryContainer
            )
    ) {
        Icon(
            imageVector = if (esPdf) Icons.Filled.PictureAsPdf else Icons.Filled.Description,
            contentDescription = if (esPdf) "PDF" else "Word",
            tint = if (esPdf) MaterialTheme.colorScheme.onErrorContainer
            else MaterialTheme.colorScheme.onPrimaryContainer,
            modifier = Modifier.size((tamano * 0.55f).dp)
        )
    }
}

/**
 * Un dato con su título arriba (es el DatoAlumno del proyecto original,
 * ahora reutilizable en alumnos, documentos y perfil).
 */
@Composable
fun DatoConTitulo(titulo: String, valor: String) {
    Column {
        Text(
            text = titulo,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyLarge
        )
    }
}
