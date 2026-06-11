package com.civiq.app.data.remote.dto.firestore

import com.google.firebase.firestore.DocumentId

/** Firestore document shape for `questions/{questionId}`. See docs/DATABASE.md. */
data class QuestionDto(
    @DocumentId val id: String = "",
    val type: String = "MULTIPLE_CHOICE",
    val category: String = "DEMOCRACY",
    val difficulty: String = "BEGINNER",
    val questionText: String = "",
    val options: List<String> = emptyList(),
    val correctAnswerIndex: Int = 0,
    val explanation: String = "",
    val tone: String = "EDUCATIONAL",
    val countryCode: String? = null,
    val tags: List<String> = emptyList(),
    val source: String = "CURATED",
    val createdAt: Long = 0L,
    val createdBy: String? = null,
)
