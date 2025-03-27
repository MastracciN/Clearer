package com.example.clearer.fragments

import android.content.Intent
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableString
import android.text.style.BackgroundColorSpan
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.example.clearer.R
import com.example.clearer.databinding.FragmentRenderedTextBinding
import com.example.clearer.models.TextRazorAPI
import kotlinx.coroutines.*
import java.util.regex.Pattern
import android.speech.tts.TextToSpeech
import android.speech.tts.TextToSpeech.OnInitListener
import android.speech.tts.UtteranceProgressListener
import androidx.core.content.ContextCompat
import java.util.*

class RenderedTextFragment : Fragment(R.layout.fragment_rendered_text), OnInitListener {

    private lateinit var textToSpeech: TextToSpeech

    private var _binding: FragmentRenderedTextBinding? = null
    private val binding get() = _binding!!
    private val args: RenderedTextFragmentArgs by navArgs()

    private val PUNCT_SYMBOLS = Pattern.compile("[!\"#$%&'()*+,-./:;<=>?@\\[\\]^_`{|}~]")
    private val TTS_CHECK_CODE = 101

    private var adjectivesList: MutableList<String> = mutableListOf("")
    private var nounsList: MutableList<String> = mutableListOf("")
    private var pronounsList: MutableList<String> = mutableListOf("")
    private var verbsList: MutableList<String> = mutableListOf("")

    private var tagList: MutableList<String> = mutableListOf()

    private var adjectivesPressed = false
    private var nounsPressed = false
    private var pronounsPressed = false
    private var verbsPressed = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentRenderedTextBinding.inflate(inflater, container, false)
        return binding.root
    }

    // TextToSpeech initialization
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            // Set language (for example, US English)
            val result = textToSpeech.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("ABC", "Language not supported")
            } else {
                Log.d("ABC", "TextToSpeech initialization succeeded")
            }
        } else {
            Log.e("ABC", "TextToSpeech initialization failed with status $status")
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == TTS_CHECK_CODE) {
            if (resultCode == TextToSpeech.Engine.CHECK_VOICE_DATA_PASS) {
                Log.e("ABC", "TTS engine is available")
            } else {
                Log.e("ABC", "TTS engine is not available")
                val installIntent = Intent()
                installIntent.action = TextToSpeech.Engine.ACTION_INSTALL_TTS_DATA
                startActivity(installIntent)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        textToSpeech.stop()
        textToSpeech.shutdown()
    }

    private fun reapplyFilters() {
        if (adjectivesPressed) {
            setHighLightedText(
                binding.renderedText,
                adjectivesList,
                Color.parseColor(getString(R.color.adjectives))
            )
        }
        if (nounsPressed) {
            setHighLightedText(
                binding.renderedText,
                nounsList,
                Color.parseColor(getString(R.color.nouns))
            )
        }
        if (pronounsPressed) {
            setHighLightedText(
                binding.renderedText,
                pronounsList,
                Color.parseColor(getString(R.color.pronouns))
            )
        }
        if (verbsPressed) {
            setHighLightedText(
                binding.renderedText,
                verbsList,
                Color.parseColor(getString(R.color.verbs))
            )
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        textToSpeech = TextToSpeech(requireContext(), this)

        val renderedText = args.renderedText
        binding.renderedText.setText(renderedText)

        binding.speakButton.setOnClickListener {
            textToSpeech.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {

                    activity?.runOnUiThread {
                        binding.speakButton.apply {
                            backgroundTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(context, R.color.light_blue)
                            )
                            isEnabled = false
                        }
                    }
                    Log.d("ABC", "Speech synthesis started")
                }

                override fun onRangeStart(utteranceId: String?, start: Int, end: Int, frame: Int) {
                    highlightTextRange(
                        binding.renderedText,
                        Color.parseColor(getString(R.color.tts)),
                        start,
                        end
                    )
                }

                override fun onDone(utteranceId: String?) {
                    highlightTextRange(
                        binding.renderedText,
                        Color.parseColor(getString(com.google.android.material.R.color.mtrl_btn_transparent_bg_color)),
                        0,
                        renderedText.length
                    )

                    activity?.runOnUiThread {
                        binding.speakButton.apply {
                            backgroundTintList = ColorStateList.valueOf(
                                ContextCompat.getColor(
                                    context,
                                    R.color.blue
                                )
                            )
                            isEnabled = true
                        }
                    }
                    reapplyFilters()
                }

                override fun onError(utteranceId: String?) {
                    Log.d("ABC", "Error")
                }
            })
            textToSpeech.setSpeechRate(0.8f)
            textToSpeech.speak(renderedText, TextToSpeech.QUEUE_ADD, null, "utteranceId")
        }

        lifecycleScope.launch(Dispatchers.IO) {
            val textRazor = TextRazorAPI.callTextRazor(renderedText)
            adjectivesList = textRazor[0]
            nounsList = textRazor[1]
            pronounsList = textRazor[2]
            verbsList = textRazor[3]
            tagList = textRazor[4]

            binding.menuAdjectives.setOnClickListener {
                adjectivesPressed = !adjectivesPressed

                if (adjectivesPressed) {
                    binding.menuAdjectives.colorNormal =
                        Color.parseColor(getString(R.color.adjectives))
                    setHighLightedText(
                        binding.renderedText,
                        adjectivesList,
                        Color.parseColor(getString(R.color.adjectives))
                    )
                } else {
                    binding.menuAdjectives.colorNormal =
                        Color.parseColor(getString(R.color.adjectives_light))
                    setHighLightedText(
                        binding.renderedText, adjectivesList, Color.parseColor(
                            getString(
                                com.google.android.material.R.color.mtrl_btn_transparent_bg_color
                            )
                        )
                    )
                }
            }

            binding.menuNouns.setOnClickListener {
                nounsPressed = !nounsPressed

                if (nounsPressed) {
                    binding.menuNouns.colorNormal = Color.parseColor(getString(R.color.nouns))
                    setHighLightedText(
                        binding.renderedText,
                        nounsList,
                        Color.parseColor(getString(R.color.nouns))
                    )
                } else {
                    binding.menuNouns.colorNormal = Color.parseColor(getString(R.color.nouns_light))
                    setHighLightedText(
                        binding.renderedText, nounsList, Color.parseColor(
                            getString(
                                com.google.android.material.R.color.mtrl_btn_transparent_bg_color
                            )
                        )
                    )
                }
            }

            binding.menuPronouns.setOnClickListener {
                pronounsPressed = !pronounsPressed

                if (pronounsPressed) {
                    binding.menuPronouns.colorNormal = Color.parseColor(getString(R.color.pronouns))
                    setHighLightedText(
                        binding.renderedText,
                        pronounsList,
                        Color.parseColor(getString(R.color.pronouns))
                    )
                } else {
                    binding.menuPronouns.colorNormal =
                        Color.parseColor(getString(R.color.pronouns_light))
                    setHighLightedText(
                        binding.renderedText, pronounsList, Color.parseColor(
                            getString(
                                com.google.android.material.R.color.mtrl_btn_transparent_bg_color
                            )
                        )
                    )
                }
            }

            binding.menuVerbs.setOnClickListener {
                verbsPressed = !verbsPressed

                if (verbsPressed) {
                    binding.menuVerbs.colorNormal = Color.parseColor(getString(R.color.verbs))
                    setHighLightedText(
                        binding.renderedText,
                        verbsList,
                        Color.parseColor(getString(R.color.verbs))
                    )
                } else {
                    binding.menuVerbs.colorNormal = Color.parseColor(getString(R.color.verbs_light))
                    setHighLightedText(
                        binding.renderedText, verbsList, Color.parseColor(
                            getString(
                                com.google.android.material.R.color.mtrl_btn_transparent_bg_color
                            )
                        )
                    )
                }
            }
        }
    }
}

private fun highlightTextRange(textView: TextView, colour: Int, start: Int, end: Int) {
    val spannable = SpannableString(textView.text.toString())
    spannable.setSpan(
        BackgroundColorSpan(colour),
        start,
        end,
        Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
    )
    textView.text = spannable
}

fun setHighLightedText(textView: TextView, textToHighlight: MutableList<String>, colour: Int) {
    val text = textView.text.toString()
    val wordToSpan: Spannable = SpannableString(textView.text)
    for (i in 0 until textToHighlight.size) {
        var offsetEnd = text.indexOf(textToHighlight[i], 0)
        var offsetStart = 0
        while (offsetStart < text.length && offsetEnd != -1) {
            offsetEnd = text.indexOf(textToHighlight[i], offsetStart)
            if (offsetEnd == -1) break else {
                // set color here
                wordToSpan.setSpan(
                    BackgroundColorSpan(colour),
                    offsetEnd,
                    offsetEnd + textToHighlight[i].length,
                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
                )
            }
            offsetStart = offsetEnd + 1
        }
    }
    textView.text = wordToSpan
}