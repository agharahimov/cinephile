package com.example.cinephile.ui.quiz

import android.os.CountDownTimer
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinephile.data.local.UserListEntity
import com.example.cinephile.domain.model.Question
import com.example.cinephile.domain.model.Quiz
import com.example.cinephile.domain.repository.QuizRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class QuizUiState {
    object Loading : QuizUiState()
    data class Error(val message: String) : QuizUiState()
    data class ListSelection(val lists: List<UserListEntity>) : QuizUiState()
    data class Playing(
        val currentQuestionIndex: Int,
        val totalQuestions: Int,
        val question: Question,
        val score: Int
    ) : QuizUiState()
    data class GameOver(val score: Int, val totalQuestions: Int) : QuizUiState()
}

class QuizViewModel(private val repository: QuizRepository) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState

    // --- TIMER STATE ---
    private val _timerProgress = MutableStateFlow(100)
    val timerProgress: StateFlow<Int> = _timerProgress

    private var currentQuiz: Quiz? = null
    private var questionIndex = 0
    private var score = 0
    private var timer: CountDownTimer? = null
    private val TIME_PER_QUESTION = 15000L // 15 Seconds per question

    init {
        loadLists()
    }

    fun loadLists() {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            val result = repository.getAllLists()

            result.onSuccess { lists ->
                if (lists.isEmpty()) {
                    _uiState.value = QuizUiState.Error("No watchlists found. Create one first!")
                } else {
                    _uiState.value = QuizUiState.ListSelection(lists)
                }
            }.onFailure { e ->
                _uiState.value = QuizUiState.Error(e.message ?: "Failed to load lists")
            }
        }
    }

    fun startQuizForList(list: UserListEntity) {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            // Fetch quiz from repository
            val result = repository.generateQuizFromList(list.listId, list.name)

            result.onSuccess { quiz ->
                currentQuiz = quiz
                questionIndex = 0
                score = 0
                emitQuestion()
            }.onFailure { e ->
                // Show error but provide a way to go back to selection (by reloading lists)
                _uiState.value = QuizUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun submitAnswer(selectedOption: String) {
        // Stop timer immediately when user answers
        timer?.cancel()

        val quiz = currentQuiz ?: return
        val currentQ = quiz.questions[questionIndex]

        if (selectedOption == currentQ.correctAnswer) {
            score++
        }

        // Check if more questions exist
        if (questionIndex < quiz.questions.lastIndex) {
            questionIndex++
            emitQuestion()
        } else {
            _uiState.value = QuizUiState.GameOver(score, quiz.questions.size)
        }
    }

    private fun emitQuestion() {
        val quiz = currentQuiz ?: return

        // 1. Update UI to show question
        _uiState.value = QuizUiState.Playing(
            currentQuestionIndex = questionIndex,
            totalQuestions = quiz.questions.size,
            question = quiz.questions[questionIndex],
            score = score
        )

        startTimer()
    }

    private fun startTimer() {
        timer?.cancel()
        timer = object : CountDownTimer(TIME_PER_QUESTION, 100) {
            override fun onTick(millisUntilFinished: Long) {
                // Calculate percentage (100 -> 0)
                val progress = ((millisUntilFinished.toFloat() / TIME_PER_QUESTION) * 100).toInt()
                _timerProgress.value = progress
            }

            override fun onFinish() {
                _timerProgress.value = 0
                // Time is up! Submit empty answer (wrong)
                submitAnswer("")
            }
        }.start()
    }

    override fun onCleared() {
        super.onCleared()
        timer?.cancel()
    }
}