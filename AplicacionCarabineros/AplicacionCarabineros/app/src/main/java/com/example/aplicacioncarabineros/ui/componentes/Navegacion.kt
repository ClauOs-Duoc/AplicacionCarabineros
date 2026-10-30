package com.example.aplicacioncarabineros.ui.componentes

import android.app.Activity
import android.app.ActivityOptions
import android.content.Context
import android.content.Intent
import com.example.aplicacioncarabineros.CerrarSesionActivity
import com.example.aplicacioncarabineros.MainActivity
import com.example.aplicacioncarabineros.PrincipalActivity
import com.example.aplicacioncarabineros.R
import com.example.aplicacioncarabineros.data.SesionManager

/**
 * Centraliza la navegación entre Activities (Intents, como en clase).
 * Todas las pantallas se abren con una ANIMACIÓN de transición
 * usando ActivityOptions.makeCustomAnimation + archivos de res/anim
 * (la misma técnica del ejemplo EjemplosAnimaciones).
 */
object Navegacion {

    /** Abre una pantalla con animación. "configurar" permite agregar extras o flags. */
    fun abrir(
        context: Context,
        destino: Class<*>,
        animacionEntrada: Int = R.anim.entrada_derecha,
        configurar: Intent.() -> Unit = {}
    ) {
        val intent = Intent(context, destino).apply(configurar)
        val opciones = ActivityOptions.makeCustomAnimation(
            context,
            animacionEntrada,       // cómo entra la nueva pantalla
            R.anim.salida_izquierda // cómo sale la actual
        )
        context.startActivity(intent, opciones.toBundle())
    }

    /** Vuelve a la vista de bienvenida (Principal) cerrando las pantallas de encima */
    fun irAPrincipal(context: Context) {
        abrir(context, PrincipalActivity::class.java) {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        }
    }

    /** Secciones de la barra inferior: si ya están abiertas, las trae al frente */
    fun irASeccion(context: Context, destino: Class<*>) {
        abrir(context, destino) {
            addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT)
        }
    }

    /** Después del login: abre Principal con rebote y borra Bienvenida/Login de la pila */
    fun irAPrincipalLimpiando(context: Context) {
        abrir(context, PrincipalActivity::class.java, R.anim.entrada_bounce) {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    /** Vuelve a la pantalla de bienvenida y BORRA todas las pantallas anteriores */
    fun irABienvenidaLimpiando(context: Context) {
        val intent = Intent(context, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        context.startActivity(intent)
    }

    fun irACerrarSesion(context: Context) {
        abrir(context, CerrarSesionActivity::class.java)
    }
}

/**
 * Protege las pantallas privadas: devuelve el id del usuario conectado.
 * Si no hay sesión, vuelve a la bienvenida, cierra la pantalla y devuelve null.
 */
fun Activity.obtenerUsuarioOSalir(): Long? {
    val id = SesionManager(this).obtenerUsuarioId()
    if (id == -1L) {
        Navegacion.irABienvenidaLimpiando(this)
        finish()
        return null
    }
    return id
}
