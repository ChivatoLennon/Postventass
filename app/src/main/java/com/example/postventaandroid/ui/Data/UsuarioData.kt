package com.example.postventaandroid.ui.Data

//Clase datos de Usuarios
//Para login y sessionManager. Mantener usuario sesion y datos
data class UsuarioData(
    val id: Int,
    val nombre: String,
    val apellido: String,
    val correo: String,
    val cargoTrabajador: String,
    val estadoUsuario: String,
    val Rut : String
)
