package com.example.postventaandroid.ui.documentos

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.example.postventaandroid.R
import com.example.postventaandroid.databinding.ActivitySignatureBinding
import com.itextpdf.text.Image
import com.itextpdf.text.Rectangle
import com.itextpdf.text.pdf.PdfReader
import com.itextpdf.text.pdf.PdfStamper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream

//Clase de la pantalla donde el usuario dibuja la firma
class SignatureActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignatureBinding
    private lateinit var signatureView: SignatureView
    private val PERMISSION_REQUEST_CODE = 1001

    private lateinit var progressBar: ProgressBar

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignatureBinding.inflate(layoutInflater)
        setContentView(binding.root)

        signatureView = findViewById(R.id.signatureView)
        progressBar = findViewById(R.id.progressBar_SignatureActivity)
        progressBar.visibility = View.GONE

        binding.btnBorrarSignatureActivity.setOnClickListener() {
            binding.signatureView.clear()
        }

        binding.btnGuardarSignatureActivity.setOnClickListener() {
            progressBar.visibility = View.VISIBLE
            if (signatureView.isSignatureEmpty()) {
                progressBar.visibility = View.GONE
                Toast.makeText(this, "Por favor, ingrese el dibujo de la firma", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }else{
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    checkAndRequestPermission(getStringPDF())
                    progressBar.visibility = View.GONE
                } else {
                    onSaveSignatureClicked(getStringPDF(), getNombreRegistrar())
                    progressBar.visibility = View.GONE
                }
            }

        }

    }

    //obtiene el numero respectivo de seleccion de leer terminos o ver politicas. De la pantalla RegistroActivity
    private fun getNumTermino(): Int {
        val termino: Int = intent.getIntExtra("numTermino", 2)
        return termino
    }

    //obtener nombre de la pantalla registrar, aunque no se esta usando
    private fun getNombreRegistrar(): String {
        val nombre: String = intent.getStringExtra("nombreRegistrar")!!
        return nombre
    }

    //nombre del pdf obtenido
    private fun getStringPDF(): String {
        val pdf: String = intent.getStringExtra("nombreArchivoPdf")!!
        return pdf
    }

    //permisos de almacenamiento
    private fun checkAndRequestPermission(archivo: String) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                PERMISSION_REQUEST_CODE
            )
        } else {
            onSaveSignatureClicked(archivo, getNombreRegistrar())
        }
    }

    //Permisos de almacenamiento
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CODE -> {
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    onSaveSignatureClicked(getStringPDF(), getNombreRegistrar())
                } else {
                    Toast.makeText(
                        this,
                        "Permiso denegado. No se puede guardar el PDF.",
                        Toast.LENGTH_LONG
                    ).show()
                }
                return
            }
        }
    }


    //copia del archivo pdf de asset a archivo
    private fun copyAssetToFile(assetName: String): String {
        val outputFile = File(getExternalFilesDir(null), assetName)
        if (!outputFile.exists()) {
            assets.open(assetName).use { input ->
                FileOutputStream(outputFile).use { output ->
                    input.copyTo(output)
                }
            }
        }
        return outputFile.absolutePath
    }

    //Inicio del guardado de la firma en el pdf
    private fun onSaveSignatureClicked(archivo: String, nombreRegistrar: String?) {
        val signatureBitmap = signatureView.getSignatureBitmap()

        val inputPath = copyAssetToFile(archivo)
        //val outputPath = getExternalFilesDir(null)?.absolutePath + "/PDFFirmado.pdf"
        val stringNombrePDFFirmado = "PDFFirmado${nombreRegistrar}$archivo"
        val fileName = stringNombrePDFFirmado

        lifecycleScope.launch {
            savePdfToDownloads(inputPath, signatureBitmap, fileName, getNumTermino())
        }
    }

    //guarda el pdf a la carpeta descarga de cada dispositivo
    suspend private fun savePdfToDownloads(
        inputPath: String,
        signatureBitmap: Bitmap,
        fileName: String,
        numTermino: Int
    ) {
        try {
            /*if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveToDownloads29ParaArriba(inputPath, signatureBitmap, fileName)
            } else {
                saveToDownloads28ParaABajo(inputPath, signatureBitmap, fileName)
            }*/

            lifecycleScope.launch {
                generateAndSavePdf(inputPath, fileName)
            }

            when (numTermino) {
                0 -> {
                    Toast.makeText(this, "Guardando... ", Toast.LENGTH_LONG).show()
                    val resultIntent = Intent()
                    resultIntent.putExtra("firmaPDF", numTermino)
                    setResult(Activity.RESULT_OK, resultIntent)
                    Thread.sleep(1500)
                    //mensaje("PDF Terminos. Guardado en  carpeta Descargas")
                    finish() // Termina SignatureActivity

                }
                1 -> {
                    Toast.makeText(this, "Guardando... ", Toast.LENGTH_LONG).show()
                    val resultIntent = Intent()
                    resultIntent.putExtra("firmaPDF", numTermino)
                    setResult(Activity.RESULT_OK, resultIntent)
                    Thread.sleep(1500)
                    //mensaje("PDF Politicas. Guardado en  carpeta Descargas")
                    finish() // Termina SignatureActivity

                }
                else -> {
                    //nothing
                }
            }


        } catch (e: Exception) {
            Toast.makeText(this, "Error al guardar el PDF: ${e.message}", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }
    }

    //Guardado de pdf si es API 29 hacia arriba
    @SuppressLint("NewApi")
    private fun saveToDownloads29ParaArriba(
        inputPath: String,
        signatureBitmap: Bitmap,
        fileName: String
    ) {
        val contentValues = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val uri = contentResolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues)
        if (uri != null) {
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                processAndSavePdf(inputPath, signatureBitmap, outputStream)
            }
        }
    }

    //Guardado de pdf si es API 28 hacia abajo
    private fun saveToDownloads28ParaABajo(
        inputPath: String,
        signatureBitmap: Bitmap,
        fileName: String
    ) {
        val downloadsDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        val file = File(downloadsDir, fileName)
        FileOutputStream(file).use { outputStream ->
            processAndSavePdf(inputPath, signatureBitmap, outputStream)
        }
    }

    //PRoceso de guardado y modificacion de la firma en el PDF
    private fun processAndSavePdf(inputPath: String, signatureBitmap: Bitmap, outputStream: OutputStream) {
        // Lee el PDF existente desde el inputPath
        val reader = PdfReader(inputPath)
        // Crea un PdfStamper para agregar contenido al PDF y escribir el resultado en outputStream
        val stamper = PdfStamper(reader, outputStream)

        // Define el área donde se colocará la firma en la primera página del PDF
        val rect = Rectangle(400f, 100f, 500f, 300f) // Cambia las coordenadas según sea necesario

        // Crea una imagen a partir del bitmap de la firma
        val image = Image.getInstance(signatureBitmap.toByteArray())

        // Ajusta el tamaño de la imagen al área definida por el rectángulo
        image.scaleToFit(rect.width, rect.height)
        // Establece la posición de la imagen dentro del rectángulo
        image.setAbsolutePosition(rect.left, rect.bottom)

        // Obtiene el contenido de la primera página donde se agregará la firma
        val overContent = stamper.getOverContent(2)
        // Agrega la imagen de la firma al contenido de la primera página
        overContent.addImage(image)

        // Cierra el PdfStamper y el PdfReader para finalizar la escritura del PDF
        stamper.close()
        reader.close()
    }

    //Bitmap a bytearray
    private fun Bitmap.toByteArray(): ByteArray {
        val stream = ByteArrayOutputStream()
        this.compress(Bitmap.CompressFormat.PNG, 100, stream)
        return stream.toByteArray()
    }

    //Genera y guarda el pdf
    private suspend fun generateAndSavePdf(inputPath: String, fileName: String) {
        withContext(Dispatchers.Default) {
            // Generar el bitmap de la firma
            val signatureBitmap = signatureView.getSignatureBitmap()

            // Guardar el PDF
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                saveToDownloads29ParaArriba(inputPath, signatureBitmap, fileName)
            } else {
                saveToDownloads28ParaABajo(inputPath, signatureBitmap, fileName)
            }
        }
    }

    private fun mensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }

}