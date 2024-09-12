package com.example.postventaandroid.ui.perfil

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.postventaandroid.R
import com.example.postventaandroid.databinding.FragmentHistorialBinding
import com.example.postventaandroid.ui.Data.ConnSQL
import com.example.postventaandroid.ui.Data.GestionData
import com.example.postventaandroid.ui.Data.HallazgoData
import com.example.postventaandroid.ui.Data.SessionManager
import com.example.postventaandroid.ui.detalleEvento.MyAdapterDetalleEvento
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp

//Este es el historial de todas las alertas
class HistorialFragment : Fragment(), MyAdapterDetalleEvento.OnItemClickListener {

    private var conexionSql = ConnSQL()
    private lateinit var sessionManager: SessionManager

    //Para recyclerview y mostrar en las cardview cada item de la BD
    private lateinit var recyclerView: RecyclerView
    private lateinit var adaptador: MyAdapterDetalleEvento

    private lateinit var buscador: SearchView

    // Lista original y lista para filtrar en spinnerPantallaOptions()
    private var originalhallazgoArrayList: ArrayList<HallazgoData> = arrayListOf()

    //array para filtrar la lista
    private val queryId = arrayListOf("Abierto", "Pendiente", "Cerrado")

    private lateinit var progressBar: ProgressBar

    private lateinit var txtNoDatosDisponible: TextView

    private lateinit var spinner: Spinner
    private var opcionSpinner: String = ""
    private var conf = 0

    private lateinit var linearLayoutRecyclerView: LinearLayout

    private lateinit var refreshLayout: SwipeRefreshLayout

    private var _binding: FragmentHistorialBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val MapaViewModel =
            ViewModelProvider(this).get(HistorialViewModel::class.java)

        _binding = FragmentHistorialBinding.inflate(inflater, container, false)
        val root: View = binding.root

        refreshLayout = root.findViewById(R.id.swipeRefresh_HistorialFragment)

        sessionManager = SessionManager(requireContext())
        txtNoDatosDisponible = root.findViewById(R.id.txtHistorialNoDisponible_HistorialFragment)
        txtNoDatosDisponible.visibility = View.GONE
        buscador = root.findViewById(R.id.txtBuscador_HistorialFragment)

        progressBar = root.findViewById(R.id.progressBar_HistorialFragment)
        progressBar.visibility = View.VISIBLE
        spinner = root.findViewById(R.id.spinnerPantallas_HistorialFragment)
        linearLayoutRecyclerView =
            root.findViewById(R.id.recyclerView_linearlayout_HistorialFragment)

        //Items para vista del spinner
        val itemSpinner = listOf(
            "Seleccione estado de Alerta",
            "Abierto",
            "Pendiente",
            "Cerrado"
        )

        val spinnerAdapter =
            ArrayAdapter(
                requireContext(),
                android.R.layout.simple_spinner_dropdown_item,
                itemSpinner
            )
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = spinnerAdapter

        recyclerView = root.findViewById(R.id.recyclerView_HistorialFragment)

        //Para recyclerView
        recyclerView = root.findViewById(R.id.recyclerView_HistorialFragment)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)
        //--------------

        //Listener del campo de busqueda
        buscador.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(newText: String?): Boolean = false

            //Se actualiza cada que cambia de input, en este caso un entero
            // para buscar el ID del Riesgo asociado
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })


        //Uso continuo asincrono para tener datos actualizados de última instancia de la base de datos siempre
        viewLifecycleOwner.lifecycleScope.launch {
            spinner.isEnabled = false
            buscador.visibility = View.GONE
            spinner.visibility = View.GONE
            //"hallazgoArrayList" asigna la tabla de la consulta SQL al Adapter
            // para que pueda leerse en el cardview
            // Inicialización de la lista a filtrar

            originalhallazgoArrayList = getHallazgoData()
            adaptador = MyAdapterDetalleEvento(originalhallazgoArrayList, this@HistorialFragment)
            recyclerView.adapter = adaptador
            if (originalhallazgoArrayList.isNotEmpty()) {
                spinner.isEnabled = true
                buscador.visibility = View.VISIBLE
                spinner.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
            } else {
                txtNoDatosDisponible.visibility = View.VISIBLE
            }

            adaptador.notifyDataSetChanged()
        }


        //Spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                //val itemSeleccionado = originalhallazgoArrayList
                spinnerEstadoOptions(originalhallazgoArrayList, position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Manejo cuando no se selecciona nada
            }
        }
        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        refreshLayout = view.findViewById(R.id.swipeRefresh_HistorialFragment)
        buscador = view.findViewById(R.id.txtBuscador_HistorialFragment)

        //Recarga asincrona
        var array = originalhallazgoArrayList
        lifecycleScope.launch {
            array = getHallazgoData()
        }

        //Refresh que recarga la lista deslizando hacia abajo
        refreshLayout.setOnRefreshListener {
            adaptador.actualizarLista(array)
            Handler(Looper.getMainLooper()).postDelayed({
                refreshLayout.isRefreshing = false
                if (array.isEmpty()) mensaje("No hay datos, intente de nuevo")
            }, 1500)
        }
    }

    //OPciones del estado de los hallazgos
    fun spinnerEstadoOptions(
        itemSeleccionada: ArrayList<HallazgoData>,
        indiceSeleccionado: Int
    ) {
        when (indiceSeleccionado) {
            0 -> {
                opcionSpinner = "No ingresado"
                val listaFiltrada: List<HallazgoData> = itemSeleccionada
                if (listaFiltrada.isNotEmpty()) {
                    adaptador.actualizarLista(ArrayList(listaFiltrada))
                }
                conf = 0
            }

            1 -> {
                //"abierto"
                val listaFiltrada: List<HallazgoData> =
                    itemSeleccionada.filter { it.EstadoCierre == queryId[0] }
                adaptador.actualizarLista(ArrayList(listaFiltrada))
            }

            2 -> {
                //"pendiente"
                val listaFiltrada: List<HallazgoData> =
                    itemSeleccionada.filter { it.EstadoCierre == queryId[1] }
                adaptador.actualizarLista(ArrayList(listaFiltrada))
            }

            3 -> {
                //"Cerrados"
                val listaFiltrada: List<HallazgoData> =
                    itemSeleccionada.filter { it.EstadoCierre == queryId[2] }
                adaptador.actualizarLista(ArrayList(listaFiltrada))
            }
        }
    }


    //Los métodos toHallazgoData y getHallazgoData trabajan juntos para convertir
    //los resultados de una consulta SQL en una lista de objetos HallazgoData
    private fun ResultSet.toHallazgoData(): HallazgoData {
        return HallazgoData(
            getInt(1),      //ID
            getString(2),   //Sector
            getString(3),   //Supervisor
            getString(4),   //NivelAlerta
            getString(5),   //DescripcionAlerta
            getBytes(6),    //IMGBinary
            getBoolean(7),  //Verificado o no
            //removeLastNchars(getString(8), 5),     //Fecha
            getTimestamp(8),
            getString(9),   //EstadoCierre
            getString(10),  //Proyecto/Mina
            getString(11),  //Area
            getString(12),  // Actividad
            getString(13),  //Riesgo_RC
            getString(14),  //Reportado por
            getInt(15)  //Responsable
        )
    }


    //getHallazgoData ejecuta una consulta SQL para obtener datos de hallazgo
    // y los convierte en una lista de objetos HallazgoData.
    suspend private fun getHallazgoData(): ArrayList<HallazgoData> {
        return withContext(Dispatchers.IO) {
            val datasetArrayList = arrayListOf<HallazgoData>()
            try {
                val supervisor = sessionManager.fetchUser()?.nombre
                val query = "[dbo].[_1_Historial_PerfilFragment] ?"

                conexionSql.dbConn()?.prepareStatement(query)?.use { statement ->
                    statement.setString(1, supervisor)
                    val resultSet = statement.executeQuery()
                    while (resultSet.next()) {
                        datasetArrayList.add(resultSet.toHallazgoData())
                    }
                } ?: mensaje("Error al conectar con la base de datos")

                activity?.runOnUiThread() {
                    progressBar.visibility = View.GONE
                }

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

    //Resumen del Flujo

    //Consulta SQL: getHallazgoData ejecuta una consulta SQL para obtener datos.
    //Conversión de ResultSet: Dentro de getHallazgoData, se itera sobre el ResultSet y cada fila se
    // convierte en un objeto HallazgoData utilizando toHallazgoData.
    //Construcción de la Lista: Los objetos HallazgoData se agregan a una lista (datasetArrayList).
    //Resultado Final: getHallazgoData retorna la lista de objetos HallazgoData obtenidos de
    // la bd SQL Server .

    //Funcion para filtrar la busqueda en el listener de "searchview".
    //El resultado de la condicion dentro de la variable constante "listaFiltrada"
    //obtiene el resultado de si el campo de busqueda esta vacio, se queda la lista original
    //Si no, se asigna la constante "queryId" el parametro "query" con parse a entero
    //Y luego filtra en la lista por el ID entregado con el metodo filter
    //En donde ID_Hallazgo sea igual a queryId
    //Para terminar "adaptador" de MyAdapterDetalleEvento usa el metodo actualizarLista, el cual recibe
    //el resultado de la constante con los datos almacenados de "listaFiltrada"
    //Es necesario que se ingrese ArrayList(listaFiltrada). nota: Informacion no actualizada 29-07-24
    private fun filterList(query: String?) {
        val listaFiltrada: ArrayList<HallazgoData> = ArrayList()

        for (item in originalhallazgoArrayList) {
            if (item.ID_Hallazgo.toString().contains(query.toString())) {
                listaFiltrada.add(item)
            }
        }
        if (listaFiltrada.isEmpty()) {
            mensaje("No hay datos encontrados...")
        } else {
            adaptador.actualizarLista(ArrayList(listaFiltrada))
        }
    }

    private fun mensaje(mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onResume() {
        super.onResume()
        lifecycleScope.launch {
            val act = getHallazgoData()
            adaptador = MyAdapterDetalleEvento(act, null)
            recyclerView.adapter = adaptador
        }

    }

    //Para remover el ultimo caracter de un string
    private fun removeLastNchars(str: String, n: Int): String {
        return str.substring(0, str.length - n)
    }


    override fun onUpdateClick(position: Int, hallazgo: HallazgoData) {
        TODO("Not yet implemented")
    }

    override fun onDeleteClick(position: Int) {
        TODO("Not yet implemented")
    }

    override fun onButtonClick(position: Int) {
        TODO("Not yet implemented")
    }

    //FINAL
}




