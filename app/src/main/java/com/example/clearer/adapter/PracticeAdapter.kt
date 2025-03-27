package com.example.clearer.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.clearer.R
import com.example.clearer.models.Practice

class PracticeAdapter (private val practiceList:ArrayList<Practice>,
                       private val onClickListener: OnClickListener)
    : RecyclerView.Adapter<PracticeAdapter.PracticeViewHolder>(){

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PracticeViewHolder {
       val view = LayoutInflater.from(parent.context).inflate(R.layout.each_item, parent, false)
        return PracticeViewHolder(view)
    }

    private fun displayString(input: String): String {
        return if (input.length > 30) {
            input.substring(0, 30) + "..."
        } else {
            input
        }
    }

    override fun onBindViewHolder(holder: PracticeViewHolder, position: Int) {
        val practice = practiceList[position]
        holder.quizName.text = practice.quizName

        holder.quizDescription.text = displayString(practice.quizDescription)

        if (practice.isAttempted){
            holder.container.setBackgroundColor(Color.parseColor("#EDEDED"))
        }
            holder.itemView.setOnClickListener {
                onClickListener.onClick(practice)
        }

    }

    override fun getItemCount(): Int {
       return practiceList.size
    }

    class PracticeViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val quizName : TextView = itemView.findViewById(R.id.txtQuizName)
        val quizDescription : TextView = itemView.findViewById(R.id.txtQuizDescription)
        val container : View = itemView.findViewById(R.id.container)
    }

    class OnClickListener(val clickListener: (practice: Practice) -> Unit) {
        fun onClick(practice: Practice) = clickListener(practice)
    }

}