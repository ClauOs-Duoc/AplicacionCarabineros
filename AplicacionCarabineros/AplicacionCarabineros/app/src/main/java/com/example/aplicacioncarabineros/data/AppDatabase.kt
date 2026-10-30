package com.example.aplicacioncarabineros.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.aplicacioncarabineros.model.Alumno
import com.example.aplicacioncarabineros.model.Documento
import com.example.aplicacioncarabineros.model.Grupo
import com.example.aplicacioncarabineros.model.Usuario

/**
 * Base de datos SQLite local (Room). REEMPLAZA a BaseDatosSimulada:
 * los datos ya no se pierden al cerrar la app.
 * Se crea UNA sola vez (Singleton) y todas las pantallas la comparten.
 */
@Database(
    entities = [Usuario::class, Grupo::class, Alumno::class, Documento::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun usuarioDao(): UsuarioDAO
    abstract fun alumnoDao(): AlumnoDAO
    abstract fun grupoDao(): GrupoDAO
    abstract fun documentoDao(): DocumentoDAO

    companion object {
        @Volatile
        private var instancia: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return instancia ?: synchronized(this) {
                instancia ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "aplicacion_carabineros.db"
                ).build().also { instancia = it }
            }
        }
    }
}
