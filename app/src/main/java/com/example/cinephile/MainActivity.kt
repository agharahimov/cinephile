package com.example.cinephile

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // Splash Screen
        val splashScreen = installSplashScreen()
        var keepSplashOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }
        Handler(Looper.getMainLooper()).postDelayed({ keepSplashOnScreen = false }, 1000)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Navigation Setup
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        bottomNav.setupWithNavController(navController)

        // VISIBILITY LOGIC
        navController.addOnDestinationChangedListener { _, destination, _ ->
            if (destination.id == R.id.loginFragment) {
                // --- LOGIN SCREEN ---
                // 1. Hide Bottom Bar
                bottomNav.visibility = View.GONE

                // 2. Hide Status Bar (Battery/Time) for Immersion
                window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            } else {
                // --- HOME SCREEN ---
                // 1. Show Bottom Bar
                bottomNav.visibility = View.VISIBLE

                // 2. Show Status Bar
                window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
            }
        }
    }
}