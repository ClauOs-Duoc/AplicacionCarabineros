package com.example.aplicacioncarabineros.model

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Tabla "usuarios" (antes era una lista en BaseDatosSimulada).
 * Se mantienen los campos originales: nombreUsuario, correo y contraseña,
 * pero la contraseña ahora se guarda como hash (nunca el texto real).
 * El correo es único: no puede haber dos cuentas con el mismo correo.
 */
@Entity(
    tableName = "usuarios",
    indices = [Index(value = ["correo"], unique = true)]
)
data class Usuario(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombreUsuario: String,
    val correo: String,
    val contrasenaHash: String,
    val fechaRegistro: Long = System.currentTimeMillis()
)
