package com.example.postventaandroid.ui.perfil

import android.content.Intent
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.postventaandroid.R
import com.example.postventaandroid.ui.Data.GestionData
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.imageview.ShapeableImageView
import java.sql.Timestamp
import java.time.Duration

class GestionAdapter(private var gestiones: ArrayList<GestionData>) :
    RecyclerView.Adapter<GestionAdapter.GestionViewHolder>() {


    class GestionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val titulo_idTicket : TextView = itemView.findViewById(R.id.txtTitulo_idTicket_CardviewGestion)
        val titulo_fechaCreacion : TextView = itemView.findViewById(R.id.txtTitulo_fechaCreacion_CardviewGestion)
        val titulo_fechaVencimiento : TextView = itemView.findViewById(R.id.txtTitulo_fechaVencimiento_CardviewGestion)
        val titulo_estadoTicket : TextView = itemView.findViewById(R.id.txtTitulo_estadoTicket_CardviewGestion)

        val txtDetalleTicket: TextView =
            itemView.findViewById(R.id.txt_DetalleTicket_cardviewGestion)
        val txtIdGestion: TextView = itemView.findViewById(R.id.txt_gestionNo_cardviewGestion)
        val txtFechaCreacion: TextView = itemView.findViewById(R.id.txt_creadoFecha_cardviewGestion)
        val txtDeadline: TextView = itemView.findViewById(R.id.txt_deadline_cardviewGestion)
        val txtResponsable: TextView = itemView.findViewById(R.id.txt_responsable_cardviewGestion)
        val txtIdHallazgo: TextView = itemView.findViewById(R.id.txt_idHallazgo_cardviewGestion)
        val txtDerivado: TextView = itemView.findViewById(R.id.txt_derivado_cardviewGestion)
        val txtControlRiesgo: TextView =
            itemView.findViewById(R.id.txt_controlRiesgo_cardviewGestion)
        val txtEstadoGestion: TextView =
            itemView.findViewById(R.id.txt_EstadoGestion_cardviewGestion)
        val layoutVencimiento: LinearLayout =
            itemView.findViewById(R.id.linearLayout_Vencimiento_cardviewGestion)

        var cardViewItemGestion = itemView.findViewById<LinearLayout>(R.id.layoutCardview_CardviewGestion)

        val btnEditar = itemView.findViewById<FloatingActionButton>(R.id.btnEditar_cardviewGestion)

        val imgEstadoGestion = itemView.findViewById<ShapeableImageView>(R.id.img_iconEstado_cardViewGestion)

        fun mensaje(mensaje: String) {
            Toast.makeText(itemView.context, mensaje, Toast.LENGTH_SHORT).show()
        }

        fun irA_AccionesActivity(idTicket: Int, verificacion: Boolean, detalles: String, idAlerta : Int) {
            val intent = Intent(itemView.context, CheckActivity::class.java)
            //intent.putExtra("datos", datos)
            intent.putExtra("id", idTicket)
            intent.putExtra("verificacion", verificacion)
            intent.putExtra("detalleTicket", detalles)
            intent.putExtra("IdAlerta", idAlerta)
            itemView.context.startActivity(intent)
        }
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): GestionAdapter.GestionViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.cardview_gestion, parent, false)

        return GestionAdapter.GestionViewHolder(view)
    }

    override fun onBindViewHolder(holder: GestionAdapter.GestionViewHolder, position: Int) {
        //itera la posicion de la lista
        val item = gestiones[position]
        val id = item.iD_Gestion

        holder.txtIdGestion.text = item.iD_Gestion.toString()
        holder.txtFechaCreacion.text = item.fechaInicial.toString()
        holder.txtDeadline.text = item.deadLine.toString()
        holder.txtResponsable.text = item.responsable
        holder.txtIdHallazgo.text = item.hallazgoID.toString()
        holder.txtDerivado.text = item.deadline_derivacion
        holder.txtControlRiesgo.text = item.controlRiesgo
        holder.txtEstadoGestion.text = item.EstadoGestion
        holder.txtDetalleTicket.text = item.detalle

        //guarda en array todos los textos
        val arrayText = arrayListOf(
            holder.txtIdGestion.text,
            holder.txtFechaCreacion.text,
            holder.txtDeadline.text,
            holder.txtResponsable.text,
            holder.txtIdHallazgo.text,
            holder.txtDerivado.text,
            holder.txtControlRiesgo.text,
            holder.txtEstadoGestion.text,
            holder.txtDetalleTicket.text,
        )
        //Setea textos al array
        val arraySet = arrayListOf(
            item.iD_Gestion.toString(),
            item.fechaInicial.toString(),
            item.deadLine.toString(),
            item.responsable,
            item.hallazgoID.toString(),
            item.deadline_derivacion,
            item.controlRiesgo,
            item.EstadoGestion,
            item.detalle
        )
        //color para los textos
        val color = arrayListOf(
            holder.txtIdGestion,
            holder.txtFechaCreacion,
            holder.txtDeadline,
            holder.txtEstadoGestion
        )

        //guarda en array los titulos de algunos textos
        val arrayTitulo = arrayListOf(
            holder.titulo_idTicket,
            holder.titulo_fechaCreacion,
            holder.titulo_fechaVencimiento,
            holder.titulo_estadoTicket
        )

        //button para ir a la pantalla de edicion
        holder.btnEditar.setOnClickListener() {
            holder.irA_AccionesActivity(
                id,
                false,
                item.detalle,
                item.hallazgoID
            ) //false si es para editar la gestion seleccionada
        }

        val fechaInicial = item.fechaInicial    //Fecha creada de la gestion
        val fechaActual = Timestamp(System.currentTimeMillis()) //FECHA ACTUAL DEL DISPOSITIVO
        val deadLine = item.deadLine            //Fecha deadline (Final) para atender gestion

        val duration = Duration.between(
            fechaActual.toInstant(),
            deadLine.toInstant()
        ) //Duration para añadir fecha completo año, mes, dia, hora, minuto, segs preciso

        val days = duration.toDays().toInt() //Calculo para obtencion del dia
        val hours = (duration.toHours() % 24).toInt() //Calculo para obtencion de la hora
        val minutes = (duration.toMinutes() % 60).toInt()//Calculo para obtencion de la minuto
        //val seconds = (duration.seconds % 60).toInt()

        if (fechaInicial == deadLine) {
            //Si tienen las mismas fechas de creacion y deadline significa que fue derivado o no aplica
            holder.cardViewItemGestion.setBackgroundColor(Color.WHITE) // Derivado o no aplica
            holder.layoutVencimiento.visibility = View.GONE

            holder.cardViewItemGestion.setOnClickListener() {
                holder.mensaje(item.deadline_derivacion)
            }
            for(i in color.indices){
                color[i].setTextColor(Color.BLACK)
            }
            for(i in arrayTitulo.indices){
                arrayTitulo[i].setTextColor(Color.BLACK)
            }

        } else if (fechaActual.after(deadLine)) { //Si es mayor HOY a deadline, ha vencido la gestion
            // Vencido rojo
            holder.cardViewItemGestion.setBackgroundColor(Color.rgb(214, 58, 58))
            holder.layoutVencimiento.visibility = View.VISIBLE
            holder.cardViewItemGestion.setOnClickListener() {
                holder.mensaje("Vencido por $days dias, $hours horas y $minutes minutos")
            }
            for (i in arrayText.indices){
                arrayText[i] = arraySet[i]
            }
            for(i in arrayTitulo.indices){
                arrayTitulo[i].setTextColor(Color.WHITE)
            }
            for(i in color.indices){
                color[i].setTextColor(Color.WHITE)
            }
        } else if (fechaActual.before(deadLine)) {    //Si es menor HOY que deadline, aun queda tiempo antes de que venza
            when {
                days == 2 && hours < 24 -> {
                    holder.cardViewItemGestion.setBackgroundColor(
                        Color.rgb(
                            129,
                            222,
                            73
                        )
                    ) // Verde, 3 dias para vencer
                    holder.cardViewItemGestion.setOnClickListener() {
                        holder.mensaje("Vence en $days dias, $hours horas y $minutes minutos")
                    }
                    for(i in arrayTitulo.indices){
                        arrayTitulo[i].setTextColor(Color.BLACK)
                    }
                    for(i in color.indices){
                        color[i].setTextColor(Color.BLACK)
                    }
                }

                days == 1 && hours < 24 -> {
                    holder.cardViewItemGestion.setBackgroundColor(
                        Color.rgb(
                            221,
                            212,
                            36
                        )
                    ) // Amarillo, 2 dias para vencer
                    holder.cardViewItemGestion.setOnClickListener() {
                        holder.mensaje("Vence en $days dias, $hours horas y $minutes minutos")
                    }
                    for(i in arrayTitulo.indices){
                        arrayTitulo[i].setTextColor(Color.BLACK)
                    }
                    for(i in color.indices){
                        color[i].setTextColor(Color.BLACK)
                    }
                }

                days == 0 && hours < 24 -> {
                    holder.cardViewItemGestion.setBackgroundColor(
                        Color.rgb(
                            229,
                            88,
                            28
                        )
                    ) // Naranjo, 1 solo dia para vencer
                    holder.cardViewItemGestion.setOnClickListener() {
                        holder.mensaje("Vence en ${hours.toInt()} horas y ${minutes.toInt()} minutos")
                    }
                    for(i in arrayTitulo.indices){
                        arrayTitulo[i].setTextColor(Color.WHITE)
                    }
                    for(i in color.indices){
                        color[i].setTextColor(Color.WHITE)
                    }
                }

                else -> {
                    //Nada
                }
            }
        }

        //Asignamos icono dependiendo del estado del ticket
        when (item.EstadoGestion) {
            "Abierto" -> {
                holder.imgEstadoGestion.setImageResource(R.drawable.candado_abierto_icon)
            }

            "Pendiente" -> {
                holder.imgEstadoGestion.setImageResource(R.drawable.pendiente_icon)
            }

            "Resuelto" -> {
                holder.imgEstadoGestion.setImageResource(R.drawable.resuelto_icon)
            }

            "Cerrado" -> {
                holder.imgEstadoGestion.setImageResource(R.drawable.candado_cerrado_icon)
            }

            else -> {
                //Nothing
            }
        }

    }

    fun actualizarLista(nuevaLista: ArrayList<GestionData>) {
        gestiones = nuevaLista
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return gestiones.size
    }


}