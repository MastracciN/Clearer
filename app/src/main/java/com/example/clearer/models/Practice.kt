package com.example.clearer.models

import java.time.LocalDateTime

data class Practice(
    var quizName:String,
    var quizDescription:String,
    var documentId:String,
    var isAttempted: Boolean,
    var date: LocalDateTime? = null
)
