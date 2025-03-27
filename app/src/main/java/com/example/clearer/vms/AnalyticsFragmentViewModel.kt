package com.example.clearer.vms

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.clearer.models.Problem
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.toObject
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.ktx.functions
import com.google.firebase.ktx.Firebase
import kotlin.math.roundToInt

class AnalyticsFragmentViewModel: ViewModel() {
    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var firebase: FirebaseFirestore = FirebaseFirestore.getInstance()
    private var functions: FirebaseFunctions = Firebase.functions
    private var userId: String = auth.currentUser!!.uid
    fun getAttempts(type: String, difficulty: String, callback: (Int) -> Unit) {

        firebase.collection("users")
            .document(userId)
            .collection("problems_attempted")
            .document("AdminAssessment")
            .collection(type)
            .whereEqualTo("difficulty", difficulty)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Invoke the callback with the count
                    val count = task.result?.size() ?: 0
                    callback(count)
                } else {
                    Log.d("ABC", "Error getting documents: ", task.exception)
                    callback(0)
                }
            }
    }

    fun getAttemptsSelf(type: String, callback: (Int) -> Unit) {

        val fieldName = when (type) {
            "Adjectives" -> "userAdjectivesList"
            "Nouns" -> "userNounsList"
            "Pronouns" -> "userPronounsList"
            "Verbs" -> "userVerbsList"
            else -> return callback(0) // If type is not recognized, return 0
        }

        firebase.collection("users")
            .document(userId)
            .collection("problems_generated")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var count = 0
                    for (document in task.result!!) {
                        // Check if the specified field is not null
                        val list = document.data[fieldName] as? List<*>
                        Log.d("ABC", "List $list")
                        if (list != null && list.isNotEmpty()) {
                            count++
                        }
                    }
                    // Invoke the callback with the count
                    callback(count)
                } else {
                    Log.d("ABC", "Error getting documents: ", task.exception)
                    callback(0)
                }
            }
    }

    fun getProgress(userId: String, difficulty: String): Task<Int> {
        // Create the arguments to the callable function
        val data = hashMapOf(
            "userId" to userId,
            "difficulty" to difficulty
        )

        return functions
            .getHttpsCallable("getProgressByDifficulty")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then getResult() will throw an Exception which will be
                // propagated down.
                val result = (task.result?.data as? Number)?.toDouble()?.roundToInt() ?: 0
                result
            }
    }

    fun getOverallAverage(userId: String, partOfSpeech: String): Task<Int> {
        // Create the arguments to the callable function.
        val easy = when (partOfSpeech) {
            "Adjectives" -> getAdjectivesEasy(userId)
            "Nouns" -> getNounsEasy(userId)
            "Pronouns" -> getPronounsEasy(userId)
            "Verbs" -> getVerbsEasy(userId)
            else -> throw IllegalArgumentException("Invalid part of speech")
        }
        val medium = when (partOfSpeech) {
            "Adjectives" -> getAdjectivesMedium(userId)
            "Nouns" -> getNounsMedium(userId)
            "Pronouns" -> getPronounsMedium(userId)
            "Verbs" -> getVerbsMedium(userId)
            else -> throw IllegalArgumentException("Invalid part of speech")
        }
        val hard = when (partOfSpeech) {
            "Adjectives" -> getAdjectivesHard(userId)
            "Nouns" -> getNounsHard(userId)
            "Pronouns" -> getPronounsHard(userId)
            "Verbs" -> getVerbsHard(userId)
            else -> throw IllegalArgumentException("Invalid part of speech")
        }

        // Wait for all tasks to complete and then calculate the sum
        return Tasks.whenAll(easy, medium, hard)
            .continueWithTask { task ->
                if (task.isSuccessful) {
                    val result = (easy.result + medium.result + hard.result)/3
                    Log.d("Firebase", "result easy medium ${easy.result} ${medium.result} ${hard.result} ${result}")
                    Tasks.forResult(result)
                } else {
                    // Handle the case where any of the tasks failed
                    task.exception?.let {
                        throw it
                    } ?: Tasks.forException(RuntimeException("Unknown error occurred"))
                }
            }
    }

    // ADJECTIVES
    fun getAdjectivesEasy(userId: String): Task<Int> {
        // Create the arguments to the callable function.
        val data = hashMapOf(
            "userId" to userId,
        )
        return functions
            .getHttpsCallable("getAdjectivesEasy")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data as Int
                result
            }
    }

    fun getAdjectivesMedium(userId: String): Task<Int> {
        // Create the arguments to the callable function.
        val data = hashMapOf(
            "userId" to userId,
        )
        return functions
            .getHttpsCallable("getAdjectivesMedium")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data as Int
                result
            }
    }

    fun getAdjectivesHard(userId: String): Task<Int> {
        // Create the arguments to the callable function.
        val data = hashMapOf(
            "userId" to userId,
        )
        return functions
            .getHttpsCallable("getAdjectivesHard")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data as Int
                result
            }
    }

    // VERBS
    fun getVerbsEasy(userId: String): Task<Int> {
        // Create the arguments to the callable function.
        val data = hashMapOf(
            "userId" to userId,
        )
        return functions
            .getHttpsCallable("getVerbsEasy")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data as Int
                result
            }
    }

    fun getVerbsMedium(userId: String): Task<Int> {
        // Create the arguments to the callable function.
        val data = hashMapOf(
            "userId" to userId,
        )
        return functions
            .getHttpsCallable("getVerbsMedium")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data as Int
                result
            }
    }

    fun getVerbsHard(userId: String): Task<Int> {
        // Create the arguments to the callable function.
        val data = hashMapOf(
            "userId" to userId,
        )
        return functions
            .getHttpsCallable("getVerbsHard")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data as Int
                result
            }
    }

    // NOUNS
    fun getNounsEasy(userId: String): Task<Int> {
        // Create the arguments to the callable function.
        val data = hashMapOf(
            "userId" to userId,
        )
        return functions
            .getHttpsCallable("getNounsEasy")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data as Int
                result
            }
    }

    fun getNounsMedium(userId: String): Task<Int> {
        // Create the arguments to the callable function.
        val data = hashMapOf(
            "userId" to userId,
        )
        return functions
            .getHttpsCallable("getNounsMedium")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data as Int
                result
            }
    }

    fun getNounsHard(userId: String): Task<Int> {
        // Create the arguments to the callable function.
        val data = hashMapOf(
            "userId" to userId,
        )
        return functions
            .getHttpsCallable("getNounsHard")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data as Int
                result
            }
    }

    // PRONOUNS
    fun getPronounsEasy(userId: String): Task<Int> {
        // Create the arguments to the callable function.
        val data = hashMapOf(
            "userId" to userId,
        )
        return functions
            .getHttpsCallable("getPronounsEasy")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data as Int
                result
            }
    }

    fun getPronounsMedium(userId: String): Task<Int> {
        // Create the arguments to the callable function.
        val data = hashMapOf(
            "userId" to userId,
        )
        return functions
            .getHttpsCallable("getPronounsMedium")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data as Int
                result
            }
    }

    fun getPronounsHard(userId: String): Task<Int> {
        // Create the arguments to the callable function.
        val data = hashMapOf(
            "userId" to userId,
        )
        return functions
            .getHttpsCallable("getPronounsHard")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data as Int
                result
            }
    }

    // SELF ASSESSMENT
    fun getSelfAssessmentAdjectives(userId: String): Task<Int> {
        // Create the arguments to the callable function.
        val data = hashMapOf(
            "userId" to userId,
        )
        return functions
            .getHttpsCallable("getSelfAssessmentAdjectives")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data as Int
                result
            }
    }
    fun getSelfAssessmentVerbs(userId: String): Task<Int> {
        // Create the arguments to the callable function.
        val data = hashMapOf(
            "userId" to userId,
        )
        return functions
            .getHttpsCallable("getSelfAssessmentVerbs")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data as Int
                result
            }
    }
    fun getSelfAssessmentPronouns(userId: String): Task<Int> {
        // Create the arguments to the callable function.
        val data = hashMapOf(
            "userId" to userId,
        )
        return functions
            .getHttpsCallable("getSelfAssessmentPronouns")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data as Int
                result
            }
    }
    fun getSelfAssessmentNouns(userId: String): Task<Int> {
        // Create the arguments to the callable function.
        val data = hashMapOf(
            "userId" to userId,
        )
        return functions
            .getHttpsCallable("getSelfAssessmentNouns")
            .call(data)
            .continueWith { task ->
                // This continuation runs on either success or failure, but if the task
                // has failed then result will throw an Exception which will be
                // propagated down.
                val result = task.result?.data as Int
                result
            }
    }
}

