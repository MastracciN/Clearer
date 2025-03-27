package com.example.clearer.vms

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.clearer.adapter.PracticeAdapter
import com.example.clearer.models.Practice
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PracticeFragmentViewModel : ViewModel() {

    private var auth: FirebaseAuth = FirebaseAuth.getInstance()
    private var userId: String = auth.currentUser!!.uid
    private var firebase: FirebaseFirestore = FirebaseFirestore.getInstance()

    lateinit var practiceList: ArrayList<Practice>
    lateinit var adapter: PracticeAdapter

    // Populate RecyclerView with relevant data
    fun populateRecyclerView(type: String, diff: String){
        practiceList.clear()
        val types = type + "s"
        getPracticeByTypeAndDifficulty(types, diff)
    }

    // Returns Firebase problems collection based on quiz type and difficulty
    // Populates recycler view
    private fun getPracticeByTypeAndDifficulty(type: String, diff: String){
        // Query Firestore for data
        firebase.collection("problems")
            .document("Categories")
            .collection(type)
            .whereEqualTo("difficulty", diff)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    var counter = 1
                    for (document in task.result) {
                        val problemString = document.data["problemString"].toString()
                        val attempted = false
                        val practice = Practice(
                            "Quiz $counter",
                            problemString,
                            document.id,
                            attempted
                        )
                        counter += 1
                        practiceList.add(practice)
                        Log.d("ABC",practice.quizDescription)
                    }
                    // Notify the adapter that the data has changed
                    adapter.notifyDataSetChanged()
                } else {
                    // Handle the error
                    Log.e("ABC", "Error getting documents: ", task.exception)
                }
            }
    }

    // Checks if user has completed this question before
    // Returns true if completed before, else false
    private fun checkAttempted(type: String, documentId: String): Boolean {
        var attempted = false
        firebase.collection("users")
            .document(userId)
            .collection("problems_attempted")
            .document("AdminAssessment")
            .collection(type)
            .document(documentId)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful)
                    attempted = true
                Log.d("ABC","Attempted: $attempted")
            }

        return attempted
    }

}
