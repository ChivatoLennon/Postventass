package com.example.postventaandroid.ui.Data

import android.os.StrictMode
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.sql.Connection
import java.sql.DriverManager
import java.sql.SQLException

//Clase con las instancias de conexiones a la bd Sql Server
class ConnSQL {

    //Para servidor online de Magistra
    private val ip = "35.202.43.196"
    private val puerto2 = "1433"
    private val db = "Webpay"
    private val userName = "test_activa"
    private val password = "test_erp_2023"

    fun dbConn(): Connection? {
        val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
        StrictMode.setThreadPolicy(policy)
        var conn: Connection? = null
        val connString: String
        try {
            Class.forName("net.sourceforge.jtds.jdbc.Driver")
            connString =
                "jdbc:jtds:sqlserver://$ip:$puerto2;databaseName=$db;user=$userName;password=$password"
            conn = DriverManager.getConnection(connString)
        } catch (ex: SQLException) {
            Log.e("Error en dbConn SQlExce", ex.message!!)
        } catch (ex1: ClassNotFoundException) {
            Log.e("Error en dbConn classNF", ex1.message!!)
        } catch (ex2: Exception) {
            Log.e("Error 3 excepcion", ex2.message!!)
        }

        return conn
    }


    //Devuelve el total de los hallazgos sin verificar por medio de una consulta sql
    //Luego retorna el total de hallazgos que no se han verificado para el Badge
    suspend fun getUnseenItemsCount(supervisor: String): Int {
        return withContext(Dispatchers.IO) {
            var count = 0
            try {
                val query = "[dbo].[conteoNoVerificadoBadgeNotificacion] ?" //CHECK
                dbConn()?.prepareStatement(query)?.use { statement ->
                    statement.setString(1, supervisor)
                    val resultSet = statement.executeQuery()
                    if (resultSet.next()) {
                        count = resultSet.getInt(1)
                    }
                    resultSet.close()
                    statement.close()
                }
            } catch (ex: SQLException) {
                Log.e("Error getUnseenItems", ex.message!!)
            } catch (ex1: ClassNotFoundException) {
                Log.e("Error en getUnseenItems", ex1.message!!)
            } catch (ex2: Exception) {
                Log.e("Error 3 excepcion", ex2.message!!)
            } finally {
                dbConn()?.close()
            }

            count
        }

    }

    //Metodo logueo para entrar al main, homefragment o pantalla de inicio
    fun login(usuario: String, password: String): UsuarioData? {
        return try {
            val connection = dbConn() ?: throw SQLException("Error conexion bd")
            val query = "PostVentaLogin ?,?" //CHEKC WEBPAY BD
            //val query = "[dbo].[PostVentaValidacionLogin] ?,?" //local
            val statement = connection.prepareStatement(query)
            statement.setString(1, usuario)
            statement.setString(2, password)
            val resu = statement.executeQuery()

            if (resu.next()) {
                //Webpay BD
                UsuarioData(
                    resu.getInt(1), //idusuario
                    resu.getString("Nombre"),//Nombre
                    resu.getString("Apellido"),//Apellido
                    resu.getString("correo"),//correo
                    resu.getString("Cargo"),//cargo
                    resu.getString("estado"),//estado
                    resu.getString("Rut") //Rut
                )
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        } finally {
            dbConn()?.close()
        }

    }

    fun closeConecction(){
        dbConn()?.close()
    }


}