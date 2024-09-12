package com.example.postventaandroid.ui.detalleEvento

import android.annotation.SuppressLint
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import androidx.navigation.fragment.findNavController
import com.example.postventaandroid.R
import com.example.postventaandroid.databinding.ActivityAccionesBinding
import com.example.postventaandroid.databinding.ActivitySignatureBinding
import com.example.postventaandroid.ui.Data.ConnSQL
import com.example.postventaandroid.ui.Data.HallazgoData
import java.sql.SQLException

//AVISO
//Esta clase no se esta usando en nada, la dejo por si le daran uso

//Logica del boton Acciones del DialogFragment DetalleEvento, extendido hacia el Interface para cerrar activity
class AccionesActivity : AppCompatActivity() {


    private lateinit var binding: ActivityAccionesBinding

    private var numeroID = 0
    private val conn = ConnSQL()


    @SuppressLint("ClickableViewAccessibility")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityAccionesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)

        //Oculta el teclado al hacer clic fuera de este
        val main = findViewById<View>(R.id.main)
        main.setOnTouchListener { _, _ ->
            val inputMethodManager =
                getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            inputMethodManager.hideSoftInputFromWindow(main.windowToken, 0)
            false
        }

        //Son 14
        val txtIDHallazgo: TextView = findViewById(R.id.txtIDHallazgo_AccionesActivity)
        val txtSector: TextView = findViewById(R.id.txtSector_AccionesActivity)
        val txtSupervisor: TextView = findViewById(R.id.txtSupervisor_AccionesActivity)
        val txtNivelAlerta: TextView = findViewById(R.id.txtNivelAlerta_AccionesActivity)
        val txtDescripcion: TextView = findViewById(R.id.txtDescripcion_AccionesActivity)
        val txtVerificado: TextView = findViewById(R.id.txtVerificado_AccionesActivity)
        val txtFecha: TextView = findViewById(R.id.txtFecha_AccionesActivity)
        val txtEstadoCierre: TextView = findViewById(R.id.txtEstadoCierre_AccionesActivity)
        val txtProyectoMina: TextView = findViewById(R.id.txtProyectoMina_AccionesActivity)
        val txtArea: TextView = findViewById(R.id.txtArea_AccionesActivity)
        val txtActividad: TextView = findViewById(R.id.txtActividad_AccionesActivity)
        val txtRiesgoRC: TextView = findViewById(R.id.txtRiesgoRC_AccionesActivity)
        val txtReportadoPor: TextView = findViewById(R.id.txtReportadoPor_AccionesActivity)
        val txtResponsable: TextView = findViewById(R.id.txtResponsable_AccionesActivity)

        val btnEditar: Button = findViewById(R.id.btnEditar_AccionesActivity)
        val btnEliminar: Button = findViewById(R.id.btnEliminar_AccionesActivity)
        val btnSI: Button = findViewById(R.id.btnSI_Eliminacion_ActivityAcciones)
        val btnNO: Button = findViewById(R.id.btnNO_Eliminacion_ActivityAcciones)
        val linearLayout: LinearLayout = findViewById(R.id.layoutBtns_ActivityAcciones)


        //Obtenemos los datos del item seleccionado en el DetalleEvento
        val bundle = intent.extras
        val datos = bundle?.getSerializable("datos") as HallazgoData
        numeroID = datos.ID_Hallazgo


        txtIDHallazgo.text = datos.ID_Hallazgo.toString()
        txtSector.text = datos.Sector
        txtSupervisor.text = datos.Supervisor
        txtNivelAlerta.text = datos.NivelAlerta
        txtDescripcion.text = datos.Descripcion
        val verificacionParse = if (datos.verificacion == true) {
            "Verificado"
        } else {
            "No Verificado"
        }
        txtVerificado.text = verificacionParse
        txtFecha.text = datos.Fecha.toString()
        txtEstadoCierre.text = datos.EstadoCierre
        txtProyectoMina.text = datos.Proyecto_Mina
        txtArea.text = datos.Area
        txtActividad.text = datos.Actividad
        txtRiesgoRC.text = datos.Riesgo_RC
        txtReportadoPor.text = datos.Reportado_Por
        txtResponsable.text = datos.userId.toString()

        linearLayout.visibility = View.GONE

        // Datos a enviar al Fragment
        val data = datos.ID_Hallazgo

        // Crea una instancia de Editar_DetalleEvento con los datos
        /*val myFragment = Editar_DetalleEvento.newInstance(data)
        var confContainerEditar_DetalleEvento = 0
        btnEditar.setOnClickListener() {
            //Variable para confirmar el btn editar y que aparezca o desaparezca el Fragment
            var texto = if(confContainerEditar_DetalleEvento == 0){
                "Cancelar Cambios"
            }else{
                "Editar"
            }
            if (savedInstanceState == null && confContainerEditar_DetalleEvento == 0) {
                myFragment.setOnCloseButtonClickListener(this) //INstanciamos el metodo
                // Iniciar la transacción para agregar o reemplazar el Fragment
                supportFragmentManager.beginTransaction()
                    .replace(
                        R.id.fragment_container_EditarDetalleEvento_AccionesActivity,
                        myFragment
                    ) // Reemplaza cualquier fragmento existente en el contenedor
                    .commit()
                confContainerEditar_DetalleEvento = 1
                btnEditar.text = texto
            } else {
                btnEditar.text = texto
                confContainerEditar_DetalleEvento = 0
                val transaction = supportFragmentManager.beginTransaction()
                transaction.remove(myFragment)
                transaction.commit()
            }
        }*/

        btnEliminar.setOnClickListener() {
            linearLayout.visibility = View.VISIBLE
        }

        btnSI.setOnClickListener() {
            eliminarFilaPorID(datos.ID_Hallazgo)
            linearLayout.visibility = View.GONE
            this.onBackPressed()
        }
        btnNO.setOnClickListener() {
            linearLayout.visibility = View.GONE
        }


    }

    fun eliminarFilaPorID(id_alerta: Int) {
        try {

            val query = "[dbo].[_1_EliminarAlerta_porID] ?"
            val conexion = conn.dbConn() ?: throw SQLException("Error conexion BD")
            val statement = conexion.prepareStatement(query)
            statement.setInt(1, id_alerta)
            val resu = statement.executeUpdate()

            if (resu > 0) {
                mensaje("Alerta eliminada")
            } else {
                mensaje("No se encontraron filas para eliminar.")
            }

        } catch (e: Exception) {
            e.printStackTrace()
        } catch (x: SQLException) {
            x.printStackTrace()
        } finally {
            conn.closeConecction()
        }


    }



    fun mensaje(mensaje: String) {
        Toast.makeText(this, mensaje, Toast.LENGTH_SHORT).show()
    }
}

