package com.example.clearer.vms

import android.util.Log
import androidx.lifecycle.ViewModel
import com.example.clearer.adapter.PracticeAdapter
import com.example.clearer.models.Practice
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class HistoryFragmentViewModel(
    auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val firebase: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private var userId: String = auth.currentUser!!.uid) : ViewModel() {

    lateinit var adapter: PracticeAdapter
    lateinit var practiceList: ArrayList<Practice>

    // Populates the practice list
    fun populatePracticeLists(){
        val categories = listOf("Nouns", "Verbs", "Adjectives", "Pronouns")
        getGeneratedProblems()
        for (category in categories){
            getProblems(category, practiceList)
        }
    }

    // Returns Firestore documents from a specific collection and adds to a list
    private fun getProblems(problemType: String, list: ArrayList<Practice>){
        firebase.collection("users")
            .document(userId)
            .collection("problems_attempted")
            .document("AdminAssessment")
            .collection(problemType)
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val problemString = document.data["problemString"].toString()
                        val date = stringToLocalDateTime(document.data["date"].toString())
                        val quizName = "$problemType - ${document.data["difficulty"].toString()}"
                        val practice = Practice(
                            quizName,
                            problemString,
                            document.id,
                            document.data["isAttempted"] as? Boolean?: false,
                            date
                        )
                        list.add(practice)
                    }
                    Log.d("ABC","Practice List: $practiceList")
                    sortList("Newest First")
                    adapter.notifyDataSetChanged()
                } else {
                    Log.e("ABC", "Error getting documents: ", task.exception)
                }
            }

    }

    private fun getGeneratedProblems(){
        firebase.collection("users")
            .document(userId)
            .collection("problems_generated")
            .get()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    for (document in task.result) {
                        val problemString = document.data["problemString"].toString()
                        val date = stringToLocalDateTime(document.data["date"].toString())
                        val practice = Practice(
                            "Self Assessment",
                            problemString,
                            document.id,
                            document.data["isAttempted"] as? Boolean?: false,
                            date
                        )
                        practiceList.add(practice)
//                        Log.d("ABC", "$practice")
                    }
//                    Log.d("ABC", "Attempted: $attemptedList\nGenerated: $generatedList")
                    Log.d("ABC","Practice List: $practiceList")
                    adapter.notifyDataSetChanged()
                } else {
                    Log.e("ABC", "Error getting documents: ", task.exception)
                }
            }
    }

    // Convert date string to LocalDateTime format
    fun stringToLocalDateTime(date: String): LocalDateTime {
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
        return LocalDateTime.parse(date, formatter)
    }

    // Sorts list and updates RecyclerView
    fun sortList(sortMethod: String){
        sortListByDate(sortMethod)
        adapter.notifyDataSetChanged()
    }

    // Sorts list by date based on provided sort method
    fun sortListByDate(sortMethod: String){
        if (sortMethod == "Newest First")
            practiceList.sortByDescending { it.date?.dayOfYear }
        else
            practiceList.sortBy { it.date?.dayOfYear }
    }



}