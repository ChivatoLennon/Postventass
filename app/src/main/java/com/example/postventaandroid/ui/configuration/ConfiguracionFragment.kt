package com.example.postventaandroid.ui.configuration

import android.os.Bundle
import androidx.preference.PreferenceFragmentCompat
import com.example.postventaandroid.R

//En desarrollo
class ConfiguracionFragment : PreferenceFragmentCompat() {

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.root_preferences, rootKey)
    }
}