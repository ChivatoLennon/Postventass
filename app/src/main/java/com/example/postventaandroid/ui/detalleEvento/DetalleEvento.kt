package com.example.postventaandroid.ui.detalleEvento

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.ViewModelProvider
import com.example.postventaandroid.R
import com.example.postventaandroid.databinding.FragmentDetalleEventoBinding
import com.example.postventaandroid.ui.Data.HallazgoData

//DialogFragment para crear un popup en el HistorialFragment
//Y poder ver la informacion del cardview seleccionado en las listas de historial de cada alerta
class DetalleEvento : DialogFragment(), MyAdapterDetalleEvento.OnItemClickListener {

    private var _binding: FragmentDetalleEventoBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    private lateinit var imagenA_subir: ImageView
    private lateinit var txtIdReporte: TextView
    private lateinit var txtSector: TextView
    private lateinit var txtSupervisor: TextView
    private lateinit var txtNivelAlerta: TextView
    private lateinit var txtDescripcion: TextView
    private lateinit var txtVerificado: TextView
    private lateinit var txtFecha : TextView
    private lateinit var txtEstadoCierre: TextView
    private lateinit var txtProyectoMina: TextView
    private lateinit var txtArea: TextView
    private lateinit var txtActividad : TextView
    private lateinit var txtRiesgoRC : TextView
    private lateinit var txtReportadoPor: TextView
    private lateinit var txtResponsable : TextView

    private lateinit var linearLayoutBotonesOcultos : LinearLayout


    //obtenemos datos
    //almacena los datos de comunicacionDetalleEvento obtenidos de HallazgoData
    // ya iniciado en HistorialFragment
    companion object {
        fun comunicacionDetalleEvento(datos: HallazgoData): DetalleEvento {
            val fragment = DetalleEvento()
            val args = Bundle()
            //HallazgoData implementa Serializable
            args.putSerializable("datos", datos)
            fragment.arguments = args
            return fragment
        }
    }




    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(DetalleEventoViewModel::class.java)

        _binding = FragmentDetalleEventoBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //Datos como array de HallazgoData
        var datos = arguments?.getSerializable("datos") as HallazgoData

        //Oculta el teclado al hacer clic fuera de este
        val layout = root.findViewById<View>(R.id.id_detalleEvento_Scrollview)
        layout.setOnTouchListener { _, _ ->
            val inputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(layout.windowToken, 0)
            false
        }

        //Todos los detalles de cada hallazgo
        txtIdReporte = root.findViewById(R.id.txtReporte_detalleEvento)
        txtSector = root.findViewById(R.id.txtSector_detalleEvento)
        txtSupervisor = root.findViewById(R.id.txtSupervisor_detalleEvento)
        txtNivelAlerta = root.findViewById(R.id.txtNivelAlerta_detalleEvento)
        txtDescripcion = root.findViewById(R.id.txtDescripcion_detalleEvento)
        imagenA_subir = root.findViewById(R.id.imgview_hallazgo_detalleEvento)
        txtVerificado = root.findViewById(R.id.txtVerificado_detalleEvento)
        txtFecha = root.findViewById(R.id.txtFecha_detalleEvento)
        txtEstadoCierre = root.findViewById(R.id.txtEstadoCierre_detalleEvento)
        txtProyectoMina = root.findViewById(R.id.txtProyectoMina_detalleEvento)
        txtArea = root.findViewById(R.id.txtArea_detalleEvento)
        txtActividad = root.findViewById(R.id.txtActividad_detalleEvento)
        txtRiesgoRC = root.findViewById(R.id.txtRiesgo_RC_detalleEvento)
        txtReportadoPor = root.findViewById(R.id.txtReportadoPor_detalleEvento)
        txtResponsable = root.findViewById(R.id.txtResponsable_detalleEvento)

        recibirDatos(datos)

        return root
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    fun recibirDatos(datos : HallazgoData){
        //si la Verificacion es 0(false), es falso, si es 1(true), esta verificado
        var verificadoString = if(!datos.verificacion){
            "No revisado"
        }else{
            "Revisado"
        }
        try {
            txtIdReporte.text = datos.ID_Hallazgo.toString()
            txtSector.text = datos.Sector
            txtSupervisor.text = datos.Supervisor
            txtNivelAlerta.text = datos.NivelAlerta
            txtDescripcion.text = datos.Descripcion
            txtVerificado.text = verificadoString
            txtFecha.text = removeLastNchars(datos.Fecha.toString(), 5)
            txtEstadoCierre.text = datos.EstadoCierre
            txtProyectoMina.text = datos.Proyecto_Mina
            txtArea.text = datos.Area
            txtActividad.text = datos.Actividad
            txtRiesgoRC.text = datos.Riesgo_RC
            txtReportadoPor.text = datos.Reportado_Por
            txtResponsable.text = datos.userId.toString()

            val ByteArrayFromDB = datos.bitmapFoto
            val bitmap = byteArrayToBitmap(ByteArrayFromDB)
            imagenA_subir.setImageBitmap(bitmap)

        } catch (e: Exception) {

            Log.w("Error recibirDatos()", "Error: revise recibirDatos, DetalleEvento(). \n\n $e")

        }
    }


    fun mensaje(mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }

    //Funcion para recibir un ArrayByte y transformarlo a bitmap para asignarlo a un imageview
    fun byteArrayToBitmap(data: ByteArray): Bitmap {
        return BitmapFactory.decodeByteArray(data, 0, data.size)
    }

    private fun removeLastNchars(str: String, n: Int): String {
        return str.substring(0, str.length - n)
    }


    override fun onButtonClick(position: Int) {
        TODO("Not yet implemented")

    }

    override fun onUpdateClick(position: Int, hallazgo: HallazgoData) {
        TODO("Not yet implemented")
    }

    override fun onDeleteClick(position: Int) {
        TODO("Not yet implemented")
    }
}