package com.example.postventaandroid.ui.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Patterns
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.example.postventaandroid.R
import com.example.postventaandroid.ui.Data.ConnSQL
import com.example.postventaandroid.ui.documentos.PdfModal
import com.google.android.material.textfield.TextInputLayout
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.SQLException


class Registro_pantalla : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar

    private lateinit var txtRut: TextInputLayout
    private lateinit var spinnerRut_digito: Spinner
    private lateinit var txtNombre: EditText
    private lateinit var txtApellido: EditText
    private lateinit var txtCorreo: EditText
    private lateinit var txtContrasena: EditText
    private lateinit var txtConfirmarContrasena: EditText
    private lateinit var txtGuardandoPDF_firmado : TextView

    private lateinit var checkboxTerminos: CheckBox
    private lateinit var checkBoxPoliticas: CheckBox

    private lateinit var btnRegistrarse: Button

    private lateinit var txtLeerTerminos: TextView
    private lateinit var txtVerPoliticasEmpresa: TextView

    private val conexion = ConnSQL()

    private var opcionSpinnerDigito = ""

    lateinit var signatureActivityResultLauncher: ActivityResultLauncher<Intent>

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_registro_pantalla)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        //Oculta el teclado al hacer clic fuera de este
        val main = findViewById<View>(R.id.main)
        main.setOnTouchListener { _, _ ->
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(main.windowToken, 0)
            false
        }

        checkboxTerminos = findViewById(R.id.checkBoxTerminos_RegistroPantalla)
        checkBoxPoliticas = findViewById(R.id.checkBoxPoliticasEmpresa_RegistroPantalla)

        btnRegistrarse = findViewById(R.id.btnRegistrarse_RegistroPantalla)

        txtGuardandoPDF_firmado = findViewById(R.id.txtGuardandoPDF_firmado_MiPerfilFragment)
        txtLeerTerminos = findViewById(R.id.txtLeerTerminos_RegistroPantalla)
        txtVerPoliticasEmpresa = findViewById(R.id.txtVerPoliticasEmpresa_RegistroPantalla)
        txtNombre = findViewById(R.id.txt_nombre_acitivityRegistro)
        txtApellido = findViewById(R.id.txt_apellido_acitivityRegistro)
        txtCorreo = findViewById(R.id.txt_correo_acitivityRegistro)
        txtContrasena = findViewById(R.id.txt_contrasena_acitivityRegistro)
        txtConfirmarContrasena = findViewById(R.id.txt_confirmacionContrasena_acitivityRegistro)
        txtRut = findViewById(R.id.txt_Rut_acitivityRegistro)
        spinnerRut_digito = findViewById(R.id.txt_RutDigito_acitivityRegistro)


        txtGuardandoPDF_firmado.visibility = View.GONE

        //Array de digito verificador para spinner
        val arrayDigito = arrayListOf(
            "k", "0", "1", "2", "3", "4", "5", "6", "7", "8", "9"
        )

        //Adapter para spinner
        val adapterDigito = ArrayAdapter(this, android.R.layout.simple_spinner_item, arrayDigito)
        adapterDigito.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRut_digito.adapter = adapterDigito
        var indiceSeleccionado: Int
        spinnerRut_digito.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {

                indiceSeleccionado = position
                val itemSeleccionada = arrayDigito[indiceSeleccionado]
                spinnerDigitoOptions(
                    itemSeleccionada,
                    indiceSeleccionado
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Manejo cuando no se selecciona nada
            }
        }

        progressBar = findViewById(R.id.progressBar_registroActivity)
        progressBar.visibility = View.GONE

        val nombreRegistrar = txtNombre.text.toString()
        checkboxTerminos.isEnabled = false
        checkBoxPoliticas.isEnabled = false

        //Variable que envia el numero de identificacion, si se selecciono Terminos y Condiciones o Política Empresa para empezar a firmar
        var terminos: Int

        txtLeerTerminos.setOnClickListener() {
            val directorio =
                "Terminos y Condiciones Ejemplo - appCodelco.pdf"
            terminos = 0
            val pdfModal = PdfModal(this, directorio, nombreRegistrar, terminos)
            pdfModal.show()
            pdfModal.ocultarBtnDescargaPDF()
        }

        txtVerPoliticasEmpresa.setOnClickListener() {
            val directorio =
                "Política Empresa ejemplo PDF - appCodelco.pdf"
            terminos = 1
            val pdfModal = PdfModal(this, directorio, nombreRegistrar, terminos)

            pdfModal.show()
            pdfModal.ocultarBtnDescargaPDF()
        }


        btnRegistrarse.setOnClickListener() {
            registrarNuevoUsuario()
        }

        confirmacionSignatureActivity()


    }

    // Registro del ActivityResultLauncher, aca recibimos la confirmacion de SignatureActivity
    private fun confirmacionSignatureActivity(){
        signatureActivityResultLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    result.data?.let {
                        val resultValue = it.getIntExtra("firmaPDF", 2)

                        // Manejar el dato recibido
                        when (resultValue) {
                            0 -> {
                                //Terminos y condiciones firmado, CHECKBOX setteado
                                checkboxTerminos.isChecked = true
                                txtGuardandoPDF_firmado.visibility = View.VISIBLE
                                lifecycleScope.launch {
                                    progressBar.visibility = View.VISIBLE
                                    delay(5000)
                                    txtGuardandoPDF_firmado.visibility = View.GONE
                                    progressBar.visibility = View.GONE
                                    mensaje("Listo. Guardado en su carpeta Descargas")
                                }
                            }

                            1 -> {
                                //Politicas de empresa firmado, CHECKBOX setteado
                                checkBoxPoliticas.isChecked = true
                                txtGuardandoPDF_firmado.visibility = View.VISIBLE
                                lifecycleScope.launch {
                                    progressBar.visibility = View.VISIBLE
                                    delay(5000)
                                    txtGuardandoPDF_firmado.visibility = View.GONE
                                    progressBar.visibility = View.GONE
                                    mensaje("Listo. Guardado en su carpeta Descargas")
                                }
                            }

                            else -> {
                                //nothing
                                checkboxTerminos.isChecked = false
                                checkBoxPoliticas.isChecked = false
                            }

                        }
                    }
                }
            }
    }

    private fun registrarNuevoUsuario(){
        val email = txtCorreo.text.toString()
        val rutSinDigitoVerificador = txtRut.editText!!.text.length
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            if (rutSinDigitoVerificador >= 8){
                txtRut.error = null
                if (!consultarRutSQL()) {
                    if (validarEmail(email)) {
                        if (!consultarCorreoSQL()) {
                            if ((validarContrasena())) {
                                if (!validarDatos()){
                                    if (checkboxTerminos.isChecked && checkBoxPoliticas.isChecked) {
                                        if (registrarUsuarioSQL()) {
                                            progressBar.visibility = View.GONE
                                            mensaje("Registro exitoso")
                                            finish()
                                        } else {
                                            progressBar.visibility = View.GONE
                                            mensaje("Error al registrar usuario. Intente de nuevo")
                                        }
                                    } else {
                                        progressBar.visibility = View.GONE
                                        mensaje("Debe aceptar Politicas de empresa, terminos y condiciones.")
                                    }
                                }else{
                                    progressBar.visibility = View.GONE
                                    mensaje("Faltan datos por ingresar")
                                }
                            } else {
                                progressBar.visibility = View.GONE
                                //mensaje("Confirme contraseñas iguales")
                            }
                        } else {
                            progressBar.visibility = View.GONE
                            mensaje("Correo ya existe")
                        }
                    } else {
                        progressBar.visibility = View.GONE
                        mensaje("Formato de Email invalido")
                    }
                } else {
                    progressBar.visibility = View.GONE
                    mensaje("Ya existe RUT o faltan datos")
                }
            }else{
                progressBar.visibility = View.GONE
                txtRut.error = "Campo Rut incompleto"
                mensaje("Campo Rut incompleto")
            }
        }
    }

    private fun spinnerDigitoOptions(itemSeleccionada: String, indiceSeleccionado: Int) {
        // Imprimes el índice y el contenido
        println("Índice seleccionado: $indiceSeleccionado, itemSeleccionado: $itemSeleccionada")
        when (indiceSeleccionado) {
            0 -> {
                opcionSpinnerDigito = itemSeleccionada

            }

            1 -> {
                opcionSpinnerDigito = itemSeleccionada

            }

            2 -> {
                opcionSpinnerDigito = itemSeleccionada

            }

            3 -> {
                opcionSpinnerDigito = itemSeleccionada

            }

            4 -> {
                opcionSpinnerDigito = itemSeleccionada

            }

            5 -> {
                opcionSpinnerDigito = itemSeleccionada

            }

            6 -> {
                opcionSpinnerDigito = itemSeleccionada

            }

            7 -> {
                opcionSpinnerDigito = itemSeleccionada

            }

            8 -> {
                opcionSpinnerDigito = itemSeleccionada

            }

            9 -> {
                opcionSpinnerDigito = itemSeleccionada

            }

            10 -> {
                opcionSpinnerDigito = itemSeleccionada

            }

        }
    }

    private fun validarEmail(email: String): Boolean {
        val patron = Patterns.EMAIL_ADDRESS
        return patron.matcher(email).matches()
    }

    suspend private fun registrarUsuarioSQL(): Boolean {
        var bool = false
        val rutSinDigitoVerificador = txtRut.editText!!.text.length
        val rutUnido = "${txtRut.editText!!.text}-$opcionSpinnerDigito"
        return withContext(Dispatchers.IO) {

            if (rutSinDigitoVerificador == 8) {
                try {
                    val query = "[dbo].[_1_RegistrarUsuario] ?, ?, ?, ?, ?"
                    val statement = conexion.dbConn()!!.prepareStatement(query)
                    statement.setString(1, txtCorreo.text.toString().lowercase()) //Correo
                    statement.setString(2, txtContrasena.text.toString()) //Clave
                    statement.setString(3, txtApellido.text.toString()) //Apellido
                    statement.setString(4, txtNombre.text.toString()) //Nombre
                    statement.setString(5, rutUnido)                    //Rut unido
                    val resu = statement.executeUpdate()
                    if (resu == 1) {
                        bool = true
                    } else bool = false

                } catch (e: Exception) {
                    e.printStackTrace()
                } catch (x: SQLException) {
                    x.printStackTrace()
                }
            } else {
            }

            return@withContext bool
        }
    }


    private fun validarDatos(): Boolean {
        val bool: Boolean
        if (txtNombre.text.isEmpty() || txtApellido.text.isEmpty() || txtCorreo.text.isEmpty() || txtRut.editText!!.text.isEmpty()) {
            bool = true
        } else bool = false
        return bool
    }

    private fun validarContrasena(): Boolean {
        val bool: Boolean
        val contrase = txtContrasena.text.toString()
        val confContrase = txtConfirmarContrasena.text.toString()

        if (contrase.isNotEmpty() || confContrase.isNotEmpty()) {
            if (contrase == confContrase) bool = true else {
                bool = false
                mensaje("Las contraseñas no son iguales")
            }
        } else {
            mensaje("Campo contraseñas vacios")
            bool = false
        }
        return bool
    }


    private fun mensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }


    suspend private fun consultarRutSQL(): Boolean {
        var num = 0
        var conf = false
        val rutSinDigitoVerificador = txtRut.editText!!.text.length
        val rutUnido = "${txtRut.editText!!.text}-$opcionSpinnerDigito"
        val arrayRut = arrayListOf<String>()
        return withContext(Dispatchers.IO) {

            if (rutSinDigitoVerificador == 8) {
                try {
                    val query = "select Rut from dbo.Usuario"
                    val statement = conexion.dbConn()!!.prepareStatement(query)
                    val resu = statement.executeQuery()

                    while (resu.next()) {
                        arrayRut.add(resu.getString(1))
                    }
                    for (i in arrayRut.indices) {
                        if (rutUnido == arrayRut[i]) {
                            num++
                        }
                    }

                    if (num > 0) {
                        conf = true
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                } catch (x: SQLException) {
                    x.printStackTrace()
                }
            } else {
                conf = false
            }
            return@withContext conf
        }
    }

    suspend private fun consultarCorreoSQL(): Boolean {
        var num = 0
        var conf = false
        val correo = txtCorreo.text.toString().lowercase()
        val arrayCorreo = arrayListOf<String>()
        return withContext(Dispatchers.IO) {

            try {

                val query = "select correo from dbo.Usuario"
                val statement = conexion.dbConn()!!.prepareStatement(query)
                val resu = statement.executeQuery()

                while (resu.next()) {
                    arrayCorreo.add(resu.getString(1))
                }

                for (i in arrayCorreo.indices) {
                    if (correo == arrayCorreo[i]) {
                        num++
                    }
                }

                if (num > 0) {
                    conf = true
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } catch (x: SQLException) {
                x.printStackTrace()
            }

            return@withContext conf
        }
    }


}