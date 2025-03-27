package com.example.clearer.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.camera.core.impl.Observable
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.example.clearer.R
import com.example.clearer.databinding.FragmentAnalyticsBinding
import com.example.clearer.vms.AnalyticsFragmentViewModel
import com.google.firebase.auth.FirebaseAuth

class AnalyticsFragment : Fragment(R.layout.fragment_analytics) {

    //auto size textview = https://developer.android.com/develop/ui/views/text-and-emoji/autosizing-textview
    private var _binding: FragmentAnalyticsBinding? = null
    private val binding get() = _binding!!

    private lateinit var auth: FirebaseAuth
    private lateinit var userId: String

    private val model:AnalyticsFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAnalyticsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        auth = FirebaseAuth.getInstance()
        userId = auth.currentUser!!.uid

        // ADJECTIVES
        model.getAdjectivesEasy(userId).addOnCompleteListener {
            if (it.isSuccessful){
                binding.txtAdjectivesEasy.text = it.result.toString() + "%"
            }
        }
        model.getAdjectivesMedium(userId).addOnCompleteListener {
            if (it.isSuccessful){
                binding.txtAdjectivesMedium.text = it.result.toString() + "%"
            }
        }
        model.getAdjectivesHard(userId).addOnCompleteListener {
            if (it.isSuccessful){
                binding.txtAdjectivesHard.text = it.result.toString() + "%"
            }
        }

        // NOUNS
        model.getNounsEasy(userId).addOnCompleteListener {
            if (it.isSuccessful){
                binding.txtNounsEasy.text = it.result.toString() + "%"
            }
        }
        model.getNounsMedium(userId).addOnCompleteListener {
            if (it.isSuccessful){
                binding.txtNounsMedium.text = it.result.toString() + "%"
            }
        }
        model.getNounsHard(userId).addOnCompleteListener {
            if (it.isSuccessful){
                binding.txtNounsHard.text = it.result.toString() + "%"
            }
        }

        // PRONOUNS
        model.getPronounsEasy(userId).addOnCompleteListener {
            if (it.isSuccessful){
                binding.txtPronounsEasy.text = it.result.toString() + "%"
            }
        }
        model.getPronounsMedium(userId).addOnCompleteListener {
            if (it.isSuccessful){
                binding.txtPronounsMedium.text = it.result.toString() + "%"
            }
        }
        model.getPronounsHard(userId).addOnCompleteListener {
            if (it.isSuccessful){
                binding.txtPronounsHard.text = it.result.toString() + "%"
            }
        }

        // VERBS
        model.getVerbsEasy(userId).addOnCompleteListener {
            if (it.isSuccessful){
                binding.txtVerbsEasy.text = it.result.toString() + "%"
            }
        }
        model.getVerbsMedium(userId).addOnCompleteListener {
            if (it.isSuccessful){
                binding.txtVerbsMedium.text = it.result.toString() + "%"
            }
        }
        model.getVerbsHard(userId).addOnCompleteListener {
            if (it.isSuccessful){
                binding.txtVerbsHard.text = it.result.toString() + "%"
            }
        }

        //SELF ASSESSMENT
        model.getSelfAssessmentAdjectives(userId).addOnCompleteListener {
            if (it.isSuccessful){
                binding.txtAdjectives.text = it.result.toString() + "%"
            }
        }
        model.getSelfAssessmentNouns(userId).addOnCompleteListener {
            if (it.isSuccessful){
                binding.txtNouns.text = it.result.toString() + "%"
            }
        }
        model.getSelfAssessmentPronouns(userId).addOnCompleteListener {
            if (it.isSuccessful){
                binding.txtPronouns.text = it.result.toString() + "%"
            }
        }
        model.getSelfAssessmentVerbs(userId).addOnCompleteListener {
            if (it.isSuccessful){
                binding.txtVerbs.text = it.result.toString() + "%"
            }
        }

        model.getAttempts("Adjectives", "Easy") { count ->
            binding.txtAdjectivesEasyAtt.text = count.toString()
        }

        model.getAttempts("Adjectives", "Medium") { count ->
            binding.txtAdjectivesMediumAtt.text = count.toString()
        }

        model.getAttempts("Adjectives", "Hard") { count ->
            binding.txtAdjectivesHardAtt.text = count.toString()
        }

        model.getAttempts("Nouns", "Easy") { count ->
            binding.txtNounsEasyAtt.text = count.toString()
        }

        model.getAttempts("Nouns", "Medium") { count ->
            binding.txtNounsMediumAtt.text = count.toString()
        }

        model.getAttempts("Nouns", "Hard") { count ->
            binding.txtNounsHardAtt.text = count.toString()
        }

        model.getAttempts("Pronouns", "Easy") { count ->
            binding.txtPronounsEasyAtt.text = count.toString()
        }

        model.getAttempts("Pronouns", "Medium") { count ->
            binding.txtPronounsMediumAtt.text = count.toString()
        }

        model.getAttempts("Pronouns", "Hard") { count ->
            binding.txtPronounsHardAtt.text = count.toString()
        }

        model.getAttempts("Verbs", "Easy") { count ->
            binding.txtVerbsEasyAtt.text = count.toString()
        }

        model.getAttempts("Verbs", "Medium") { count ->
            binding.txtVerbsMediumAtt.text = count.toString()
        }

        model.getAttempts("Verbs", "Hard") { count ->
            binding.txtVerbsHardAtt.text = count.toString()
        }

        model.getAttemptsSelf("Adjectives") { count ->
            binding.txtAdjectivesAtt.text = count.toString()
        }

        model.getAttemptsSelf("Nouns") { count ->
            binding.txtNounsAtt.text = count.toString()
        }

        model.getAttemptsSelf("Pronouns") { count ->
            binding.txtPronounsAtt.text = count.toString()
        }

        model.getAttemptsSelf("Verbs") { count ->
            binding.txtVerbsAtt.text = count.toString()
        }
    }
}