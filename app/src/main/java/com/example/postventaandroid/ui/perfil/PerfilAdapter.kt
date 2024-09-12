package com.example.postventaandroid.ui.perfil

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.postventaandroid.R
import com.example.postventaandroid.ui.Data.HallazgoData

open class PerfilAdapter(private var hallazgos: ArrayList<HallazgoData>) : RecyclerView.Adapter<PerfilAdapter.HallazgoViewHolder>() {

    //SOLO Variables y Funciones aqui!!
    class HallazgoViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val txtID_Hallazgo: TextView = itemView.findViewById(R.id.txt_reporteId_cardviewPerfil)
        val txtNivelAlerta: TextView = itemView.findViewById(R.id.txt_nivelAlerta_cardviewPerfil)
        val txtFecha: TextView = itemView.findViewById(R.id.txt_fecha_cardviewPerfil)
        val txtSector : TextView = itemView.findViewById(R.id.txt_sector_cardviewPerfil)
        val txtArea : TextView = itemView.findViewById(R.id.txtArea_cardview_miPerfil)
        val txtActividad : TextView = itemView.findViewById(R.id.txtActividad_cardview_miPerfil)
        val txtRiesgoRC : TextView = itemView.findViewById(R.id.txtRiesgoRC_cardview_miPerfil)
        val txtReportadoPor : TextView = itemView.findViewById(R.id.txtReportadoPor_cardview_miPerfil)
        val txtResponsable : TextView = itemView.findViewById(R.id.txtResponsable_cardview_miPerfil)
        val txtDescripcion : TextView = itemView.findViewById(R.id.txtDescripcion_cardview_miPerfil)
        val btnCrearTicket : Button = itemView.findViewById(R.id.btnCrearTicket_cardView_MiPerfil)

        val imgHallazgo : ImageView = itemView.findViewById(R.id.img_cardviewMiPerfil)

        val imgBtn: ImageButton = itemView.findViewById(R.id.btnImg_flecha_cardviewMiPerfil)
        val gridLayout : GridLayout = itemView.findViewById(R.id.gridLayout_desplegable_cardviewMiPerfil)

        fun mensaje(mensaje: String){
            Toast.makeText(itemView.context, mensaje, Toast.LENGTH_SHORT).show()
        }

        fun irA_AccionesActivity(datos: Int, verificacion: Boolean) {
            val intent = Intent(itemView.context, CheckActivity::class.java)
            //intent.putExtra("datos", datos)
            intent.putExtra("id", datos)
            intent.putExtra("verificacion", verificacion)
            itemView.context.startActivity(intent)
        }

        fun byteArrayToBitmap(data: ByteArray): Bitmap {
            return BitmapFactory.decodeByteArray(data, 0, data.size)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HallazgoViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.cardview_miperfil, parent, false)

        return HallazgoViewHolder(view)
    }

    //Aqui en onBindViewHolder con el holder, asignamos los elementos que necesitamos ver en el cardview
    //Y las funciones que creemos en HallazgoViewHolder

    //Aplicar funciones acá
    override fun onBindViewHolder(holder: HallazgoViewHolder, position: Int) {

        //itera la posicion de la lista
        val hallazgo = hallazgos[position]

        val ByteArrayFromDB = hallazgo.bitmapFoto
        val bitmap = holder.byteArrayToBitmap(ByteArrayFromDB)

        holder.txtID_Hallazgo.text = hallazgo.ID_Hallazgo.toString()
        val id = hallazgo.ID_Hallazgo
        holder.txtNivelAlerta.text = hallazgo.NivelAlerta
        holder.txtFecha.text = hallazgo.Fecha.toString()
        holder.txtSector.text = hallazgo.Sector
        holder.txtArea.text = hallazgo.Area
        holder.txtActividad.text = hallazgo.Actividad
        holder.txtRiesgoRC.text = hallazgo.Riesgo_RC
        holder.txtReportadoPor.text = hallazgo.Reportado_Por
        holder.txtResponsable.text = hallazgo.userId.toString()
        holder.txtDescripcion.text = hallazgo.Descripcion
        holder.imgHallazgo.setImageBitmap(bitmap)

        holder.gridLayout.visibility = View.GONE
        holder.imgBtn.setOnClickListener(){
            if (holder.gridLayout.visibility == View.GONE){
                holder.gridLayout.visibility = View.VISIBLE
                holder.imgBtn.setBackgroundResource(R.drawable.flecha_menu_abajo)
            }else{
                holder.gridLayout.visibility = View.GONE
                holder.imgBtn.setBackgroundResource(R.drawable.flecha_menu_arriba)
            }
        }

        holder.btnCrearTicket.setOnClickListener(){
            holder.irA_AccionesActivity(id, true)
        }

    }

    fun actualizarLista(nuevaLista: ArrayList<HallazgoData>){
        hallazgos = nuevaLista
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return hallazgos.size
    }

    //FINAL

}
