    package com.example.communication

    import android.content.Context
    import android.content.Intent
    import android.os.Bundle
    import android.widget.Button
    import androidx.appcompat.app.AppCompatActivity
    import com.google.android.material.textfield.TextInputEditText
    import com.google.android.material.textfield.TextInputLayout

    class MainActivity : AppCompatActivity() {

        private lateinit var firstNameEditText: TextInputEditText
        private lateinit var lastNameEditText: TextInputEditText
        private lateinit var emailEditText: TextInputEditText
        private lateinit var phoneEditText: TextInputEditText

        private lateinit var firstNameLayout: TextInputLayout
        private lateinit var lastNameLayout: TextInputLayout
        private lateinit var emailLayout: TextInputLayout
        private lateinit var phoneLayout: TextInputLayout

        companion object {
            const val PREFS_NAME = "UserInfoPrefs"
            const val KEY_FIRST_NAME = "firstName"
            const val KEY_LAST_NAME = "lastName"
            const val KEY_EMAIL = "email"
            const val KEY_PHONE = "phone"
        }

        override fun onCreate(savedInstanceState: Bundle?) {
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            firstNameEditText = findViewById(R.id.firstNameEditText)
            lastNameEditText = findViewById(R.id.lastNameEditText)
            emailEditText = findViewById(R.id.emailEditText)
            phoneEditText = findViewById(R.id.phoneEditText)

            firstNameLayout = findViewById(R.id.firstNameLayout)
            lastNameLayout = findViewById(R.id.lastNameLayout)
            emailLayout = findViewById(R.id.emailLayout)
            phoneLayout = findViewById(R.id.phoneLayout)

            val clearButton: Button = findViewById(R.id.clearButton)
            val sendButton: Button = findViewById(R.id.sendButton)

            clearButton.setOnClickListener {
                clearFields()
            }

            sendButton.setOnClickListener {
                sendData()
            }

            // Restore saved data
            loadData()
        }

        override fun onPause() {
            super.onPause()
            saveData()
        }

        private fun validateFields(): Boolean {
            var isValid = true

            if (firstNameEditText.text.isNullOrBlank()) {
                firstNameLayout.error = "First name is required"
                isValid = false
            } else {
                firstNameLayout.error = null
            }

            if (lastNameEditText.text.isNullOrBlank()) {
                lastNameLayout.error = "Last name is required"
                isValid = false
            } else {
                lastNameLayout.error = null
            }

            if (emailEditText.text.isNullOrBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(emailEditText.text.toString()).matches()) {
                emailLayout.error = "A valid email is required"
                isValid = false
            } else {
                emailLayout.error = null
            }

            if (phoneEditText.text.isNullOrBlank()) {
                phoneLayout.error = "Phone number is required"
                isValid = false
            } else {
                phoneLayout.error = null
            }

            return isValid
        }

        private fun sendData() {
            if (validateFields()) {
                val intent = Intent(this, SecondActivity::class.java).apply {
                    putExtra("FIRST_NAME", firstNameEditText.text.toString())
                    putExtra("LAST_NAME", lastNameEditText.text.toString())
                    putExtra("EMAIL", emailEditText.text.toString())
                    putExtra("PHONE", phoneEditText.text.toString())
                }
                startActivity(intent)
            }
        }

        private fun clearFields() {
            firstNameEditText.text?.clear()
            lastNameEditText.text?.clear()
            emailEditText.text?.clear()
            phoneEditText.text?.clear()

            firstNameLayout.error = null
            lastNameLayout.error = null
            emailLayout.error = null
            phoneLayout.error = null
        }

        private fun saveData() {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit()
            prefs.putString(KEY_FIRST_NAME, firstNameEditText.text.toString())
            prefs.putString(KEY_LAST_NAME, lastNameEditText.text.toString())
            prefs.putString(KEY_EMAIL, emailEditText.text.toString())
            prefs.putString(KEY_PHONE, phoneEditText.text.toString())
            prefs.apply()
        }

        private fun loadData() {
            val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            firstNameEditText.setText(prefs.getString(KEY_FIRST_NAME, ""))
            lastNameEditText.setText(prefs.getString(KEY_LAST_NAME, ""))
            emailEditText.setText(prefs.getString(KEY_EMAIL, ""))
            phoneEditText.setText(prefs.getString(KEY_PHONE, ""))
        }
    }