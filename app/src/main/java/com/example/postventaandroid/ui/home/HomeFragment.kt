package com.example.postventaandroid.ui.home


import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.postventaandroid.NavigationHost
import com.example.postventaandroid.R
import com.example.postventaandroid.databinding.FragmentHomeBinding
import com.example.postventaandroid.ui.Data.ConnSQL
import com.example.postventaandroid.ui.Data.SessionManager
import com.example.postventaandroid.ui.login.Login_Pantalla
import com.google.android.material.navigation.NavigationView
import kotlinx.coroutines.*

//Clase de la primera pantalla luego de iniciar sesion
//Pantalla de inicio
class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private var navigationHost: NavigationHost? = null
    private val conecction = ConnSQL()

    private lateinit var sessionManager: SessionManager

    private lateinit var progressBar: ProgressBar

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var counter_Badge : EditText

    //CoroutineScope para ejecutar la tarea periodicamente de los 5 secs
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onAttach(context: Context) {
        super.onAttach(context)
        if (context is NavigationHost) {
            navigationHost = context
        } else {
            throw RuntimeException(context.toString() + " must implement NavigationHost")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        val root: View = binding.root
        sessionManager = SessionManager(root.context)

        val btn_cerrarSesion: Button = root.findViewById(R.id.btn_cerrarSesion_homeFragment)
        val imgBtn_alerta: ImageButton = root.findViewById(R.id.imgBtn_alertas_homeFragment)
        val imgBtn_dashboard: ImageButton = root.findViewById(R.id.imgBtn_dashboard_homeFragment)
        val imgBtn_soporteTecnico: ImageButton = root.findViewById(R.id.imgBtn_soporteTecnico_homeFragment)
        val imgBtn_detalleEvento: ImageButton = root.findViewById(R.id.imgBtn_informes_homeFragment)
        val imgBtn_perfil: ImageButton = root.findViewById(R.id.imgBtn_perfil_homeFragment)
        val imgBtn_metas: ImageButton = root.findViewById(R.id.imgBtn_metas_homeFragment)

        progressBar = root.findViewById(R.id.progressBar_badgeCount_HomeFragment)
        progressBar.visibility = View.GONE

        counter_Badge = root.findViewById(R.id.counterNotificationBadge_homefragment)
        counter_Badge.visibility = View.GONE

        imgBtn_perfil.setOnClickListener() {
            navigationHost?.navigateTo(R.id.nav_miPerfil)
            val navView: NavigationView = activity?.findViewById(R.id.nav_view)!!
            navView.setCheckedItem(R.id.nav_miPerfil)
        }

        imgBtn_metas.setOnClickListener() {
        }


        imgBtn_alerta.setOnClickListener() {
            navigationHost?.navigateTo(R.id.nav_gallery)
            val navView: NavigationView = activity?.findViewById(R.id.nav_view)!!
            navView.setCheckedItem(R.id.nav_gallery)

        }

        imgBtn_dashboard.setOnClickListener() {
            navigationHost?.navigateTo(R.id.nav_slideshow)
            val navView: NavigationView = activity?.findViewById(R.id.nav_view)!!
            navView.setCheckedItem(R.id.nav_slideshow)
        }

        imgBtn_detalleEvento.setOnClickListener() {
            navigationHost?.navigateTo(R.id.nav_documentos)
            val navView: NavigationView = activity?.findViewById(R.id.nav_view)!!
            navView.setCheckedItem(R.id.nav_documentos)
        }

        imgBtn_soporteTecnico.setOnClickListener() {
            navigationHost?.navigateTo(R.id.nav_soporteTecnico)
            val navView: NavigationView = activity?.findViewById(R.id.nav_view)!!
            navView.setCheckedItem(R.id.nav_soporteTecnico)
        }

        btn_cerrarSesion.setOnClickListener() {
            cerrarSesion()
        }


        // Llama a la función para actualizar el Counter badge
        //updateBadgeCount()
        startUpdatingBadge()
        return root
    }

    private fun startUpdatingBadge() {
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            while (isActive) {
                val count = getUnverifiedHallazgosCount()
                if (count > 0) {
                    progressBar.visibility = View.GONE
                    counter_Badge.setText(count.toString())
                    counter_Badge.visibility = View.VISIBLE
                } else {
                    counter_Badge.visibility = View.GONE
                    progressBar.visibility = View.GONE
                }
                delay(2000) // Actualizar cada 2 segundos
            }
        }
        //conecction.dbConn()?.close()
    }

    suspend fun getUnverifiedHallazgosCount(): Int {
        val supervisor = sessionManager.fetchUser()?.nombre.toString()
        return conecction.getUnseenItemsCount(supervisor)
    }


    private fun cerrarSesion() {
        sessionManager.clearUser()
        mensaje("Sesion cerrada.")
        val intent = Intent(context, Login_Pantalla::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        this.activity?.finish()
    }


    fun mensaje(mensaje: String){
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
        _binding = null
    }

}