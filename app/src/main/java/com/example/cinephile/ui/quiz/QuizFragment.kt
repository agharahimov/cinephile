package com.example.cinephile.ui.quiz

import android.app.AlertDialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.*
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import coil.load
import com.example.cinephile.R
import com.example.cinephile.ui.ViewModelFactory
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class QuizFragment : Fragment(R.layout.fragment_quiz) {

    private lateinit var viewModel: QuizViewModel
    private lateinit var listAdapter: QuizListAdapter

    // Containers
    private lateinit var containerSelection: View
    private lateinit var containerGame: View
    private lateinit var rvQuizLists: RecyclerView
    private lateinit var progressBar: ProgressBar

    // Selection UI Variables
    private lateinit var ivHeaderIcon: ImageView

    // Game UI Variables
    private lateinit var tvQuestion: TextView
    private lateinit var ivImage: ImageView
    private lateinit var buttons: List<Button>
    private lateinit var progressTimer: ProgressBar
    private lateinit var tvScore: TextView
    private lateinit var tvCount: TextView

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory = ViewModelFactory(requireContext().applicationContext)
        viewModel = ViewModelProvider(this, factory)[QuizViewModel::class.java]

        // 1. Bind Views
        containerSelection = view.findViewById(R.id.containerSelection)
        containerGame = view.findViewById(R.id.containerGame)
        rvQuizLists = view.findViewById(R.id.rvQuizLists)
        progressBar = view.findViewById(R.id.progressBarQuiz)
        ivHeaderIcon = view.findViewById(R.id.ivQuizHeaderIcon)

        tvQuestion = view.findViewById(R.id.tvQuestionText)
        ivImage = view.findViewById(R.id.ivQuestionImage)
        progressTimer = view.findViewById(R.id.progressTimer)
        tvScore = view.findViewById(R.id.tvScore)
        tvCount = view.findViewById(R.id.tvQuestionCount)

        buttons = listOf(
            view.findViewById(R.id.btnOption1),
            view.findViewById(R.id.btnOption2),
            view.findViewById(R.id.btnOption3),
            view.findViewById(R.id.btnOption4)
        )

        // 2. Setup Selection Screen Animations
        // Start the pulse animation on the header icon
        val pulseAnim = AnimationUtils.loadAnimation(requireContext(), R.anim.pulse)
        ivHeaderIcon.startAnimation(pulseAnim)

        // 3. Setup List Adapter
        listAdapter = QuizListAdapter { list ->
            // When a list is clicked, tell ViewModel to start the quiz
            viewModel.startQuizForList(list)
        }
        rvQuizLists.adapter = listAdapter

        // 4. Set Game Button Listeners
        buttons.forEach { btn ->
            btn.setOnClickListener {
                handleAnswerSelection(btn.text.toString(), btn)
            }
        }

        // 5. Observe Timer Progress
        lifecycleScope.launch {
            viewModel.timerProgress.collect { progress ->
                // 1. Update the length of the bar
                progressTimer.progress = progress

                // 2. Update the Color
                if (progress < 30) {
                    // Turn RED when time is low
                    // Use setProgressTintList to ONLY color the moving part
                    progressTimer.progressTintList = ColorStateList.valueOf(Color.parseColor("#B00020"))
                } else {
                    // Normal PURPLE
                    progressTimer.progressTintList = ColorStateList.valueOf(Color.parseColor("#6200EE"))
                }
            }
        }

        // 6. Observe Game State
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                when (state) {
                    is QuizUiState.Loading -> {
                        progressBar.visibility = View.VISIBLE
                        containerSelection.visibility = View.GONE
                        containerGame.visibility = View.GONE
                    }
                    is QuizUiState.ListSelection -> {
                        progressBar.visibility = View.GONE
                        containerSelection.visibility = View.VISIBLE
                        containerGame.visibility = View.GONE
                        listAdapter.submitList(state.lists)
                    }
                    is QuizUiState.Error -> {
                        progressBar.visibility = View.GONE
                        showErrorDialog(state.message)
                    }
                    is QuizUiState.Playing -> {
                        progressBar.visibility = View.GONE
                        containerSelection.visibility = View.GONE
                        containerGame.visibility = View.VISIBLE

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

        // Handle Image logic with Error Logging
        if (state.question.imageUrl != null) {
            ivImage.visibility = View.VISIBLE
            Log.d("QuizFragment", "Loading Image: ${state.question.imageUrl}")

            ivImage.load(state.question.imageUrl) {
                crossfade(true)
                placeholder(android.R.drawable.ic_menu_gallery)
                error(android.R.drawable.ic_menu_report_image)
                listener(
                    onError = { _, result ->
                        Log.e("QuizFragment", "Image Load Error: ${result.throwable.message}")
                    }
                )
            }
        } else {
            ivImage.visibility = View.GONE
        }

        // Reset Buttons
        buttons.forEachIndexed { index, btn ->
            btn.text = state.question.options[index]
            btn.backgroundTintList = null
            btn.setTextColor(ContextCompat.getColor(requireContext(), android.R.color.white))
        }
    }

    private fun handleAnswerSelection(answer: String, selectedBtn: Button) {
        setButtonsEnabled(false)

        val state = viewModel.uiState.value
        if (state is QuizUiState.Playing) {
            val isCorrect = answer == state.question.correctAnswer

            if (isCorrect) {
                selectedBtn.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.holo_green_dark)
                // Manually update score text for immediate feedback
                val newScore = state.score + 1
                tvScore.text = "Score: $newScore"
            } else {
                selectedBtn.backgroundTintList = ContextCompat.getColorStateList(requireContext(), android.R.color.holo_red_dark)
                // Highlight the correct answer
                buttons.find { it.text == state.question.correctAnswer }?.backgroundTintList =
                    ContextCompat.getColorStateList(requireContext(), android.R.color.holo_green_dark)
            }

            // Wait 1 second before moving to next question
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
            .setPositiveButton("Back to Lists") { _, _ ->
                viewModel.loadLists() // Go back to selection
            }
            .setCancelable(false)
            .show()
    }

    private fun showGameOverDialog(score: Int, total: Int) {
        AlertDialog.Builder(requireContext())
            .setTitle("Quiz Completed!")
            .setMessage("You scored $score out of $total.")
            .setPositiveButton("Play Again") { _, _ ->
                viewModel.loadLists() // Restart current quiz
            }
            .setNegativeButton("Exit") { _, _ ->
                findNavController().navigateUp()
            }
            .setCancelable(false)
            .show()
    }
}