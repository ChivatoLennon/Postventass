package com.example.postventaandroid.ui.login

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
import java.util.*
import javax.mail.*
import javax.mail.internet.InternetAddress
import javax.mail.internet.MimeMessage


class Registro_pantalla : AppCompatActivity() {

    private lateinit var progressBar: ProgressBar

    private lateinit var txtRut: TextInputLayout
    private lateinit var spinnerRut_digito: Spinner
    private lateinit var txtNombre: EditText
    private lateinit var txtApellido: EditText
    private lateinit var txtCorreo: EditText
    private lateinit var txtContrasena: EditText
    private lateinit var txtConfirmarContrasena: EditText
    private lateinit var txtOtp: EditText
    private lateinit var txtGuardandoPDF_firmado : TextView

    private lateinit var checkboxTerminos: CheckBox
    private lateinit var checkBoxPoliticas: CheckBox

    private lateinit var btnRegistrarse: Button
    private lateinit var btnOtp: Button


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
        btnOtp = findViewById(R.id.btnEnviar)

        txtGuardandoPDF_firmado = findViewById(R.id.txtGuardandoPDF_firmado_MiPerfilFragment)
        txtLeerTerminos = findViewById(R.id.txtLeerTerminos_RegistroPantalla)
        txtVerPoliticasEmpresa = findViewById(R.id.txtVerPoliticasEmpresa_RegistroPantalla)
        txtNombre = findViewById(R.id.txt_nombre_acitivityRegistro)
        txtApellido = findViewById(R.id.txt_apellido_acitivityRegistro)
        txtCorreo = findViewById(R.id.txt_correo_acitivityRegistro)
        txtContrasena = findViewById(R.id.txt_contrasena_acitivityRegistro)
        txtConfirmarContrasena = findViewById(R.id.txt_confirmacionContrasena_acitivityRegistro)
        txtRut = findViewById(R.id.txt_Rut_acitivityRegistro)
        txtOtp = findViewById(R.id.txtOtp)
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
        btnOtp.setOnClickListener() {
            lifecycleScope.launch(Dispatchers.IO) {
                try {
                    val otp : String = generarOTP()
                    enviarOtpPorCorreo(txtOtp.text.toString(), otp)
                } catch (e: Exception) {
                    e.printStackTrace()
                    // Switch back to the main thread to show the Toast
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@Registro_pantalla, "Error al generar o enviar OTP", Toast.LENGTH_SHORT).show()
                    }
                }
            }
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
    private fun validarRut(rut: String, dv: String): Boolean {
        try {
            var rutTemp = rut.toInt()
            var m = 0
            var s = 1
            while (rutTemp != 0) {
                s = (s + rutTemp % 10 * (9 - m++ % 6)) % 11
                rutTemp /= 10
            }
            val checkDigit = if (s != 0) (s - 1).toString() else "k"
            return dv.equals(checkDigit, ignoreCase = true)
        } catch (e: NumberFormatException) {
            return false
        }
    }
    private fun registrarNuevoUsuario() {
        val email = txtCorreo.text.toString()
        val rut = txtRut.editText!!.text.toString()
        val otp = txtOtp.text.toString()

        progressBar.visibility = View.VISIBLE

        lifecycleScope.launch {
            if (!validarDatosUsuario(email, rut, otp)) {
                progressBar.visibility = View.GONE
                return@launch
            }

            if (registrarUsuarioSQL()) {
                progressBar.visibility = View.GONE
                mensaje("Usuario registrado")
            } else {
                progressBar.visibility = View.GONE
                mensaje("Error al registrar usuario")
            }
        }

    }
    private suspend fun validarDatosUsuario(email: String, rut: String, otp: String): Boolean {
        return when {
            rut.length <= 8 -> {
                mensaje("RUT Inválido")
                false
            }
            !consultarRutSQL() -> {
                mensaje("El RUT ya existe")
                false
            }
            !validarEmail(email) -> {
                mensaje("Correo no válido")
                false
            }
            !validarRut(rut, opcionSpinnerDigito) -> {
                mensaje("RUT Inválido")
                false
            }
            consultarCorreoSQL() -> {
                mensaje("Correo ya existe")
                false
            }
            !validarContrasena() -> {
                mensaje("Las contraseñas no coinciden")
                false
            }
            !validarDatos() -> {
                mensaje("Faltan datos por ingresar")
                false
            }
            !checkboxTerminos.isChecked || !checkBoxPoliticas.isChecked -> {
                mensaje("Debe aceptar Políticas de empresa, términos y condiciones.")
                false
            }
            otp.isEmpty() -> {
                mensaje("Debe ingresar OTP")
                false
            }
            otp.length != 6 -> {
                mensaje("OTP debe tener 6 dígitos")
                false
            }
            otp != obtenerOtpDelUsuario() -> {
                Log.d("OTP Incorrecto", "OTP Generado: $otp - OTP Text: ${obtenerOtpDelUsuario()}")
                mensaje("OTP incorrecto")
                false
            }
            else -> true
        }
    }

    // OTP Generation (Random 6-digit number)
    private fun generarOTP(): String {
        val random = Random()
        return String.format("%06d", random.nextInt(999999))
    }

    // Send OTP via email using JavaMail
    private fun enviarOtpPorCorreo(correo: String, otp: String) {
        Log.d("enviarOtpPorCorreo", "Iniciando envío de OTP")
        val login : String = "CORREO@CORREO.COM" // USAR CORREO CREADO
        val password : String = "clave" // USAR CLAVE CREADA
        val properties = Properties()
        properties["mail.smtp.host"] = "smtp.gmail.com"
        properties["mail.smtp.port"] = "587"
        properties["mail.smtp.auth"] = "true"
        properties["mail.smtp.starttls.enable"] = "true"

        val session = Session.getInstance(properties, object : Authenticator() {
            override fun getPasswordAuthentication(): PasswordAuthentication {
                return PasswordAuthentication(login, password)
            }
        })

        try {
            Log.d("enviarOtpPorCorreo", "Preparando mensaje de correo electrónico")
            val message = MimeMessage(session)
            message.setFrom(InternetAddress(login)) // Use a valid email address
            message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(correo))
            message.subject = "Tú Código de Verificación de App Prevención"
            message.setText("Tú Codigo de Verificación es: $otp")

            Log.d("enviarOtpPorCorreo", "Enviando mensaje de correo electrónico")
            Transport.send(message)
            Log.d("enviarOtpPorCorreo", "Mensaje de correo electrónico enviado exitosamente")
        } catch (e: MessagingException) {
            Log.e("enviarOtpPorCorreo", "Error al enviar mensaje de correo electrónico", e)
        }
    }

    // OTP Verification UI (mockup, implement in your app's UI)

    // Mockup function for user OTP input (replace with actual input method)
    private fun obtenerOtpDelUsuario(): String {
        return txtOtp.text.toString()// Simulated user input
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