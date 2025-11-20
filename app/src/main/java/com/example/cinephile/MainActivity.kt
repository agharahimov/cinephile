package com.example.cinephile

import android.content.Intent // Import Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        // ==============================================================
        // 1. ANIMATED SPLASH SCREEN LOGIC
        // ==============================================================
        val splashScreen = installSplashScreen()

        // Force the app to wait 2000ms (2 seconds) so the animation plays fully.
        var keepSplashOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        Handler(Looper.getMainLooper()).postDelayed({
            // This runs after 2 seconds
            keepSplashOnScreen = false

            // ==========================================================
            // 2. NAVIGATE TO LOGIN SCREEN (Integrated Code)
            // ==========================================================
            val intent = Intent(this, LoginActivity::class.java)
            startActivity(intent)
            finish() // This closes MainActivity so the user can't go back to the splash

        }, 1000)

        // ==============================================================
        // 3. STANDARD APP SETUP
        // (This loads in the background while the splash is showing)
        // ==============================================================
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Note: Even though we set this up, the user won't see it yet
        // because we redirect to LoginActivity immediately after the splash.
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController

        findViewById<BottomNavigationView>(R.id.bottom_navigation)
            .setupWithNavController(navController)
    }
}