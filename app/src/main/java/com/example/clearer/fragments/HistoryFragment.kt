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
import com.example.clearer.databinding.FragmentHistoryBinding
import com.example.clearer.models.Practice
import com.example.clearer.vms.HistoryFragmentViewModel

class HistoryFragment : Fragment() {

    private var _binding: FragmentHistoryBinding? = null
    private val binding get() = _binding!!

    private lateinit var recyclerView: RecyclerView
    private lateinit var practiceList: ArrayList<Practice>
    private lateinit var adapter: PracticeAdapter

    private val model : HistoryFragmentViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHistoryBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize practiceList
        practiceList = arrayListOf()
        model.practiceList = practiceList

        model.populatePracticeLists()

        // Initialize dropdowns
        val tilSort = binding.tilSort

        ArrayAdapter.createFromResource(
            requireContext(),
            R.array.sort_options,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            tilSort.adapter = adapter
        }
        tilSort.setSelection(0)

        // Listener to change list based on selected item in dropdown
        tilSort.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parentView: AdapterView<*>?,
                selectedItemView: View?,
                position: Int,
                id: Long
            ) {
                // Handle the selected item
                model.sortList(tilSort.selectedItem.toString())
            }

            override fun onNothingSelected(parentView: AdapterView<*>?) {
                // Do nothing here if nothing is selected
            }
        }

        val layoutManager = LinearLayoutManager(context)

        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = layoutManager
        recyclerView.setHasFixedSize(true)

        // RecyclerView item information is passed to QuizFragment on click
        adapter = PracticeAdapter(practiceList, PracticeAdapter.OnClickListener { practice ->
            Log.d("ABC","Item clicked: ${practice.documentId}")
            val action = HistoryFragmentDirections
                .actionHistoryFragmentToFragmentRenderedText(practice.quizDescription)
            view.findNavController().navigate(action)
        })
        model.adapter = adapter
        recyclerView.adapter = adapter

    }

}