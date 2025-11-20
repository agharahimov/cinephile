package com.example.cinephile

import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.Toast
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var videoView: VideoView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Hide the Status Bar/Action Bar for full immersion
        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_FULLSCREEN
        supportActionBar?.hide()

        setContentView(R.layout.activity_login)

        // --- VIDEO BACKGROUND LOGIC ---
        videoView = findViewById(R.id.videoViewBackground)

        // Path to your file in res/raw/login_bg.mp4
        val path = "android.resource://" + packageName + "/" + R.raw.login_bg
        val uri = Uri.parse(path)
        videoView.setVideoURI(uri)

        videoView.setOnPreparedListener { mediaPlayer ->
            // 1. Loop the video
            mediaPlayer.isLooping = true

            // 2. Mute the sound (even if the file has no sound, this is safer)
            mediaPlayer.setVolume(0f, 0f)

            // 3. Scaling Logic: Try to fill the screen
            // Note: Standard VideoView preserves aspect ratio.
            // If you see black bars, we might need a custom scaling view later.
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

        // --- BUTTON LOGIC ---

        findViewById<View>(R.id.btnSignUp).setOnClickListener {
            // TODO: Go to Sign Up Screen
            Toast.makeText(this, "Sign Up Clicked", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.btnGuest).setOnClickListener {
            // TODO: Go to Main Activity (Home)
            Toast.makeText(this, "Guest Mode", Toast.LENGTH_SHORT).show()
        }

        findViewById<View>(R.id.tvLoginOption).setOnClickListener {
            // TODO: Go to Login Screen
            Toast.makeText(this, "Log In Clicked", Toast.LENGTH_SHORT).show()
        }
    }

    // Restart video if user comes back to app
    override fun onResume() {
        super.onResume()
        videoView.start()
    }
}