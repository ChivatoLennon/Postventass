package com.example.postventaandroid.ui.Data

import java.io.Serializable
import java.sql.Timestamp

//clase datos de los tickets
data class GestionData(
    var iD_Gestion : Int,
    var fechaInicial : Timestamp,
    var controlRiesgo : String,
    var deadLine : Timestamp,
    var responsable : String,
    var detalle : String,
    var hallazgoID : Int,
    var deadline_derivacion : String,
    var EstadoGestion : String
): Serializable