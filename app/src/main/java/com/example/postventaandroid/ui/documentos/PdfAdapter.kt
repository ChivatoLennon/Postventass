package com.example.postventaandroid.ui.documentos

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.postventaandroid.R
import com.google.android.material.floatingactionbutton.FloatingActionButton

class PdfAdapter (private val pdfs: ArrayList<String>, private val directorios : ArrayList<String>) : RecyclerView.Adapter<PdfAdapter.PdfAdapterViewHolder>() {

    class PdfAdapterViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val txtNombrePDf : TextView = itemView.findViewById(R.id.txtNombrePDf_Cardview_PDFList)
        val btnVer : FloatingActionButton = itemView.findViewById(R.id.btnVer_cardviewPDFList)
        val btnDescargar : FloatingActionButton = itemView.findViewById(R.id.btnDescargar_cardviewPDFList)

        fun mensaje(mensaje: String){
            Toast.makeText(itemView.context, mensaje, Toast.LENGTH_SHORT).show()
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PdfAdapter.PdfAdapterViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_pdf_list, parent, false)

        return PdfAdapterViewHolder(view)
    }

    override fun onBindViewHolder(holder: PdfAdapterViewHolder, position: Int) {
        //Lista de PDF en nombre
        val pdfList = pdfs[position]

        //Lista de los directorios del path de PDFs en assets
        val directorios = directorios[position]

        holder.txtNombrePDf.text = pdfList

        holder.btnVer.setOnClickListener(){
            val pdfModal = PdfModal(holder.itemView.context, directorios, null, null)
            //Muestra ventana flotante con la vista del PDF
            pdfModal.show()
            pdfModal.ocultarFirmar()
        }

        holder.btnDescargar.setOnClickListener(){
            //Añade funcion de descarga?
        }

    }

    override fun getItemCount(): Int {
        return pdfs.size
    }

}