package com.example.clearer.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment
import com.example.clearer.activities.MainActivity
import com.example.clearer.R
import com.example.clearer.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Locale

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firebase: FirebaseFirestore
    private lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentProfileBinding.inflate(inflater, container, false)
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

        if (userId.isNotEmpty()){
            getUserData()
        }

        binding.btnHistory.setOnClickListener{
            val navController = NavHostFragment.findNavController(this)
            navController.popBackStack()
            navController.navigate(R.id.historyFragment)
        }

        binding.btnLogOut.setOnClickListener{
            logOut()
        }
    }

    private fun getUserData() {
        val docRef = firebase.collection("users").document(userId)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("Firebase", "Document saved")
                    binding.txtFirstName.text = document.data?.getValue("firstName").toString()
                        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }
                    binding.txtEmail.text = document.data?.getValue("email").toString()

                    val pointsValue = document.data?.getValue("points") as? Number ?: 0
                    val pointsString = pointsValue.toInt().toString() + " points"
                    binding.txtPoints.text = pointsString

                    if (pointsValue.toInt() < 50) {
                        binding.trophy.setImageResource(R.drawable.bronze)
                    } else if (pointsValue.toInt() < 300) {
                        binding.trophy.setImageResource(R.drawable.silver)
                    } else {
                        binding.trophy.setImageResource(R.drawable.gold)
                    }

                } else {
                    Log.d("Firebase", "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d("Firebase", "Could not save", exception)
            }
    }

    private fun logOut() {
        FirebaseAuth.getInstance().signOut()
        val intent = Intent (activity, MainActivity::class.java)
        activity?.startActivity(intent)
    }
}