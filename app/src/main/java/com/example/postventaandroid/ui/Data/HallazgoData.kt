package com.example.postventaandroid.ui.Data

import java.io.Serializable
import java.sql.Timestamp

//Clase de datos de las alertas
data class HallazgoData(
    var ID_Hallazgo: Int,
    var Sector: String,
    var Supervisor: String,
    var NivelAlerta: String,
    var Descripcion: String,
    var bitmapFoto: ByteArray,
    var verificacion: Boolean,
    var Fecha: Timestamp,
    var EstadoCierre: String,
    var Proyecto_Mina: String,
    var Area: String,
    var Actividad: String,
    var Riesgo_RC: String,
    var Reportado_Por: String,
    var userId: Int
) : Serializable