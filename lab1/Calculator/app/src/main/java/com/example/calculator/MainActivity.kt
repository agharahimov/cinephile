package com.example.calculator

import android.content.Context
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var resultTextView: TextView

    private var currentNumber = ""

    private var leftOperand = ""

    private var currentOperator = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        resultTextView = findViewById(R.id.resultTextView)

        // Restore the last calculation from SharedPreferences
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val lastCalculation = prefs.getString(KEY_LAST_CALCULATION, "0")
        resultTextView.text = lastCalculation
        // If the restored value is a result, set it as the current number
        if (lastCalculation != "0" && lastCalculation?.isNotEmpty() == true) {
            currentNumber = lastCalculation
        }
    }

    fun onDigitClick(view: View) {
        val button = view as Button
        if (currentOperator.isNotEmpty() && leftOperand.isNotEmpty() && currentNumber == leftOperand) {
            // After pressing equals, if a new number is typed, start fresh
            currentNumber = ""
        }
        currentNumber += button.text.toString()
        updateDisplay()
    }

    fun onOperatorClick(view: View) {
        if (currentNumber.isNotEmpty()) {
            if (leftOperand.isNotEmpty()) {
                // A calculation is pending, so perform it
                calculate()
            }
            leftOperand = currentNumber
        }
        currentOperator = (view as Button).text.toString()
    }

    fun onEqualsClick(view: View) {
        if (leftOperand.isNotEmpty() && currentNumber.isNotEmpty() && currentOperator.isNotEmpty()) {
            calculate()
            leftOperand = currentNumber
            currentOperator = ""
        }
    }

    fun onClearClick(view: View) {
        currentNumber = ""
        leftOperand = ""
        currentOperator = ""
        resultTextView.text = "0"
    }

    fun onClearEntryClick(view: View) {
        currentNumber = ""
        updateDisplay()
    }

    fun onDotClick(view: View) {
        if (!currentNumber.contains(".")) {
            currentNumber += if (currentNumber.isEmpty()) "0." else "."
            updateDisplay()
        }
    }

    private fun updateDisplay() {
        resultTextView.text = if (currentNumber.isEmpty()) "0" else currentNumber
    }

    private fun calculate() {
        val left = leftOperand.toDoubleOrNull() ?: return
        val right = currentNumber.toDoubleOrNull() ?: return
        var result = 0.0

        when (currentOperator) {
            "+" -> result = left + right
            "-" -> result = left - right
            "*" -> result = left * right
            "/" -> {
                if (right != 0.0) {
                    result = left / right
                } else {
                    // Handle division by zero
                    resultTextView.text = "Error"
                    return
                }
            }
        }
        currentNumber = if (result % 1 == 0.0) {
            result.toInt().toString() // It's a whole number
        } else {
            result.toString()
        }
        updateDisplay()
    }

    override fun onStop() {
        super.onStop()
        // Save the current display text to SharedPreferences when the app is closed
        val prefs = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putString(KEY_LAST_CALCULATION, resultTextView.text.toString())
        editor.apply()
    }

    companion object {
        private const val PREFS_NAME = "CalculatorPrefs"
        private const val KEY_LAST_CALCULATION = "lastCalculation"
    }
}