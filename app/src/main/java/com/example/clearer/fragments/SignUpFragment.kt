package com.example.clearer.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.clearer.R
import com.example.clearer.activities.SignedInActivity
import com.example.clearer.databinding.FragmentSignUpBinding
import com.example.clearer.models.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpFragment : Fragment(R.layout.fragment_sign_up) {

    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        binding.btnSignup.setOnClickListener{
            saveUser()
        } //btnSignup

        binding.txtLogin.setOnClickListener{
            val fragment = LoginFragment()
            val transaction = parentFragmentManager.beginTransaction()
            transaction.replace(R.id.activity_main_nav_host_fragment,fragment).commit()
        }
    }

    private fun saveUser() {
        //TODO: implement profile image storage collection, problem image storage collection

        val name = binding.inputFirstName.text.toString()
        val email = binding.inputEmail.text.toString()
        val password = binding.inputPassword.text.toString()
        val newUser = User(name, email)

        if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty()){
            auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener{
                val uid = auth.currentUser?.uid
                if (it.isSuccessful){
                    if (uid != null){
                        firestore.collection("users")
                            .document(uid)
                            .set(newUser)
                        Toast.makeText(activity, "User Created", Toast.LENGTH_SHORT).show()
                    }
                    val intent = Intent (activity, SignedInActivity::class.java)
                    activity?.startActivity(intent)
                }
                else {
                    Toast.makeText(activity, it.exception.toString(), Toast.LENGTH_SHORT).show()
                }
            }
        }
        else {
            Toast.makeText(activity, "Empty fields are not allowed", Toast.LENGTH_SHORT).show()
        }
    }
}