package com.example.clearer.fragments

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.NavHostFragment
import app.futured.donut.DonutProgressView
import app.futured.donut.DonutSection
import com.example.clearer.R
import com.example.clearer.databinding.FragmentHomeScreenBinding
import com.example.clearer.vms.AnalyticsFragmentViewModel
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale


class HomeScreenFragment : Fragment(R.layout.fragment_home_screen) {

    private var _binding: FragmentHomeScreenBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firebase: FirebaseFirestore
    private lateinit var userId: String

    private val model: AnalyticsFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentHomeScreenBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        firebase = FirebaseFirestore.getInstance()
        userId = auth.currentUser!!.uid

        if (userId.isNotEmpty()) {
            getUserData()
        }

        binding.btnNeedHelp.setOnClickListener {
            val navController = NavHostFragment.findNavController(this)
            navController.popBackStack()
            navController.navigate(R.id.fragment_camera)
        }

        binding.txtViewMore.setOnClickListener {
            val navController = NavHostFragment.findNavController(this)
            navController.popBackStack()
            navController.navigate(R.id.fragment_analytics)
        }

        binding.btnMorePoints.setOnClickListener {
            val navController = NavHostFragment.findNavController(this)
            navController.popBackStack()
            navController.navigate(R.id.fragment_practice)
        }
    }

    private fun updateOverallAverage(category: String, textView: TextView) {
        model.getOverallAverage(userId, category).addOnCompleteListener {
            if (it.isSuccessful) {
                textView.text = it.result.toString() + "%"
            }
        }
    }

    private fun updateDonutChart(donutViewId: Int, textView: TextView, progress: Int, color: String) {
        val donut = view?.findViewById<DonutProgressView>(donutViewId)
        textView.text = "${progress}%"

        val section1 = DonutSection(
            name = "Complete",
            color = Color.parseColor(color),
            amount = progress.toFloat()
        )
        val section2 = DonutSection(
            name = "Not complete",
            color = Color.parseColor("#D9D9D9"),
            amount = (100.0 - progress).toFloat()
        )

        donut?.cap = 100f
        donut?.submitData(listOf(section1, section2))
    }

    private fun getUserData() {
        val docRef = firebase.collection("users").document(userId)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("Firebase", "Document saved")
                    binding.txtWelcome.text =
                        "Hello " + document.data?.getValue("firstName").toString()
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } + '!'

                    val pointsValue = document.data?.getValue("points") as? Number ?: 0
                    val pointsString = pointsValue.toInt().toString() + " points"
                    binding.txtPoints.text = pointsString

                    if (pointsValue.toInt() < 50) {
                        binding.imgTrophy.setImageResource(R.drawable.bronze)
                    } else if (pointsValue.toInt() < 300) {
                        binding.imgTrophy.setImageResource(R.drawable.silver)
                    } else {
                        binding.imgTrophy.setImageResource(R.drawable.gold)
                    }

                } else {
                    Log.d("Firebase", "No such document")
                }

                updateOverallAverage("Adjectives", binding.txtAdjectivesCount)
                updateOverallAverage("Nouns", binding.txtNounsCount)
                updateOverallAverage("Pronouns", binding.txtPronounsCount)
                updateOverallAverage("Verbs", binding.txtVerbsCount)

                model.getProgress(userId, "Easy").addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val progress = task.result
                        updateDonutChart(R.id.donutEasy, binding.txtEasyPercent, progress, "#CDFFB6")
                    }
                }

                model.getProgress(userId, "Medium").addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val progress = task.result
                        updateDonutChart(R.id.donutMedium, binding.txtMediumPercent, progress, "#FFEF64")
                    }
                }

                model.getProgress(userId, "Hard").addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val progress = task.result
                        updateDonutChart(R.id.donutHard, binding.txtHardPercent, progress, "#FF9B9B")
                    }
                }

            }
            .addOnFailureListener { exception ->
                Log.d("Firebase", "Could not save", exception)
            }
    }
}