package com.example.cinephile.domain.quiz

import com.example.cinephile.domain.model.Movie
import com.example.cinephile.domain.model.QuestionType
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class QuizGeneratorTest {

    private lateinit var quizGenerator: QuizGenerator
    private lateinit var sampleMovies: List<Movie>

    @Before
    fun setUp() {
        quizGenerator = QuizGenerator()
        // Create 5 sample movies so we have enough data for options
        sampleMovies = listOf(
            Movie(1, "Inception", "http://url1", "http://url1", "A thief enters dreams...", "2010-07-16"),
            Movie(2, "Dark Knight", "http://url2", "http://url2", "Batman fights Joker...", "2008-07-18"),
            Movie(3, "Interstellar", "http://url3", "http://url3", "Astronauts go to space...", "2014-11-05"),
            Movie(4, "Dunkirk", "http://url4", "http://url4", "Soldiers evacuate beach...", "2017-07-21"),
            Movie(5, "Tenet", "http://url5", "http://url5", "Spies invert time...", "2020-08-26")
        )
    }

    @Test
    fun `generateQuizFromMovies returns null if fewer than 4 movies`() {
        val smallList = sampleMovies.take(3)
        val quiz = quizGenerator.generateQuizFromMovies("Fail Quiz", smallList)
        assertNull("Should return null because we need at least 4 movies for options", quiz)
    }

    @Test
    fun `generateQuizFromMovies returns valid quiz with correct size`() {
        val quiz = quizGenerator.generateQuizFromMovies("Test Quiz", sampleMovies, questionCount = 3)

        assertNotNull(quiz)
        assertEquals("Test Quiz", quiz?.name)
        assertEquals(3, quiz?.questions?.size)
    }

    @Test
    fun `generated questions have valid structure`() {
        val quiz = quizGenerator.generateQuizFromMovies("Test Quiz", sampleMovies)!!
        val question = quiz.questions[0]

        // 1. Check Options
        assertEquals(4, question.options.size)
        assertEquals(4, question.options.distinct().size) // Options must be unique
        assertTrue("Correct answer must be in options", question.options.contains(question.correctAnswer))

        // 2. Check Logic based on Type
        when (question.type) {
            QuestionType.RELEASE_YEAR -> {
                assertEquals("Answer should be a 4-digit year", 4, question.correctAnswer.length)
                assertTrue(question.questionText.contains(question.correctMovie.title))
            }
            QuestionType.GUESS_FROM_POSTER -> {
                assertNotNull("Poster question needs image URL", question.imageUrl)
                assertEquals(question.correctMovie.posterUrl, question.imageUrl)
                assertEquals(question.correctMovie.title, question.correctAnswer)
            }
            QuestionType.GUESS_FROM_PLOT -> {
                // The question text should NOT contain the title (it should be hidden)
                assertFalse(
                    "Plot question should obscure the movie title",
                    question.questionText.contains(question.correctMovie.title, ignoreCase = true)
                )
                assertEquals(question.correctMovie.title, question.correctAnswer)
            }
        }
    }
}