package com.example.cinephile

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController // Required for navigation
import com.google.android.material.materialswitch.MaterialSwitch

class SettingsFragment : Fragment(R.layout.fragment_settings) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Find Views
        val switchTheme = view.findViewById<MaterialSwitch>(R.id.switchTheme)
        val btnAuth = view.findViewById<Button>(R.id.btnAuthAction) // The new button

        // =================================================================
        // 1. THEME SWITCHING LOGIC
        // =================================================================
        val themePref = requireActivity().getSharedPreferences("AppSettings", Context.MODE_PRIVATE)

        val isDarkMode = themePref.getBoolean("IS_DARK_MODE", true)
        switchTheme.isChecked = isDarkMode

        switchTheme.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                enableDarkMode(themePref)
            } else {
                enableLightMode(themePref)
            }
        }

        // =================================================================
        // 2. AUTH LOGIC (Log In vs Log Out)
        // =================================================================

        // Open the User Session memory to check who is logged in
        val userPrefs = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val username = userPrefs.getString("KEY_USERNAME", "Guest")

        if (username == "Guest") {
            // --- SCENARIO A: User is a Guest ---
            btnAuth.text = "Log In / Sign Up"

            btnAuth.setOnClickListener {
                // Just navigate to Login Screen so they can create an account
                findNavController().navigate(R.id.action_settingsFragment_to_loginFragment)
            }

        } else {
            // --- SCENARIO B: User is Logged In (e.g., "John") ---
            btnAuth.text = "Log Out"

            btnAuth.setOnClickListener {
                // 1. Wipe the memory (Delete username)
                userPrefs.edit().clear().apply()

                // 2. Show confirmation
                Toast.makeText(context, "Logged out successfully", Toast.LENGTH_SHORT).show()

                // 3. Force user back to Login Screen
                findNavController().navigate(R.id.action_settingsFragment_to_loginFragment)
            }
        }
    }

    private fun enableDarkMode(pref: SharedPreferences) {
        pref.edit().putBoolean("IS_DARK_MODE", true).apply()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        Toast.makeText(context, "Dark Mode Enabled", Toast.LENGTH_SHORT).show()
    }

    private fun enableLightMode(pref: SharedPreferences) {
        pref.edit().putBoolean("IS_DARK_MODE", false).apply()
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        Toast.makeText(context, "Light Mode Enabled", Toast.LENGTH_SHORT).show()
    }
}