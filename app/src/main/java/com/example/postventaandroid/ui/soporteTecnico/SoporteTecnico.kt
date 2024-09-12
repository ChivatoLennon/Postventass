package com.example.postventaandroid.ui.soporteTecnico

import androidx.fragment.app.viewModels
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.example.postventaandroid.R

class SoporteTecnico : Fragment() {

    companion object {
        fun newInstance() = SoporteTecnico()
    }

    private val viewModel: SoporteTecnicoViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // TODO: Use the ViewModel
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        return inflater.inflate(R.layout.fragment_soporte_tecnico, container, false)
    }
}