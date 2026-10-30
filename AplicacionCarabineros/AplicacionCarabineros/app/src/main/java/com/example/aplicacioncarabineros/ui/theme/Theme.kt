package com.example.aplicacioncarabineros.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColorScheme = lightColorScheme(
    primary = Verde,
    onPrimary = SobreVerde,
    primaryContainer = VerdeContenedor,
    onPrimaryContainer = SobreVerdeContenedor,
    secondary = VerdeGris,
    secondaryContainer = VerdeGrisContenedor,
    tertiary = Dorado,
    tertiaryContainer = DoradoContenedor,
    background = FondoClaro,
    error = RojoError
)

private val DarkColorScheme = darkColorScheme(
    primary = VerdeOscuro,
    onPrimary = SobreVerdeOscuro,
    primaryContainer = VerdeContenedorOscuro,
    onPrimaryContainer = SobreVerdeContenedorOscuro,
    secondary = VerdeGrisOscuro,
    secondaryContainer = VerdeGrisContenedorOscuro,
    tertiary = DoradoOscuro,
    tertiaryContainer = DoradoContenedorOscuro,
    background = FondoOscuro,
    error = RojoErrorOscuro
)

/**
 * Tema Material 3 de la app. Ahora SÍ se aplica en todas las pantallas
 * (antes setContent no lo usaba). Se quitó el color dinámico para que
 * la app siempre tenga los colores institucionales.
 */
@Composable
fun AplicacionCarabinerosTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme,
        typography = Typography,
        content = content
    )
}
