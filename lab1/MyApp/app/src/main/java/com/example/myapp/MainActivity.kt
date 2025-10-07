package com.example.myapp // Make sure this matches your package name

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView

class MainActivity : AppCompatActivity() {

    private lateinit var messageEditText: EditText
    private lateinit var resultTextView: TextView

    private val KEY_RESULT_TEXT = "key_result_text"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        messageEditText = findViewById(R.id.messageEditText)
        resultTextView = findViewById(R.id.resultTextView)
        val clearButton: Button = findViewById(R.id.clearButton)
        val validateButton: Button = findViewById(R.id.validateButton)

        validateButton.setOnClickListener {
            val enteredMessage = messageEditText.text.toString()
            resultTextView.text = enteredMessage
        }

        clearButton.setOnClickListener {
            messageEditText.text.clear()
            resultTextView.text = ""
        }

        if (savedInstanceState != null) {
            val savedText = savedInstanceState.getString(KEY_RESULT_TEXT)
            resultTextView.text = savedText
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString(KEY_RESULT_TEXT, resultTextView.text.toString())

        super.onSaveInstanceState(outState)
    }
}