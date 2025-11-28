package com.example.cinephile.util

import android.app.AlertDialog
import android.content.Context
import androidx.navigation.NavController
import androidx.navigation.NavOptions
import com.example.cinephile.R

object GuestManager {

    /**
     * Returns TRUE if the user is a "Guest".
     */
    fun isGuest(context: Context): Boolean {
        val sharedPref = context.getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        val username = sharedPref.getString("KEY_USERNAME", "Guest")
        return username == "Guest"
    }

    /**
     * Checks if user is Guest.
     * If YES: Shows Login Dialog.
     * If NO: Runs the action (onAuthorized).
     */
    fun checkAndRun(
        context: Context,
        navController: NavController,
        onAuthorized: () -> Unit
    ) {
        if (isGuest(context)) {
            showLoginDialog(context, navController)
        } else {
            onAuthorized()
        }
    }

    private fun showLoginDialog(context: Context, navController: NavController) {
        AlertDialog.Builder(context)
            .setTitle("Account Required")
            .setMessage("This feature is for registered users only.\n\nWould you like to Log In now?")
            .setPositiveButton("Log In") { _, _ ->

                // --- FIX: Standard Navigation Logic ---
                // This clears the back stack so they can't press 'Back' to return to the app
                val navOptions = NavOptions.Builder()
                    .setPopUpTo(R.id.nav_graph, true)
                    .build()

                navController.navigate(R.id.loginFragment, null, navOptions)
            }
            .setNegativeButton("Stay Guest", null)
            .show()
    }
}