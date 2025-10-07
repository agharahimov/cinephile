package com.example.staticfragmentdemo

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment

class BaseFragment : Fragment() {

    private val TAG = "FragmentLifecycle"

    // Called when the fragment is first attached to its context (the activity).
    override fun onAttach(context: Context) {
        super.onAttach(context)
        Log.d(TAG, "onAttach() called")
    }

    // Called to do initial creation of the fragment.
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate() called")
    }

    // Called to have the fragment instantiate its user interface view.
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Log.d(TAG, "onCreateView() called")
        // Inflate the layout for this fragment. This is a required step.
        return inflater.inflate(R.layout.base_fragment, container, false)
    }

    // Called immediately after onCreateView() has returned, but before any saved state has been restored into the view.
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated() called")

        val editText: EditText = view.findViewById(R.id.fragmentEditText)
        val sendButton: Button = view.findViewById(R.id.fragmentSendButton)

        sendButton.setOnClickListener {
            val inputText = editText.text.toString()
            // Get the host activity and cast it to MainActivity to call its public method.
            // requireActivity() will throw an exception if the fragment is not attached.
            val mainActivity = requireActivity() as MainActivity
            mainActivity.updateActivityText(inputText)
        }
    }

    // Called when the fragment's activity has been created and this fragment's view hierarchy instantiated.
    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart() called")
    }

    // Called when the fragment is visible to the user.
    override fun onResume() {
        super.onResume()
        Log.d(TAG, "onResume() called")
    }

    // Called when the fragment is no longer resumed.
    override fun onPause() {
        super.onPause()
        Log.d(TAG, "onPause() called")
    }

    // Called when the fragment is no longer visible to the user.
    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop() called")
    }

    // Called when the view previously created by onCreateView() has been detached from the fragment.
    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView() called")
    }

    // Called when the fragment is no longer in use.
    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "onDestroy() called")
    }

    // Called when the fragment is detached from its context.
    override fun onDetach() {
        super.onDetach()
        Log.d(TAG, "onDetach() called")
    }
}