package com.example.cinephile

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.VideoView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController

class LoginFragment : Fragment(R.layout.fragment_login) {

    private lateinit var videoView: VideoView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- VIDEO BACKGROUND ---
        videoView = view.findViewById(R.id.videoViewBackground)
        // Note: Make sure you have the video file in res/raw/login_bg
        val path = "android.resource://" + requireContext().packageName + "/" + R.raw.login_bg
        val uri = Uri.parse(path)
        videoView.setVideoURI(uri)

        videoView.setOnPreparedListener { mediaPlayer ->
            mediaPlayer.isLooping = true
            mediaPlayer.setVolume(0f, 0f)

            // Scaling logic to fill screen
            val videoRatio = mediaPlayer.videoWidth / mediaPlayer.videoHeight.toFloat()
            val screenRatio = videoView.width / videoView.height.toFloat()
            val scaleX = videoRatio / screenRatio
            if (scaleX >= 1f) {
                videoView.scaleX = scaleX
            } else {
                videoView.scaleY = 1f / scaleX
            }
        }
        videoView.start()

        // --- BUTTONS ---
        view.findViewById<View>(R.id.btnSignUp).setOnClickListener {
            Toast.makeText(requireContext(), "Sign Up Clicked", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<View>(R.id.btnGuest).setOnClickListener {
            // Navigate to Home
            findNavController().navigate(R.id.action_loginFragment_to_homeFragment)
        }

        view.findViewById<View>(R.id.tvLoginOption).setOnClickListener {
            Toast.makeText(requireContext(), "Log In Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onResume() {
        super.onResume()
        videoView.start() // Keep video playing
    }
}