package com.example.postventaandroid.ui.Data

import android.content.Context
import android.content.SharedPreferences

//Para mantener estado de sesion del USuario con sus datos
// SharedPreferences
//Datos de cada usuario de la BD
class SessionManager (context:Context){
    private val prefs: SharedPreferences = context.getSharedPreferences(
        "user_session", Context.MODE_PRIVATE
    )

    //Guarda el usuario
    fun saveUser(user: UsuarioData) {
        val editor = prefs.edit()
        editor.putInt("user_id", user.id)
        editor.putString("user_nombre", user.nombre)
        editor.putString("user_apellido", user.apellido)
        editor.putString("user_correo", user.correo)
        editor.putString("user_cargoTrabajador", user.cargoTrabajador)
        editor.putString("user_estadoUsuario", user.estadoUsuario)
        editor.putString("user_rut", user.Rut)
        editor.apply()
    }

    //Obtiene datos de usuario como patron de diseño Singleton
    fun fetchUser(): UsuarioData? {
        val id = prefs.getInt("user_id", -1)
        if (id == -1) {
            return null
        }
        val nombre = prefs.getString("user_nombre", null) //Nombre usuario o supervisor
        val apellido = prefs.getString("user_apellido", null)
        val correo = prefs.getString("user_correo", null)
        val cargoTrabajador = prefs.getString("user_cargoTrabajador", null)
        val estadoUsuario = prefs.getString("user_estadoUsuario", null)
        val rut = prefs.getString("user_rut", null)
        if (nombre != null && apellido != null && correo != null && cargoTrabajador != null && estadoUsuario != null && rut != null) {
            return UsuarioData(id, nombre, apellido, correo, cargoTrabajador, estadoUsuario, rut)
        }
        return null
    }

    //Elimina todos los datos del usuario
    //Este se esta llamando para cerrar sesion
    fun clearUser() {
        val editor = prefs.edit()
        editor.clear()
        editor.apply()
    }




//Este es para un token por medio del ID, se guarda para el futuro
    /*fun saveAuthToken(token: String){
        val editor = prefs.edit()
        editor.putString("auth_token", token)
        editor.apply()
    }*/

    /*fun fetchAuthToken(): String?{
        return prefs.getString("auth_token", null)

    }
    fun clearAuthToken(){
        val editor = prefs.edit()
        editor.remove("auth_token")
        editor.apply()
    }
    */
}