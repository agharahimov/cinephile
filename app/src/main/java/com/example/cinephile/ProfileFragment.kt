package com.example.cinephile

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.cinephile.util.GuestManager

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // =================================================================
        // 1. DISPLAY USERNAME
        // =================================================================
        val tvTitle = view.findViewById<TextView>(R.id.tvProfileTitle)

        // Open the same "memory box" we used in LoginFragment
        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)

        // Get the name (default to "Guest" if nothing is found)
        val savedUsername = sharedPref.getString("KEY_USERNAME", "Guest")

        // Update the UI
        tvTitle.text = "Welcome, $savedUsername"

        // =================================================================
        // 2. BUTTON NAVIGATION
        // =================================================================

        // Navigate to Watchlist
        view.findViewById<View>(R.id.btnGoToWatchlist).setOnClickListener {
            // Check if Guest -> Block Access
            GuestManager.checkAndRun(requireContext(), findNavController()) {
                findNavController().navigate(R.id.action_profileFragment_to_watchlistFragment)
            }
        }


        // Navigate to Settings
        view.findViewById<View>(R.id.btnGoToSettings).setOnClickListener {
            try {
                findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
            } catch (e: Exception) {
                // Fallback if you haven't created the SettingsFragment in nav_graph yet
                Toast.makeText(context, "Settings coming soon!", Toast.LENGTH_SHORT).show()
            }
        }
        view.findViewById<View>(R.id.btnGoToFavorites).setOnClickListener {
            // Check if Guest -> Block Access
            GuestManager.checkAndRun(requireContext(), findNavController()) {
                findNavController().navigate(R.id.action_profileFragment_to_favoritesFragment)
            }
        }

    }
}