package com.example.postventaandroid.ui.detalleEvento

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.GridLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.FragmentActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.postventaandroid.R
import com.example.postventaandroid.ui.Data.HallazgoData
import com.google.android.material.imageview.ShapeableImageView

class MyAdapterDetalleEvento(
    private var hallazgoList: ArrayList<HallazgoData>,
    private val listener: OnItemClickListener?
) : RecyclerView.Adapter<MyAdapterDetalleEvento.MyViewHolder>() {



    fun actualizarLista(nuevaLista: ArrayList<HallazgoData>) {
        hallazgoList = nuevaLista
        notifyDataSetChanged()
    }

    //Infla el layout, en este caso el cardview
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.cardview_item_hallazgo, parent, false)
        return MyViewHolder(itemView)
    }

    interface OnItemClickListener {
        fun onUpdateClick(position: Int, hallazgo: HallazgoData)
        fun onDeleteClick(position: Int)
        fun onButtonClick(position: Int)
    }

    override fun getItemCount(): Int {
        return hallazgoList.size
    }

    //Aqui en onBindViewHolder con el holder, asignamos los elementos que necesitamos ver en el cardview
    //Y las funciones que creemos en MyViewHolder
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {

        //Itera cada Item actual de la lista
        val currentItem = hallazgoList[position]

        holder.reporteID.text = currentItem.ID_Hallazgo.toString()
        holder.riesgo.text = currentItem.NivelAlerta
        holder.descripcion.text = currentItem.Descripcion

        val datosParaCardView = HallazgoData(
            currentItem.ID_Hallazgo,
            currentItem.Sector,
            currentItem.Supervisor,
            currentItem.NivelAlerta,
            currentItem.Descripcion,
            currentItem.bitmapFoto,
            currentItem.verificacion,
            currentItem.Fecha,
            currentItem.EstadoCierre,
            currentItem.Proyecto_Mina,
            currentItem.Area,
            currentItem.Actividad,
            currentItem.Riesgo_RC,
            currentItem.Reportado_Por,
            currentItem.userId
        )

        //Icono de exclamacion si la alerta no se le ha creado un ticket
        if(!currentItem.verificacion){
            holder.imgExclamacion.visibility = View.VISIBLE
        }else holder.imgExclamacion.visibility = View.GONE

        //boton ver
        holder.btnCardview_Ver.setOnClickListener() {
            val fragmentManager =
                (holder.itemView.context as FragmentActivity).supportFragmentManager
            val dialogFragment = DetalleEvento.comunicacionDetalleEvento(datosParaCardView)
            //Mostramos la ventana flotante (dialog)
            dialogFragment.show(fragmentManager, "detalle_evento_dialog")

        }
    }

    //SOLO Variables y Funciones aqui!!
    inner class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //Aqui entran las variables y funciones
        //Para que funcione todo lo del cardview
        var btnCardview_Ver = itemView.findViewById<Button>(R.id.btn_verHallazgo_cardviewHallazgo)
        var cardViewItemHallazgo = itemView.findViewById<GridLayout>(R.id.layoutCardview_CardviewItemHallazgo)

        var reporteID: TextView = itemView.findViewById(R.id.txt_reporteId_cardview)
        var riesgo: TextView = itemView.findViewById(R.id.txt_riesgo_cardviewHallazgo)
        var descripcion: TextView = itemView.findViewById(R.id.txt_Descripcion_cardviewHallazgo)

        var imgExclamacion : ShapeableImageView = itemView.findViewById(R.id.imgExclamacion_cardview_ItemHallazgo)

        fun mensaje(mensaje: String) {
            Toast.makeText(itemView.context, mensaje, Toast.LENGTH_SHORT).show()
        }


    }


}