package com.example.cinephile

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import com.google.android.material.materialswitch.MaterialSwitch

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val switchTheme = view.findViewById<MaterialSwitch>(R.id.switchTheme)

        // 1. Open Preferences
        val sharedPref = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        // 2. Check saved state (Default is true/Dark)
        val isDarkMode = sharedPref.getBoolean("IS_DARK_MODE", true)
        switchTheme.isChecked = isDarkMode

        // 3. Handle Click
        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableDarkMode(sharedPref)
            } else {
                enableLightMode(sharedPref)
            }
        }
    }

    private fun enableDarkMode(pref: SharedPreferences) {
        // Save to memory
        pref.edit().putBoolean("IS_DARK_MODE", true).apply()
        // Change App Theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        Toast.makeText(context, "Dark Mode Enabled", Toast.LENGTH_SHORT).show()
    }

    private fun enableLightMode(pref: SharedPreferences) {
        // Save to memory
        pref.edit().putBoolean("IS_DARK_MODE", false).apply()
        // Change App Theme
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        Toast.makeText(context, "Light Mode Enabled", Toast.LENGTH_SHORT).show()
    }
}