package com.example.postventaandroid.ui.slideshow

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.postventaandroid.R
import com.example.postventaandroid.databinding.FragmentMapaBinding
import com.example.postventaandroid.ui.Data.ConnSQL
import com.google.android.material.imageview.ShapeableImageView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.SQLException

//Este es la clase de los circulos de alertas dibujados en colores
class MapaFragment : Fragment() {

    private var _binding: FragmentMapaBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private var conexionSql = ConnSQL()

    //CoroutineScope para ejecutar la tarea periodicamente de los 5 secs
    private val coroutineScope = CoroutineScope(Dispatchers.Main + Job())

    private lateinit var txtHallazgoViews: List<TextView>
    lateinit var txtTituloEmergencia: TextView

    lateinit var txtEmergencia: TextView
    lateinit var txtNumRojo: TextView
    lateinit var txtNumAmarillo: TextView
    lateinit var txtNumVerde: TextView
    lateinit var txtNumAzul: TextView

    lateinit var progressBar: ProgressBar

    lateinit var circuloEMergencia: ShapeableImageView
    lateinit var circuloRojo: ShapeableImageView
    lateinit var circuloAmarillo: ShapeableImageView
    lateinit var circuloVerde: ShapeableImageView
    lateinit var circuloAzul: ShapeableImageView



    private lateinit var SwipeRefreshLayout: SwipeRefreshLayout


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val MapaViewModel =
            ViewModelProvider(this).get(MapaViewModel::class.java)

        _binding = FragmentMapaBinding.inflate(inflater, container, false)
        val root: View = binding.root
        conexionSql = ConnSQL()
        //SwipeRefreshLayout = root.findViewById(R.id.refreshLayout_MapaFragment)


        circuloEMergencia = root.findViewById(R.id.shapeableImageView_EMERGENCIA_MapaFragment)
        circuloRojo = root.findViewById(R.id.shapeableImageView_AlertaGrave_MapaFragment)
        circuloAmarillo = root.findViewById(R.id.shapeableImageView_AlertaLeve_MapaFragment)
        circuloVerde = root.findViewById(R.id.shapeableImageView_AlertaPositiva_MapaFragment)
        circuloAzul = root.findViewById(R.id.shapeableImageView_OportunidadMejora_MapaFragment)

        txtEmergencia = root.findViewById(R.id.txt_EMERGENCIA_MapaFragment)
        txtNumRojo = root.findViewById(R.id.txt_AlertaGrave_MapaFragment)
        txtNumAmarillo = root.findViewById(R.id.txt_AlertaLeve_MapaFragment)
        txtNumVerde = root.findViewById(R.id.txt_AlertaPositiva_MapaFragment)
        txtNumAzul = root.findViewById(R.id.txt_OportunidadMejora_MapaFragment)
        txtTituloEmergencia = root.findViewById(R.id.txtNombreTitulo_Emergencia_MapaFragment)
        progressBar = root.findViewById(R.id.progressBar_MapaFragment)

        //Inicio ancho y altura de los 5 circulos
        val layoutparamsCirculos = arrayOf(
            circuloEMergencia.layoutParams,
            circuloRojo.layoutParams,
            circuloAmarillo.layoutParams,
            circuloVerde.layoutParams,
            circuloAzul.layoutParams
        )

        //Tarea asincrona
        lifecycleScope.launch {
            while (isActive) {
                aumentarCirculo(layoutparamsCirculos, conteoColorMapa())
                delay(1500)
            }
        }

        return root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        //SwipeRefreshLayout = view.findViewById(R.id.refreshLayout_MapaFragment)

        //Posible futuro añadido
        /*SwipeRefreshLayout.setOnRefreshListener {
            Handler(Looper.getMainLooper()).postDelayed({
                SwipeRefreshLayout.isRefreshing = false
            }, 1500)
        }*/
    }


    //Logica de los circulos
    private fun aumentarCirculo(
        layoutparamsCirculos: Array<ViewGroup.LayoutParams>,
        conteoColorMapa: ArrayList<Int>
    ) {

        //Tamaños de circulos de mayor a menor
        var numPrimero = 850
        var numSegundo = 680
        var numTercero = 500
        var numCuarto = 350
        var numQuinto = 150

        val listaPrueba = arrayListOf(
            50,
            40,
            10,
            42,
            50
        )

        //lista de los tipos de hallazgos
        val mapa = mapOf(
            "Emergencia" to conteoColorMapa[0],
            "rojo" to conteoColorMapa[1],
            "amarillo" to conteoColorMapa[2],
            "verde" to conteoColorMapa[3],
            "azul" to conteoColorMapa[4],
        )

        //PARA PRUEBAS DE CIRCULOS
/*        val mapa = mapOf(
            "Emergencia" to listaPrueba[0],
            "rojo" to listaPrueba[1],
            "amarillo" to listaPrueba[2],
            "verde" to listaPrueba[3],
            "azul" to listaPrueba[4],
        )*/

        //ORdena los hallazgos de mayor a menor
        val orden = mapa.entries.sortedByDescending { it.value }.toMutableList()
        //asignamos el orden
        val mapaOrdenado = mapOf(
            "Primero" to orden[0],
            "Segundo" to orden[1],
            "Tercero" to orden[2],
            "Cuarto" to orden[3],
            "Quinto" to orden[4],
        ).toMap()
        //Elemento de cada circulo para validar
        val primero = mapaOrdenado["Primero"]
        val segundo = mapaOrdenado["Segundo"]
        val tercero = mapaOrdenado["Tercero"]
        val cuarto = mapaOrdenado["Cuarto"]
        val quinto = mapaOrdenado["Quinto"]
        
        //Realiza las comparaciones entre cantidad de hallazgos
        //Y crece, iguala, o decrece los circulos
        for ((clave, valor) in mapaOrdenado) {
            //Log.d("CICLOFOREXCLUSIVO","Clave: $clave, Valor: $valor")
            val valorEnMapa = valor.value
            val a = primero!!.value
            val b = segundo!!.value
            val c = tercero!!.value
            val d = cuarto!!.value
            val e = quinto!!.value
            if (a == b) {
                numPrimero = 750
                numSegundo = 750
            }
            if (b == c) {
                numSegundo = 450
                numTercero = 450
            }
            if (c == d) {
                numTercero = 300
                numCuarto = 300
            }
            if (d == e) {
                numCuarto = 190
                numQuinto = 190
            }
            if (a == b && b == c && c == a) {
                numPrimero = 650
                numSegundo = 650
                numTercero = 650
            }
            if (b == c && b == d) {
                numSegundo = 450
                numTercero = 450
                numCuarto = 450
            }
            if (a == b && b == c && c == d && a == d) {
                numPrimero = 650
                numSegundo = 650
                numTercero = 650
                numCuarto = 650
            }
            if (e == d) {
                numCuarto = 250
                numQuinto = 250
            }
            if (e == d && e == c) {
                numTercero = 250
                numCuarto = 250
                numQuinto = 250
            }
            if (e == d && e == c && e == b) {
                numSegundo = 250
                numTercero = 250
                numCuarto = 250
                numQuinto = 250
            }
            if (a == b && b == c && c == d && d == e) {
                numPrimero = 450
                numSegundo = 450
                numTercero = 450
                numCuarto = 450
                numQuinto = 450
            }
        }

        //Sobrepone el circulo con mas radio
        val elevationDP = 5f
        //-----------PRIMER  CIRCULO-----------
        if (primero!!.value >= segundo!!.value) {
            if (primero.key == "Emergencia") {
                //Emergencia
                layoutparamsCirculos[0].width = numPrimero  //Ancho circulo
                layoutparamsCirculos[0].height = numPrimero //Altura circulo
                circuloEMergencia.layoutParams = layoutparamsCirculos[0] //Asigna ancho y altura previamente obtenidos y representa en el layout
                circuloEMergencia.elevation = elevationDP
                txtEmergencia.elevation = 20f
                txtTituloEmergencia.elevation = 20f

            }
            if (primero.key == "rojo") {
                //Rojo
                layoutparamsCirculos[1].width = numPrimero
                layoutparamsCirculos[1].height = numPrimero
                circuloRojo.layoutParams = layoutparamsCirculos[1]
                circuloRojo.elevation = elevationDP

            }
            if (primero.key == "amarillo") {
                //Amarillo
                layoutparamsCirculos[2].width = numPrimero
                layoutparamsCirculos[2].height = numPrimero
                circuloAmarillo.layoutParams = layoutparamsCirculos[2]
                circuloAmarillo.elevation = elevationDP

            }
            if (primero.key == "verde") {
                //Verde
                layoutparamsCirculos[3].width = numPrimero
                layoutparamsCirculos[3].height = numPrimero
                circuloVerde.layoutParams = layoutparamsCirculos[3]
                circuloVerde.elevation = elevationDP

            }
            if (primero.key == "azul") {
                //Azul
                layoutparamsCirculos[4].width = numPrimero
                layoutparamsCirculos[4].height = numPrimero
                circuloAzul.layoutParams = layoutparamsCirculos[4]
                circuloAzul.elevation = elevationDP

            }
        }
        //-----------SEGUNDO  CIRCULO-----------
        if (segundo.value >= tercero!!.value) {
            if (segundo.key == "Emergencia") {
                //Emergencia
                layoutparamsCirculos[0].width = numSegundo
                layoutparamsCirculos[0].height = numSegundo
                circuloEMergencia.layoutParams = layoutparamsCirculos[0]

            }
            if (segundo.key == "rojo") {
                //Rojo
                layoutparamsCirculos[1].width = numSegundo
                layoutparamsCirculos[1].height = numSegundo
                circuloRojo.layoutParams = layoutparamsCirculos[1]

            }
            if (segundo.key == "amarillo") {
                //Amarillo
                layoutparamsCirculos[2].width = numSegundo
                layoutparamsCirculos[2].height = numSegundo
                circuloAmarillo.layoutParams = layoutparamsCirculos[2]

            }
            if (segundo.key == "verde") {
                //Verde
                layoutparamsCirculos[3].width = numSegundo
                layoutparamsCirculos[3].height = numSegundo
                circuloVerde.layoutParams = layoutparamsCirculos[3]

            }
            if (segundo.key == "azul") {
                //Azul
                layoutparamsCirculos[4].width = numSegundo
                layoutparamsCirculos[4].height = numSegundo
                circuloAzul.layoutParams = layoutparamsCirculos[4]

            }

        }
        //-----------TERCER CIRCULO-----------
        if (tercero.value >= cuarto!!.value) {
            if (tercero.key == "Emergencia") {
                //Emergencia
                layoutparamsCirculos[0].width = numTercero
                layoutparamsCirculos[0].height = numTercero
                circuloEMergencia.layoutParams = layoutparamsCirculos[0]

            }
            if (tercero.key == "rojo") {
                //Rojo
                layoutparamsCirculos[1].width = numTercero
                layoutparamsCirculos[1].height = numTercero
                circuloRojo.layoutParams = layoutparamsCirculos[1]

            }
            if (tercero.key == "amarillo") {
                //Amarillo
                layoutparamsCirculos[2].width = numTercero
                layoutparamsCirculos[2].height = numTercero
                circuloAmarillo.layoutParams = layoutparamsCirculos[2]

            }
            if (tercero.key == "verde") {
                //Verde
                layoutparamsCirculos[3].width = numTercero
                layoutparamsCirculos[3].height = numTercero
                circuloVerde.layoutParams = layoutparamsCirculos[3]

            }
            if (tercero.key == "azul") {
                //Azul
                layoutparamsCirculos[4].width = numTercero
                layoutparamsCirculos[4].height = numTercero
                circuloAzul.layoutParams = layoutparamsCirculos[4]

            }
        }
        //-----------CUARTO CIRCULO-----------
        if (cuarto.value >= quinto!!.value) {
            if (cuarto.key == "Emergencia") {
                //Emergencia
                layoutparamsCirculos[0].width = numCuarto
                layoutparamsCirculos[0].height = numCuarto
                circuloEMergencia.layoutParams = layoutparamsCirculos[0]
            }
            if (cuarto.key == "rojo") {
                //Rojo
                layoutparamsCirculos[1].width = numCuarto
                layoutparamsCirculos[1].height = numCuarto
                circuloRojo.layoutParams = layoutparamsCirculos[1]
            }
            if (cuarto.key == "amarillo") {
                //Amarillo
                layoutparamsCirculos[2].width = numCuarto
                layoutparamsCirculos[2].height = numCuarto
                circuloAmarillo.layoutParams = layoutparamsCirculos[2]
            }
            if (cuarto.key == "verde") {
                //Verde
                layoutparamsCirculos[3].width = numCuarto
                layoutparamsCirculos[3].height = numCuarto
                circuloVerde.layoutParams = layoutparamsCirculos[3]
            }
            if (cuarto.key == "azul") {
                //Azul
                layoutparamsCirculos[4].width = numCuarto
                layoutparamsCirculos[4].height = numCuarto
                circuloAzul.layoutParams = layoutparamsCirculos[4]
            }
            //-----------QUINTO CIRCULO-----------
            if (quinto.key == "Emergencia") {
                //Emergencia
                layoutparamsCirculos[0].width = numQuinto
                layoutparamsCirculos[0].height = numQuinto
                circuloEMergencia.layoutParams = layoutparamsCirculos[0]
            }
            if (quinto.key == "rojo") {
                //Rojo
                layoutparamsCirculos[1].width = numQuinto
                layoutparamsCirculos[1].height = numQuinto
                circuloRojo.layoutParams = layoutparamsCirculos[1]
            }
            if (quinto.key == "amarillo") {
                //Amarillo
                layoutparamsCirculos[2].width = numQuinto
                layoutparamsCirculos[2].height = numQuinto
                circuloAmarillo.layoutParams = layoutparamsCirculos[2]
            }
            if (quinto.key == "verde") {
                //Verde
                layoutparamsCirculos[3].width = numQuinto
                layoutparamsCirculos[3].height = numQuinto
                circuloVerde.layoutParams = layoutparamsCirculos[3]
            }
            if (quinto.key == "azul") {
                //Azul
                layoutparamsCirculos[4].width = numQuinto
                layoutparamsCirculos[4].height = numQuinto
                circuloAzul.layoutParams = layoutparamsCirculos[4]
            }
        }

        //Si el numero de emergencia tiene mas de un hallazgo se cambia
        // a color blanco, ya que el fondo es negro
        if (conteoColorMapa[0] >= 1) {
            txtEmergencia.setTextColor(Color.WHITE)
            txtTituloEmergencia.setTextColor(Color.WHITE)
        }

        //Asigna los numeros correspondientes a los textViews
        txtEmergencia.text = removeLastNchars(conteoColorMapa[0].toString(), 1)
        txtNumRojo.text = removeLastNchars(conteoColorMapa[1].toString(), 1)
        txtNumAmarillo.text = removeLastNchars(conteoColorMapa[2].toString(), 1)
        txtNumVerde.text = removeLastNchars(conteoColorMapa[3].toString(), 1)
        txtNumAzul.text = removeLastNchars(conteoColorMapa[4].toString(), 1)

/*        txtEmergencia.text = removeLastNchars(listaPrueba[0].toString(), 1)
        txtNumRojo.text = removeLastNchars(listaPrueba[1].toString(), 1)
        txtNumAmarillo.text = removeLastNchars(listaPrueba[2].toString(), 1)
        txtNumVerde.text = removeLastNchars(listaPrueba[3].toString(), 1)
        txtNumAzul.text = removeLastNchars(listaPrueba[4].toString(), 1)*/

        //desaparece barra de carga
        progressBar.visibility = View.GONE
    }

    //Consulta SQL para obtencion de datos de los mapas
    suspend fun conteoColorMapa(): ArrayList<Int> {
        val nivelesAlertaAntesConvertir = arrayListOf<Int>()

        return withContext(Dispatchers.IO) {
            try {
                val query = "[dbo].[_1_Conteo_Mapa_HallazgosAlertas]"
                val statement = conexionSql.dbConn()?.prepareStatement(query)!!
                val resu = statement.executeQuery()

                if (resu.next()) {
                    for (i in 1..5) {
                        nivelesAlertaAntesConvertir.add(resu.getInt(i)) // Obtiene resultado de la consulta SQL y lo almacena en el array
                        val numero =
                            nivelesAlertaAntesConvertir[i - 1] //Almacena del array un numero | ejemplo : 2 (cantidad de nivel alerta : Grave)
                        val combinacion =
                            "$numero" + "0"               //Trae el numero y lo combina en modo String: "2" + "0" = "20"
                        val conversion =
                            combinacion.toInt()            //Convierte el string combinacion a Int. "20" = 20
                        nivelesAlertaAntesConvertir[i - 1] =
                            conversion // sustituye el numero 2 obtenido de nivelesAlertaAntesConvertir.add(resu.getInt(i)): 2 = 20
                    }
                }

            } catch (e: Exception) {
                e.printStackTrace()
            } catch (x: SQLException) {
                x.printStackTrace()
            }
            return@withContext nivelesAlertaAntesConvertir
        }

    }

    //Para eliminar ultimo caracter del numero almacenado en nivelesAlertaAntesConvertir
    //Asi en vez de salir "30" se elimina el ultimo caracter "0", quedando 3
    private fun removeLastNchars(str: String, n: Int): String {
        return str.substring(0, str.length - n)
    }

    fun mensaje(mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        coroutineScope.cancel()
        _binding = null
    }

    //Final
}