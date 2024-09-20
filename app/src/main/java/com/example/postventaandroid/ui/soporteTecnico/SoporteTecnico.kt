package com.example.postventaandroid.ui.soporteTecnico

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import com.example.postventaandroid.R

class SoporteTecnico : Fragment() {

    private lateinit var imgViewHallazgo: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val view = inflater.inflate(R.layout.fragment_soporte_tecnico, container, false)

        // Inicializar el ImageView
        imgViewHallazgo = view.findViewById(R.id.imgview_hallazgo_detalleEvento)

        // Manejar el clic del botón para seleccionar una imagen
        val buttonAttachImage = view.findViewById<View>(R.id.button_attach_image)
        buttonAttachImage.setOnClickListener {
            openGallery()
        }

        return view
    }

    // Registrar el Activity Result para manejar la selección de la imagen
    private val selectImageLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val data: Intent? = result.data
            val selectedImageUri: Uri? = data?.data

            // Si se seleccionó una imagen, mostrarla en el ImageView
            if (selectedImageUri != null) {
                imgViewHallazgo.setImageURI(selectedImageUri)
            } else {
                Toast.makeText(requireContext(), "No se seleccionó ninguna imagen", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Función para abrir la galería
    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK)
        intent.type = "image/*"
        selectImageLauncher.launch(intent)
    }
}
