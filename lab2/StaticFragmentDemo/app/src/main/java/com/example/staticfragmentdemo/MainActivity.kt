package com.example.staticfragmentdemo

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var activityTextView: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        activityTextView = findViewById(R.id.activityTextView)
    }

    fun updateActivityText(text: String) {
        activityTextView.text = "Message from Fragment: $text"
    }
}