package com.example.postventaandroid

import android.annotation.SuppressLint
import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentTransaction
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.example.postventaandroid.databinding.ActivityMainBinding
import com.example.postventaandroid.ui.Data.ConnSQL
import com.example.postventaandroid.ui.Data.Notificacion
import com.example.postventaandroid.ui.Data.SessionManager
import com.example.postventaandroid.ui.login.Login_Pantalla
import com.example.postventaandroid.ui.perfil.MiPerfilFragment
import com.google.firebase.messaging.FirebaseMessaging
import org.json.JSONObject
import java.sql.SQLException

interface NavigationHost {
    fun navigateTo(detinationId: Int)
}

class MainActivity : AppCompatActivity(), NavigationHost {

    //Instancia ConnSQL bd
    private var conexion = ConnSQL()

    companion object {
        const val CHANNEL_ID = "my_channel_id_01" // ID del canal de notificación
        const val CHANNEL_NAME = "My Notification Channel" // Nombre del canal de notificación
        const val CHANNEL_DESCRIPTION = "Channel for my app notifications" // Descripción del canal de notificación
        const val TAG = "MainActivity" //Tag para ver en el debugger
    }

    private lateinit var sessionManager: SessionManager

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding
    private lateinit var btn_cerrarSesion: Button

    private lateinit var firebaseMessaging: FirebaseMessaging


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        //inicializa el sessionManager
        sessionManager = SessionManager(this)
        val userId = sessionManager.fetchUser()!!.id


        //Pregunta por permisos a API 33 o mayor
        askNotificationPermission()
        crearCanalNotificacion()
        FirebaseMessaging.getInstance().isAutoInitEnabled = true

        //Inicializa FirebaseMessaging
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token = task.result
                Log.d(TAG, "Token: $token")
                Log.d("TOKEEENN", "Revision de token $token")
                sendTokenToServer(userId, token.toString())

            }else{
                Log.w(TAG, "Fallo al obtener el Token", task.exception)
            }

        }

        //Para volver a habilitar el inicio automático de FCM, realice una llamada en tiempo de ejecución
        //Firebase.messaging.isAutoInitEnabled = true

        //Solo se reciben notificaciones si esta suscrito en l firebase console por el tema elegido
        //FirebaseMessaging.getInstance().subscribeToTopic("tutorial")
        //Para recibir notificaciones push personalizadas y unicas para cada usuario
        val url = intent.getStringExtra("url")
        url?.let {
            println("ha llegado informacion en push: ${it}")
        }
        //-------

        btn_cerrarSesion = findViewById(R.id.btn_cerrarSesion_homeFragment)

        //Desactiva el modo oscuro de la aplicacion
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        setSupportActionBar(binding.appBarMain.toolbar)
        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.nav_home,
                R.id.nav_gallery,
                R.id.nav_slideshow,
                R.id.nav_panelDeControl,
                R.id.nav_configuracion,
                R.id.nav_soporteTecnico,
                R.id.nav_documentos,
                R.id.nav_miPerfil
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)

        //Oculta el teclado al hacer clic fuera de este
        val main = findViewById<View>(R.id.drawer_layout)
        main.setOnTouchListener { _, _ ->
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(main.windowToken, 0)
            false
        }


        //Cierra sesion
        btn_cerrarSesion.setOnClickListener() {
            cerrarSesion()
        }

        //Esto es para que los imgButton puedan navegar como navigationDrawer, asi funcionan los imgbutton
        //Y el navigationDrawer
        navView.setNavigationItemSelectedListener { menuItem ->
            when (menuItem.itemId) {
                R.id.nav_home -> {
                    // Navega de vuelta al nav_home
                    navController.navigate(R.id.nav_home)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.nav_gallery -> {
                    // Navega de vuelta al nav_gallery
                    navController.navigate(R.id.nav_gallery)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.nav_slideshow -> {
                    // Navega de vuelta al nav_slideshow Dashboard powerBi
                    navController.navigate(R.id.nav_slideshow)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.nav_panelDeControl -> {
                    // Navega de vuelta al nav_panelDeControl
                    navController.navigate(R.id.nav_panelDeControl)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.nav_soporteTecnico -> {
                    // Navega de vuelta al nav_soporteTecnico
                    navController.navigate(R.id.nav_soporteTecnico)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.nav_configuracion -> {
                    // Navega de vuelta al nav_configuracion
                    navController.navigate(R.id.nav_configuracion)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.nav_documentos -> {
                    // Navega de vuelta al nav_detalleEvento
                    navController.navigate(R.id.nav_documentos)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }

                R.id.nav_miPerfil -> {
                    // Navega de vuelta al nav_miPerfil
                    navController.navigate(R.id.nav_miPerfil)
                    drawerLayout.closeDrawer(GravityCompat.START)
                    true
                }
                // ... manejo de otros ítems ...
                else -> false
            }
        }

        //--------NOTIFICATION QUE DURA 1 MINUTO    MALA PRACTICA. CONSUME BATERIA AUNQUE LA APP ESTE "CERRADA"-----------
        //Creamos el canal de notifications
        //createChannel()
        // Manejar el Intent que inicia la actividad de notificacion
        handleIntent(intent)
        // Manejar el intent para abrir MiPerfilFragment
        intent.getStringExtra("OPEN_FRAGMENT")?.let { fragmentTag ->
            // Lógica para abrir MiPerfilFragment aquí
        }

        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val alarmIntent = Intent(this, Notificacion::class.java).apply {
            action = "com.example.postventaandroid.ACTION_START_ALARM"
        }
        //Para añadir en el alarmManager
        val pendingIntent = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            PendingIntent.getBroadcast(
                this,
                0,  // Debe ser único para cada PendingIntent
                alarmIntent,
                PendingIntent.FLAG_IMMUTABLE  // Usar FLAG_IMMUTABLE para Android 12 y superior
            )
        } else {
            PendingIntent.getBroadcast(
                this,
                0,  // Debe ser único para cada PendingIntent
                alarmIntent,
                PendingIntent.FLAG_IMMUTABLE  // No es necesario especificar FLAG en versiones anteriores a Android 12
            )
        }
        // Configurar la alarma para que se repita cada 1 minuto (900,000 milisegundos)
        val intervalMillis = 2000  // 15 minutos en milisegundos
        val triggerAtMillis = System.currentTimeMillis()
        //Repite la notification
        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            triggerAtMillis,
            intervalMillis.toLong(),
            pendingIntent
        )
        //----------------------FIN NOTIFICATION--------


    }

    //Menu de 3 puntos. Esquina superior derecha
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.main, menu)
        return true
    }

    //Menu de 3 puntos. Esquina superior derecha. Listener de seleccion
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.cerrarSesion_menuItem_MAIN -> {
                // Llama a la función de cierre de sesión
                cerrarSesion()
                return true
            }

            R.id.notificacionPrueba_menuItem_MAIN -> {
                //scheduleNotification()
                return true

            }

            else -> return super.onOptionsItemSelected(item)
        }
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun navigateTo(destinationId: Int) {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        navController.navigate(destinationId)
    }

    //logica para cerrar sesion del usuario
    private fun cerrarSesion() {
        //val idUser = sessionManager.fetchUser()!!.id
        //sendTokenToServerSql(idUser, "sesionCerrada...")
        sessionManager.clearUser()
        mensaje("Sesion cerrada.")
        val intent = Intent(this, Login_Pantalla::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }

    fun mensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }


    // Declare the launcher at the top of your Activity/Fragment:
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { isGranted: Boolean ->
        if (isGranted) {
            // FCM SDK (and your app) can post notifications.
        } else {
            // TODO: Inform user that that your app will not show notifications.
        }
    }

    private fun askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(android.Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(android.Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }


    private fun crearCanalNotificacion() {
        // Crea el canal de notificación si la versión de Android es Oreo (API 26) o superior
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val canalImportancia = NotificationManager.IMPORTANCE_HIGH // Importancia del canal
            val canal = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, canalImportancia).apply {
                description = CHANNEL_DESCRIPTION // Descripción del canal
            }
            val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            manager.createNotificationChannel(canal) // Crea el canal de notificación
        }
    }

    //Envia token cada vez que inicia sesion a la API y este, la envia a la bd
    private fun sendTokenToServer(userId: Int, token: String) {
        val url = "http://192.168.1.104:3000/api/save-token" // Reemplaza con la IP de tu servidor
        val requestBody = JSONObject().apply {
            put("userId", userId)
            put("token", token)
        }

        val request = JsonObjectRequest(Request.Method.POST, url, requestBody,
            { response ->
                Log.d("Token a NODEJS API", "Token guardado exitosamente: $response")
            },
            //Si el error que sale es: "org.json.JSONException: Value Token of type java.lang.String cannot be converted to JSONObject"
            //Funciona y guarda el token en la bd. Nose por que funciona, pero funciona
            { error ->
                Log.e("Token a NODEJS API", " Error al guardar el token: ${error.message}")
            })


        Volley.newRequestQueue(this).add(request)
    }

    //Version de sql de la funcion de arriba
    private fun sendTokenToServerSql(userId: Int, token: String): Boolean{
        var bool = false
        try {
            val query = "[dbo].[_1_UpdateUsuarioToken] ?, ?"
            val statement = conexion.dbConn()!!.prepareStatement(query)
            statement.setString(1, token)   //Token actual del dispositivo
            statement.setInt(2, userId)//Id del usuario
            val resu = statement.executeUpdate()
            if (resu == 1){
                //Exito
                bool = true
            }else{
                //fallo
                bool = false
            }
        }catch (x: SQLException){
            x.printStackTrace()
        }catch (e: Exception){
            e.printStackTrace()
        }
        return bool
    }

    //De notificacion worker de un minuto
    //Despues de aqui vienen todas las funciones de la notificacion de 1 minuto

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // Manejar el nuevo Intent cuando la actividad ya está en marcha
        handleIntent(intent)
    }


    private fun handleIntent(intent: Intent) {
        val openFragment = intent.getStringExtra("OPEN_FRAGMENT")
        if (openFragment == "MiPerfilFragment") {
            // Navegar a MiPerfilFragment
            navigateToFragment(MiPerfilFragment())
        }
    }

    private fun navigateToFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(
                R.id.nav_miPerfil,
                fragment
            )  // Reemplaza con el ID de tu contenedor de fragmentos
            .setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN)
            .addToBackStack(null)
            .commit()
    }
    //termina notificacion 1 minuto


    /*@SuppressLint("MissingSuperCall")
    override fun onBackPressed() {
        // super.onBackPressed()
    // No llames a super.onBackPressed() si no quieres que haga nada por defecto
    }*/
}
