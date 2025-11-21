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
import com.google.android.material.appbar.AppBarLayout
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

        // Find Views
        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)
        val topAppBar = findViewById<AppBarLayout>(R.id.appBarLayout) // <--- NEW: Reference to Top Bar

        // Connect Bottom Bar to Controller
        bottomNav.setupWithNavController(navController)

        // 3. VISIBILITY LOGIC (Controls Top Bar, Bottom Bar, and Fullscreen Mode)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {

                // CASE 1: LOGIN SCREEN
                // Action: Hide everything for cinematic experience
                R.id.loginFragment -> {
                    bottomNav.visibility = View.GONE
                    topAppBar.visibility = View.GONE
                    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                }

                // CASE 2: SEARCH SCREEN
                // Action: Hide Top Logo (because Search has its own search bar), Show Bottom Nav
                R.id.searchFragment -> {
                    bottomNav.visibility = View.VISIBLE
                    topAppBar.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                }

                // CASE 3: MAIN APP (Home, Profile, Quiz, Watchlist, etc.)
                // Action: Show everything
                else -> {
                    bottomNav.visibility = View.VISIBLE
                    topAppBar.visibility = View.VISIBLE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                }
            }
        }
    }
}