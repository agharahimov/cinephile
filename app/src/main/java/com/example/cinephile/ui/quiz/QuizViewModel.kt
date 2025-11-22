package com.example.cinephile.ui.quiz

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.cinephile.data.QuizRepositoryImpl
import com.example.cinephile.domain.model.Question
import com.example.cinephile.domain.model.Quiz
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

// UI State for the Quiz Screen
sealed class QuizUiState {
    object Loading : QuizUiState()
    data class Error(val message: String) : QuizUiState()
    data class Playing(
        val currentQuestionIndex: Int,
        val totalQuestions: Int,
        val question: Question,
        val score: Int
    ) : QuizUiState()
    data class GameOver(val score: Int, val totalQuestions: Int) : QuizUiState()
}

class QuizViewModel(private val repository: QuizRepositoryImpl) : ViewModel() {

    private val _uiState = MutableStateFlow<QuizUiState>(QuizUiState.Loading)
    val uiState: StateFlow<QuizUiState> = _uiState

    private var currentQuiz: Quiz? = null
    private var questionIndex = 0
    private var score = 0

    init {
        startNewQuiz()
    }

    fun startNewQuiz() {
        viewModelScope.launch {
            _uiState.value = QuizUiState.Loading
            val result = repository.generateWatchlistQuiz()

            result.onSuccess { quiz ->
                currentQuiz = quiz
                questionIndex = 0
                score = 0
                emitQuestion()
            }.onFailure { e ->
                _uiState.value = QuizUiState.Error(e.message ?: "Unknown error")
            }
        }
    }

    fun submitAnswer(selectedOption: String) {
        val quiz = currentQuiz ?: return
        val currentQ = quiz.questions[questionIndex]

        // Check logic
        if (selectedOption == currentQ.correctAnswer) {
            score++
        }

        // Move to next
        if (questionIndex < quiz.questions.lastIndex) {
            questionIndex++
            emitQuestion()
        } else {
            // Game Over
            _uiState.value = QuizUiState.GameOver(score, quiz.questions.size)
        }
    }

    private fun emitQuestion() {
        val quiz = currentQuiz ?: return
        _uiState.value = QuizUiState.Playing(
            currentQuestionIndex = questionIndex,
            totalQuestions = quiz.questions.size,
            question = quiz.questions[questionIndex],
            score = score
        )
    }
}