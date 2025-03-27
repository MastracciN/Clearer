package com.example.clearer.models

import android.util.Log
import com.textrazor.TextRazor

object TextRazorAPI {

    private const val API_KEY = ""
    private var adjectivesList: MutableList<String> = mutableListOf("")
    private var nounsList: MutableList<String> = mutableListOf("")
    private var pronounsList: MutableList<String> = mutableListOf("")
    private var verbsList: MutableList<String> = mutableListOf("")

    private var tagList: MutableList<String> = mutableListOf()

    fun callTextRazor(renderedText: String): List<MutableList<String>> {
        // Need to clear lists otherwise successive calls will have the previous calls words
        clearLists()

        val client = TextRazor(API_KEY)
        client.addExtractor("words")
        client.addExtractor("entities")

        val response = client.analyze(renderedText)

        for (word in response.response.words) {
            Log.d("ABC", "Word: ${word.token}")
            val partOfSpeech = tagToText(word.partOfSpeech)
            Log.d("ABC", "partOfSpeech: $partOfSpeech")
            when(partOfSpeech){
                "Adjective" -> adjectivesList.add(word.token)
                "Noun" -> nounsList.add(word.token)
                "Pronoun" -> pronounsList.add(word.token)
                "Verb" -> verbsList.add(word.token)
            }
        }

        return listOf(adjectivesList, nounsList, pronounsList, verbsList, tagList)
    }

    // TextRazor returns parts of speech as a tag, convert tag to be human readable
    // https://www.ling.upenn.edu/courses/Fall_2003/ling001/penn_treebank_pos.html
    private fun tagToText(tag: String): String {
        val text: String

        when(tag) {
            "JJ" -> text = "Adjective"
            "JJR" -> text = "Adjective"
            "JJS" -> text = "Adjective"
            "NN" -> text = "Noun"
            "NNS" -> text = "Noun"
            "NNP" -> text = "Noun"
            "NNPS" -> text = "Noun"
            "PRP" -> text = "Pronoun"
            "PRP$" -> text = "Pronoun"
            "VB" -> text = "Verb"
            "VBD" -> text = "Verb"
            "VBG" -> text = "Verb"
            "VBN" -> text = "Verb"
            "VBP" -> text = "Verb"
            "VBZ" -> text = "Verb"
            else -> {
                text = ""
            }
        }

        return text
    }

    private fun clearLists(){
        nounsList.clear()
        verbsList.clear()
        adjectivesList.clear()
        pronounsList.clear()
        tagList.clear()
    }

}
