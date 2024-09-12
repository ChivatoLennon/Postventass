package com.example.postventaandroid.ui.login

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.DialogFragment
import androidx.lifecycle.lifecycleScope
import com.example.postventaandroid.R
import com.example.postventaandroid.databinding.FragmentOlvidoContrasenaDialogBinding
import com.example.postventaandroid.ui.Data.ConnSQL
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.sql.SQLException

//Clase de la pantalla flotante que recupera contraseña del correo
class olvidoContrasenaDialog : DialogFragment() {

    private val conexion = ConnSQL()

    private lateinit var progressBar: ProgressBar

    private lateinit var txtContrasenaRecuperada: TextView
    private lateinit var txtLabelContrasena: TextView
    private lateinit var txtCorreo: EditText

    private lateinit var btnRecuperar: Button

    private var _binding: FragmentOlvidoContrasenaDialogBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentOlvidoContrasenaDialogBinding.inflate(inflater, container, false)
        val root: View = binding.root

        txtCorreo = root.findViewById(R.id.txtCorreo_olvidoContrasena_dialogFragment)

        progressBar = root.findViewById(R.id.progressBar_olvidoContrasenaDialogFragment)
        progressBar.visibility = View.GONE

        txtLabelContrasena = root.findViewById(R.id.txtLabelContrasena_olvidoContrasenaDialogFragment)
        txtContrasenaRecuperada = root.findViewById(R.id.txtContrasenaRecuperada_olvidoContrasena_dialogFragment)

        txtLabelContrasena.visibility = View.GONE
        txtContrasenaRecuperada.visibility = View.GONE

        btnRecuperar = root.findViewById(R.id.btnRecuperar_olvidoContrasenaDialog)

        btnRecuperar.setOnClickListener() {
            RecuperarClaveFinal()
        }

        return root
    }

    private fun RecuperarClaveFinal() {
        progressBar.visibility = View.VISIBLE
        lifecycleScope.launch {
            if (RecuperarClaveSQL() == "") {
                txtLabelContrasena.text = "No existe el correo"
                txtLabelContrasena.visibility = View.VISIBLE
                progressBar.visibility = View.GONE
                delay(2000)
                txtLabelContrasena.visibility = View.GONE
                txtLabelContrasena.text = "Su contraseña es:"
                txtContrasenaRecuperada.visibility = View.GONE
            } else {
                progressBar.visibility = View.GONE
                txtLabelContrasena.text = "Su contraseña es:"
                txtLabelContrasena.visibility = View.VISIBLE
                txtContrasenaRecuperada.text = RecuperarClaveSQL()
                txtContrasenaRecuperada.visibility = View.VISIBLE
            }
        }
    }

    suspend private fun RecuperarClaveSQL(): String {
        var contrasenaRecuperada = ""
        return withContext(Dispatchers.IO) {
            try {
                val query = "[dbo].[_1_recuperarContrasena] ?"
                val statement = conexion.dbConn()!!.prepareStatement(query)
                statement.setString(1, txtCorreo.text.toString())
                val resu = statement.executeQuery()
                if (resu.next()) {
                    contrasenaRecuperada = resu.getString(1)
                } else contrasenaRecuperada = ""

            } catch (e: Exception) {
                e.printStackTrace()
            } catch (x: SQLException) {
                x.printStackTrace()
            }

            return@withContext contrasenaRecuperada
        }
    }

}