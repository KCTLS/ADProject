package com.example.adproject

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView

class QuestionAdapter(context: Context, private val questions: List<Pair<String, Int>>) :
    ArrayAdapter<Pair<String, Int>>(context, 0, questions) {

    override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        var itemView = convertView
        if (itemView == null) {
            itemView = LayoutInflater.from(context).inflate(R.layout.list_item_question, parent, false)
        }

        val currentQuestion = questions[position]

        val questionText = itemView?.findViewById<TextView>(R.id.questionText)
        val questionImage = itemView?.findViewById<ImageView>(R.id.questionImage)

        questionText?.text = currentQuestion.first
        questionImage?.setImageResource(currentQuestion.second)

        return itemView!!
    }
}