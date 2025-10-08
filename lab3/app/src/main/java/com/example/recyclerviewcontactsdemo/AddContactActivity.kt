package com.example.recyclerviewcontactsdemo

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity

class AddContactActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_contact)

        val nameEt: EditText = findViewById(R.id.name_et)
        val addressEt: EditText = findViewById(R.id.address_et)
        val phoneEt: EditText = findViewById(R.id.phone_et)
        val emailEt: EditText = findViewById(R.id.email_et)
        val saveButton: Button = findViewById(R.id.save_button)

        saveButton.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("name", nameEt.text.toString())
            resultIntent.putExtra("address", addressEt.text.toString())
            resultIntent.putExtra("phone", phoneEt.text.toString())
            resultIntent.putExtra("email", emailEt.text.toString())
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
    }
}