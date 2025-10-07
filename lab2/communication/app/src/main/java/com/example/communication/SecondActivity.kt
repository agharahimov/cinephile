package com.example.communication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class SecondActivity : AppCompatActivity() {

    private lateinit var email: String
    private lateinit var phone: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_second)

        val fullNameTextView: TextView = findViewById(R.id.fullNameTextView)
        val emailTextView: TextView = findViewById(R.id.emailTextView)
        val phoneTextView: TextView = findViewById(R.id.phoneTextView)
        val shareEmailButton: Button = findViewById(R.id.shareEmailButton)
        val shareSmsButton: Button = findViewById(R.id.shareSmsButton)

        val firstName = intent.getStringExtra("FIRST_NAME") ?: "N/A"
        val lastName = intent.getStringExtra("LAST_NAME") ?: "N/A"
        email = intent.getStringExtra("EMAIL") ?: ""
        phone = intent.getStringExtra("PHONE") ?: ""

        fullNameTextView.text = getString(R.string.full_name_label, firstName, lastName)
        emailTextView.text = getString(R.string.email_label, email)
        phoneTextView.text = getString(R.string.phone_label, phone)

        shareEmailButton.setOnClickListener {
            shareEmail()
        }

        shareSmsButton.setOnClickListener {
            shareSms()
        }
    }

    private fun shareEmail() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:$email")
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }

    private fun shareSms() {
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("smsto:$phone")
            putExtra("sms_body", getString(R.string.sms_default_body))
        }
        if (intent.resolveActivity(packageManager) != null) {
            startActivity(intent)
        }
    }
}