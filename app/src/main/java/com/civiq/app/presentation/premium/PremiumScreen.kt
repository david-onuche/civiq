package com.civiq.app.presentation.premium

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.AllInclusive
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.civiq.app.R
import com.civiq.app.domain.model.PaymentProvider
import com.civiq.app.domain.model.Subscription
import com.civiq.app.domain.model.SubscriptionStatus
import com.civiq.app.domain.model.SubscriptionTier
import com.civiq.app.presentation.components.CiviQButton
import com.civiq.app.presentation.components.CiviQOutlinedButton
import com.civiq.app.presentation.components.CiviQTopAppBar
import com.civiq.app.presentation.components.LoadingState
import com.civiq.app.presentation.theme.extendedColors
import com.civiq.app.utils.toFormattedDate

/**
 * CiviQ's Premium paywall/management screen. Free users see pricing tiers
 * and a feature list with a (mocked) subscribe flow; Premium users see their
 * current plan and a cancel action; Admins, who already have every Premium
 * capability via their role, see an informational card instead of pricing.
 */
@Composable
fun PremiumScreen(
    onBackClick: () -> Unit,
    viewModel: PremiumViewModel = hiltViewModel(),
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var showCancelDialog by remember { mutableStateOf(false) }

    Scaffold(
        topBar = { CiviQTopAppBar(title = stringResource(R.string.premium_title), onBackClick = onBackClick) },
    ) { paddingValues ->
        val user = uiState.user
        if (uiState.isLoading || user == null) {
            LoadingState(modifier = Modifier.padding(paddingValues))
            return@Scaffold
        }

        val subscription = uiState.subscription
        val isActivePremium = user.isPremium ||
            (subscription != null && subscription.isActive && subscription.tier != SubscriptionTier.FREE)

        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            item { PremiumHeader() }

            uiState.errorMessage?.let { error ->
                item {
                    Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                        Text(
                            text = error.asString(),
                            modifier = Modifier.padding(16.dp),
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    }
                }
            }

            when {
                user.isAdmin -> item { AdminAccessCard() }
                isActivePremium -> item {
                    CurrentPlanCard(
                        subscription = subscription,
                        isProcessing = uiState.isProcessing,
                        onCancelClick = { showCancelDialog = true },
                    )
                }
                else -> {
                    item {
                        PricingCard(
                            tier = SubscriptionTier.PREMIUM_MONTHLY,
                            isProcessing = uiState.isProcessing,
                            onSubscribeClick = { viewModel.subscribe(SubscriptionTier.PREMIUM_MONTHLY, PaymentProvider.GOOGLE_PLAY) },
                        )
                    }
                    item {
                        PricingCard(
                            tier = SubscriptionTier.PREMIUM_YEARLY,
                            isProcessing = uiState.isProcessing,
                            isBestValue = true,
                            onSubscribeClick = { viewModel.subscribe(SubscriptionTier.PREMIUM_YEARLY, PaymentProvider.GOOGLE_PLAY) },
                        )
                    }
                }
            }

            item { PremiumFeatureList() }
        }
    }

    if (showCancelDialog) {
        AlertDialog(
            onDismissRequest = { showCancelDialog = false },
            title = { Text(stringResource(R.string.premium_cancel_dialog_title)) },
            text = { Text(stringResource(R.string.premium_cancel_dialog_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.cancelSubscription()
                        showCancelDialog = false
                    },
                ) {
                    Text(stringResource(R.string.premium_cancel_dialog_confirm), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCancelDialog = false }) {
                    Text(stringResource(R.string.premium_cancel_dialog_dismiss))
                }
            },
        )
    }
}

@Composable
private fun PremiumHeader(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Icon(
            imageVector = Icons.Filled.WorkspacePremium,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.extendedColors.coin,
        )
        Text(
            text = stringResource(R.string.premium_header_title),
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center,
        )
        Text(
            text = stringResource(R.string.premium_header_subtitle),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun AdminAccessCard(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Icon(
                imageVector = Icons.Filled.AdminPanelSettings,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiaryContainer,
            )
            Text(
                text = stringResource(R.string.premium_admin_access_message),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onTertiaryContainer,
            )
        }
    }
}

@Composable
private fun CurrentPlanCard(
    subscription: Subscription?,
    isProcessing: Boolean,
    onCancelClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Card(modifier = modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Icon(
                    imageVector = Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = MaterialTheme.extendedColors.correct,
                )
                Column {
                    Text(
                        text = stringResource(R.string.premium_status_active),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        text = (subscription?.tier ?: SubscriptionTier.PREMIUM_MONTHLY).displayName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
            val expiresAt = subscription?.expiresAt
            Text(
                text = if (expiresAt != null) {
                    stringResource(R.string.premium_renews_on, expiresAt.toFormattedDate())
                } else {
                    stringResource(R.string.premium_no_expiry)
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
            CiviQOutlinedButton(
                text = stringResource(R.string.premium_cancel_cta),
                onClick = onCancelClick,
                enabled = !isProcessing && subscription?.status != SubscriptionStatus.CANCELED,
            )
        }
    }
}

@Composable
private fun PricingCard(
    tier: SubscriptionTier,
    isProcessing: Boolean,
    onSubscribeClick: () -> Unit,
    isBestValue: Boolean = false,
    modifier: Modifier = Modifier,
) {
    val priceRes = when (tier) {
        SubscriptionTier.PREMIUM_YEARLY -> R.string.premium_plan_yearly_price
        else -> R.string.premium_plan_monthly_price
    }
    Card(
        modifier = modifier.fillMaxWidth(),
        colors = if (isBestValue) {
            CardDefaults.cardColors(containerColor = MaterialTheme.extendedColors.coinContainer)
        } else {
            CardDefaults.cardColors()
        },
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                Text(
                    text = tier.displayName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.weight(1f),
                )
                if (isBestValue) {
                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(MaterialTheme.extendedColors.coin)
                            .padding(horizontal = 10.dp, vertical = 4.dp),
                    ) {
                        Text(
                            text = stringResource(R.string.premium_best_value_badge),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onPrimary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            }
            Text(text = stringResource(priceRes), style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            CiviQButton(
                text = stringResource(R.string.premium_subscribe_cta),
                onClick = onSubscribeClick,
                isLoading = isProcessing,
            )
        }
    }
}

@Composable
private fun PremiumFeatureList(modifier: Modifier = Modifier) {
    Column(modifier = modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(
            text = stringResource(R.string.premium_features_heading),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
        )
        PremiumFeatureRow(
            icon = Icons.Filled.AllInclusive,
            title = stringResource(R.string.premium_feature_unlimited_quizzes_title),
            description = stringResource(R.string.premium_feature_unlimited_quizzes_desc),
        )
        PremiumFeatureRow(
            icon = Icons.Filled.Shield,
            title = stringResource(R.string.premium_feature_ad_free_title),
            description = stringResource(R.string.premium_feature_ad_free_desc),
        )
        PremiumFeatureRow(
            icon = Icons.Filled.AutoAwesome,
            title = stringResource(R.string.premium_feature_ai_coach_title),
            description = stringResource(R.string.premium_feature_ai_coach_desc),
        )
        PremiumFeatureRow(
            icon = Icons.Filled.EmojiEvents,
            title = stringResource(R.string.premium_feature_exclusive_challenges_title),
            description = stringResource(R.string.premium_feature_exclusive_challenges_desc),
        )
    }
}

@Composable
private fun PremiumFeatureRow(icon: ImageVector, title: String, description: String, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(28.dp),
        )
        Column {
            Text(text = title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.SemiBold)
            Text(text = description, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}
