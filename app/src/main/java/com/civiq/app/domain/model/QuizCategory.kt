package com.civiq.app.domain.model

/**
 * The eight core civic learning areas covered by CiviQ's quiz content,
 * mirroring the "Core Learning Areas" defined in the project README.
 */
enum class QuizCategory(
    val displayName: String,
    val description: String,
    val emoji: String,
) {
    DEMOCRACY(
        displayName = "Democracy",
        description = "How democratic systems work and why they matter.",
        emoji = "🗳️", // ballot box
    ),
    ELECTIONS(
        displayName = "Elections",
        description = "Voting systems, electoral processes, and political participation.",
        emoji = "🗳",
    ),
    GOVERNANCE(
        displayName = "Governance",
        description = "How governments function at local, regional, and national levels.",
        emoji = "🏛️", // classical building
    ),
    CONSTITUTIONS(
        displayName = "Constitutions",
        description = "Rights, responsibilities, institutions, and the rule of law.",
        emoji = "📜", // scroll
    ),
    PUBLIC_POLICY(
        displayName = "Public Policy",
        description = "How policies are created, implemented, and evaluated.",
        emoji = "📋", // clipboard
    ),
    POLITICAL_HISTORY(
        displayName = "Political History",
        description = "Important historical events, leaders, movements, and transitions.",
        emoji = "📚", // books
    ),
    INTERNATIONAL_RELATIONS(
        displayName = "International Relations",
        description = "Diplomacy, global institutions, and world affairs.",
        emoji = "🌍", // globe
    ),
    CIVIC_RESPONSIBILITY(
        displayName = "Civic Responsibility",
        description = "Knowledge and skills to be an active, informed citizen.",
        emoji = "🤝", // handshake
    ),
}
