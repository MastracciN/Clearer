package com.example.clearer.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.clearer.R
import com.example.clearer.adapter.PracticeAdapter
import com.example.clearer.databinding.FragmentPracticeBinding
import com.example.clearer.models.Practice
import com.example.clearer.vms.PracticeFragmentViewModel

class PracticeFragment : Fragment() {

    private var _binding: FragmentPracticeBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var practiceList: ArrayList<Practice>
    private lateinit var adapter: PracticeAdapter

    private val model: PracticeFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentPracticeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize practiceList
        practiceList = arrayListOf()
        model.practiceList = practiceList

        // Initialize dropdowns
        val tilQuiz = binding.tilQuiz
        val tilDifficulty = binding.tilDifficulty

        // Set flags to check if the selection is made programmatically
        var isDifficultyProgrammaticallySelected = false

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.quiz_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            tilQuiz.adapter = adapter
        }
        tilQuiz.setSelection(0)

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.difficulty,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            tilDifficulty.adapter = adapter
        }
        tilDifficulty.setSelection(0)

        // Listeners to change list based on current dropdown selection
        tilQuiz.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                // Handle the selected item
                model.populateRecyclerView(tilQuiz.selectedItem.toString(), tilDifficulty.selectedItem.toString())
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing here if nothing is selected
            }
        }
        tilDifficulty.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                // Handle the selected item
                if (!isDifficultyProgrammaticallySelected)
                    model.populateRecyclerView(tilQuiz.selectedItem.toString(), tilDifficulty.selectedItem.toString())

                isDifficultyProgrammaticallySelected = false
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing here if nothing is selected
            }
        }

        // Create RecyclerView
        val layoutManager = LinearLayoutManager(context)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        // RecyclerView item information is passed to QuizFragment on click
        adapter = PracticeAdapter(practiceList, PracticeAdapter.OnClickListener { practice ->
            Log.d("ABC","Item clicked: ${practice.documentId}")
            val action = PracticeFragmentDirections
                .actionFragmentPracticeToQuizFragment(
                    practice.quizDescription,
                    tilQuiz.selectedItem.toString(),
                    practice.documentId,
                )

            view.findNavController().navigate(action)
        })

        recyclerView.adapter = adapter
        model.adapter = adapter

        isDifficultyProgrammaticallySelected = true

    }

}