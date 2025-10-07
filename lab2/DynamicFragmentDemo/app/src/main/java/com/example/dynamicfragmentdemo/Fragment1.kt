package com.example.dynamicfragmentdemo

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.setFragmentResult
import androidx.fragment.app.setFragmentResultListener

class Fragment1 : Fragment(R.layout.fragment_1) {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val messageReceiverTextView: TextView = view.findViewById(R.id.messageReceiverTextView)
        val messageEditText: EditText = view.findViewById(R.id.messageEditText)
        val sendMessageButton: Button = view.findViewById(R.id.sendMessageButton)

        setFragmentResultListener("requestKey_F2_to_F1") { _, bundle ->
            val result = bundle.getString("bundleKey")
            messageReceiverTextView.text = "From F2: $result"
        }

        sendMessageButton.setOnClickListener {
            val message = messageEditText.text.toString()
            setFragmentResult("requestKey_F1_to_F2", bundleOf("bundleKey" to message))
        }
    }
}