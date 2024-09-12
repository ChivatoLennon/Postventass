package com.example.postventaandroid.ui.documentos


import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.EditText
import android.widget.Toast
import com.example.postventaandroid.R
import com.example.postventaandroid.ui.login.Registro_pantalla
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.pdfview.PDFView
import java.io.File
import java.io.FileOutputStream
import java.io.IOException


//Ventana flotante DIalog para mostrar PDF, firmar y descargar
class PdfModal(context: Context, directorio: String, nombreRegistrar : String?, termino : Int?) : Dialog(context) {

    private var directorioo = directorio
    private var nombreRegistrarr = nombreRegistrar
    private var terminoo = termino
    private val assetManager = context.assets
    private var btnFirmar: FloatingActionButton
    private var btnDescargarPdf: FloatingActionButton
    private var txtNombrePDF : EditText

    init {
        setContentView(R.layout.view_pdf)
        btnFirmar = findViewById(R.id.btnFirmar_PDFModal)
        btnFirmar.setOnClickListener {
            when (termino){
                0 ->{
                    // Abrimos pantalla de registro de firma para terminos y condiciones
                    //E ingresamos datos a la pantalla de firmas signatureActivity
                    val intent = Intent(context, SignatureActivity::class.java)
                    intent.putExtra("numTermino", termino)
                    intent.putExtra("nombreArchivoPdf", directorioo)
                    intent.putExtra("nombreRegistrar", nombreRegistrarr)
                    (context as? Registro_pantalla)?.signatureActivityResultLauncher?.launch(intent)
                    dismiss() // Opcional: cierra el diálogo si es necesario
                }
                1 ->{
                    // Abrimos pantalla de registro de firma para politicas de la empresa
                    //E ingresamos datos a la pantalla de firmas signatureActivity
                    val intent = Intent(context, SignatureActivity::class.java)
                    intent.putExtra("numTermino", termino)
                    intent.putExtra("nombreArchivoPdf", directorioo)
                    intent.putExtra("nombreRegistrar", nombreRegistrarr)
                    (context as? Registro_pantalla)?.signatureActivityResultLauncher?.launch(intent)
                    dismiss() // Opcional: cierra el diálogo si es necesario
                }
            }

        }

        btnDescargarPdf = findViewById(R.id.btnDescargar_PDFModal)
        txtNombrePDF = findViewById(R.id.txtNombrePDF_aSubir_view_pdfFragment)


        // Busca el ID del PDFView en el layout
        val pdfView = findViewById<PDFView>(R.id.pdfView)

        // Muestra PDF desde el directorio assets
        pdfView.fromAsset(directorioo).show()

        btnDescargarPdf.setOnClickListener {
            val nombrePDF = txtNombrePDF.text.toString()
            if(nombrePDF.isEmpty()){
                mensaje("Ingrese un nombre primero")
            }else{
                // Copiar el PDF desde assets al almacenamiento interno
                savePdfToInternalStorage(directorioo, nombrePDF)
                dismiss()
            }
        }
    }

    // Función para copiar el PDF a la carpeta Downloads o Descargas
    private fun savePdfToInternalStorage(assetFileName: String, outputFileName: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // Para Android 10 y versiones superiores
            val contentResolver = context.contentResolver
            val contentValues = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, outputFileName)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, "Download/")
            }

            val uri =
                contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)

            uri?.let {
                try {
                    val inputStream = assetManager.open(assetFileName)
                    contentResolver.openOutputStream(it).use { outputStream ->
                        inputStream.use { input ->
                            input.copyTo(outputStream!!)
                        }
                    }

                    // Notificación o mensaje de éxito
                    mensaje("PDF guardado en Downloads")
                } catch (e: IOException) {
                    e.printStackTrace()
                    // Manejar el error, por ejemplo, mostrando un Toast o logueando el error
                    mensaje("Error al guardar el PDF")
                }
            }
        } else {
            // Para versiones anteriores a Android 10
            val downloadsDir =
                Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val outputFile = File(downloadsDir, outputFileName)

            try {
                val inputStream = assetManager.open(assetFileName)
                FileOutputStream(outputFile).use { outputStream ->
                    inputStream.use { input ->
                        input.copyTo(outputStream)
                    }
                }

                // Notificación o mensaje de éxito
                mensaje("PDF guardado en: ${outputFile.absolutePath}")
            } catch (e: IOException) {
                e.printStackTrace()
                // Manejar el error, por ejemplo, mostrando un Toast o logueando el error
                mensaje("Error al guardar el PDF")
            }
        }
    }



    fun ocultarBotones() {
        btnFirmar.visibility = View.GONE
        btnDescargarPdf.visibility = View.GONE
        txtNombrePDF.visibility = View.GONE
    }

    fun ocultarBtnDescargaPDF() {
        btnDescargarPdf.visibility = View.GONE
        txtNombrePDF.visibility = View.GONE
    }

    fun ocultarFirmar(){
        btnFirmar.visibility = View.GONE
    }

    fun mensaje(mensaje: String){
        Toast.makeText(context, mensaje, Toast.LENGTH_SHORT).show()
    }

}

/*val assetManager = context.assets

private  var btnFirmar : FloatingActionButton
private  var btnDescargarPdf : FloatingActionButton

init {
    setContentView(R.layout.view_pdf)

    val inputStream = assetManager.open(directorio)
    val file = File(getExternalStorageDirectory(), "nuevo_PDF.pdf") // Cambia el nombre del archivo según prefieras



    btnFirmar = findViewById(R.id.btnFirmar_PDFModal)
    btnDescargarPdf = findViewById(R.id.btnDescargar_PDFModal)

    //Busca ID del PDFView en el layout
    val pdfView = findViewById<PDFView>(R.id.pdfView)

    //Muestra PDF desde el directorio assets


    btnFirmar.setOnClickListener(){
        //Abrimos pantalla de registro de firma
        val intent = Intent(context, SignatureActivity::class.java)
        context.startActivity(intent)
    }

    btnDescargarPdf.setOnClickListener(){

        try {
            inputStream.use { input ->
                file.outputStream().use { output ->
                    input.copyTo(output)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }


    }

    pdfView.fromAsset(directorio).show()

}*/
