package com.example.postventaandroid.ui.perfil

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.postventaandroid.R
import com.example.postventaandroid.databinding.FragmentGestionBinding
import com.example.postventaandroid.ui.Data.ConnSQL
import com.example.postventaandroid.ui.Data.GestionData
import com.example.postventaandroid.ui.Data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.ResultSet
import java.sql.SQLException
import java.sql.Timestamp

class GestionFragment : Fragment() {

    private var _binding: FragmentGestionBinding? = null
    private lateinit var sessionManager: SessionManager

    private val conexionSql = ConnSQL()

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var listGestiones: ArrayList<GestionData> = arrayListOf()


    private lateinit var progressBar: ProgressBar
    private lateinit var recyclerView: RecyclerView
    private lateinit var adaptador: GestionAdapter

    private lateinit var layoutLeyenda: LinearLayout
    private lateinit var btnLeyenda: Button
    private lateinit var btnHistorialTicket : Button


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val gestionViewModel =
            ViewModelProvider(this).get(MiPerfilViewModel::class.java)

        _binding = FragmentGestionBinding.inflate(inflater, container, false)
        val root: View = binding.root

        btnLeyenda = root.findViewById(R.id.btnLinearLayout_fragmentGestion)
        btnHistorialTicket = root.findViewById(R.id.btnHistorialTickets_GestionFragment)

        progressBar = root.findViewById(R.id.progressBar_GestionFragment)
        progressBar.visibility = View.VISIBLE

        layoutLeyenda = root.findViewById(R.id.LinearLayout_fragmentGestion)
        layoutLeyenda.visibility = View.GONE

        recyclerView = root.findViewById(R.id.recyclerView_GestionFragment)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.setHasFixedSize(true)

        sessionManager = SessionManager(requireContext())

        btnLeyenda.setOnClickListener() {
            if (layoutLeyenda.visibility == View.GONE) {
                layoutLeyenda.visibility = View.VISIBLE
            } else {
                layoutLeyenda.visibility = View.GONE
            }
        }

        btnHistorialTicket.setOnClickListener(){
            irHistorialTickets()
        }

        viewLifecycleOwner.lifecycleScope.launch {
            listGestiones = getGestionData()
            adaptador = GestionAdapter(listGestiones)
            recyclerView.adapter = adaptador
            progressBar.visibility = View.GONE
        }

        return root
    }

    private fun irHistorialTickets(){
        val navController = findNavController()
        navController.navigate(R.id.action_gestionFragment_to_historialTickets)
    }

    suspend fun getGestionData(): ArrayList<GestionData> {
        val nombreSupervisor = sessionManager.fetchUser()!!.nombre
        return withContext(Dispatchers.IO) {
            val datasetArrayList = arrayListOf<GestionData>()
            try {
                val query = "[dbo].[_1_listadoGestion] ?"
                val statement = conexionSql.dbConn()!!.prepareStatement(query)
                statement.setString(1, nombreSupervisor)
                val resu = statement?.executeQuery()

                while (resu!!.next()) {
                    datasetArrayList.add(resu.toGestionData())
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } catch (x: SQLException) {
                x.printStackTrace()
            }
            return@withContext datasetArrayList
        }
    }

    private fun ResultSet.toGestionData(): GestionData {
        return GestionData(
            getInt(1),      //ID de la gestion
            getTimestamp(2),   //Fecha inicial de la gestion
            getString(3),   //Control de riesgo
            getTimestamp(4),   //Tiempo final asignado(deadline)
            getString(5),   //Responsable
            getString(6),   //Detalles
            getInt(7),      //ID del hallazgo asignado a esta gestion
            getString(8),    //trae informacion sobre si es Deadline derivado o no aplica, y hrs asignadas
            getString(9) //Trae estado de la gestion
        )
    }

    override fun onResume() {
        super.onResume()
        viewLifecycleOwner.lifecycleScope.launch {
            listGestiones = getGestionData()

            adaptador = GestionAdapter(listGestiones)
            recyclerView.adapter = adaptador

            progressBar.visibility = View.GONE

        }
    }
}