package com.example.cinephile

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController

// --- IMPORTANT: Make sure these imports match your file structure ---
import com.example.cinephile.data.UserRepositoryImpl
import com.example.cinephile.data.local.AppDatabase
import com.example.cinephile.ui.ViewModelFactory
import com.example.cinephile.ui.auth.AuthState
import com.example.cinephile.ui.auth.AuthViewModel

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var viewModel: AuthViewModel
    private lateinit var videoView: VideoView

    // Variable to save video playback position on rotation
    private var currentVideoPosition: Int = 0

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // =================================================================
        // 0. CLEAR PREVIOUS SESSION (NEW STEP)
        // =================================================================
        // Whenever we land on the Login screen, we assume a new session is starting.
        // We clear the old username so Guest doesn't inherit User A's data.
        val sharedPref = requireActivity().getSharedPreferences("UserSession", Context.MODE_PRIVATE)
        with(sharedPref.edit()) {
            clear() // Wipes "KEY_USERNAME"
            apply()
        }

        // =================================================================
        // 1. DEFINE ALL VIEWS FIRST
        // =================================================================

        // Layout Containers
        val layoutIntro: LinearLayout = view.findViewById(R.id.layoutIntro)
        val layoutSignUp: LinearLayout = view.findViewById(R.id.layoutSignUp)
        val layoutLogin: LinearLayout = view.findViewById(R.id.layoutLogin)

        // Sign Up Inputs
        val etSignUser: EditText = view.findViewById(R.id.etSignUser)
        val etSignEmail: EditText = view.findViewById(R.id.etSignEmail)
        val etSignPass: EditText = view.findViewById(R.id.etSignPass)

        // Login Inputs
        val etLoginEmail: EditText = view.findViewById(R.id.etLoginEmail)
        val etLoginPass: EditText = view.findViewById(R.id.etLoginPass)

        // Buttons
        val btnGuest: View = view.findViewById(R.id.btnGuest)
        val btnShowSignUp: View = view.findViewById(R.id.btnShowSignUp)
        val btnShowLogin: View = view.findViewById(R.id.btnShowLogin)
        val tvBackFromSignUp: View = view.findViewById(R.id.tvBackFromSignUp)
        val tvBackFromLogin: View = view.findViewById(R.id.tvBackFromLogin)
        val btnConfirmSignUp: View = view.findViewById(R.id.btnConfirmSignUp)
        val btnConfirmLogin: View = view.findViewById(R.id.btnConfirmLogin)

        // =================================================================
        // 2. SETUP VIEWMODEL
        // =================================================================
        val applicationContext = requireActivity().applicationContext
        val factory = ViewModelFactory(applicationContext)
        viewModel = ViewModelProvider(this, factory)[AuthViewModel::class.java]

        // =================================================================
        // 3. OBSERVE STATE
        // =================================================================
        viewModel.authState.observe(viewLifecycleOwner) { state ->
            when (state) {
                is AuthState.Loading -> {
                    // Optional: Show loading spinner
                }
                is AuthState.LoginSuccess -> {
                    // --- SAVE USERNAME LOGIC ---
                    val username = state.user.username
                    // We reuse the sharedPref variable defined at the top
                    with(sharedPref.edit()) {
                        putString("KEY_USERNAME", username)
                        apply() // Save to memory
                    }

                    Toast.makeText(context, "Welcome back, $username!", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
                    viewModel.resetState()
                }
                is AuthState.RegistrationSuccess -> {
                    Toast.makeText(context, "Account Created! Please Log In.", Toast.LENGTH_SHORT).show()
                    // Switch UI to Login
                    layoutSignUp.visibility = View.GONE
                    layoutLogin.visibility = View.VISIBLE
                    viewModel.resetState()
                }
                is AuthState.Error -> {
                    Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                    viewModel.resetState()
                }
                else -> {} // Idle state
            }
        }

        // =================================================================
        // 4. CLICK LISTENERS
        // =================================================================

        // GUEST
        btnGuest.setOnClickListener {
            // Save "Guest" as the name
            with(sharedPref.edit()) {
                putString("KEY_USERNAME", "Guest")
                apply()
            }
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }

        // SHOW SIGN UP
        btnShowSignUp.setOnClickListener {
            layoutIntro.visibility = View.GONE
            layoutSignUp.visibility = View.VISIBLE
        }

        // SHOW LOGIN
        btnShowLogin.setOnClickListener {
            layoutIntro.visibility = View.GONE
            layoutLogin.visibility = View.VISIBLE
        }

        // BACK BUTTONS
        tvBackFromSignUp.setOnClickListener {
            layoutSignUp.visibility = View.GONE
            layoutIntro.visibility = View.VISIBLE
        }
        tvBackFromLogin.setOnClickListener {
            layoutLogin.visibility = View.GONE
            layoutIntro.visibility = View.VISIBLE
        }

        // CONFIRM SIGN UP
        btnConfirmSignUp.setOnClickListener {
            viewModel.register(
                username = etSignUser.text.toString(),
                email = etSignEmail.text.toString(),
                pass = etSignPass.text.toString()
            )
        }

        // CONFIRM LOGIN
        btnConfirmLogin.setOnClickListener {
            viewModel.login(
                email = etLoginEmail.text.toString(),
                pass = etLoginPass.text.toString()
            )
        }

        // =================================================================
        // 5. VIDEO BACKGROUND
        // =================================================================
        videoView = view.findViewById(R.id.videoViewBackground)
        val path = "android.resource://" + requireContext().packageName + "/" + R.raw.login_bg
        videoView.setVideoURI(Uri.parse(path))

        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = true
            mediaPlayer.setVolume(0f, 0f)

            val videoRatio = mediaPlayer.videoWidth / mediaPlayer.videoHeight.toFloat()
            val screenRatio = videoView.width / videoView.height.toFloat()

            // 1. Calculate base scale to fill screen
            var scale = if (screenRatio > videoRatio) {
                screenRatio / videoRatio
            } else {
                videoRatio / screenRatio
            }

            // 2. DETECT LANDSCAPE: Reduce Zoom
            val isLandscape = resources.configuration.orientation == android.content.res.Configuration.ORIENTATION_LANDSCAPE
            if (isLandscape) {
                // Reduces the zoom slightly so it's not too close
                scale *= 0.85f
            }

            // 3. Apply Scale (Ensure it doesn't shrink smaller than original)
            if (scale >= 1f) {
                videoView.scaleX = scale
                videoView.scaleY = scale
            } else {
                videoView.scaleX = 1f
                videoView.scaleY = 1f
            }

            // Resume video position
            if (currentVideoPosition > 0) {
                videoView.seekTo(currentVideoPosition)
            }
            videoView.start()
        }
    }

    // Save video position when screen rotates or app pauses
    override fun onPause() {
        super.onPause()
        currentVideoPosition = videoView.currentPosition
    }

    override fun onResume() {
        super.onResume()
        videoView.start()
    }
}