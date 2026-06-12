package com.civiq.app.presentation.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenu
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MenuAnchorType
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.civiq.app.R
import com.civiq.app.domain.model.UserRole
import com.civiq.app.presentation.theme.extendedColors

/** Localized display label for [role], or "All" when [role] is `null` (used by role filter UI). */
@Composable
fun roleLabel(role: UserRole?): String = when (role) {
    null -> stringResource(R.string.admin_role_all)
    UserRole.GUEST -> stringResource(R.string.admin_role_guest)
    UserRole.REGISTERED -> stringResource(R.string.admin_role_registered)
    UserRole.PREMIUM -> stringResource(R.string.profile_premium_badge)
    UserRole.ADMIN -> stringResource(R.string.profile_admin_badge)
}

/** Small colored pill summarizing a user's [UserRole], used in the admin user list and detail screens. */
@Composable
fun AdminRoleChip(role: UserRole, modifier: Modifier = Modifier) {
    val (container, content) = when (role) {
        UserRole.ADMIN -> MaterialTheme.colorScheme.tertiaryContainer to MaterialTheme.colorScheme.onTertiaryContainer
        UserRole.PREMIUM -> MaterialTheme.extendedColors.coinContainer to MaterialTheme.extendedColors.coin
        UserRole.REGISTERED -> MaterialTheme.colorScheme.secondaryContainer to MaterialTheme.colorScheme.onSecondaryContainer
        UserRole.GUEST -> MaterialTheme.colorScheme.surfaceVariant to MaterialTheme.colorScheme.onSurfaceVariant
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(50))
            .background(container)
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = roleLabel(role),
            style = MaterialTheme.typography.labelSmall,
            color = content,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

/**
 * Read-only text field that opens a dropdown menu of [options] when tapped,
 * used by the question editor and daily challenge form to pick enum values
 * (category, difficulty, type, tone).
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T> AdminDropdownField(
    label: String,
    selectedLabel: String,
    options: List<T>,
    optionLabel: (T) -> String,
    onOptionSelected: (T) -> Unit,
    modifier: Modifier = Modifier,
) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = it },
        modifier = modifier.fillMaxWidth(),
    ) {
        OutlinedTextField(
            value = selectedLabel,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier
                .menuAnchor(MenuAnchorType.PrimaryNotEditable)
                .fillMaxWidth(),
        )
        ExposedDropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onOptionSelected(option)
                        expanded = false
                    },
                )
            }
        }
    }
}
