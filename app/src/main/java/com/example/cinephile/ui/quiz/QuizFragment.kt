package com.example.cinephile.ui.quiz

import android.app.AlertDialog
import android.os.Bundle
import android.graphics.Color
import android.view.View
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import coil.load
import com.example.cinephile.R
import com.example.cinephile.ui.ViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QuizFragment : Fragment(R.layout.fragment_quiz) {

    private lateinit var viewModel: QuizViewModel

    // UI Variables
    private lateinit var tvQuestion: TextView
    private lateinit var ivImage: ImageView
    private lateinit var buttons: List<Button>
    private lateinit var progressBar: ProgressBar
    private lateinit var progressTimer: ProgressBar
    private lateinit var tvScore: TextView
    private lateinit var tvCount: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 1. Setup ViewModel
        val factory = ViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, factory)[QuizViewModel::class.java]

        // 2. Bind Views
        tvQuestion = view.findViewById(R.id.tvQuestionText)
        ivImage = view.findViewById(R.id.ivQuestionImage)
        progressBar = view.findViewById(R.id.progressBarQuiz)
        progressTimer = view.findViewById(R.id.progressTimer)
        tvScore = view.findViewById(R.id.tvScore)
        tvCount = view.findViewById(R.id.tvQuestionCount)

        buttons = listOf(
            view.findViewById(R.id.btnOption1),
            view.findViewById(R.id.btnOption2),
            view.findViewById(R.id.btnOption3),
            view.findViewById(R.id.btnOption4)
        )

        // 3. Set Click Listeners
        buttons.forEach { btn ->
            btn.setOnClickListener {
                handleAnswerSelection(btn.text.toString(), btn)
            }
        }

        // 4. Observe Timer Progress
        lifecycleScope.launch {
            viewModel.timerProgress.collect { progress ->
                progressTimer.progress = progress
                // Change color to Red if time is running out (< 30%)
                if (progress < 30) {
                    progressTimer.progressDrawable.setTint(
                        ContextCompat.getColor(requireContext(), android.R.color.holo_red_light)
                    )
                } else {
                    progressTimer.progressDrawable.setTint(
                        Color.parseColor("#6200EE")
                    )
                }
            }
        }

        // 5. Observe Game State
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is QuizUiState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        setButtonsEnabled(false)
                    }
                    is QuizUiState.Error -> {
                        progressBar.visibility = View.GONE
                        showErrorDialog(state.message)
                    }
                    is QuizUiState.Playing -> {
                        progressBar.visibility = View.GONE
                        setButtonsEnabled(true)
                        displayQuestion(state)
                    }
                    is QuizUiState.GameOver -> {
                        showGameOverDialog(state.score, state.totalQuestions)
                    }
                }
            }
        }
    }

    private fun displayQuestion(state: QuizUiState.Playing) {
        tvScore.text = "Score: ${state.score}"
        tvCount.text = "Question ${state.currentQuestionIndex + 1}/${state.totalQuestions}"
        tvQuestion.text = state.question.questionText

        // Handle Image Visibility
        if (state.question.imageUrl != null) {
            ivImage.visibility = View.VISIBLE
            ivImage.load(state.question.imageUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
            }
        } else {
            ivImage.visibility = View.GONE
        }

        // Reset Buttons visual state
        buttons.forEachIndexed { index, btn ->
            btn.text = state.question.options[index]
            btn.backgroundTintList = null // Reset color to default
            btn.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }
    }

    private fun handleAnswerSelection(answer: String, selectedBtn: Button) {
        // Disable buttons to prevent double clicks
        setButtonsEnabled(false)

        val state = viewModel.uiState.value
        if (state is QuizUiState.Playing) {
            val isCorrect = answer == state.question.correctAnswer

            // --- VISUAL FEEDBACK ---
            if (isCorrect) {
                selectedBtn.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.holo_green_dark)
            } else {
                selectedBtn.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.holo_red_dark)
                // Highlight the correct one so user learns
                buttons.find { it.text == state.question.correctAnswer }?.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), android.R.color.holo_green_dark)
            }

            // Wait 1 second before moving to next question so user sees the result
            lifecycleScope.launch {
                delay(1000)
                viewModel.submitAnswer(answer)
            }
        }
    }

    private fun setButtonsEnabled(enabled: Boolean) {
        buttons.forEach { it.isEnabled = enabled }
    }

    private fun showErrorDialog(msg: String) {
        AlertDialog.Builder(requireContext())
            .setTitle("Quiz Error")
            .setMessage(msg)
            .setPositiveButton("Go Back") { _, _ ->
                findNavController().navigateUp()
            }
            .setCancelable(false)
            .show()
    }

    private fun showGameOverDialog(score: Int, total: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Quiz Completed!")
            .setMessage("You scored $score out of $total.")
            .setPositiveButton("Play Again") { _, _ ->
                viewModel.startNewQuiz()
            }
            .setNegativeButton("Exit") { _, _ ->
                findNavController().navigateUp()
            }
            .setCancelable(false)
            .show()
    }
}