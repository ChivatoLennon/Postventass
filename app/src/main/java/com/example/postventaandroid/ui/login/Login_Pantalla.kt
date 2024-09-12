package com.example.postventaandroid.ui.login

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentActivity
import com.example.postventaandroid.MainActivity
import com.example.postventaandroid.R
import com.example.postventaandroid.ui.Data.ConnSQL
import com.example.postventaandroid.ui.Data.SessionManager
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.SQLException

class Login_Pantalla : AppCompatActivity() {

    private lateinit var progressBar : ProgressBar

    //Para conexion SQL
    private var conexionSql = ConnSQL()

    private lateinit var sessionManager: SessionManager

    //Variables iniciales de XML activity_login_pantalla
    lateinit var boton_ingresar: Button
    private lateinit var txt_usuario: TextInputLayout
    private lateinit var txt_contrasena: TextInputLayout
    lateinit var boton_registro: Button
    lateinit var textOlvidoContrasena: TextView

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login_pantalla)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        //Desactivamos modo oscuro de los telefonos
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        //inicializa el sessionManager
        sessionManager = SessionManager(this)
        if (sessionManager.fetchUser()?.id == null){
            FirebaseMessaging.getInstance().isAutoInitEnabled = false
        }else{
            FirebaseMessaging.getInstance().isAutoInitEnabled = true
        }

        progressBar = findViewById(R.id.progressBar_loginActivity)
        progressBar.visibility = View.GONE

        //almacena los id en las variables
        txt_usuario = findViewById(R.id.editText_ingreseNombre)
        txt_contrasena = findViewById(R.id.editText_ingreseContrasena)
        boton_ingresar = findViewById(R.id.buttonIngresarLogin)
        boton_registro = findViewById(R.id.buttonRegistrar_usuario)
        textOlvidoContrasena = findViewById(R.id.textOlvidoContrasena_loginActivity)

        //Oculta el teclado al hacer clic fuera de este
        val main = findViewById<View>(R.id.main)
        main.setOnTouchListener { _, _ ->
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(main.windowToken, 0)
            false
        }

        //Validar e iniciar sesion
        boton_ingresar.setOnClickListener {
            val usuario = txt_usuario.editText!!.text.toString()
            val password = txt_contrasena.editText!!.text.toString()
            if (usuario.isEmpty() or password.isEmpty()) {
                mensaje("Uno o dos campos vacios")
            } else {
                try {
                    progressBar.visibility = View.VISIBLE
                    CoroutineScope(Dispatchers.IO).launch {
                        val token = conexionSql.login(usuario, password) //metodo desde la clase ConnSql
                        withContext(Dispatchers.Main) {
                            if(token != null){
                                sessionManager.saveUser(token)
                                mensaje("Bienvenid@")
                                GoHome()
                            }else{
                                progressBar.visibility = View.GONE
                                mensaje("Credenciales incorrectas")
                            }
                        }
                    }

                } catch (x: SQLException) {
                    Log.e("Error en ValidarLogin", x.message!!)
                    mensaje("Revisar sqlLogin exception sql")
                }
            }

        }

        textOlvidoContrasena.setOnClickListener {
            AbriDialogOlvidoContrasena()
        }

        boton_registro.setOnClickListener() {
            GoRegistration()
        }
    }

    //Funcion que al iniciar el activityLogin verificca que el token tenga datos
    //Para mantener la sesion
    //De lo contrario tiene que loguearse
    override fun onStart() {
        super.onStart()
        val authToken = sessionManager.fetchUser()
        if (authToken != null){
            // El usuario ya está autenticado, redirige a la siguiente actividad
            Toast.makeText(this, "Bienvenid@ de nuevo", Toast.LENGTH_SHORT).show()
            // Redirigir a la actividad principal de la aplicación
            GoHome()
        }
    }

    //Funcion para entrar a pantalla principal
    fun GoHome() {
        //Nos vamos al menu principal
        startActivity(Intent(this, MainActivity::class.java))
        finish()
    }

    //Funcion para entrar al registro de usuario
    fun GoRegistration() {
        startActivity(Intent(this, Registro_pantalla::class.java))
    }

    fun mensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

    override fun onBackPressed() {
        super.onBackPressed()
    }

    private fun AbriDialogOlvidoContrasena(){
        val fragmentManager = (this as FragmentActivity).supportFragmentManager
        val dialogFragment = olvidoContrasenaDialog()
        dialogFragment.show(fragmentManager, "olvidoContrasena_dialog")
    }

}