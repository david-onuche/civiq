package com.civiq.app.data.repository

import com.civiq.app.domain.model.AiQuestionRequest
import com.civiq.app.domain.model.Question
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuestionSource
import com.civiq.app.domain.model.QuestionTone
import com.civiq.app.domain.model.QuestionType
import com.civiq.app.domain.model.QuizCategory

/**
 * Bundled, hand-written civic education questions used as a last-resort
 * fallback when both the Gemini and OpenAI providers fail or are not
 * configured (missing API keys, no network, malformed response). Ensures
 * the quiz experience never fully breaks, even fully offline.
 */
object FallbackQuestionProvider {

    /** Returns up to [AiQuestionRequest.count] bundled questions for [request]'s category. */
    fun getQuestions(request: AiQuestionRequest): List<Question> {
        val matching = questions.filter { it.category == request.category }
        val pool = matching.ifEmpty { questions }
        return pool.take(request.count)
    }

    private fun question(
        category: QuizCategory,
        questionText: String,
        options: List<String>,
        correctAnswerIndex: Int,
        explanation: String,
    ) = Question(
        id = "",
        type = QuestionType.MULTIPLE_CHOICE,
        category = category,
        difficulty = QuestionDifficulty.BEGINNER,
        questionText = questionText,
        options = options,
        correctAnswerIndex = correctAnswerIndex,
        explanation = explanation,
        tone = QuestionTone.EDUCATIONAL,
        countryCode = null,
        tags = listOf("fallback"),
        source = QuestionSource.CURATED,
        createdAt = 0L,
        createdBy = null,
    )

    private val questions: List<Question> = listOf(
        question(
            category = QuizCategory.DEMOCRACY,
            questionText = "What is the core principle of a democracy?",
            options = listOf(
                "Power comes from the people",
                "Power comes from a single ruler",
                "Power comes from the military",
                "Power comes from religious leaders",
            ),
            correctAnswerIndex = 0,
            explanation = "In a democracy, political power is derived from the consent of the governed, typically expressed through voting.",
        ),
        question(
            category = QuizCategory.DEMOCRACY,
            questionText = "Which of these is a defining feature of a representative democracy?",
            options = listOf(
                "Citizens elect officials to make decisions on their behalf",
                "All laws are decided by a king",
                "Only wealthy citizens can vote",
                "A single party rules without elections",
            ),
            correctAnswerIndex = 0,
            explanation = "In a representative democracy, citizens elect officials to legislate and govern on their behalf.",
        ),
        question(
            category = QuizCategory.ELECTIONS,
            questionText = "What is the term for the geographic area represented by an elected official?",
            options = listOf("A constituency", "A monarchy", "A tribunal", "A cabinet"),
            correctAnswerIndex = 0,
            explanation = "A constituency (or district) is the area whose voters elect a representative to a legislative body.",
        ),
        question(
            category = QuizCategory.ELECTIONS,
            questionText = "What does it mean for an election to be 'free and fair'?",
            options = listOf(
                "Voters can choose without intimidation and votes are counted accurately",
                "Only the ruling party can nominate candidates",
                "Citizens must pay a fee to vote",
                "Results are announced before voting ends",
            ),
            correctAnswerIndex = 0,
            explanation = "Free and fair elections require that voters can express their choice without coercion and that the count accurately reflects those choices.",
        ),
        question(
            category = QuizCategory.GOVERNANCE,
            questionText = "What is the principle of 'separation of powers'?",
            options = listOf(
                "Dividing government into branches with distinct responsibilities",
                "Combining all government power into one office",
                "Allowing only the military to govern",
                "Letting courts write all new laws",
            ),
            correctAnswerIndex = 0,
            explanation = "Separation of powers divides government into branches (e.g. executive, legislative, judicial) so no single branch holds all power.",
        ),
        question(
            category = QuizCategory.GOVERNANCE,
            questionText = "Which level of government typically handles services like local trash collection and zoning?",
            options = listOf(
                "Local/municipal government",
                "International organizations",
                "The national supreme court",
                "Foreign embassies",
            ),
            correctAnswerIndex = 0,
            explanation = "Local or municipal governments are usually responsible for community-level services such as waste collection, zoning, and local infrastructure.",
        ),
        question(
            category = QuizCategory.CONSTITUTIONS,
            questionText = "What is a constitution?",
            options = listOf(
                "A foundational document establishing a government's structure and rights",
                "A list of current government employees",
                "An annual government budget report",
                "A treaty between two private companies",
            ),
            correctAnswerIndex = 0,
            explanation = "A constitution is the foundational legal document that establishes how a government is organized and what rights citizens have.",
        ),
        question(
            category = QuizCategory.CONSTITUTIONS,
            questionText = "What does 'rule of law' mean?",
            options = listOf(
                "Everyone, including leaders, is subject to the law",
                "Leaders may ignore laws when convenient",
                "Laws only apply to foreigners",
                "Customs override written law",
            ),
            correctAnswerIndex = 0,
            explanation = "Rule of law means that all people and institutions, including those in power, are accountable to laws that are fairly applied.",
        ),
        question(
            category = QuizCategory.PUBLIC_POLICY,
            questionText = "What is 'public policy'?",
            options = listOf(
                "A course of action adopted by a government to address a public issue",
                "A private company's internal rulebook",
                "A religious doctrine",
                "A foreign country's military strategy",
            ),
            correctAnswerIndex = 0,
            explanation = "Public policy refers to the laws, regulations, and actions a government takes to address issues affecting the public.",
        ),
        question(
            category = QuizCategory.PUBLIC_POLICY,
            questionText = "Which of these is typically an example of public policy?",
            options = listOf(
                "A national minimum wage law",
                "A family's grocery list",
                "A private club's membership fee",
                "A company's logo design",
            ),
            correctAnswerIndex = 0,
            explanation = "A national minimum wage law is a government action that affects the public and is therefore an example of public policy.",
        ),
        question(
            category = QuizCategory.POLITICAL_HISTORY,
            questionText = "Which ancient civilization is often credited with developing one of the earliest forms of democracy?",
            options = listOf("Ancient Athens (Greece)", "Ancient Egypt", "The Roman Empire's monarchy period", "The Mongol Empire"),
            correctAnswerIndex = 0,
            explanation = "Ancient Athens is widely credited with developing one of the earliest known forms of democracy, where citizens could participate directly in decision-making.",
        ),
        question(
            category = QuizCategory.POLITICAL_HISTORY,
            questionText = "What term describes the transfer of power from a colonial power to local self-governance?",
            options = listOf("Decolonization", "Annexation", "Federalism", "Globalization"),
            correctAnswerIndex = 0,
            explanation = "Decolonization refers to the process by which colonies gain independence and establish self-governance.",
        ),
        question(
            category = QuizCategory.INTERNATIONAL_RELATIONS,
            questionText = "What is the primary purpose of the United Nations?",
            options = listOf(
                "To promote international peace, security, and cooperation",
                "To enforce a single global currency",
                "To run elections in every country",
                "To replace national governments",
            ),
            correctAnswerIndex = 0,
            explanation = "The United Nations was founded to maintain international peace and security and foster cooperation among nations.",
        ),
        question(
            category = QuizCategory.INTERNATIONAL_RELATIONS,
            questionText = "What is 'diplomacy'?",
            options = listOf(
                "Managing relations between countries through dialogue and negotiation",
                "A type of military invasion",
                "A form of domestic taxation",
                "An economic recession",
            ),
            correctAnswerIndex = 0,
            explanation = "Diplomacy is the conduct of relations between nations primarily through negotiation and dialogue rather than force.",
        ),
        question(
            category = QuizCategory.CIVIC_RESPONSIBILITY,
            questionText = "Which of these is generally considered a civic responsibility?",
            options = listOf(
                "Voting in elections",
                "Avoiding all contact with neighbors",
                "Ignoring local laws",
                "Refusing to pay any taxes",
            ),
            correctAnswerIndex = 0,
            explanation = "Voting is a key civic responsibility that allows citizens to participate in shaping their government.",
        ),
        question(
            category = QuizCategory.CIVIC_RESPONSIBILITY,
            questionText = "What is one way citizens can stay informed about civic issues?",
            options = listOf(
                "Following credible news sources and government announcements",
                "Only relying on rumors from friends",
                "Avoiding all news",
                "Believing everything shared on social media without checking",
            ),
            correctAnswerIndex = 0,
            explanation = "Staying informed through credible news sources and official announcements helps citizens make informed civic decisions.",
        ),
    )
}
