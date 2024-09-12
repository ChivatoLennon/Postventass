package com.example.postventaandroid.ui.gallery


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.postventaandroid.databinding.FragmentGalleryBinding
import com.example.postventaandroid.R
import com.example.postventaandroid.ui.Data.ConnSQL
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.sql.PreparedStatement
import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.view.inputmethod.InputMethodManager
import android.widget.ProgressBar
import androidx.lifecycle.lifecycleScope
import com.example.postventaandroid.ui.Data.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

//Logica pantalla para ingresar alertas a la BD
class GalleryFragment : Fragment() {

    private val PERMISO_CAMARA: Int = 99
    private var conexionSql = ConnSQL()

    private lateinit var progressBar: ProgressBar

    private lateinit var sessionManager: SessionManager

    //Estado para saber si la variable img de cameraResultLauncher es null(vacia)
    //Y validarlo en la funcion buttonEnviarDatos.setOnClickListener
    private var confirmacionImg = 0

    //para validar EnviarDatos() de los spinner de supervisor, sector y nivelAlerta
    var confSector = 0
    var confSup = 0
    var confAlerta = 0
    var confProyectoMina = 0
    var confRiesgoAsociado = 0
    var confArea = 0

    private lateinit var buttonEnviarDatos: Button
    private lateinit var buttonTomarFoto: Button
    private lateinit var buttonSubirImg: Button
    private lateinit var imgAlerta: ImageView

    private var opcionSpinnerAlerta: String = ""
    private var opcionSpinnerSector: String = ""
    private var opcionSpinnerSupervisor: String = ""
    private var opcionSpinnerProyectoMina: String = ""
    private var opcionSpinnerRiesgoAsociado: String = ""
    private var opcionSpinnerArea : String = ""

    private lateinit var txtDescripcion: EditText
    private lateinit var spinnerArea: Spinner
    private lateinit var txtActividad: EditText
    private lateinit var txtReportadoPor: EditText
    private lateinit var txtEstadoCierre: EditText


    private var IdAsignadoSupervisor = 0

    private val idSupervisores = listOf(
        7, //Mauricio
        6,  //HEctor
        8
    )

    //item de cada spinner para mostrar en el layout xml
    val itemsSpinner = listOf(
        //Nivel de alerta
        listOf(
            "Nivel de la Alerta a reportar...",
            "OP - Oportunidad de Mejora",
            "HP - Hallazgo Positivo",
            "N2 - Hallazgo leve",
            "N1 - Hallazgo Grave",
            "EMERGENCIA"
        ),
        //Supervisores
        listOf(
            "Asignar alerta al supervisor...",
            "Mauricio",
            "Hector",
            "Miguel"
        ),
        //Sectores
        listOf(
            "Sector de la Alerta...",
            "Loop de Transporte NTI",
            "Barrio Civico",
            "Taller NTI",
            "TC-TAP"
        ),
        //Proyecto/mina
        listOf(
            "Seleccione Proyecto o Mina...",
            "CONSTRUCCIÓN PROYECTO EXPLOTACIÓN MINA ANDES NORTE"
        ),
        //Riesgo_RC
        listOf(
            "Seleccione nombre de riesgo asociado...",
            "RC01 - Interacción con energía eléctrica",
            "RC02 - Pérdida de equilibrio trabajo en altura física",
            "RC03 - Pérdida de control maniobras de izaje",
            "RC04 - Pérdida de control de energía hidráulica y neumática a alta presión",
        ),
        //Areas
        listOf(
            "Asignar Area...",
            "1",
            "2",
            "3",
            "4"
        )
    )

    //lanzamiento de la camara
    val cameraResultLauncher =
        registerForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
            // Aquí recibe el bitmap de la foto tomada con la cámara
            val img: Bitmap? = bitmap
            if (img == null) {
                confirmacionImg = 0
                imgAlerta.setImageResource(R.drawable.img_view_subirdatos_foto)
            } else {
                imgAlerta.setImageBitmap(img)
                confirmacionImg = 1
                imgAlerta.setBackgroundColor(Color.WHITE)
            }

        }

    //Para subir img desde el mismo telefono
    private var uriToBitmap: Bitmap? = null
    val galleryResultLauncher =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            // Aquí recibe la URI de la imagen seleccionada de la galería
            //Y la asigna a la variable destinada al ImageView del layout "imgSubida"
            imgAlerta.setImageURI(uri)
            uriToBitmap = uri?.let { getBitmapFromUri(uri, requireContext()) }
            imgAlerta.setBackgroundColor(Color.WHITE)
        }

    private var _binding: FragmentGalleryBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val galleryViewModel = ViewModelProvider(this).get(GalleryViewModel::class.java)

        _binding = FragmentGalleryBinding.inflate(inflater, container, false)
        val root: View = binding.root
        val layout = root.findViewById<View>(R.id.id_galleryFragmentLayout)

        //Oculta el teclado al hacer clic fuera de este
        layout.setOnTouchListener { _, _ ->
            val inputMethodManager =
                requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(layout.windowToken, 0)
            false
        }

        //inicializa el sessionManager
        sessionManager = SessionManager(root.context)
        progressBar = root.findViewById(R.id.progressBar_GalleryFragment)
        progressBar.visibility = View.GONE

        //Inicializamos variables
        buttonEnviarDatos = root.findViewById(R.id.button_enviarDatos_galleryFragment)
        //buttonSubirImg = root.findViewById(R.id.buttonSubirImagen_galleryFragment)
        buttonTomarFoto = root.findViewById(R.id.button_tomarFoto_galleryFragment)

        imgAlerta = root.findViewById(R.id.imgAlerta_galleryFragment)
        imgAlerta.setBackgroundResource(R.drawable.img_view_subirdatos_foto)

        txtActividad = root.findViewById(R.id.editTxt_Actividad_galleryFragment)
        txtDescripcion = root.findViewById(R.id.editTxt_descripcion_galleryFragment)
        spinnerArea = root.findViewById(R.id.spinner_Area_galleryFragment)
        txtEstadoCierre = root.findViewById(R.id.editTxt_EstadoCierre_galleryFragment)

        txtReportadoPor = root.findViewById(R.id.editTxt_ReportadoPor_galleryFragment)
        val textReporte = "Alerta reportada por: " + sessionManager.fetchUser()?.nombre.toString()
        txtReportadoPor.setText(textReporte)

        val spinnerSector: Spinner = root.findViewById(R.id.spinner_Sector_galleryFragment)
        val spinnerSupervisor: Spinner = root.findViewById(R.id.spinner_Supervisor_galleryFragment)
        val spinnerNivelAlerta: Spinner =
            root.findViewById(R.id.spinner_nivelAlerta_galleryFragment)
        val spinnerProyectoMina: Spinner =
            root.findViewById(R.id.spinner_ProyectoMina_galleryFragment)
        val spinnerRiesgoAsociado: Spinner =
            root.findViewById(R.id.spinner_RC_RiesgoAsociado_galleryFragment)
        //val spinnerResponsable : Spinner = root.findViewById(R.id.spinner_Responsable_galleryFragment)

        val cuadroColor: View = root.findViewById(R.id.colorSquare)

        //Adaptadores spinners
        val adapterNivelAlerta =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, itemsSpinner[0])
        adapterNivelAlerta.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerNivelAlerta.adapter = adapterNivelAlerta

        val adapterSupervisor =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, itemsSpinner[1])
        adapterSupervisor.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSupervisor.adapter = adapterSupervisor

        val adapterSector =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, itemsSpinner[2])
        adapterSector.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerSector.adapter = adapterSector

        val adapterProyectoMina =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, itemsSpinner[3])
        adapterProyectoMina.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerProyectoMina.adapter = adapterProyectoMina

        val adapterRiesgoAsociado =
            ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, itemsSpinner[4])
        adapterRiesgoAsociado.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerRiesgoAsociado.adapter = adapterRiesgoAsociado

        val AdapterArea = ArrayAdapter(requireContext(), android.R.layout.simple_spinner_item, itemsSpinner[5])
        AdapterArea.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerArea.adapter = AdapterArea

        var indiceSeleccionado: Int

        spinnerNivelAlerta.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {

                indiceSeleccionado = position
                val itemSeleccionada = itemsSpinner[0][indiceSeleccionado]
                spinnerAlertaOptions(
                    itemSeleccionada,
                    cuadroColor,
                    indiceSeleccionado
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Manejo cuando no se selecciona nada
            }
        }

        spinnerSupervisor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {

                indiceSeleccionado = position
                val itemSeleccionada = itemsSpinner[1][indiceSeleccionado]
                spinnerSupervisorOptions(
                    itemSeleccionada,
                    indiceSeleccionado
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Manejo cuando no se selecciona nada
            }
        }

        spinnerSector.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?, view: View?, position: Int, id: Long
            ) {

                indiceSeleccionado = position
                val itemSeleccionada = itemsSpinner[2][indiceSeleccionado]
                spinnerSectorOptions(
                    itemSeleccionada,
                    indiceSeleccionado
                )
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                // Manejo cuando no se selecciona nada
            }
        }

        spinnerProyectoMina.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                indiceSeleccionado = position
                val itemSeleccionada = itemsSpinner[3][indiceSeleccionado]
                spinnerProyectoMinaOptions(itemSeleccionada, indiceSeleccionado)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        spinnerRiesgoAsociado.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                indiceSeleccionado = position
                val itemSeleccionada = itemsSpinner[4][indiceSeleccionado]
                spinnerRiesgoAsociadoOptions(itemSeleccionada, indiceSeleccionado)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        spinnerArea.onItemSelectedListener = object : AdapterView.OnItemSelectedListener{
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                indiceSeleccionado = position
                val itemSeleccionada = itemsSpinner[5][indiceSeleccionado]
                spinnerAreaOptions(itemSeleccionada, indiceSeleccionado)
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                TODO("Not yet implemented")
            }
        }

        buttonTomarFoto.setOnClickListener {
            //TomarFoto(requireView())
            solicitarPermisos()
        }

        /*buttonSubirImg.setOnClickListener {
            openGallery()
        }*/

        buttonEnviarDatos.setOnClickListener {
            lifecycleScope.launch {
                if (validarEnviar()) {
                    mostrarMensaje("Faltan datos por ingresar")
                } else {
                    progressBar.visibility = View.VISIBLE
                    if (subirSql() == 1) {
                        mostrarMensaje("Foto subida correctamente")
                        limpiarCampos(
                            spinnerSector,
                            spinnerSupervisor,
                            spinnerNivelAlerta,
                            spinnerProyectoMina,
                            spinnerRiesgoAsociado,
                            spinnerArea)
                            /*spinnerResponsable*/
                        progressBar.visibility = View.GONE

                    } else {
                        mostrarMensaje("Error al subir foto")
                        progressBar.visibility = View.GONE
                    }
                }
            }

        }

        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun openCamera() {
        cameraResultLauncher.launch(null)
    }


    private fun openGallery() {
        galleryResultLauncher.launch("image/*")
    }

    fun spinnerAlertaOptions(
        itemSeleccionada: String,
        cuadroColor: View,
        indiceSeleccionado: Int
    ) {
        // Imprimes el índice y el contenido
        println("Índice seleccionado: $indiceSeleccionado, itemSeleccionado: $itemSeleccionada")
        when (indiceSeleccionado) {
            0 -> {
                cuadroColor.setBackgroundColor(Color.WHITE)
                opcionSpinnerAlerta = "No ingresado"
                confAlerta = 0
            }

            1 -> {
                cuadroColor.setBackgroundColor(Color.rgb(6, 229, 236))
                opcionSpinnerAlerta = itemSeleccionada
                confAlerta = 1
            }

            2 -> {
                cuadroColor.setBackgroundColor(Color.rgb(6, 229, 41))
                opcionSpinnerAlerta = itemSeleccionada
                confAlerta = 1
            }

            3 -> {
                cuadroColor.setBackgroundColor(Color.rgb(245, 229, 54))
                opcionSpinnerAlerta = itemSeleccionada
                confAlerta = 1
            }

            4 -> {
                cuadroColor.setBackgroundColor(Color.rgb(255, 0, 0))
                opcionSpinnerAlerta = itemSeleccionada
                confAlerta = 1
            }

            5 -> {
                cuadroColor.setBackgroundResource(R.drawable.emergency_icon)
                opcionSpinnerAlerta = itemSeleccionada
                confAlerta = 1
            }
        }
    }

    fun spinnerSupervisorOptions(
        itemSeleccionada: String,
        indiceSeleccionado: Int
    ) {
        // Imprimes el índice y el contenido
        println("Índice seleccionado: $indiceSeleccionado, itemSeleccionado: $itemSeleccionada")
        when (indiceSeleccionado) {
            0 -> {
                opcionSpinnerSupervisor = "No ingresado"
                confSup = 0

            }

            1 -> {
                //"Mauricio"
                opcionSpinnerSupervisor = itemSeleccionada
                confSup = 1
                IdAsignadoSupervisor = idSupervisores[0]
            }

            2 -> {
                //"Hector"
                opcionSpinnerSupervisor = itemSeleccionada
                confSup = 1
                IdAsignadoSupervisor = idSupervisores[1]
            }

            3 -> {
                //"Miguel"
                opcionSpinnerSupervisor = itemSeleccionada
                confSup = 1
                IdAsignadoSupervisor = idSupervisores[2]
            }
        }
    }

    fun spinnerSectorOptions(
        itemSeleccionada: String,
        indiceSeleccionado: Int
    ) {
        // Imprimes el índice y el contenido
        println("Índice seleccionado: $indiceSeleccionado, itemSeleccionado: $itemSeleccionada")
        when (indiceSeleccionado) {
            0 -> {
                opcionSpinnerSector = "No ingresado"
                confSector = 0
            }

            1 -> {
                //"Loop de Transporte NTI"
                opcionSpinnerSector = itemSeleccionada
                confSector = 1
            }

            2 -> {
                //"Barrio Civico"
                opcionSpinnerSector = itemSeleccionada
                confSector = 1
            }

            3 -> {
                //"Taller NTI"
                opcionSpinnerSector = itemSeleccionada
                confSector = 1
            }

            4 -> {
                //"TC-TAP"
                opcionSpinnerSector = itemSeleccionada
                confSector = 1
            }
        }
    }

    fun spinnerProyectoMinaOptions(itemSeleccionada: String, indiceSeleccionado: Int) {
        when (indiceSeleccionado) {
            0 -> {
                opcionSpinnerProyectoMina = "No ingresado"
                confProyectoMina = 0
            }

            1 -> {
                opcionSpinnerProyectoMina = itemSeleccionada
                confProyectoMina = 1
            }
            //Añadir mas si hay mas proyectos o Minas
        }
    }

    fun spinnerRiesgoAsociadoOptions(itemSeleccionada: String, indiceSeleccionado: Int) {
        when (indiceSeleccionado) {
            0 -> {
                opcionSpinnerRiesgoAsociado = "No ingresado"
                confRiesgoAsociado = 0
            }

            1 -> {
                opcionSpinnerRiesgoAsociado = itemSeleccionada
                confRiesgoAsociado = 1
            }

            2 -> {
                opcionSpinnerRiesgoAsociado = itemSeleccionada
                confRiesgoAsociado = 1
            }

            3 -> {
                opcionSpinnerRiesgoAsociado = itemSeleccionada
                confRiesgoAsociado = 1
            }

            4 -> {
                opcionSpinnerRiesgoAsociado = itemSeleccionada
                confRiesgoAsociado = 1
            }
            //Añadir mas si hay mas Riesgos asociados
        }
    }

    fun spinnerAreaOptions(itemSeleccionada: String, indiceSeleccionado: Int) {
        when (indiceSeleccionado) {
            0 -> {
                opcionSpinnerArea = "No ingresado"
                confArea = 0
            }

            1 -> {
                opcionSpinnerArea = itemSeleccionada
                confArea = 1
            }

            2 -> {
                opcionSpinnerArea = itemSeleccionada
                confArea = 1
            }

            3 -> {
                opcionSpinnerArea = itemSeleccionada
                confArea = 1
            }

            4 -> {
                opcionSpinnerArea = itemSeleccionada
                confArea = 1
            }
            //Añadir mas si hay mas Areas
        }
    }

    //Se crea funcion para devolver un byteArray para poder subir
    //a la base de datos.
    //Es necesario que en la bd SQL, el campo de la tabla se "varbinary(max)"
    private fun imageToByteArrayFoto(): ByteArray {
        val drawable =
            imgAlerta.drawable as BitmapDrawable  //imageView sería el id que le das a tu ImageView
        val bitmap = drawable.bitmap
        val stream = ByteArrayOutputStream()
        // El número '50' es el valor de la compresión que desea del bitmap.
        // A menos valor menor calidad pero menos peso.
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, stream)

        return stream.toByteArray()
    }

    fun getBitmapFromUri(uri: Uri, context: Context): Bitmap? {
        try {
            val parcelFileDescriptor = context.contentResolver.openFileDescriptor(uri, "r")
            val fileDescriptor = parcelFileDescriptor?.fileDescriptor
            val image = BitmapFactory.decodeFileDescriptor(fileDescriptor)
            parcelFileDescriptor?.close()
            return image
        } catch (e: IOException) {
            e.printStackTrace()
        }
        return null
    }


    suspend private fun subirSql(): Int {
        return withContext(Dispatchers.IO) {
            var num = 0
            try {
                val fotoArray = imageToByteArrayFoto()
                val uriByte = uriToBitmap

                //consulta INSERT
                val query: PreparedStatement = conexionSql.dbConn()
                    ?.prepareStatement("RecibirHallazgo ?,?,?,?,?,?,?,?,?,?,?")!!

                query.setString(1, opcionSpinnerSector)
                query.setString(2, opcionSpinnerSupervisor)
                query.setString(3, opcionSpinnerAlerta)
                query.setString(4, txtDescripcion.text.toString())
                query.setBytes(5, fotoArray)
                //query.setString(6, txtEstadoCierre.text.toString())
                query.setString(6, opcionSpinnerProyectoMina)
                query.setString(7, opcionSpinnerArea)
                query.setString(8, txtActividad.text.toString())
                query.setString(9, opcionSpinnerRiesgoAsociado)
                query.setString(10, sessionManager.fetchUser()?.nombre.toString())
                query.setInt(11, IdAsignadoSupervisor)

                // Ejecutar el procedimiento almacenado
                val resultado = query.execute()
                if (!resultado) {
                    //Foto subida
                    num = 1
                } else {
                    //Foto no subida
                    num = 0
                }
            } catch (e: Exception) {
                Log.w(
                    "ErrorSubirFoto",
                    "Error al hacer insert en la base de datos SQL Server. \n\n $e"
                )
            } finally {
                conexionSql.dbConn()?.close()
            }

            return@withContext num
        }
    }


    fun limpiarCampos(
        spinnerSector: Spinner,
        spinnerSupervisor: Spinner,
        spinnerAlerta: Spinner,
        spinnerProyectoMina: Spinner,
        spinnerRiesgoAsociado: Spinner,
        spinnerArea : Spinner
    ) {
        imgAlerta.setImageResource(R.drawable.img_view_subirdatos_foto)
        txtDescripcion.setText("")
        spinnerArea.setSelection(0)
        txtActividad.setText("")
        txtEstadoCierre.setText("")
        spinnerAlerta.setSelection(0)
        spinnerSupervisor.setSelection(0)
        spinnerSector.setSelection(0)
        spinnerProyectoMina.setSelection(0)
        spinnerRiesgoAsociado.setSelection(0)
        //spinnerResponsable.setSelection(0)
    }

    private fun solicitarPermisos() {
        when {
            ContextCompat.checkSelfPermission(
                requireActivity(), Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                openCamera()
            }

            shouldShowRequestPermissionRationale(Manifest.permission.CAMERA) -> {
                mostrarMensaje("El permiso fue rechazado, habilite en los Ajustes")
            }

            else -> {
                requestPermissions(arrayOf(Manifest.permission.CAMERA), PERMISO_CAMARA)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISO_CAMARA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    openCamera()
                }
            }

            else -> {
                super.onRequestPermissionsResult(requestCode, permissions, grantResults)
            }
        }

    }

    fun mostrarMensaje(mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }

    //valida los campos y spinner vacios
    fun validarEnviar(): Boolean {
        if (confSector == 0 || confSup == 0 || txtDescripcion.text.toString().isEmpty()
            || confAlerta == 0 || imgAlerta.drawable == null
            || confirmacionImg == 0 || confProyectoMina == 0
            || confRiesgoAsociado == 0 || txtActividad.text.isEmpty()
            || confArea == 0  || txtReportadoPor.text.isEmpty()
        ) {
            return true
        } else {
            return false
        }
    }



    //FINAL
}