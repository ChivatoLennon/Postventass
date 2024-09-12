package com.example.postventaandroid.ui.documentos

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.postventaandroid.R
import com.example.postventaandroid.databinding.FragmentDocumentosBinding

//Clase de la pantalla politicas empresa
class DocumentosFragment : Fragment() {

    private var _binding: FragmentDocumentosBinding? = null

    //Listview
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: PdfAdapter


    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {/*val slideshowViewModel =
            ViewModelProvider(this).get(SlideshowViewModel::class.java)*/

        _binding = FragmentDocumentosBinding.inflate(inflater, container, false)
        val root: View = binding.root

        //Inicializamos recyclerView
        recyclerView = root.findViewById(R.id.listView_DocumentosFragment)
        recyclerView.layoutManager = LinearLayoutManager(activity)
        recyclerView.setHasFixedSize(true)


        //DocumentoPDF como directorios tienen que coincidir Indices
        //para que sea el mismo documento que el nombre
        val documentosPDF = arrayListOf(
            "Política Empresa ejemplo PDF - appCodelco",
            "Terminos y Condiciones Ejemplo - appCodelco.pdf",
            "ejemplo_PDF"
        )

        val directorios = arrayListOf(
            "Política Empresa ejemplo PDF - appCodelco.pdf",
            "Terminos y Condiciones Ejemplo - appCodelco.pdf",
            "ejemplo_PDF.pdf"
        )

        //Adapter para desplegar lista en la pantalla
        adapter = PdfAdapter(documentosPDF, directorios)
        recyclerView.adapter = adapter

        //Solicitud de permisos Almacenamiento
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.UPSIDE_DOWN_CAKE) {
            if (checkPermission()){
                //Aprobados
            }else{
                requestPermissions()
            }
        }
        return root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }


    private fun mensaje(mensaje: String) {
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }


    //Permiso de acceso read y write de almacenamiento
    private fun checkPermission(): Boolean {

        val permission1 = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.WRITE_EXTERNAL_STORAGE
        )
        val permission2 = ContextCompat.checkSelfPermission(
            requireContext(),
            Manifest.permission.READ_EXTERNAL_STORAGE
        )

        return permission1 == PackageManager.PERMISSION_GRANTED && permission2 == PackageManager.PERMISSION_GRANTED
    }


    //solicita  //Permiso de acceso read y write de almacenamiento
    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf<String>(
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ),
            200
        )
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 200) {
            if (grantResults.isNotEmpty()) {
                val readStorage = grantResults[0] == PackageManager.PERMISSION_GRANTED
                val writeStorage = grantResults[1] == PackageManager.PERMISSION_GRANTED
                if (writeStorage && readStorage) {
                    mensaje("Permiso concedido")
                } else {
                    mensaje("Permiso Denegado")
                }
            }
        }
    }


}