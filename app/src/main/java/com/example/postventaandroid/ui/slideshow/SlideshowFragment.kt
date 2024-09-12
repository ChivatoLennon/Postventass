package com.example.postventaandroid.ui.slideshow

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.NavController
import androidx.navigation.fragment.findNavController
import com.example.postventaandroid.R
import com.example.postventaandroid.databinding.FragmentSlideshowBinding
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.github.mikephil.charting.utils.ColorTemplate

//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\
//PARA PANTALLLA DASHBOARD!!!!
//\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\\

class SlideshowFragment : Fragment() {

    private var _binding: FragmentSlideshowBinding? = null
    //private lateinit var barChart : BarChart

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!
    val fragment_Mapa = MapaFragment()

    private lateinit var btn_irMapa: Button
    private lateinit var btn_irDatosMapa : Button

    //creamos variable para el nav pase al otro fragment
    private lateinit var nav : NavController

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val slideshowViewModel =
            ViewModelProvider(this).get(SlideshowViewModel::class.java)

        _binding = FragmentSlideshowBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //incicializamos variable
        nav = findNavController()

        btn_irMapa = root.findViewById(R.id.btn_IrA_Mapa_slideshowFragment)
        btn_irMapa.setOnClickListener() {
            irFragmentMapa()
        }

        btn_irDatosMapa = root.findViewById(R.id.btn_IrA_DatosMapa_slideshowFragment)
        btn_irDatosMapa.setOnClickListener(){
            irFragmentDatosMapa()
        }

        //barChart = root.findViewById(R.id.barChart_slideShowFragment)

        return root
    }

    fun irFragmentMapa() {
        //utiliza el <action> de navGraph para pasar al otro fragment
        nav.navigate(R.id.action_nav_slideshow_to_mapaFragment)
    }

    fun irFragmentDatosMapa() {
        //utiliza el <action> de navGraph para pasar al otro fragment
        nav.navigate(R.id.action_nav_slideshow_to_datosMapaFragment)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun mensaje(mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }
}