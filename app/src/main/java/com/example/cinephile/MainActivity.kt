package com.example.cinephile


import android.content.Context
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.google.android.material.appbar.AppBarLayout
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    // Variable to access the subtitle (Welcome text) in the Toolbar
    private var tvSubtitle: TextView? = null

    override fun onCreate(savedInstanceState: Bundle?) {

        val sharedPref = getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        val isDarkMode = sharedPref.getBoolean("IS_DARK_MODE", true)

        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
        // 1. Splash Screen Logic
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
        val topAppBar = findViewById<AppBarLayout>(R.id.appBarLayout)
        // Find the subtitle textview inside the Toolbar (for Profile screen)
        tvSubtitle = findViewById(R.id.tvToolbarSubtitle)

        // Connect Bottom Bar to Controller
        bottomNav.setupWithNavController(navController)

        // 3. VISIBILITY LOGIC (Controls Top Bar, Bottom Bar, Subtitle, and Fullscreen)
        navController.addOnDestinationChangedListener { _, destination, _ ->
            when (destination.id) {

                // CASE 1: LOGIN SCREEN
                // Hide everything for cinematic video background
                R.id.loginFragment -> {
                    bottomNav.visibility = View.GONE
                    topAppBar.visibility = View.GONE
                    tvSubtitle?.visibility = View.GONE
                    window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                }

                // CASE 2: SEARCH SCREEN
                // Hide Top Logo (Search has its own bar), Show Bottom Nav
                R.id.searchFragment -> {
                    bottomNav.visibility = View.VISIBLE
                    topAppBar.visibility = View.GONE
                    tvSubtitle?.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                }

                // CASE 3: DETAILS SCREEN (NEW)
                // Hide everything for immersive movie details
                R.id.detailsFragment -> {
                    bottomNav.visibility = View.GONE
                    topAppBar.visibility = View.GONE
                    tvSubtitle?.visibility = View.GONE
                    // Keep status bar visible so user can see time/battery
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                }

                // CASE 4: PROFILE SCREEN
                // Show Bars + Show "Welcome" Subtitle
                R.id.profileFragment -> {
                    bottomNav.visibility = View.VISIBLE
                    topAppBar.visibility = View.VISIBLE
                    tvSubtitle?.visibility = View.VISIBLE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                }

                // CASE 5: HOME, QUIZ, OTHERS
                // Show Bars, but Hide Subtitle
                else -> {
                    bottomNav.visibility = View.VISIBLE
                    topAppBar.visibility = View.VISIBLE
                    tvSubtitle?.visibility = View.GONE
                    window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN)
                }
            }
        }
    }

    // Helper function to allow ProfileFragment to set the welcome text
    fun setSubtitle(text: String) {
        tvSubtitle?.text = text
    }
}