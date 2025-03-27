package com.example.clearer.fragments

import android.Manifest
import android.app.Activity
import android.app.AlertDialog
import android.app.ProgressDialog
import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import com.example.clearer.R
import com.example.clearer.databinding.FragmentCameraBinding
import com.example.clearer.vms.CameraFragmentViewModel
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizer
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import com.canhub.cropper.CropImageContract
import com.canhub.cropper.CropImageView
import com.canhub.cropper.options

class CameraFragment : Fragment(R.layout.fragment_camera) {

    private var _binding: FragmentCameraBinding? = null
    private val binding get() = _binding!!

    private companion object {
        private const val CAMERA_REQUEST_CODE = 100
        private const val STORAGE_REQUEST_CODE = 101
    }

    private var imageUri: Uri? = null

    private lateinit var cameraPermissions: Array<String>
    private lateinit var storagePermissions: Array<String>

    private lateinit var progressDialog: ProgressDialog

    private lateinit var textRecognizer: TextRecognizer

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCameraBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        cameraPermissions =
            arrayOf(Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE)
        storagePermissions = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

        context?.let {
            progressDialog = ProgressDialog(it)
        }

        progressDialog.setTitle("Please Wait")
        progressDialog.setCanceledOnTouchOutside(false)

        textRecognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        binding.IvSelectedImg.setOnClickListener {
            startCrop()
        }

        binding.btnProcess.setOnClickListener {
            if (imageUri == null) {
                showToast("Please select an image")
            } else {
                imageUri?.let { it -> recognizeTextFromImage(it) }
            }
        }

        binding.btnQuiz.setOnClickListener {
            if (imageUri == null) {
                showToast("Please select an image")
            } else {
                imageUri?.let { it -> quizTypeDialog(it) }
            }
        }
    }

    private fun recognizeTextFromImage(imageUri: Uri) {
        progressDialog.setMessage("Preparing Image")
        progressDialog.show()

        try {

            val inputImage = context?.let { InputImage.fromFilePath(it, imageUri) }
            progressDialog.setMessage("Recognizing text")

            inputImage?.let {
                textRecognizer.process(it)
                    .addOnSuccessListener { text ->
                        progressDialog.dismiss()

                        var recognizedText = text.text

                        if (hasWords(recognizedText)) {
                            recognizedText = recognizedText.trimIndent()
                            // Replace the end of each line with a space
                            recognizedText = recognizedText.replace(Regex("[\r\n]"), " ")
                            val action =
                                CameraFragmentDirections.actionFragmentCameraToFragmentRenderedText2(
                                    recognizedText
                                )
                            view?.findNavController()?.navigate(action)
                        } else
                            showToast("Images need text to continue.")

                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        showToast("Failed to recognize text due to ${e.message}")
                    }
            }

        } catch (e: Exception) {
            showToast("Failed to prepare image due to ${e.message}")
        }
    }

    private fun recognizeTextFromImageQuiz(imageUri: Uri, quizType: String) {
        progressDialog.setMessage("Preparing Image")
        progressDialog.show()

        try {
            val inputImage = context?.let { InputImage.fromFilePath(it, imageUri) }
            progressDialog.setMessage("Recognizing text")


            inputImage?.let {
                textRecognizer.process(it)
                    .addOnSuccessListener { text ->
                        progressDialog.dismiss()

                        var recognizedText = text.text

                        // If image has words, continue to quiz fragment
                        if (hasWords(recognizedText)) {
                            recognizedText = recognizedText.trimIndent()
                            // Replace the end of each line with a space
                            recognizedText = recognizedText.replace(Regex("[\r\n]"), " ")
                            val action =
                                CameraFragmentDirections.actionFragmentCameraToQuizFragment(
                                    recognizedText,
                                    quizType
                                )
                            view?.findNavController()?.navigate(action)
                        } else
                            showToast("Images need text to continue.")


                    }
                    .addOnFailureListener { e ->
                        progressDialog.dismiss()
                        showToast("Failed to recognize text due to ${e.message}")
                    }
            }

        } catch (e: Exception) {
            showToast("Failed to prepare image due to ${e.message}")
        }
    }

    // Returns true if string has words in it
    fun hasWords(input: String): Boolean {
        val regex = Regex("\\b\\p{L}+\\b")
        return regex.containsMatchIn(input)
    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            // Use the returned uri.
            val uriContent = result.uriContent

            imageUri = uriContent
            binding.IvSelectedImg.setImageURI(uriContent)
        } else {
            // An error occurred.
//            val exception = result.error
            showToast("Cancelled")
        }
    }

    private fun startCrop() {
        // Start picker to get image for cropping and then use the image in cropping activity.
        cropImage.launch(
            options {
                setGuidelines(CropImageView.Guidelines.ON)
            }
        )
    }

    private fun pickImageCamera() {
        val values = ContentValues()
        values.put(MediaStore.Images.Media.TITLE, "Sample Title")
        values.put(MediaStore.Images.Media.DESCRIPTION, "Sample Description")
        val resolver = activity!!.contentResolver
        imageUri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)

        val intent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri)

        cameraActivityResultLauncher.launch(intent)
    }

    private val cameraActivityResultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                binding.IvSelectedImg.setImageURI(imageUri)
            } else {
                showToast("Cancelled")
            }
        }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            CAMERA_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    val cameraAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED
                    val storageAccepted = grantResults[1] == PackageManager.PERMISSION_GRANTED

                    if (cameraAccepted && storageAccepted) {
                        pickImageCamera()
                    } else {
                        showToast("Camera & Storage permission are required")
                    }
                }
            }
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty()) {
                    val storageAccepted = grantResults[0] == PackageManager.PERMISSION_GRANTED

                    if (storageAccepted) {
                        startCrop()
                    } else {
                        showToast("Storage permission is required")
                    }
                }
            }
        }
    }

    private fun showToast(message: String) {
        Toast.makeText(requireActivity(), message, Toast.LENGTH_SHORT).show()
    }

    private fun quizTypeDialog(imageUri: Uri) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(context)
        val quizTypes = arrayOf("Noun", "Verb", "Adjective", "Pronoun")

        builder
            .setTitle("Choose Quiz Type:")
            .setNegativeButton("Back") { dialog, _ ->
                dialog.dismiss()
            }
            .setItems(quizTypes) { _, which ->
                val selectedQuizType = quizTypes[which]
                Log.d("ABC", "quizType: $selectedQuizType")
                recognizeTextFromImageQuiz(imageUri, selectedQuizType)
            }

        val dialog: AlertDialog = builder.create()
        dialog.show()
    }


}