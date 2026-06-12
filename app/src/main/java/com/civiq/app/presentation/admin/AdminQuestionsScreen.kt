package com.civiq.app.presentation.admin

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.civiq.app.R
import com.civiq.app.domain.model.Question
import com.civiq.app.domain.model.QuizCategory
import com.civiq.app.presentation.components.CiviQTopAppBar
import com.civiq.app.presentation.components.EmptyState
import com.civiq.app.presentation.components.ResourceContent

/**
 * Lists the question bank, filterable by [QuizCategory], with create/edit/delete
 * actions. Editing opens [AdminQuestionEditorScreen].
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminQuestionsScreen(
    onBackClick: () -> Unit,
    onAddQuestionClick: () -> Unit,
    onQuestionClick: (String) -> Unit,
    viewModel: AdminQuestionsViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var questionPendingDelete by remember { mutableStateOf<Question?>(null) }

    Scaffold(
        topBar = { CiviQTopAppBar(title = stringResource(R.string.admin_questions_title), onBackClick = onBackClick) },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddQuestionClick) {
                Icon(imageVector = Icons.Filled.Add, contentDescription = stringResource(R.string.admin_questions_add))
            }
        },
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                val options: List<QuizCategory?> = listOf(null) + QuizCategory.entries
                options.forEach { category ->
                    FilterChip(
                        selected = uiState.categoryFilter == category,
                        onClick = { viewModel.onCategoryFilterChanged(category) },
                        label = { Text(category?.let { "${it.emoji} ${it.displayName}" } ?: stringResource(R.string.admin_role_all)) },
                    )
                }
            }
            ResourceContent(resource = uiState.questions, modifier = Modifier.fillMaxSize()) { questions ->
                if (questions.isEmpty()) {
                    EmptyState(
                        title = stringResource(R.string.admin_questions_empty_title),
                        icon = Icons.Filled.MenuBook,
                        modifier = Modifier.fillMaxSize(),
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(start = 16.dp, top = 0.dp, end = 16.dp, bottom = 96.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp),
                    ) {
                        items(questions, key = { it.id }) { question ->
                            AdminQuestionRow(
                                question = question,
                                onClick = { onQuestionClick(question.id) },
                                onDeleteClick = { questionPendingDelete = question },
                            )
                        }
                    }
                }
            }
        }
    }

    val questionToDelete = questionPendingDelete
    if (questionToDelete != null) {
        AlertDialog(
            onDismissRequest = { questionPendingDelete = null },
            title = { Text(stringResource(R.string.admin_questions_delete_title)) },
            text = { Text(stringResource(R.string.admin_questions_delete_message)) },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.onDeleteQuestion(questionToDelete.id)
                    questionPendingDelete = null
                }) {
                    Text(stringResource(R.string.common_delete))
                }
            },
            dismissButton = {
                TextButton(onClick = { questionPendingDelete = null }) {
                    Text(stringResource(R.string.common_cancel))
                }
            },
        )
    }
}

@Composable
private fun AdminQuestionRow(
    question: Question,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(onClick = onClick, modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = question.questionText,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = "${question.category.emoji} ${question.category.displayName} • ${question.difficulty.displayName}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
            IconButton(onClick = onDeleteClick) {
                Icon(
                    imageVector = Icons.Filled.Delete,
                    contentDescription = stringResource(R.string.common_delete),
                    tint = MaterialTheme.colorScheme.error,
                )
            }
        }
    }
}
