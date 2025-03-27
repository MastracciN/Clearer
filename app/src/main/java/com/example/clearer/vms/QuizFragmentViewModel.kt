package com.example.clearer.vms

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.clearer.models.Problem
import com.example.clearer.models.TextRazorAPI
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import kotlin.math.roundToLong

class QuizFragmentViewModel(
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firebase: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private var userId: String = auth.currentUser!!.uid) : ViewModel() {

    // Document Id passed to fragment
    var documentId : String = "none"

    // Stores problem information and user attempt
    lateinit var problem : Problem

    // Stores Quiz Type
    lateinit var quizType : String

    // Words currently selected by the user
    var tappedWords: MutableList<String> = mutableListOf()

    // Reference to the relevant list for answers
    var listToCheck: MutableList<String>? = mutableListOf()

    // Words user selected that were correct
    var correctWords: MutableList<String> = mutableListOf()

    // Maximum words user can select
    var maxSelectedWords: Int = 0

    // Used when user is creating their own quiz via image to text
    fun createQuiz(text: String){
        callTextRazor(text)
        listsToLowercase()
        removeDuplicates()
        setListToCheck()
        maxSelectedWords()
    }

    // Function calls TextRazor API and returns a response with the sent text analyzed
    private fun callTextRazor(renderedText: String) {
        val textRazor = TextRazorAPI.callTextRazor(renderedText)

        this.problem = Problem(
            image = null,
            problemString = renderedText,
            apiAdjectivesList = textRazor[0],
            apiNounsList = textRazor[1],
            apiPronounsList = textRazor[2],
            apiVerbsList = textRazor[3],
            date = currentDateToString()
        )
        problem.apiNounsList?.toString()?.let { Log.d("problem", it) }
        Log.d("ABC","API Nouns List: ${problem.apiNounsList}")

    }

    private fun currentDateToString(): String {
        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        return currentDateTime.format(formatter)
    }

    private fun listsToLowercase(){
        // Make lists elements lowercase so they can be compared to tappedWords later
        problem.apiNounsList?.replaceAll(String::lowercase)
        problem.apiVerbsList?.replaceAll(String::lowercase)
        problem.apiPronounsList?.replaceAll(String::lowercase)
        problem.apiAdjectivesList?.replaceAll(String::lowercase)
    }

    // Removes duplicate words from API List
    // Necessary because when user selects word "moose", all instances of "moose" are highlighted automatically
    private fun removeDuplicates() {
        problem.apiNounsList = problem.apiNounsList?.takeIf { it.isNotEmpty() }?.distinct() as MutableList<String>?
        problem.apiVerbsList = problem.apiVerbsList?.takeIf { it.isNotEmpty() }?.distinct() as MutableList<String>?
        problem.apiPronounsList = problem.apiPronounsList?.takeIf { it.isNotEmpty() }?.distinct() as MutableList<String>?
        problem.apiAdjectivesList = problem.apiAdjectivesList?.takeIf { it.isNotEmpty() }?.distinct() as MutableList<String>?
        Log.d("ABC", "removeDuplicates: ${problem.apiNounsList}")
    }

    // Sets listToCheck variable based on quiz's type
    private fun setListToCheck() {
        listToCheck = when (quizType) {
            "Noun" -> problem.apiNounsList
            "Verb" -> problem.apiVerbsList
            "Adjective" -> problem.apiAdjectivesList
            "Pronoun" -> problem.apiPronounsList
            else -> {
                problem.apiNounsList
            }
        }
        Log.d("ABC","setListToCheck(): $listToCheck")
    }

    // Sets variable to represent max. number of words user can select
    // If user creates quiz with 0 answers(words), user can only select 1 word, trick question
    fun maxSelectedWords(){
        maxSelectedWords = listToCheck!!.size
        if (maxSelectedWords == 0)
            maxSelectedWords = 1
    }

    // Function helps manage state of highlights made by user
    // Adds word to list if it is not present in list or removes word if it is already in list
    fun manageTappedWords(word: String){
        val lowercaseWord = word.lowercase()    // Make word lowercase to avoid "Apple" and "apple" being added
        val cleanWord = lowercaseWord.replace(Regex("[^\\w\\s\\-\']"),"") // Clean word of punctuation otherwise "apple" and "apple." are different
        val clickedWord = tappedWords.firstOrNull { it == cleanWord }
        // If word not present in list, user is adding it, otherwise if it is, they are removing it
        if (clickedWord == null){
            // Prevents user from selecting more words than allowed, If listToCheck.size=4 then tappedWords.size<=4
            if (tappedWords.size < maxSelectedWords){
                tappedWords.add(cleanWord)
            }
        }
        else
            tappedWords.remove(cleanWord)

//        Log.d("ABC", "" + tappedWords)
    }

    fun finishQuiz(quizType: String): Pair<String, Long>{
        val result = calculateTotalCorrect(quizType)
        val results = result.first
        val average = result.second

        addPointsToUser(average)
        saveUserAnswers(quizType)
        addProblemAttemptToFirebase()
        tappedWords.clear()

        return Pair(results, average)
    }

    // Calculates user's score, returns string for results screen
    fun calculateTotalCorrect(partOfSpeech: String): Pair<String, Long> {
        val totalCorrect = listToCheck?.size
        var correct = 0
        for (word in tappedWords){
            if (listToCheck!!.contains(word)) {
                correct += 1
                correctWords.add(word)
            }
        }

        var correctWordsFormat = ""
        for (i in 0 until correctWords.size) {
            correctWordsFormat += correctWords[i]
            if (i < correctWords.size - 1) {
                correctWordsFormat += ", "
            }
        }

        val resultString = "You found $correct of the $totalCorrect $partOfSpeech" + "s.\n" +
                "${quizType}s found: ${correctWordsFormat}\n"

        var average = 0.toLong()
        if (correct > 0)
            average = (correct/totalCorrect!!.toDouble()*10).roundToLong()

        return Pair(resultString, average)
    }

    // Save correctly selected words to relevant list (ie. userNounsList)
    fun saveUserAnswers(partOfSpeech: String){
        val userListToCheck : MutableList<String> = when (partOfSpeech) {
            "Noun" -> problem.userNounsList
            "Verb" -> problem.userVerbsList
            "Adjective" -> problem.userAdjectivesList
            "Pronoun" -> problem.userPronounsList
            else -> {
                problem.userNounsList
            }
        }
        for (word in correctWords)
            userListToCheck.add(word)

//        Log.d("ABC", "saveUserAnswers(): $userListToCheck")
    }

    private fun addPointsToUser(points: Long) {
        Log.d("ABC", "points: $points")
        // Reference to the specific user's document
        val userDocRef = firebase.collection("users").document(userId)

        // Increment the 'points' field
        userDocRef.update("points", FieldValue.increment(10 + points))
            .addOnSuccessListener {
                // Handle success, e.g., update UI or log success
                Log.d("Firebase", "Points successfully incremented")
            }
            .addOnFailureListener { e ->
                // Handle failure, e.g., log error
                Log.w("Firebase", "Error incrementing points", e)
            }
    }

    // Save user attempt to Firebase
    private fun addProblemAttemptToFirebase(){
        val quizTypes = quizType + "s"

        // If user performed Firestore provided question, save to problems_attempted
        if (documentId != "none") {
            firebase.collection("users")
                .document(userId)
                .collection("problems_attempted")
                .document("AdminAssessment")
                .collection(quizTypes)
                .document(documentId)
                .set(problem)
                .addOnSuccessListener { documentReference ->
                    Log.d("ABC", "Problem added: ${documentId}")
                }
                .addOnFailureListener {
                    Log.d("ABC", "Error adding document")
                }

        // else user performed own question, save to problems_generated
        } else {
            firebase.collection("users")
                .document(userId)
                .collection("problems_generated")
                .add(problem)
                .addOnSuccessListener { documentReference ->
                    Log.d("ABC", "Problem added: ${documentId}")
                }
        }
    }

    // Get problem from Firestore using document id
    fun getProblemById(documentId: String){
        val quizTypes = quizType + "s"
//        Log.d("ABC","getProblemById():\nType: $quizTypes\nID: $documentId")

        firebase.collection("problems")
            .document("Categories")
            .collection(quizTypes)
            .document(documentId)
            .get()
            .addOnSuccessListener { documentReference ->
                if (documentReference != null){
                    problem = Problem(
                        image = null,
                        difficulty = documentReference.data!!["difficulty"].toString(),
                        problemString = documentReference.data!!["problemString"].toString(),
                        apiNounsList = documentReference.data!!["nounsList"] as MutableList<String>?,
                        apiVerbsList = documentReference.data!!["verbsList"] as MutableList<String>?,
                        apiAdjectivesList = documentReference.data!!["adjectivesList"] as MutableList<String>?,
                        apiPronounsList = documentReference.data!!["pronounsList"] as MutableList<String>?,
                        date = currentDateToString()
                    )
//                    Log.d("ABC","Problem String:${problem.problemString}\nProblem Nouns List:${problem.apiNounsList}")
                    listsToLowercase()
                    removeDuplicates()
                    setListToCheck()
                    maxSelectedWords()
                } else {
//                    Log.d("ABC","No document found")
                }
            }
            .addOnFailureListener { e ->
//                Log.e("ABC", "Error getting document: $e")
            }

    }

    // Clears all words in lists for next use
    fun resetLists(){
        tappedWords.clear()
        problem = Problem(image = null)
    }

}
