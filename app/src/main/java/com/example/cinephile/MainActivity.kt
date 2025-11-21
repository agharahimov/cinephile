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
        // 1. Splash Screen
        val splashScreen = installSplashScreen()
        var keepSplashOnScreen = true
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }
        Handler(Looper.getMainLooper()).postDelayed({ keepSplashOnScreen = false }, 1000)

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // 2. Navigation Setup
        val navHostFragment = supportFragmentManager
            .findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        val navController = navHostFragment.navController
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Connect Bottom Bar to Controller
        bottomNav.setupWithNavController(navController)

        // 3. VISIBILITY LOGIC
        // We hide the bar on Login, but show it on Home, Search, Watchlist, etc.
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {
                R.id.loginFragment -> {
                    // --- LOGIN SCREEN ---
                    bottomNav.visibility = View.GONE
                    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                }
                else -> {
                    // --- MAIN APP (Home, Search, Watchlist, etc.) ---
                    bottomNav.visibility = View.VISIBLE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                }
            }
        }
    }
}