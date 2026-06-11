package com.civiq.app.presentation.quiz

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.civiq.app.R
import com.civiq.app.domain.model.QuestionDifficulty
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.presentation.components.CiviQTopAppBar
import com.civiq.app.presentation.theme.extendedColors

/**
 * Stateless screen that lets the user pick a difficulty for [category]
 * before starting a quiz session.
 */
@Composable
fun QuizDifficultySelectScreen(
    category: QuizCategory,
    onSelectDifficulty: (QuestionDifficulty) -> Unit,
    onBackClick: () -> Unit,
) {
    Scaffold(
        topBar = {
            CiviQTopAppBar(
                title = "${category.emoji} ${category.displayName}",
                onBackClick = onBackClick,
            )
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = stringResource(R.string.quiz_difficulty_select_title),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
            QuestionDifficulty.entries.forEach { difficulty ->
                DifficultyCard(difficulty = difficulty, onClick = { onSelectDifficulty(difficulty) })
            }
        }
    }
}

@Composable
private fun DifficultyCard(difficulty: QuestionDifficulty, onClick: () -> Unit, modifier: Modifier = Modifier) {
    Card(
        onClick = onClick,
        modifier = modifier.fillMaxWidth(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(difficultyColor(difficulty)),
            )
            Text(
                text = difficulty.displayName,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Medium,
                modifier = Modifier.weight(1f),
            )
            Text(
                text = stringResource(R.string.quiz_difficulty_xp_multiplier, difficulty.xpMultiplier.toString()),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.extendedColors.xp,
            )
        }
    }
}

@Composable
private fun difficultyColor(difficulty: QuestionDifficulty): Color = when (difficulty) {
    QuestionDifficulty.BEGINNER -> MaterialTheme.extendedColors.difficultyBeginner
    QuestionDifficulty.INTERMEDIATE -> MaterialTheme.extendedColors.difficultyIntermediate
    QuestionDifficulty.ADVANCED -> MaterialTheme.extendedColors.difficultyAdvanced
    QuestionDifficulty.EXPERT -> MaterialTheme.extendedColors.difficultyExpert
}
