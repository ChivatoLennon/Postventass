package com.example.postventaandroid.ui.perfil

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.SearchView
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.postventaandroid.R
import com.example.postventaandroid.databinding.FragmentHistorialTicketsBinding
import com.example.postventaandroid.ui.Data.ConnSQL
import com.example.postventaandroid.ui.Data.GestionData
import com.example.postventaandroid.ui.Data.HallazgoData
import com.example.postventaandroid.ui.Data.SessionManager
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.ResultSet
import java.sql.SQLException

//este es el historial de todos los tickets
class HistorialTickets : Fragment() {

    private var _binding: FragmentHistorialTicketsBinding? = null
    private lateinit var sessionManager: SessionManager

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private val conexion = ConnSQL()

    private lateinit var buscador: SearchView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: GestionAdapter
    private lateinit var progressBar: ProgressBar

    private var gestionData_list : ArrayList<GestionData> = arrayListOf()

    private lateinit var spinner : Spinner

    //array para filtrar la lista
    private val queryId = arrayListOf("Abierto", "Pendiente", "Resuelto", "Cerrado")
    private var opcionSpinner = ""
    private var conf = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistorialTicketsBinding.inflate(inflater, container, false)
        val root: View = binding.root

        sessionManager = SessionManager(root.context)
        val responsable = sessionManager.fetchUser()!!.nombre

        //Items para vista del spinner
        val itemSpinner = listOf(
            "Seleccione estado del Ticket",
            "Abierto",
            "Pendiente",
            "Resuelto",
            "Cerrado"
        )
        spinner = root.findViewById(R.id.spinner_HistorialTicketFragment)
        val spinnerAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_dropdown_item, itemSpinner)
        spinner.adapter = spinnerAdapter

        //Spinner
        spinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                //val itemSeleccionado = originalhallazgoArrayList
                spinnerEstadoOptions(gestionData_list, position)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Manejo cuando no se selecciona nada
            }
        }

        recyclerView = root.findViewById(R.id.recyclerView_HistorialTicket)
        progressBar = root.findViewById(R.id.progressBar_HistorialTickets)
        progressBar.visibility = View.GONE


        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            gestionData_list = getListaTickets(responsable)
            adapter = GestionAdapter(gestionData_list)
            recyclerView.adapter = adapter
            adapter.notifyDataSetChanged()
            progressBar.visibility = View.GONE
        }

        buscador = root.findViewById(R.id.searchView_FragmentHistorialTickets)
        buscador.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(newText: String?): Boolean = false

            //Se actualiza cada que cambia de input, en este caso un entero
            // para buscar el ID del Riesgo asociado
            override fun onQueryTextChange(newText: String?): Boolean {
                filterList(newText)
                return true
            }
        })


        return root

    }

    //OPciones del estado de los hallazgos
    fun spinnerEstadoOptions(
        itemSeleccionada: ArrayList<GestionData>,
        indiceSeleccionado: Int
    ) {
        when (indiceSeleccionado) {
            0 -> {
                opcionSpinner = "No ingresado"
                //"abierto"
                val listaFiltrada: List<GestionData> = itemSeleccionada
                if (listaFiltrada.isNotEmpty()){
                    adapter.actualizarLista(ArrayList(listaFiltrada))
                }
                conf = 0
            }

            1 -> {
                //"abierto"
                val listaFiltrada: List<GestionData> = itemSeleccionada.filter { it.EstadoGestion == queryId[0] }
                adapter.actualizarLista(ArrayList(listaFiltrada))
            }

            2 -> {
                //"pendiente"
                val listaFiltrada: List<GestionData> = itemSeleccionada.filter { it.EstadoGestion == queryId[1] }
                adapter.actualizarLista(ArrayList(listaFiltrada))
            }
            3 -> {
                //"Resuelto"
                val listaFiltrada: List<GestionData> = itemSeleccionada.filter { it.EstadoGestion == queryId[2] }
                adapter.actualizarLista(ArrayList(listaFiltrada))
            }
            4 -> {
                //"Cerrado"
                val listaFiltrada: List<GestionData> = itemSeleccionada.filter { it.EstadoGestion == queryId[3] }
                adapter.actualizarLista(ArrayList(listaFiltrada))
            }
        }
    }

    private fun filterList(query: String?) {
        val listaFiltrada: ArrayList<GestionData> = ArrayList()

        for (item in gestionData_list){
            if (item.iD_Gestion.toString().contains(query.toString())){
                listaFiltrada.add(item)
            }
        }
        if (listaFiltrada.isEmpty()){
            mensaje("No hay datos encontrados...")
        }else{
            adapter.actualizarLista(ArrayList(listaFiltrada))
        }
    }

    suspend private fun getListaTickets(responsable : String): ArrayList<GestionData> {
        val array = arrayListOf<GestionData>()
        return withContext(Dispatchers.IO) {
            try {

                val query = "[dbo].[_1_TicketPor_Responsable] ?"
                val statement = conexion.dbConn()!!.prepareStatement(query)
                statement.setString(1, responsable)
                val resu = statement.executeQuery()
                while (resu.next()) {
                    array.add(resu.toGestionData())
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } catch (x: SQLException) {
                x.printStackTrace()
            }

            return@withContext array
        }
    }

    private fun ResultSet.toGestionData(): GestionData {
        return GestionData(
            getInt(1),          //ID_Gestion
            getTimestamp(2),    //FechaGestion
            getString(3),       //ControlRiesgo
            getTimestamp(4),    //Tiempo_Deadline
            getString(5),       //Responsable
            getString(6),       //Detalle
            getInt(7),          //ReporteID
            getString(8),       //DeadlineDerivacion
            getString(9)        //EstadoGestion (Estado del ticket)
        )
    }

    private fun mensaje(mensaje : String){
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }


}