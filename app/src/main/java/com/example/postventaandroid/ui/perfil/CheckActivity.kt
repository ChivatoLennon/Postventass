package com.example.postventaandroid.ui.perfil

import android.annotation.SuppressLint
import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.postventaandroid.R
import com.example.postventaandroid.ui.Data.ConnSQL
import com.example.postventaandroid.ui.Data.SessionManager
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.SQLException

//Clase de la pantalla que crea, cierra, o deriva un ticket
//Esta clase va por dos caminos:
//Camino 1 - crea, cierra, o deriva un ticket
//Camino 2 - Edita un ticket ya creado especifico de la pantalla historial de tickets
class CheckActivity : AppCompatActivity() {

    private val conexion = ConnSQL()
    private lateinit var sessionManager: SessionManager

    private lateinit var btnGuardarCambiosTicket : Button
    private lateinit var btnCrearTicket: Button
    private lateinit var btnDerivar : Button
    private lateinit var btnCerrar : Button
    private lateinit var editTextDetalles: TextInputLayout
    private lateinit var spinnerControlRiesgo: Spinner
    private lateinit var spinnerDeadLine: Spinner
    private lateinit var spinnerResponsable: Spinner
    private lateinit var spinnerDerivarSupervisor : Spinner
    private lateinit var txtID_traido: TextView
    private lateinit var txtLabelID : TextView
    private lateinit var txtLabelDetalle : TextView
    private lateinit var txtDetallesVER : TextView

    private lateinit var progressBar: ProgressBar

    private lateinit var linearLayoutCerrarDerivar : LinearLayout

    //Para validar entradas
    private var confControlRiesgo = 0
    private var confDeadLine = 0
    private var confResponsable = 0
    private var estadoGestion = ""

    //Items que los spinners
    private val itemsSpinners = listOf(
        //ControlRiesgo
        listOf(
            "Seleccionar Control Riesgo",
            "Eliminar",
            "Sustituir",
            "Rediseñar",
            "Administrar",
            "Usar EPP"
        ),
        //DEADLINE
        listOf(
            "Seleccionar Deadline",
            "24 hrs",
            "48 hrs",
            "72 hrs",
            "No Aplica",
            //"Derivado"
        ),
        //RESPONSABLE (nombres supervisor temporales)
        listOf(
            "Seleccionar Responsable",
            "Mauricio",
            "Hector",
            "Miguel"
        )
    )

    //para cuando la activity entre en editar gestion (camino 2)
    private val itemsEdicion = listOf(
        //estado Gestion
        listOf(
            "Seleccione estado",
            "Abierto",
            "Pendiente",
            "Resuelto",
            "Cerrado"
        )
    )

    //Para enviar a la BD y validar hrs, o no aplica y derivado
    var optionDerivationDeadLine = -1

    //Opciones elegidas de spinners
    private var opcionElegidaControlRiesgo = ""
    private var opcionElegidaDeadline = ""
    private var opcionElegidaResponsables = ""

    @SuppressLint("ClickableViewAccessibility", "NotifyDataSetChanged")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_check)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        sessionManager = SessionManager(this)
        val nombreSupervisor = sessionManager.fetchUser()!!.nombre

        //Oculta el teclado al hacer clic fuera de este
        val main = findViewById<View>(R.id.main)
        main.setOnTouchListener { _, _ ->
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(main.windowToken, 0)
            false
        }

        //Retorna id de alerta entregado de PerfilAdapter o id de de ticket de GestionAdapter
        val id_Ticket_devuelto = intent.getIntExtra("id", 0)
        val detalle_devuelto = intent.getStringExtra("detalleTicket").toString()
        val verificacionDevuelta = intent.getBooleanExtra("verificacion", false)
        val id_Alerta_devuelto = intent.getIntExtra("IdAlerta", 0)

        progressBar = findViewById(R.id.progressBar_CheckActivity)
        progressBar.visibility = View.GONE

        btnGuardarCambiosTicket = findViewById(R.id.btnGuardarCambiosTicket_CheckActivity)
        btnCrearTicket = findViewById(R.id.btnCrearTicket_CheckActivity)
        btnDerivar = findViewById(R.id.btnDerivar_CheckActivity)
        btnCerrar = findViewById(R.id.btnCerrar_CheckActivity)

        linearLayoutCerrarDerivar = findViewById(R.id.linearLayout_CerrarDerivar_CheckActivity)

        editTextDetalles = findViewById(R.id.editText_Detalle_CheckActivity)

        txtDetallesVER = findViewById(R.id.txtDetallesVER_CheckActivity)
        txtLabelDetalle = findViewById(R.id.txtDETALLE_label_checkActivity)
        txtLabelID = findViewById(R.id.txtID_label_CheckActivity)
        txtID_traido = findViewById(R.id.txtID_CheckActivity)
        txtID_traido.text = id_Ticket_devuelto.toString()

        spinnerResponsable = findViewById(R.id.spinner_Responsable_CheckACtivity)
        spinnerDeadLine = findViewById(R.id.spinner_Deadline_CheckACtivity)
        spinnerControlRiesgo = findViewById(R.id.spinner_ControlRiesgo_CheckACtivity)
        spinnerDerivarSupervisor = findViewById(R.id.spinnerDerivarSupervisor_CheckActivity)

        btnGuardarCambiosTicket.visibility = View.GONE
        btnCrearTicket.visibility = View.GONE
        linearLayoutCerrarDerivar.visibility = View.GONE

        //Camino 1(itemsSpinners[1]) si es para crear. Camino 2(itemsEdicion[0]) si es para editar ticket.
        //Almacena la lista en dicha variable
        val controlRiesgoEdicion = if (verificacionDevuelta) itemsSpinners[1] else itemsEdicion[0]

        //CAMINOS DISTINTOS DE LA ACTIVITY
        if (verificacionDevuelta){ // SI es True: si es para Verificar y crear un nuevo Ticket //Camino 1
            val textoID = "ID de Hallazgo seleccionado"
            btnCrearTicket.visibility = View.VISIBLE
            linearLayoutCerrarDerivar.visibility = View.VISIBLE
            txtDetallesVER.visibility = View.GONE
            txtLabelID.text = textoID
        }else{// SI es False: si es para Editar gestion seleccionada //Camino 2

            val textoID = "ID de Ticket a actualizar"
            val texto = detalle_devuelto
            val detalleActual = "Detalle del Ticket Actual"

            txtDetallesVER.visibility = View.VISIBLE
            btnGuardarCambiosTicket.visibility = View.VISIBLE
            spinnerResponsable.visibility = View.GONE
            editTextDetalles.visibility = View.VISIBLE
            editTextDetalles.hint = "Actualice los detalles si lo necesita"
            editTextDetalles.editText!!.setText(texto)
            txtLabelDetalle.text = detalleActual
            txtLabelID.text = textoID
        }
        txtDetallesVER.text = detalle_devuelto

        //Adaptadores Spinner
        val spinnerAdapterControlRiesgo =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                itemsSpinners[0]
            )
        spinnerAdapterControlRiesgo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerControlRiesgo.adapter = spinnerAdapterControlRiesgo

        //ESTE SPINNER TIENE 2 CAMBIOS: CUANDO ES PARA CREAR NUEVA GESTION TIENE DEADLINE
        //CUANDO ES PARA EDITAR GESTION TIENE SELECCION DE ESTADO
        val spinnerAdapterDeadline =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                controlRiesgoEdicion
            )
        spinnerAdapterControlRiesgo.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerDeadLine.adapter = spinnerAdapterDeadline

        val spinnerAdapterResponsable =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                itemsSpinners[2]
            )
        spinnerAdapterResponsable.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerResponsable.adapter = spinnerAdapterResponsable
        spinnerDerivarSupervisor.adapter = spinnerAdapterResponsable

        //Inicios Spinners
        spinnerControlRiesgo.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                spinnerOptionsControlRiesgo(itemsSpinners[0], position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        spinnerDeadLine.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                spinnerOptionsDeadline(controlRiesgoEdicion, position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        spinnerResponsable.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                spinnerOptionsResponsable(itemsSpinners[2], position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        spinnerDerivarSupervisor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                spinnerOptionsResponsable(itemsSpinners[2], position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }
        //------------

        //Genera un ticket nuevo para trabajar con la alerta seleccionada
        btnCrearTicket.setOnClickListener() {
            progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                if (validarDatosEntrada()) {
                    mensaje("Faltan datos por ingresar")
                    progressBar.visibility = View.GONE
                } else {
                    estadoGestion = "Abierto"
                    if (subirDatosTicket(id_Ticket_devuelto, estadoGestion, nombreSupervisor)) {
                        mensaje("Datos enviados")
                        progressBar.visibility = View.GONE
                        finish()
                    } else mensaje("Error al enviar datos. Reintente")
                }
                progressBar.visibility = View.GONE
            }
        }

        //Genera un ticket con estado cerrado sobre la alerta seleccionada
        btnCerrar.setOnClickListener(){
            progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                if (validarDatosEntrada()) {
                    mensaje("Faltan datos por ingresar")
                    progressBar.visibility = View.GONE
                } else {
                    estadoGestion = "Cerrado"
                    if (subirDatosTicket(id_Ticket_devuelto, estadoGestion, nombreSupervisor)) {
                        mensaje("Datos enviados")
                        progressBar.visibility = View.GONE
                        finish()
                    } else mensaje("Error al enviar datos. Reintente")
                }
                progressBar.visibility = View.GONE
            }
        }

        //Actualiza un ticket ya creado
        btnGuardarCambiosTicket.setOnClickListener(){
            progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                if (validarEdicion()) {
                    mensaje("Faltan datos por ingresar")
                    progressBar.visibility = View.GONE
                } else {
                    val de = editTextDetalles.editText!!.text.toString() //Detalles del ticket
                    if (actualizarDatosTicket(id_Ticket_devuelto, de, id_Alerta_devuelto)){
                        mensaje("Datos actualizados")
                        progressBar.visibility = View.GONE
                        finish()
                    }else{mensaje("Error al actualizar datos")}
                }
                progressBar.visibility = View.GONE
            }
        }


        //Deriva alerta a otro Supervisor
        btnDerivar.setOnClickListener(){
            progressBar.visibility = View.VISIBLE
            lifecycleScope.launch {
                if (confResponsable != 0){
                    if (derivarAlerta(id_Ticket_devuelto)) {
                        mensaje("Alerta derivada con exito a $opcionElegidaResponsables")
                        progressBar.visibility = View.GONE
                        finish()
                    }else mensaje("Error al derivar alerta. Intente de nuevo")
                }else{
                    progressBar.visibility = View.GONE
                    mensaje("Campo vacio")
                }
            }
            progressBar.visibility = View.GONE
        }


    }

    private fun spinnerOptionsControlRiesgo(
        ingresoItem: List<String>,
        indiceSeleccionado: Int
    ) {
        when (indiceSeleccionado) {
            0 -> {

                opcionElegidaControlRiesgo = ingresoItem[0] //Seleccionar Control Riesgo
                confControlRiesgo = 0
            }

            1 -> {
                opcionElegidaControlRiesgo = ingresoItem[1] //Seleccionar "Eliminar",
                confControlRiesgo = 1

            }

            2 -> {
                opcionElegidaControlRiesgo = ingresoItem[2] //Seleccionar "Sustituir",
                confControlRiesgo = 1

            }

            3 -> {
                opcionElegidaControlRiesgo = ingresoItem[3] //Seleccionar "Rediseñar",
                confControlRiesgo = 1

            }

            4 -> {
                opcionElegidaControlRiesgo = ingresoItem[4] //Seleccionar "Administrar",
                confControlRiesgo = 1

            }

            5 -> {
                opcionElegidaControlRiesgo = ingresoItem[5] //Seleccionar Usar EPP
                confControlRiesgo = 1

            }
        }
    }

    fun spinnerOptionsDeadline(
        ingresoItem: List<String>,
        indiceSeleccionado: Int
    ) {
        when (indiceSeleccionado) {
            0 -> {

                opcionElegidaDeadline = ingresoItem[0] //Seleccionar "Seleccionar Deadline"
                confDeadLine = 0
            }

            1 -> {
                opcionElegidaDeadline = ingresoItem[1] //Seleccionar 24 hrs
                confDeadLine = 1
                optionDerivationDeadLine = 24

            }

            2 -> {
                opcionElegidaDeadline = ingresoItem[2] //Seleccionar 48 hrs
                confDeadLine = 1
                optionDerivationDeadLine = 48
            }

            3 -> {
                opcionElegidaDeadline = ingresoItem[3] //Seleccionar 72 hrs
                confDeadLine = 1
                optionDerivationDeadLine = 72
            }

            4 -> {
                opcionElegidaDeadline = ingresoItem[4] //Seleccionar No Aplica
                confDeadLine = 1
                optionDerivationDeadLine = 0 //Si es 0 va a no aplica en el sql
            }

            /*5 -> {
                opcionElegidaDeadline = ingresoItem[5] //Seleccionar "Derivado"
                confDeadLine = 1
                optionDerivationDeadLine = 1 // Si es 1, va a derivacion en el sql
            }*/
        }
    }

    fun spinnerOptionsResponsable(
        ingresoItem: List<String>,
        indiceSeleccionado: Int
    ) {
        when (indiceSeleccionado) {
            0 -> {

                opcionElegidaResponsables = ingresoItem[0] //Seleccionar "Seleccionar Responsable"
                confResponsable = 0
            }

            1 -> {
                opcionElegidaResponsables = ingresoItem[1] //Seleccionar "Mauricio",
                confResponsable = 1

            }

            2 -> {
                opcionElegidaResponsables = ingresoItem[2] //Seleccionar "Hector",
                confResponsable = 1

            }

            3 -> {
                opcionElegidaResponsables = ingresoItem[3] //Seleccionar "Miguel"
                confResponsable = 1

            }

        }
    }

    suspend fun actualizarDatosTicket(id_ticket : Int, detalle : String, id_alerta: Int) : Boolean{
        var conf = false
        return withContext(Dispatchers.IO){
            try {

                val query = "[dbo].[_1_ActualizarGestion] ?, ?, ?, ?, ?"
                val statement = conexion.dbConn()!!.prepareStatement(query)
                statement.setString(1, opcionElegidaControlRiesgo)
                statement.setString(2, opcionElegidaDeadline)  //Si es para actuallizar Ticket ingresa estados, si es nuevo ticket ingresa Deadline
                statement.setInt(3, id_ticket)
                statement.setString(4, detalle)
                statement.setInt(5, id_alerta)
                val resu = statement.executeUpdate()
                if (resu == 1) conf = true else conf = false

            }catch (e: Exception){
                e.printStackTrace()
            }catch (x: SQLException){
                x.printStackTrace()
            }

            return@withContext conf
        }
    }

    suspend private fun subirDatosTicket(id: Int, estadoGestion : String, nombreSupervisor : String): Boolean {
        var num = false
        return withContext(Dispatchers.IO) {
            try {
                val query = "[dbo].[_1_GestionDelHallazgo] ?,?,?,?,?,?"
                val statement = conexion.dbConn()!!.prepareStatement(query)
                statement.setInt(1, optionDerivationDeadLine)               //horaElegida
                statement.setString(2, opcionElegidaControlRiesgo)          //ControlRiesgo
                statement.setString(3, nombreSupervisor)           //Responsable
                statement.setString(4, editTextDetalles.editText!!.text.toString())    //Detalle
                statement.setInt(5, id)
                statement.setString(6, estadoGestion)//ID del Hallazgo
                val resu = statement.executeUpdate()
                if (resu == 1) {
                    num = true
                } else {
                    num = false
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } catch (x: SQLException) {
                x.printStackTrace()
            }
            return@withContext num
        }
    }

    fun mensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    private fun validarDatosEntrada(): Boolean {
        //Si es true(variables sin datos) faltan datos por ingresar
        //false(variables con datos), hay datos en los campos requeridos
        /*|| confResponsable == 0*/
        if (confControlRiesgo == 0 || confDeadLine == 0  || editTextDetalles.editText?.text!!.isEmpty()) return true else return false
    }

    private fun validarEdicion() : Boolean{
        if(confControlRiesgo == 0 || confDeadLine == 0 || editTextDetalles.editText!!.text.toString().isEmpty()) return true else return false
    }

    suspend private fun derivarAlerta(idDevuelto : Int) : Boolean{
        var bool = false
        return withContext(Dispatchers.IO){
            val query = "[dbo].[_1_derivarAlerta] ?, ?"
            try {
                val statement = conexion.dbConn()!!.prepareStatement(query)
                statement.setString(1,opcionElegidaResponsables) //Supervisor
                statement.setInt(2,idDevuelto)    //ID de Hallazgo
                val resu = statement.executeUpdate()

                if (resu == 1){
                    bool = true
                }else bool = false

            }catch (e: Exception){
                e.printStackTrace()
            }catch (x: SQLException){
                x.printStackTrace()
            }
            return@withContext bool
        }
    }


    //FINAL
}
