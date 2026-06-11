package com.civiq.app.data.remote.ai

import com.civiq.app.domain.model.AiQuestionRequest
import com.civiq.app.domain.model.QuestionTone
import com.civiq.app.domain.model.QuestionType

/**
 * Builds the text prompt sent to Gemini/OpenAI to generate a batch of civic
 * education quiz questions. Both providers are configured (see
 * [com.civiq.app.data.repository.AiQuizRepositoryImpl]) to return JSON-only
 * responses, so the prompt fully specifies the expected output schema.
 */
object AiPromptTemplates {

    fun buildQuestionGenerationPrompt(request: AiQuestionRequest): String {
        val typeInstruction = when (request.type) {
            null -> "Use a mix of \"MULTIPLE_CHOICE\", \"TRUE_FALSE\", and \"SCENARIO\" question types across the set."
            else -> "Every question must use the \"${request.type.name}\" question type."
        }
        val toneInstruction = when (request.tone) {
            QuestionTone.EDUCATIONAL -> "Write in a clear, neutral, educational tone suitable for a general audience."
            QuestionTone.FUNNY -> "Write in a light, witty, good-humored tone while keeping the civic facts accurate."
            QuestionTone.SATIRICAL -> "Write with dry satirical wit (poking fun at bureaucracy, politics, etc.) while keeping the civic facts accurate and not partisan."
        }
        val countryInstruction = request.countryCode
            ?.takeIf { it.isNotBlank() }
            ?.let { "Frame examples around the civic/government system of country code \"$it\" where relevant." }
            ?: "Keep examples globally applicable rather than specific to one country, unless the topic is inherently country-specific."
        val topicInstruction = request.topicHint
            ?.takeIf { it.isNotBlank() }
            ?.let { "Focus specifically on this topic within the category: \"$it\"." }
            ?: ""

        return """
            You are a civic education content writer for CiviQ, an open-source app
            that helps people understand democracy, government, and civic
            responsibility.

            Generate exactly ${request.count} quiz questions about the category
            "${request.category.displayName}" (${request.category.description}),
            at a "${request.difficulty.displayName}" difficulty level.

            $typeInstruction
            $toneInstruction
            $countryInstruction
            $topicInstruction

            Rules:
            - For "MULTIPLE_CHOICE" and "SCENARIO" questions, provide exactly 4 plausible options.
            - For "TRUE_FALSE" questions, "options" must be exactly ["True", "False"].
            - "correctAnswerIndex" is the 0-based index of the correct option.
            - "explanation" is 1-3 sentences explaining why the correct answer is correct.
            - Questions must be factually accurate, non-partisan, and appropriate for all ages.
            - Do not repeat the same question twice within the set.

            Respond with ONLY valid JSON (no markdown, no code fences, no commentary)
            matching exactly this schema:
            {
              "questions": [
                {
                  "questionText": string,
                  "options": string[],
                  "correctAnswerIndex": number,
                  "explanation": string,
                  "type": "MULTIPLE_CHOICE" | "TRUE_FALSE" | "SCENARIO",
                  "tone": "EDUCATIONAL" | "FUNNY" | "SATIRICAL"
                }
              ]
            }
        """.trimIndent()
    }
}
