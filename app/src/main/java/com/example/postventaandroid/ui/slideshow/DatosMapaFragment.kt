package com.example.postventaandroid.ui.slideshow

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.example.postventaandroid.R
import com.example.postventaandroid.databinding.FragmentDatosMapaBinding
import com.example.postventaandroid.ui.Data.ConnSQL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.SQLException
//Clase de los datos de cada sector
class DatosMapaFragment : Fragment() {

    private val conexionSql = ConnSQL()

    private var _binding: FragmentDatosMapaBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var progressBar: ProgressBar
    private lateinit var btnActualizar: Button

    //Hay 21 variables aca
    private lateinit var txtHallazgoAbiertoBarrioCivicoMapa : TextView
    private lateinit var txtHallazgoCerradoBarrioCivicoMapa : TextView
    private lateinit var txtHallazgoCGraveBarrioCivicoMapa : TextView
    private lateinit var txtHallazgoLeveBarrioCivicoMapa : TextView
    private lateinit var txtHallazgoPositivoBarrioCivicoMapa : TextView
    private lateinit var txtHallazgoOportunidadMejoraBarrioCivicoMapa : TextView
    private lateinit var txtHallazgoEMERGENCIABarrioCivicoMapa : TextView

    private lateinit var txtHallazgoAbiertoTallerNTIMapa : TextView
    private lateinit var txtHallazgoCerradoTallerNTIMapa : TextView
    private lateinit var txtHallazgoGraveTallerNTIMapa : TextView
    private lateinit var txtHallazgoLeveTallerNTIMapa : TextView
    private lateinit var txtHallazgoPositivoTallerNTIMapa : TextView
    private lateinit var txtHallazgoOportunidadMejoraTallerNTIMapa : TextView
    private lateinit var txtHallazgoEmergenciaTallerNTIMapa : TextView

    private lateinit var txtHallazgoAbiertoTCTAPMapa : TextView
    private lateinit var txtHallazgoCerradoTCTAPMapa : TextView
    private lateinit var txtHallazgoGraveTCTAPMapa : TextView
    private lateinit var txtHallazgoLeveTCTAPMapa : TextView
    private lateinit var txtHallazgoPositivoTCTAPMapa : TextView
    private lateinit var txtHallazgoOportunidadMejoraTCTAPMapa : TextView
    private lateinit var txtHallazgoEmergenciaTCTAPMapa : TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val DatosMapaViewModel = ViewModelProvider(this).get(DatosMapaViewModel::class.java)

        _binding = FragmentDatosMapaBinding.inflate(inflater, container, false)

        val root: View = binding.root

        btnActualizar = root.findViewById(R.id.btnActualizarDatos_MapaHallazgosFragment)
        progressBar = root.findViewById(R.id.progressBar_MapaHallazgos)

        //BARRIO CIVICO
        txtHallazgoAbiertoBarrioCivicoMapa = root.findViewById(R.id.txtHallazgoAbierto_BarrioCivico_Mapa)
        txtHallazgoCerradoBarrioCivicoMapa = root.findViewById(R.id.txtHallazgoCerrado_BarrioCivico_Mapa)
        txtHallazgoCGraveBarrioCivicoMapa = root.findViewById(R.id.txtHallazgoCGrave_BarrioCivico_Mapa)
        txtHallazgoLeveBarrioCivicoMapa = root.findViewById(R.id.txtHallazgoLeve_BarrioCivico_Mapa)
        txtHallazgoPositivoBarrioCivicoMapa = root.findViewById(R.id.txtHallazgoPositivo_BarrioCivico_Mapa)
        txtHallazgoOportunidadMejoraBarrioCivicoMapa = root.findViewById(R.id.txtHallazgoOportunidadMejora_BarrioCivico_Mapa)
        txtHallazgoEMERGENCIABarrioCivicoMapa = root.findViewById(R.id.txtHallazgoEMERGENCIA_BarrioCivico_Mapa)

        //TALLERT NTI
        txtHallazgoAbiertoTallerNTIMapa = root.findViewById(R.id.txtHallazgoAbierto_Taller_NTI_Mapa)
        txtHallazgoCerradoTallerNTIMapa = root.findViewById(R.id.txtHallazgoCerrado_Taller_NTI_Mapa)
        txtHallazgoGraveTallerNTIMapa = root.findViewById(R.id.txtHallazgoGrave_Taller_NTI_Mapa)
        txtHallazgoLeveTallerNTIMapa = root.findViewById(R.id.txtHallazgoLeve_Taller_NTI_Mapa)
        txtHallazgoPositivoTallerNTIMapa = root.findViewById(R.id.txtHallazgoPositivo_Taller_NTI_Mapa)
        txtHallazgoOportunidadMejoraTallerNTIMapa = root.findViewById(R.id.txtHallazgoOportunidadMejora_Taller_NTI_Mapa)
        txtHallazgoEmergenciaTallerNTIMapa = root.findViewById(R.id.txtHallazgoEmergencia_Taller_NTI_Mapa)

        //TC TAP MAPA
        txtHallazgoAbiertoTCTAPMapa = root.findViewById(R.id.txtHallazgoAbierto_TC_TAP_Mapa)
        txtHallazgoCerradoTCTAPMapa = root.findViewById(R.id.txtHallazgoCerrado_TC_TAP_Mapa)
        txtHallazgoGraveTCTAPMapa = root.findViewById(R.id.txtHallazgoGrave_TC_TAP_Mapa)
        txtHallazgoLeveTCTAPMapa = root.findViewById(R.id.txtHallazgoLeve_TC_TAP_Mapa)
        txtHallazgoPositivoTCTAPMapa = root.findViewById(R.id.txtHallazgoPositivo_TC_TAP_Mapa)
        txtHallazgoOportunidadMejoraTCTAPMapa = root.findViewById(R.id.txtHallazgoOportunidadMejora_TC_TAP_Mapa)
        txtHallazgoEmergenciaTCTAPMapa = root.findViewById(R.id.txtHallazgoEmergencia_TC_TAP_Mapa)

        btnActualizar.setOnClickListener {
            lifecycleScope.launch {
                progressBar.visibility = View.VISIBLE
                if (añadirTextos(launchDataUpdate())){
                    progressBar.visibility = View.GONE
                }else{
                    progressBar.visibility = View.VISIBLE
                }
            }
        }


        return root
    }

    override fun onStart() {
        super.onStart()
        lifecycleScope.launch {
            progressBar.visibility = View.VISIBLE
            if (añadirTextos(launchDataUpdate())){
                progressBar.visibility = View.GONE
            }else{
                progressBar.visibility = View.VISIBLE
            }
        }
    }

    private fun añadirTextos(array : ArrayList<String>) : Boolean{
        val bool : Boolean
        if (array.isNotEmpty()){
            txtHallazgoAbiertoBarrioCivicoMapa.text = array[0]
            txtHallazgoCerradoBarrioCivicoMapa.text = array[1]
            txtHallazgoCGraveBarrioCivicoMapa.text = array[2]
            txtHallazgoLeveBarrioCivicoMapa.text = array[3]
            txtHallazgoPositivoBarrioCivicoMapa.text = array[4]
            txtHallazgoOportunidadMejoraBarrioCivicoMapa.text = array[5]
            txtHallazgoEMERGENCIABarrioCivicoMapa.text = array[6]

            txtHallazgoAbiertoTallerNTIMapa.text = array[7]
            txtHallazgoCerradoTallerNTIMapa.text = array[8]
            txtHallazgoGraveTallerNTIMapa.text = array[9]
            txtHallazgoLeveTallerNTIMapa.text = array[10]
            txtHallazgoPositivoTallerNTIMapa.text = array[11]
            txtHallazgoOportunidadMejoraTallerNTIMapa.text = array[12]
            txtHallazgoEmergenciaTallerNTIMapa.text = array[13]

            txtHallazgoAbiertoTCTAPMapa.text = array[14]
            txtHallazgoCerradoTCTAPMapa.text = array[15]
            txtHallazgoGraveTCTAPMapa.text = array[16]
            txtHallazgoLeveTCTAPMapa.text = array[17]
            txtHallazgoPositivoTCTAPMapa.text = array[18]
            txtHallazgoOportunidadMejoraTCTAPMapa.text = array[19]
            txtHallazgoEmergenciaTCTAPMapa.text = array[20]
            bool = true
        }else{
            bool = false
        }
        return bool
    }

    suspend private fun launchDataUpdate() : ArrayList<String> {
        return withContext(Dispatchers.IO) {
            val hallazgos = arrayListOf<String>()
            try {
                val query = "[dbo].[_1_Mapeo_Taller_NTI]" //CHECK
                val statement = conexionSql.dbConn()?.prepareStatement(query)!!
                val resu = statement.executeQuery()!!

                if (resu.next()) {
                    for (i in 1..21){
                        hallazgos.add(resu.getString(i).toString())
                    }
                }
            } catch (x: SQLException) {
                Log.e("Error en Mapa", x.message!!)
                // Handle UI updates for errors on main thread (optional)
            }catch (e: Exception){
                e.printStackTrace()
            }

            return@withContext hallazgos
        }
    }

}
