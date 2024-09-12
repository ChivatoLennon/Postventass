package com.example.postventaandroid.ui.perfil


import android.annotation.SuppressLint
import android.os.Bundle
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.HorizontalScrollView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.ViewCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.postventaandroid.R
import com.example.postventaandroid.databinding.FragmentMiPerfilBinding
import com.example.postventaandroid.ui.Data.ConnSQL
import com.example.postventaandroid.ui.Data.GestionData
import com.example.postventaandroid.ui.Data.HallazgoData
import com.example.postventaandroid.ui.Data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.ResultSet
import java.sql.SQLException

//clase de la pantalla Mi Perfil
class MiPerfilFragment : Fragment() {

    private var _binding: FragmentMiPerfilBinding? = null
    private lateinit var sessionManager: SessionManager

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var conexionSql = ConnSQL()

    private lateinit var recyclerView: RecyclerView
    lateinit var adaptador: PerfilAdapter
    private lateinit var horizontalScrollView: HorizontalScrollView

    private lateinit var progressBar: ProgressBar

    private var originalhallazgoArrayList: ArrayList<HallazgoData> = arrayListOf()

    private lateinit var txtRut: TextView
    private lateinit var txtNombreUsuario: TextView
    private lateinit var txtCargoUsuario: TextView
    private lateinit var txtCorreo_MiPerfilFragment: TextView
    private lateinit var txtSinAlerta: TextView
    private lateinit var txtEstado: TextView
    private lateinit var txtAlertasPendientes: TextView

    private lateinit var btnTODO: Button
    private lateinit var btnGrave: Button
    private lateinit var btnLeve: Button
    private lateinit var btnPositivo: Button
    private lateinit var btnOportunidad: Button
    private lateinit var btnHistorial: Button
    private lateinit var btnEmergencia: Button
    private lateinit var btnGestionarHallazgo: Button

    //para scroll de horizontalScrollView
    private var scrollingLeft = true

    @SuppressLint("ResourceAsColor")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel =
            ViewModelProvider(this).get(MiPerfilViewModel::class.java)

        _binding = FragmentMiPerfilBinding.inflate(inflater, container, false)
        val root: View = binding.root

        horizontalScrollView = root.findViewById(R.id.horizontalScrollview_MiPerfilFragment)

        movimientoHorizontalScrollView()

        btnGrave = root.findViewById(R.id.btnGrave_MiPerfilFragment)
        btnLeve = root.findViewById(R.id.btnLeve_MiPerfilFragment)
        btnPositivo = root.findViewById(R.id.btnPositivo_MiPerfilFragment)
        btnOportunidad = root.findViewById(R.id.btnOportunidadMejora_MiPerfilFragment)
        btnEmergencia = root.findViewById(R.id.btnEmergencia_MiPerfilFragment)
        btnTODO = root.findViewById(R.id.btnTodo_MiPerfilFragment)
        btnGestionarHallazgo = root.findViewById(R.id.btnGestionar_Alertas_MiPerfilFragment)

        txtAlertasPendientes = root.findViewById(R.id.txtAlertasPendientes_MiPerfilFragment)
        txtCorreo_MiPerfilFragment = root.findViewById(R.id.txtCorreo_MiPerfilFragment)
        txtNombreUsuario = root.findViewById(R.id.txt_usuarioBienvenida_miPerfilFragment)
        txtCargoUsuario = root.findViewById(R.id.txt_cargoUsuario_miPerfilFragment)
        txtSinAlerta = root.findViewById(R.id.txtSindatos_gone_miperfilFragment)
        txtEstado = root.findViewById(R.id.txtEstado_MiPerfilFragment)
        txtRut = root.findViewById(R.id.txtRut_MiPerfilFragment)
        txtSinAlerta.visibility = View.GONE

        btnHistorial = root.findViewById(R.id.btnHistorial_Alertas_MiPerfilFragment)


        val listaButtons = listOf(
            "N1 - Hallazgo Grave",
            "N2 - Hallazgo leve",
            "HP - Hallazgo Positivo",
            "OP - Oportunidad de Mejora",
            "EMERGENCIA"
        )

        var nombre: String = ""
        var apellido: String = ""
        var correo: String = ""
        var cargoTrabajador: String = ""
        var estado: String = ""
        var rut: String = ""

        //inicializa el sessionManager
        sessionManager = SessionManager(requireContext())
        val datosUsuario = sessionManager.fetchUser()
        if (datosUsuario != null) {
            // datos del usuario
            nombre = datosUsuario.nombre
            apellido = datosUsuario.apellido
            correo = datosUsuario.correo
            cargoTrabajador = datosUsuario.cargoTrabajador
            estado = datosUsuario.estadoUsuario
            rut = datosUsuario.Rut
            // ...
        }
        val nombreCompleto = "$nombre $apellido"
        txtNombreUsuario.text = nombreCompleto
        txtCargoUsuario.text = cargoTrabajador
        txtCorreo_MiPerfilFragment.text = correo
        txtRut.text = rut
        txtEstado.text = estado

        //Desactiva los buttons hasta que el recycler tenga datos
        btnTODO.isEnabled = false
        btnGrave.isEnabled = false
        btnLeve.isEnabled = false
        btnPositivo.isEnabled = false
        btnOportunidad.isEnabled = false
        btnEmergencia.isEnabled = false

        //Para barra de carga o progreso
        progressBar = root.findViewById(R.id.progressBar_MiPerfilFragment)
        progressBar.visibility = View.VISIBLE

        //Configura el recyclerView
        recyclerView = root.findViewById(R.id.recyclerView_perfilFragment)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)


        //Ir a la pantalla HistorialFragment
        btnHistorial.setOnClickListener() {
            val navController = findNavController()
            navController.navigate(R.id.action_nav_miPerfil_to_historialFragment)

        }

        btnGestionarHallazgo.setOnClickListener() {
            val navController = findNavController()
            navController.navigate(R.id.action_nav_miPerfil_to_gestionFragment)
        }

        //Si es trabajador ocultamos componentes que son para los supervisores
        if (datosUsuario?.cargoTrabajador == "Trabajador") {
            horizontalScrollView.visibility = View.GONE
            txtAlertasPendientes.visibility = View.GONE
            txtSinAlerta.visibility = View.GONE
            recyclerView.visibility = View.GONE
            progressBar.visibility = View.GONE
            btnHistorial.visibility = View.GONE
            btnGestionarHallazgo.visibility = View.GONE
        } else {
            viewLifecycleOwner.lifecycleScope.launch {
                // Ingresa la data de los no verificados al recyclerView
                originalhallazgoArrayList = fetchDataFromSqlServer(datosUsuario!!.nombre)

                adaptador = PerfilAdapter(originalhallazgoArrayList)
                recyclerView.adapter = adaptador
                val textoAlerta = "Nuevas alertas: "
                txtAlertasPendientes.text = "$textoAlerta ${originalhallazgoArrayList.count()}"

                //Si la lista tiene datos, los botones se activaran
                if (originalhallazgoArrayList.isNotEmpty()) {
                    btnTODO.isEnabled = true
                    btnGrave.isEnabled = true
                    btnLeve.isEnabled = true
                    btnPositivo.isEnabled = true
                    btnOportunidad.isEnabled = true
                    btnEmergencia.isEnabled = true
                }

                while (isActive) {
                    originalhallazgoArrayList = fetchDataFromSqlServer(datosUsuario!!.nombre)

                    delay(1000)
                    txtAlertasPendientes.text = "$textoAlerta ${originalhallazgoArrayList.count()}"
                    if (originalhallazgoArrayList.count() == 0) {
                        txtAlertasPendientes.text = "No tienes alertas"
                    }
                }

                progressBar.visibility = View.GONE

                if (originalhallazgoArrayList.isEmpty()) {
                    txtSinAlerta.visibility = View.VISIBLE
                    txtAlertasPendientes.visibility = View.GONE
                }
            }
        }
        btnTODO.setOnClickListener() {
            if (originalhallazgoArrayList.isNotEmpty()){
                adaptador.actualizarLista(originalhallazgoArrayList)
            }
        }

        btnGrave.setOnClickListener() {
            filterList(listaButtons[0])
        }
        btnLeve.setOnClickListener() {
            filterList(listaButtons[1])
        }
        btnPositivo.setOnClickListener() {
            filterList(listaButtons[2])
        }
        btnOportunidad.setOnClickListener() {
            filterList(listaButtons[3])
        }
        btnEmergencia.setOnClickListener() {
            filterList(listaButtons[4])
        }


        return root
    }

    private fun movimientoHorizontalScrollView() {
        //PARA QUE EL SCROLLVIEW HORIZONTAL SE MUEVA AUTOMATICAMENTE
        val intervalo = 50L // Intervalo en milisegundos (ajusta según tus necesidades)
        val handler = android.os.Handler(Looper.getMainLooper())
        val runnable = object : Runnable {
            override fun run() {
                if (scrollingLeft) {
                    if (horizontalScrollView.scrollX == 0) {
                        horizontalScrollView.smoothScrollBy(5, 0)
                        scrollingLeft = false
                    } else {
                        horizontalScrollView.smoothScrollBy(-5, 0)
                    }
                } else {
                    if (ViewCompat.canScrollHorizontally(horizontalScrollView, View.FOCUS_RIGHT)) {
                        horizontalScrollView.smoothScrollBy(5, 0)
                    } else {
                        horizontalScrollView.smoothScrollBy(-5, 0)
                        scrollingLeft = true
                    }
                }
                handler.postDelayed(this, intervalo)
            }
        }
        handler.postDelayed(runnable, intervalo)
    }

    //Filtra la busqueda
    private fun filterList(query: String?) {
        val listaFiltrada: ArrayList<HallazgoData> = ArrayList()

        for (item in originalhallazgoArrayList) {
            if (item.NivelAlerta.contains(query.toString())) {
                listaFiltrada.add(item)
            }
        }
        if (listaFiltrada.isEmpty()) {
            mensaje("No hay datos encontrados...")
        } else {
            adaptador.actualizarLista(ArrayList(listaFiltrada))
        }
    }

    private fun ResultSet.toHallazgoData(): HallazgoData {
        return HallazgoData(
            getInt(1),      //ID
            getString(2),   //Nombre
            getString(3),   //
            getString(4),   //
            getString(5),   //
            getBytes(6),    //
            getBoolean(7),  //Verificado o no
            //getString(8),     //Fecha
            getTimestamp(8),
            getString(9),   //EstadoCierre
            getString(10),  //Proyecto/Mina
            getString(11),  //Area
            getString(12),  //Actividad
            getString(13),  //Riesgo_RC
            getString(14),  //Reportado por
            getInt(15)  //userID
        )
    }


    //Obtiene los datos de los hallazgos sin verificar y
    // los muestra en el Perfil para checkear
    suspend fun fetchDataFromSqlServer(supervisorNombre: String): ArrayList<HallazgoData> {
        return withContext(Dispatchers.IO) {
            val supervisor = supervisorNombre
            val query = "[dbo].[filtrarNoVerificadoSupervisor] ?" //CHECK

            val datasetArrayList = arrayListOf<HallazgoData>()
            try {
                val connection =
                    conexionSql.dbConn() ?: throw SQLException("Error connecting to database")
                val statement = connection.prepareStatement(query)
                statement.setString(1, supervisor)
                val resultSet = statement.executeQuery()

                while (resultSet.next()) {
                    val hallazgoData =
                        resultSet.toHallazgoData()
                    datasetArrayList.add(hallazgoData)
                }

                activity?.runOnUiThread() {
                    progressBar.visibility = View.GONE
                }
                // Cierra la conexion y recursos
                resultSet.close()
                statement.close()
                connection.close()

            } catch (e: SQLException) {
                e.printStackTrace()
                //mensaje("Error al consultar hallazgos (SQL Exception)")
            } catch (e: Exception) {
                e.printStackTrace()
                //mensaje("Error al consultar hallazgos (General Exception)")
            }
            return@withContext datasetArrayList
        }
    }

    // Realiza la actualización en la base de datos
    fun marcarHallazgoVerificado(id: Int) {
        try {
            //preparamos el statement
            val query = "[dbo].[marcarNoVerificado] ?" //CHECK
            val statement = conexionSql.dbConn()?.prepareStatement(query)
                ?: throw SQLException("Error al crear el statement")

            statement.setInt(1, id)
            //executeUpdate para consultas actualizar tabla sql server
            statement.executeUpdate()
            statement.close()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun mensaje(mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }

    //Al entrar y terminar la operacion en CheckActivity.kt
    //Resume el fragment y actualizamos la lista
    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            val a = fetchDataFromSqlServer(sessionManager.fetchUser()!!.nombre)
            val conteoActual = a.count()
            txtAlertasPendientes.text = "Nuevas alertas: $conteoActual"
            adaptador = PerfilAdapter(a)
            recyclerView.adapter = adaptador
            if (conteoActual == 0) {
                txtAlertasPendientes.text = "No tienes alertas"
            }
            progressBar.visibility = View.GONE
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    //FINAL
}