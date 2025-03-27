package com.example.clearer

import com.example.clearer.models.Problem
import com.example.clearer.vms.QuizFragmentViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.MockitoAnnotations
import org.mockito.junit.MockitoJUnitRunner

@RunWith(MockitoJUnitRunner::class)
class QuizViewModelUnitTest {

    @Mock
    private lateinit var mockAuth: FirebaseAuth
    @Mock
    private lateinit var mockFire: FirebaseFirestore
    @Mock
    private lateinit var mockUser: FirebaseUser

    private lateinit var model: QuizFragmentViewModel

    @Before
    fun setUp() {
        MockitoAnnotations.openMocks(this)
        Mockito.lenient().`when`(mockAuth.currentUser).thenReturn(mockUser)
        Mockito.lenient().`when`(mockUser.uid).thenReturn("test")
        model = QuizFragmentViewModel(mockAuth, mockFire, mockUser.uid)
    }

    @Test
    fun calculateTotalCorrect_EmptyList() {
        // Arrange
        val listToCheck = mutableListOf("word1", "word2", "word3", "word4")
        val tappedWords = mutableListOf("")
        val quizType = "Noun"

        // Act
        model.listToCheck = listToCheck
        model.tappedWords = tappedWords
        model.quizType = quizType
        val results = model.calculateTotalCorrect(quizType)
        val average = results.second

        //Assert
        assertEquals(0, average)
    }

    @Test
    fun calculateTotalCorrect_OneInList() {
        // Arrange
        val listToCheck = mutableListOf("word1", "word2", "word3", "word4",
            "word5", "word6", "word7", "word8", "word9", "word10")
        val tappedWords = mutableListOf("word1")
        val quizType = "Noun"

        // Act
        model.listToCheck = listToCheck
        model.tappedWords = tappedWords
        model.quizType = quizType
        val results = model.calculateTotalCorrect(quizType)
        val average = results.second

        //Assert
        assertEquals(1, average)
    }

    @Test
    fun calculateTotalCorrect_PopulatedList() {
        // Arrange
        val listToCheck = mutableListOf("word1", "word2", "word3", "word4")
        val tappedWords = mutableListOf("word1", "word2")
        val quizType = "Noun"

        // Act
        model.listToCheck = listToCheck
        model.tappedWords = tappedWords
        model.quizType = quizType
        val results = model.calculateTotalCorrect(quizType)
        val average = results.second

        //Assert
        assertEquals(5, average)
    }

    @Test
    fun calculateTotalCorrect_FullList() {
        // Arrange
        val listToCheck = mutableListOf("word1", "word2", "word3", "word4")
        val tappedWords = mutableListOf("word1", "word2", "word3", "word4")
        val quizType = "Noun"

        // Act
        model.listToCheck = listToCheck
        model.tappedWords = tappedWords
        model.quizType = quizType
        val results = model.calculateTotalCorrect(quizType)
        val average = results.second

        //Assert
        assertEquals(10, average)
    }

    @Test
    fun maxSelectedWords_ListPopulated() {
        // Arrange
        val listToCheck = mutableListOf("word1", "word2", "word3")

        // Act
        model.listToCheck = listToCheck
        model.maxSelectedWords()

        // Assert
        assertEquals(listToCheck.size, model.maxSelectedWords)
    }

    @Test
    fun maxSelectedWords_OneInList() {
        // Arrange
        val listToCheck = mutableListOf("word1")

        // Act
        model.listToCheck = listToCheck
        model.maxSelectedWords()

        // Assert
        assertEquals(listToCheck.size, model.maxSelectedWords)
    }

    @Test
    fun maxSelectedWords_ListEmpty() {
        // Arrange
        val listToCheck = mutableListOf("")

        // Act
        model.listToCheck = listToCheck
        model.maxSelectedWords()

        // Assert
        assertEquals(1, model.maxSelectedWords)
    }

    @Test
    fun saveUserAnswers_PopulatedList() {
        // Arrange
        val correctWords = mutableListOf("word1", "word2", "word3")
        val partOfSpeech = "Noun"
        val problem = Problem(image = null)
        model.problem = problem
        model.correctWords = correctWords

        // Act
        model.saveUserAnswers(partOfSpeech)

        // Assert
        assertEquals(correctWords, problem.apiNounsList)
    }

    @Test
    fun saveUserAnswers_OneInList() {
        // Arrange
        val correctWords = mutableListOf("word1")
        val partOfSpeech = "Noun"
        val problem = Problem(image = null)
        model.problem = problem
        model.correctWords = correctWords

        // Act
        model.saveUserAnswers(partOfSpeech)

        // Assert
        assertEquals(correctWords, problem.apiNounsList)
    }

    @Test
    fun saveUserAnswers_EmptyList() {
        // Arrange
        val correctWords: MutableList<String> = mutableListOf()
        val partOfSpeech = "Noun"
        val problem = Problem(image = null)
        model.problem = problem
        model.correctWords = correctWords

        // Act
        model.saveUserAnswers(partOfSpeech)

        // Assert
        assertEquals(correctWords, problem.apiNounsList)
    }

    @Test
    fun manageTappedWords_AddWord(){
        // Arrange
        val word = "word"
        val tappedWords = mutableListOf("different")
        model.maxSelectedWords = 2
        model.tappedWords = tappedWords

        // Act
        model.manageTappedWords(word)

        // Assert
        assertEquals(mutableListOf("different","word"), model.tappedWords)
    }

    @Test
    fun manageTappedWords_RemoveWord(){
        // Arrange
        val word = "Different.."
        val tappedWords = mutableListOf("different", "word")
        model.maxSelectedWords = 2
        model.tappedWords = tappedWords

        // Act
        model.manageTappedWords(word)

        // Assert
        assertEquals(mutableListOf("word"), model.tappedWords)
    }




}