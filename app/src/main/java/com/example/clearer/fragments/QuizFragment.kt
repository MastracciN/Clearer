package com.example.clearer.fragments

import android.graphics.Color
import android.os.Bundle
import android.text.*
import android.text.method.LinkMovementMethod
import android.text.style.BackgroundColorSpan
import android.text.style.ClickableSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.fragment.navArgs
import com.example.clearer.R
import com.example.clearer.databinding.FragmentQuizBinding
import kotlinx.coroutines.*
import com.example.clearer.vms.QuizFragmentViewModel

class QuizFragment : Fragment(R.layout.fragment_quiz) {
    private var _binding: FragmentQuizBinding? = null
    private val binding get() = _binding!!
    private val args: QuizFragmentArgs by navArgs()

    // View Model: Manages logic and Firebase calls
    private val model: QuizFragmentViewModel by viewModels()

    // The quiz text to be displayed and highlighted
    private lateinit var spannableString : SpannableString

    // Type of quiz
    private lateinit var quizType: String
    private var quizColor: Int = 0

    // Document Id if quiz provided from Firestore
    private lateinit var documentId: String

    // Text to be used in quiz
    private lateinit var renderedText: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentQuizBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        renderedText = args.renderedText
        quizType = args.quizType
        documentId = args.documentId

        model.quizType = args.quizType

        // Clear lists before populating with new data
        model.resetLists()

        // Model needs documentId for business logic
        model.documentId = args.documentId

        // Set problemString to renderedText safe arg
        model.problem.problemString = renderedText

        // Document ID was passed so retrieve problem from Firestore
        if (documentId != "none"){
            Log.d("ABC", "DocumentId found: $documentId")
            model.getProblemById(documentId)
        } else {
            // createQuiz creates quiz logic for user made problems
            lifecycleScope.launch(Dispatchers.IO) {
                model.createQuiz(args.renderedText)
            }
        }

        // Sets the question text based on quiz type
        displayQuestion()
        setQuizColor()

        // Generates clickable spans so text is clickable
        binding.renderedText.text = model.problem.problemString
        makeWordsTappable(binding.renderedText, onClick = { word ->
            Log.d("ABC","Clicked: $word")
        })

        binding.btnNext.setOnClickListener{
            manageQuiz()
        }
    }

    // Makes individual words in a textview tappable
    private fun makeWordsTappable(textView: TextView, onClick: (word: String) -> Unit){
        val text = textView.text.toString()
        val words = text.split(" ", "\n")

        spannableString = SpannableString(text)

        var startIndex = 0
        words.forEach{ word ->
            val endIndex = startIndex + word.length
            val clickableSpan = object : ClickableSpan() {
                override fun onClick(widget: View) {
                    onClick(word)
                    model.manageTappedWords(word)
                    setHighLightedText(binding.renderedText, model.tappedWords, Color.parseColor(getString(quizColor)))
                }
                override fun updateDrawState(ds: TextPaint) {
                    super.updateDrawState(ds)
                    ds.color = Color.BLACK  // Set the default color of the word to black
                    ds.isUnderlineText = false  // Remove underline
                }
            }
            spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            startIndex = endIndex + 1
        }
        textView.text = spannableString
        textView.movementMethod = LinkMovementMethod.getInstance()
    }

    // Finds and highlights the words the user has selected
    fun setHighLightedText(textView: TextView, textToHighlight: MutableList<String>, colour: Int) {
        val text = textView.text.toString()
        val words = text.split(" ", "\n")

        //Remove all highlights from text - For event when word is tapped twice to remove selection
        spannableString.setSpan(
            BackgroundColorSpan(Color.TRANSPARENT),
            0,
            textView.text.length,
            Spannable.SPAN_INCLUSIVE_INCLUSIVE
        )

        var startIndex = 0
        words.forEach { word ->
            var cleanWord = word.replace(Regex("[^\\w\\s\\-\']"),"")    // Clean word of punctuation
            cleanWord = cleanWord.lowercase()   // make word lowercase as all words in textToHighlight are

            val endIndex = startIndex + word.length
            if (cleanWord in textToHighlight) {
                // Clean word will be shorter than word when the word is adjacent to punctuation
                // If true, only word will be highlighted as endIndex is adjusted 1 step back
                if (cleanWord.length < word.length){
                    spannableString.setSpan(
                        BackgroundColorSpan(colour),
                        startIndex,
                        endIndex - 1,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }else {
                    spannableString.setSpan(
                        BackgroundColorSpan(colour),
                        startIndex,
                        endIndex,
                        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                    )
                }
            }
            startIndex = endIndex + 1
        }
        textView.text = spannableString
    }

    // Sets the text highlight color based on the quiz type
    private fun setQuizColor(){
        quizColor = when (quizType){
            "Noun" -> R.color.nouns
            "Verb" -> R.color.verbs
            "Adjective" -> R.color.adjectives
            "Pronoun" -> R.color.pronouns
            else -> {
                R.color.nouns
            }
        }
    }

    // Displays question text based on quiz type
    private fun displayQuestion(){
        val question = "Find the $quizType"+ "s."
        binding.tvQuestion.text = question
    }

    // Called when user has finished quiz, calculates score, adds points, save to firebase
    // Navigates to results fragment
    private fun manageQuiz(){
        val result = model.finishQuiz(quizType)
        val results = result.first
        val average = result.second

        val action = QuizFragmentDirections.actionQuizFragmentToResultsFragment(average, results)
        view?.findNavController()?.navigate(action)
    }

}
