package com.civiq.app.presentation.design

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.InfiniteRepeatableSpec
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.Spring.DampingRatioMediumBouncy
import androidx.compose.animation.core.Spring.StiffnessLow
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.animateIntOffsetAsState
import androidx.compose.animation.core.animateOffsetAsState
import androidx.compose.animation.core.animateValueAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.keyframes
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentHeight
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Cake
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Grade
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Leaderboard
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Medal
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.TipsAndUpdates
import androidx.compose.material.icons.filled.Tornado
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.MutableState
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.paint
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val CiviQBrandBlue = Color(0xFF3C6FFF)
private val CiviQBrandTeal = Color(0xFF1DD0C0)
private val CiviQBrandYellow = Color(0xFFFFCE52)
private val CiviQBrandPurple = Color(0xFF5A3BFF)
private val CiviQBackgroundDark = Color(0xFF0F1525)
private val CiviQSurfaceDark = Color(0xFF141B32)
private val CiviQSurfaceLight = Color(0xFFF6F7FF)
private val CiviQTextHigh = Color(0xFFEFF3FF)
private val CiviQTextLow = Color(0xFFCBD4EB)
private val CiviQSuccess = Color(0xFF4CE89D)
private val CiviQError = Color(0xFFFF5E6C)
private val CiviQWarning = Color(0xFFFFAC33)

object CiviQDimens {
    val space0 = 0.dp
    val space1 = 4.dp
    val space2 = 8.dp
    val space3 = 12.dp
    val space4 = 16.dp
    val space5 = 20.dp
    val space6 = 24.dp
    val space7 = 32.dp
    val space8 = 40.dp
    val space9 = 48.dp
}

private val onboardingPages = listOf(
    OnboardingStep(
        title = "Welcome to CiviQ",
        description = "Learn politics, governance, elections, and civic power with play.",
        heroLabel = "Globe",
        highlight = "Rotating globe shows your civic world.",
    ),
    OnboardingStep(
        title = "Politics is not boring",
        description = "Funny candidates, quirky quizzes, and witty civic stories await.",
        heroLabel = "Characters",
        highlight = "Animated heroes bring policy to life.",
    ),
    OnboardingStep(
        title = "Learn with AI",
        description = "Your personal civic coach generates challenges tailored to your pace.",
        heroLabel = "Assistant",
        highlight = "AI hologram guides your next mission.",
    ),
    OnboardingStep(
        title = "Earn rewards",
        description = "Collect coins, badges and XP for every smart answer.",
        heroLabel = "Rewards",
        highlight = "Every win feels like a celebration.",
    ),
    OnboardingStep(
        title = "Choose your style",
        description = "Pick the civic path that matches your vibe and unlock daily quests.",
        heroLabel = "Style",
        highlight = "Casual, competitive, leader or champion.",
    ),
)

private val learningStyles = listOf(
    LearningStyle.CasualLearner,
    LearningStyle.QuizMaster,
    LearningStyle.FutureLeader,
    LearningStyle.CivicChampion,
)

private val cardElevation = 18.dp
private val cardShape = RoundedCornerShape(28.dp)

private const val defaultAnimationDuration = 450

enum class LearningStyle(val title: String, val subtitle: String, val icon: ImageVector) {
    CasualLearner("Casual Learner", "Easy pace, friendly missions", Icons.Default.Home),
    QuizMaster("Quiz Master", "Fast rounds and streaks", Icons.Default.FlashOn),
    FutureLeader("Future Leader", "Deep civics and policy paths", Icons.Default.Shield),
    CivicChampion("Civic Champion", "Compete and climb the board", Icons.Default.Tornado),
}

data class OnboardingStep(
    val title: String,
    val description: String,
    val heroLabel: String,
    val highlight: String,
)

data class HomeUiState(
    val xpProgress: Float = 0.6f,
    val currentLevel: Int = 7,
    val coins: Int = 290,
    val streakDays: Int = 12,
    val dailyChallengeTitle: String = "Fast Debate",
    val continueLearning: String = "Policy Flashcards",
    val nextReward: String = "50 XP for 3 correct answers",
    val mascotMessage: String = "Your civic wingman says: " +
        "Keep the streak burning!",
)

data class QuizUiState(
    val question: String,
    val options: List<String>,
    val selectedOption: String? = null,
    val isCorrect: Boolean? = null,
    val progress: Float = 0.3f,
    val xpEarned: Int = 20,
    val coinsEarned: Int = 5,
    val isLoading: Boolean = false,
)

data class AIGameCard(
    val title: String,
    val description: String,
    val multiplier: String,
    val difficulty: String,
)

data class AIChallengeUiState(
    val title: String = "AI Challenge",
    val subtitle: String = "Holographic missions from your civic guide.",
    val cards: List<AIGameCard> = listOf(
        AIGameCard("Policy Holo Sprint", "Solve 5 questions in 90 seconds", "x2 XP", "Advanced"),
        AIGameCard("Campaign Quiz", "Predict election outcomes with logic", "x3 XP", "Expert"),
        AIGameCard("Leadership Lab", "Build your civic manifesto", "x4 XP", "Master"),
    ),
)

data class LeaderboardEntry(
    val rank: Int,
    val displayName: String,
    val score: Int,
    val delta: Int,
    val isCurrentUser: Boolean,
)

data class LeaderboardUiState(
    val topThree: List<LeaderboardEntry> = listOf(
        LeaderboardEntry(1, "Ava", 2880, +2, false),
        LeaderboardEntry(2, "Mila", 2760, -1, false),
        LeaderboardEntry(3, "You", 2620, +3, true),
    ),
    val leaderboard: List<LeaderboardEntry> = listOf(
        LeaderboardEntry(4, "Noah", 2400, +1, false),
        LeaderboardEntry(5, "Theo", 2285, -2, false),
        LeaderboardEntry(6, "Zoe", 2160, +4, false),
    ),
)

data class ProfileHistoryItem(
    val title: String,
    val detail: String,
    val timestamp: String,
)

data class ProfileUiState(
    val name: String = "Jordan",
    val avatarInitials: String = "CJ",
    val level: Int = 9,
    val xpProgress: Float = 0.72f,
    val streakDays: Int = 18,
    val achievements: List<String> = listOf("Campaign Starter", "Constitution Ace", "Streak Hero"),
    val history: List<ProfileHistoryItem> = listOf(
        ProfileHistoryItem("Senate Sprint", "5/5 correct", "Today"),
        ProfileHistoryItem("Election IQ", "4/5 correct", "Yesterday"),
        ProfileHistoryItem("Policy Puzzle", "3/3 completed", "2 days ago"),
    ),
)

data class AchievementItem(
    val title: String,
    val description: String,
    val unlocked: Boolean,
)

data class AchievementsUiState(
    val achievements: List<AchievementItem> = listOf(
        AchievementItem("First Vote", "Complete your first quiz", true),
        AchievementItem("Leaderboard Entry", "Reach top 10 around the world", false),
        AchievementItem("Streak Keeper", "Keep a 7-day streak", true),
    ),
)

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun CiviQOnboardingScreen(
    activePage: Int,
    onPageChange: (Int) -> Unit,
    onSkip: () -> Unit,
    onSelectStyle: (LearningStyle) -> Unit,
) {
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(activePage) {
        if (pagerState.currentPage != activePage) {
            pagerState.animateScrollToPage(activePage)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(brush = Brush.verticalGradient(
                colors = listOf(CiviQBackgroundDark, CiviQSurfaceDark),
                endY = 1200f,
            ))
            .padding(24.dp),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End,
        ) {
            TextButton(onClick = onSkip) {
                Text("Skip", color = CiviQBrandYellow)
            }
        }

        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f),
            pageSpacing = 16.dp,
        ) { page ->
            val step = onboardingPages[page]
            CiviQOnboardingPage(step = step, pageIndex = page)
        }

        PagerIndicator(
            count = onboardingPages.size,
            activeIndex = pagerState.currentPage,
            modifier = Modifier
                .align(Alignment.CenterHorizontally)
                .padding(vertical = 18.dp),
        )

        if (pagerState.currentPage == onboardingPages.lastIndex) {
            LearningStyleSelection(
                styles = learningStyles,
                onSelect = onSelectStyle,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            val buttonLabel = if (pagerState.currentPage == onboardingPages.lastIndex) "Get Started" else "Next"
            Button(
                onClick = {
                    if (pagerState.currentPage == onboardingPages.lastIndex) {
                        onSelectStyle(learningStyles.first())
                    } else {
                        val next = pagerState.currentPage + 1
                        coroutineScope.launch { pagerState.animateScrollToPage(next) }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
            ) {
                Text(buttonLabel)
            }
        }
    }
}

@Composable
private fun CiviQOnboardingPage(step: OnboardingStep, pageIndex: Int) {
    val pulse = rememberInfiniteTransition()
    val glow by pulse.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 2200
                1.0f at 0
                1.06f at 1100
                0.96f at 2200
            },
        ),
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .clip(RoundedCornerShape(36.dp))
            .background(
                brush = Brush.linearGradient(
                    colors = listOf(CiviQBrandBlue.copy(alpha = 0.95f), CiviQBrandPurple.copy(alpha = 0.85f)),
                ),
    )
            .padding(28.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .graphicsLayer { scaleX = glow; scaleY = glow }
                .clip(RoundedCornerShape(36.dp))
                .background(
                    brush = Brush.radialGradient(
                        colors = listOf(CiviQBrandYellow.copy(alpha = 0.35f), Color.Transparent),
                    ),
        )
                .padding(16.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(140.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.14f))
                    .border(2.dp, CiviQBrandYellow, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = step.heroLabel,
                    style = MaterialTheme.typography.headlineSmall.copy(color = Color.White),
                    textAlign = TextAlign.Center,
                )
            }
            FloatingAccentParticles(count = 6)
        }

        Spacer(modifier = Modifier.height(28.dp))
        Text(
            text = step.title,
            style = MaterialTheme.typography.displaySmall.copy(color = Color.White),
            textAlign = TextAlign.Center,
        )
        Spacer(modifier = Modifier.height(14.dp))
        Text(
            text = step.description,
            style = MaterialTheme.typography.bodyLarge.copy(color = CiviQTextLow),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 10.dp),
        )
        Spacer(modifier = Modifier.height(18.dp))
        Text(
            text = step.highlight,
            style = MaterialTheme.typography.titleMedium.copy(color = CiviQBrandYellow),
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun PagerIndicator(count: Int, activeIndex: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(count) { index ->
            Box(
                modifier = Modifier
                    .size(if (index == activeIndex) 12.dp else 8.dp)
                    .clip(CircleShape)
                    .background(if (index == activeIndex) CiviQBrandYellow else CiviQTextLow),
            )
        }
    }
}

@Composable
private fun LearningStyleSelection(styles: List<LearningStyle>, onSelect: (LearningStyle) -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "Choose your learning style",
            style = MaterialTheme.typography.titleLarge.copy(color = Color.White),
        )
        Spacer(modifier = Modifier.height(16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            styles.chunked(2).forEach { rowStyles ->
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    rowStyles.forEach { style ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onSelect(style) }
                                .graphicsLayer { shadowElevation = 14f }
                                .background(MaterialTheme.colorScheme.surfaceVariant),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                            shape = RoundedCornerShape(24.dp),
                        ) {
                            Row(
                                modifier = Modifier.padding(14.dp),
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                verticalAlignment = Alignment.CenterVertically,
                            ) {
                                Icon(style.icon, contentDescription = style.title, tint = CiviQBrandYellow)
                                Column {
                                    Text(text = style.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                                    Text(text = style.subtitle, style = MaterialTheme.typography.bodySmall, color = CiviQTextLow)
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun CiviQHomeScreen(
    state: HomeUiState,
    onStartDailyChallenge: () -> Unit,
    onContinueLearning: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedCiviQBackground()
        Column(modifier = Modifier.padding(24.dp)) {
            CiviQHeader(state)
            Spacer(modifier = Modifier.height(22.dp))
            AnimatedXpBar(progress = state.xpProgress)
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                StatBubble("Level", "${state.currentLevel}", Icons.Default.Star, CiviQBrandYellow)
                StatBubble("Streak", "${state.streakDays}d", Icons.Default.FlashOn, CiviQWarning)
            }
            Spacer(modifier = Modifier.height(24.dp))
            CiviQCard(
                title = "Daily Challenge",
                subtitle = state.dailyChallengeTitle,
                accent = CiviQBrandTeal,
                buttonText = "Start",
                onButtonClick = onStartDailyChallenge,
            )
            Spacer(modifier = Modifier.height(18.dp))
            CiviQCard(
                title = "Continue Learning",
                subtitle = state.continueLearning,
                accent = CiviQBrandBlue,
                buttonText = "Resume",
                onButtonClick = onContinueLearning,
            )
            Spacer(modifier = Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth()) {
                MiniMissionCard("Policy Burst", "+30 XP")
                MiniMissionCard("Quiz Relay", "+coin")
            }
        }
        MascotCoach(
            message = state.mascotMessage,
            modifier = Modifier.align(Alignment.BottomEnd).padding(24.dp),
        )
    }
}

@Composable
private fun CiviQHeader(state: HomeUiState) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.05f), RoundedCornerShape(30.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column {
            Text("CiviQ Dashboard", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Text("Smart civic progress, playful rewards", style = MaterialTheme.typography.bodyMedium, color = CiviQTextLow)
        }
        Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            CiviQCircleBadge(icon = Icons.Default.EmojiEvents, value = "${state.coins}")
            CiviQCircleBadge(icon = Icons.Default.Grade, value = "${state.currentLevel}")
        }
    }
}

@Composable
private fun CiviQCircleBadge(icon: ImageVector, value: String) {
    Card(
        shape = CircleShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.size(56.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        Box(contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(icon, contentDescription = null, tint = CiviQBrandYellow, modifier = Modifier.size(20.dp))
                Text(value, style = MaterialTheme.typography.labelMedium, color = Color.White)
            }
        }
    }
}

@Composable
private fun AnimatedXpBar(progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium),
    )
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
            Text("XP Progress", style = MaterialTheme.typography.bodyMedium, color = CiviQTextLow)
            Text("${(animatedProgress * 100).toInt()}%", style = MaterialTheme.typography.bodyMedium, color = Color.White)
        }
        Spacer(modifier = Modifier.height(10.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(20.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(animatedProgress)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(CiviQBrandBlue, CiviQBrandTeal),
                        ),
                    ),
            )
        }
    }
}

@Composable
private fun StatBubble(label: String, value: String, icon: ImageVector, color: Color) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.weight(1f),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                Icon(icon, contentDescription = label, tint = color)
                Text(label, style = MaterialTheme.typography.bodyMedium, color = CiviQTextLow)
            }
            Text(value, style = MaterialTheme.typography.headlineSmall, color = Color.White)
        }
    }
}

@Composable
private fun CiviQCard(
    title: String,
    subtitle: String,
    accent: Color,
    buttonText: String,
    onButtonClick: () -> Unit,
) {
    Card(
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = cardElevation),
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.titleLarge, color = Color.White)
                Spacer(modifier = Modifier.height(6.dp))
                Text(subtitle, style = MaterialTheme.typography.bodyMedium, color = CiviQTextLow)
            }
            Button(
                onClick = onButtonClick,
                modifier = Modifier.height(46.dp),
                shape = RoundedCornerShape(18.dp),
            ) {
                Text(buttonText)
            }
        }
    }
}

@Composable
private fun MiniMissionCard(title: String, reward: String) {
    Card(
        modifier = Modifier.weight(1f),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
        elevation = CardDefaults.cardElevation(defaultElevation = 10.dp),
    ) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(title, style = MaterialTheme.typography.titleSmall, color = Color.White)
            Text(reward, style = MaterialTheme.typography.bodySmall, color = CiviQTextLow)
        }
    }
}

@Composable
fun CiviQQuizScreen(
    state: QuizUiState,
    onAnswerSelected: (String) -> Unit,
    onNextQuestion: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val questionEntrance = remember { Animatable(-250f) }
    val answerShake = remember { Animatable(0f) }
    val showReward by remember { mutableStateOf(state.isCorrect == true) }

    LaunchedEffect(state.question) {
        questionEntrance.snapTo(-250f)
        questionEntrance.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessLow))
    }
    LaunchedEffect(state.isCorrect) {
        if (state.isCorrect == false) {
            answerShake.snapTo(0f)
            answerShake.animateTo(12f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
            answerShake.animateTo(0f, spring(dampingRatio = Spring.DampingRatioMediumBouncy, stiffness = Spring.StiffnessMedium))
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AnimatedCiviQBackground(
            startColor = CiviQBrandPurple.copy(alpha = 0.8f),
            endColor = CiviQBrandBlue.copy(alpha = 0.5f),
        )

        Column(modifier = Modifier.padding(24.dp)) {
            QuizHeader(progress = state.progress)
            Spacer(modifier = Modifier.height(28.dp))
            Card(
                modifier = Modifier
                    .offset { IntOffset(questionEntrance.value.toInt(), 0) }
                    .fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(32.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 18.dp),
            ) {
                Column(modifier = Modifier.padding(26.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
                    Text("${(state.progress * 10).toInt()}/10", style = MaterialTheme.typography.labelLarge, color = CiviQBrandTeal)
                    Text(state.question, style = MaterialTheme.typography.headlineSmall, color = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                state.options.forEach { option ->
                    val isSelected = option == state.selectedOption
                    val answerColor = when {
                        isSelected && state.isCorrect == true -> CiviQSuccess
                        isSelected && state.isCorrect == false -> CiviQError
                        else -> MaterialTheme.colorScheme.surfaceVariant
                    }
                    val offset by animateDpAsState(
                        targetValue = if (isSelected && state.isCorrect == false) 8.dp else 0.dp,
                        animationSpec = spring(dampingRatio = DampingRatioMediumBouncy, stiffness = StiffnessLow),
                    )
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .offset(x = offset)
                            .clickable { onAnswerSelected(option) },
                        colors = CardDefaults.cardColors(containerColor = answerColor),
                        shape = RoundedCornerShape(22.dp),
                    ) {
                        Row(
                            modifier = Modifier.padding(18.dp),
                            verticalAlignment = Alignment.CenterVertically,
                        ) {
                            Text(option, style = MaterialTheme.typography.bodyLarge, color = Color.White)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(26.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp), modifier = Modifier.fillMaxWidth()) {
                ScorePill(icon = Icons.Default.Star, label = "XP", value = "+${state.xpEarned}")
                ScorePill(icon = Icons.Default.Coin, label = "Coins", value = "+${state.coinsEarned}")
            }
            Spacer(modifier = Modifier.height(24.dp))
            Button(onClick = onNextQuestion, modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(20.dp)) {
                Text("Next")
            }
        }

        if (showReward) {
            RewardCelebration(modifier = Modifier.align(Alignment.Center))
        }
    }
}

@Composable
private fun QuizHeader(progress: Float) {
    Column {
        Text("Quiz Arena", style = MaterialTheme.typography.titleLarge, color = Color.White)
        Spacer(modifier = Modifier.height(12.dp))
        LinearProgressBar(progress = progress)
    }
}

@Composable
private fun LinearProgressBar(progress: Float) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = defaultAnimationDuration, easing = FastOutSlowInEasing),
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(12.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(MaterialTheme.colorScheme.surfaceVariant),
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(RoundedCornerShape(10.dp))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(CiviQBrandYellow, CiviQBrandTeal),
                    ),
                ),
        )
    }
}

@Composable
private fun ScorePill(icon: ImageVector, label: String, value: String) {
    Card(
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.weight(1f),
    ) {
        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            Icon(icon, contentDescription = label, tint = CiviQBrandYellow)
            Column {
                Text(label, style = MaterialTheme.typography.labelMedium, color = CiviQTextLow)
                Text(value, style = MaterialTheme.typography.titleMedium, color = Color.White)
            }
        }
    }
}

@Composable
private fun RewardCelebration(modifier: Modifier = Modifier) {
    val pulse = rememberInfiniteTransition()
    val scale by pulse.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1200
                1.0f at 0
                1.05f at 600
                0.95f at 1200
            },
        ),
    )
    Card(
        modifier = modifier
            .size(240.dp)
            .graphicsLayer { scaleX = scale; scaleY = scale }
            .clip(RoundedCornerShape(32.dp)),
        colors = CardDefaults.cardColors(containerColor = CiviQBrandPurple),
        elevation = CardDefaults.cardElevation(defaultElevation = 24.dp),
    ) {
        Column(
            modifier = Modifier.padding(22.dp),
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text("Correct!", style = MaterialTheme.typography.headlineSmall, color = Color.White)
            Text("Reward earned", style = MaterialTheme.typography.bodyMedium, color = CiviQTextLow, textAlign = TextAlign.Center)
            Box(modifier = Modifier.size(80.dp).background(CiviQBrandYellow.copy(alpha = 0.18f), CircleShape), contentAlignment = Alignment.Center) {
                Icon(Icons.Default.EmojiEvents, contentDescription = null, tint = CiviQBrandYellow, modifier = Modifier.size(40.dp))
            }
        }
    }
}

@Composable
fun CiviQAIChallengeScreen(
    state: AIChallengeUiState,
    onChallengeSelected: (AIGameCard) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedCiviQBackground(startColor = CiviQBrandPurple, endColor = CiviQBrandBlue)
        Column(modifier = Modifier.padding(24.dp)) {
            Text(state.title, style = MaterialTheme.typography.displaySmall, color = Color.White)
            Spacer(modifier = Modifier.height(8.dp))
            Text(state.subtitle, style = MaterialTheme.typography.bodyLarge, color = CiviQTextLow)
            Spacer(modifier = Modifier.height(28.dp))
            state.cards.forEachIndexed { index, card ->
                CiviQGlowingChallengeCard(card = card, delayFactor = index, onClick = { onChallengeSelected(card) })
                Spacer(modifier = Modifier.height(16.dp))
            }
        }
    }
}

@Composable
private fun CiviQGlowingChallengeCard(card: AIGameCard, delayFactor: Int, onClick: () -> Unit) {
    val glow = rememberInfiniteTransition().animateFloat(
        initialValue = 0.92f,
        targetValue = 1.08f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 1800
                1.0f at 0
                1.08f at 900
                0.92f at 1800
            },
        ),
    )
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .graphicsLayer { scaleX = glow.value; scaleY = glow.value }
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(30.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
    ) {
        Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                Text(card.title, style = MaterialTheme.typography.titleLarge, color = Color.White)
                Text(card.multiplier, style = MaterialTheme.typography.labelLarge, color = CiviQBrandYellow)
            }
            Text(card.description, style = MaterialTheme.typography.bodyMedium, color = CiviQTextLow)
            Text(card.difficulty, style = MaterialTheme.typography.labelMedium, color = CiviQBrandTeal)
        }
    }
}

@Composable
fun CiviQLeaderboardScreen(
    state: LeaderboardUiState,
    onUserSelected: (LeaderboardEntry) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedCiviQBackground(startColor = CiviQBrandBlue, endColor = CiviQBackgroundDark)
        Column(modifier = Modifier.padding(24.dp)) {
            Text("Leaderboard", style = MaterialTheme.typography.displayMedium, color = Color.White)
            Spacer(modifier = Modifier.height(18.dp))
            PodiumRow(state.topThree)
            Spacer(modifier = Modifier.height(26.dp))
            Text("Rivals", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            LazyColumn(verticalArrangement = Arrangement.spacedBy(12.dp), contentPadding = PaddingValues(bottom = 24.dp)) {
                items(state.leaderboard) { entry ->
                    LeaderboardRow(entry = entry, onClick = { onUserSelected(entry) })
                }
            }
        }
    }
}

@Composable
private fun PodiumRow(entries: List<LeaderboardEntry>) {
    Row(horizontalArrangement = Arrangement.spacedBy(14.dp), modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Bottom) {
        entries.forEach { entry ->
            Card(
                modifier = Modifier.weight(1f),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 18.dp),
            ) {
                Column(modifier = Modifier.padding(18.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("#${entry.rank}", style = MaterialTheme.typography.titleLarge, color = CiviQBrandYellow)
                    Spacer(modifier = Modifier.height(10.dp))
                    Icon(if (entry.isCurrentUser) Icons.Default.Person else Icons.Default.EmojiEvents, contentDescription = null, tint = CiviQBrandTeal, modifier = Modifier.size(42.dp))
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(entry.displayName, style = MaterialTheme.typography.titleSmall, color = Color.White)
                    Text("${entry.score} pts", style = MaterialTheme.typography.bodySmall, color = CiviQTextLow)
                }
            }
        }
    }
}

@Composable
private fun LeaderboardRow(entry: LeaderboardEntry, onClick: () -> Unit) {
    val translation by animateDpAsState(targetValue = if (entry.isCurrentUser) 4.dp else 0.dp, animationSpec = spring())
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .offset(x = translation)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = if (entry.isCurrentUser) CiviQBrandPurple else MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Row(modifier = Modifier.padding(18.dp), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.SpaceBetween) {
            Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(CiviQBrandYellow.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(entry.rank.toString(), style = MaterialTheme.typography.titleMedium, color = CiviQBrandYellow)
                }
                Column {
                    Text(entry.displayName, style = MaterialTheme.typography.titleMedium, color = Color.White)
                    Text("${entry.score} pts", style = MaterialTheme.typography.bodySmall, color = CiviQTextLow)
                }
            }
            Text(if (entry.delta >= 0) "+${entry.delta}" else entry.delta.toString(), style = MaterialTheme.typography.titleSmall, color = if (entry.delta >= 0) CiviQSuccess else CiviQError)
        }
    }
}

@Composable
fun CiviQProfileScreen(
    state: ProfileUiState,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedCiviQBackground(startColor = CiviQBrandTeal, endColor = CiviQBrandBlue)
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(22.dp)) {
            ProfileHeader(state)
            Card(
                shape = RoundedCornerShape(28.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
                elevation = CardDefaults.cardElevation(defaultElevation = 16.dp),
                modifier = Modifier.fillMaxWidth(),
            ) {
                Column(modifier = Modifier.padding(22.dp), verticalArrangement = Arrangement.spacedBy(14.dp)) {
                    Text("Progress", style = MaterialTheme.typography.titleLarge, color = Color.White)
                    AnimatedXpBar(progress = state.xpProgress)
                    Text("Streak: ${state.streakDays} days", style = MaterialTheme.typography.bodyMedium, color = CiviQTextLow)
                }
            }
            Text("Achievement badges", style = MaterialTheme.typography.titleLarge, color = Color.White)
            FlowRowBadges(state.achievements)
            Text("Quiz history", style = MaterialTheme.typography.titleLarge, color = Color.White)
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                state.history.forEach { item ->
                    ProfileHistoryRow(item)
                }
            }
        }
    }
}

@Composable
private fun ProfileHeader(state: ProfileUiState) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        Box(
            modifier = Modifier
                .size(88.dp)
                .clip(CircleShape)
                .background(CiviQBrandPurple),
            contentAlignment = Alignment.Center,
        ) {
            Text(state.avatarInitials, style = MaterialTheme.typography.displaySmall, color = Color.White)
        }
        Column {
            Text(state.name, style = MaterialTheme.typography.displaySmall, color = Color.White)
            Text("Level ${state.level}", style = MaterialTheme.typography.titleLarge, color = CiviQBrandYellow)
        }
    }
}

@Composable
private fun FlowRowBadges(badges: List<String>) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        badges.chunked(2).forEach { rowBadges ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
                rowBadges.forEach { badge ->
                    Card(
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                    ) {
                        Row(modifier = Modifier.padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.Medal, contentDescription = badge, tint = CiviQBrandYellow)
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(badge, style = MaterialTheme.typography.bodySmall, color = Color.White)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ProfileHistoryRow(item: ProfileHistoryItem) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
            Column {
                Text(item.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(item.detail, style = MaterialTheme.typography.bodySmall, color = CiviQTextLow)
            }
            Text(item.timestamp, style = MaterialTheme.typography.labelMedium, color = CiviQBrandTeal)
        }
    }
}

@Composable
fun CiviQAchievementsScreen(state: AchievementsUiState, modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize()) {
        AnimatedCiviQBackground(startColor = CiviQBrandYellow.copy(alpha = 0.35f), endColor = CiviQBrandBlue)
        Column(modifier = Modifier.padding(24.dp), verticalArrangement = Arrangement.spacedBy(18.dp)) {
            Text("Achievements", style = MaterialTheme.typography.displayMedium, color = Color.White)
            state.achievements.forEach { achievement ->
                AchievementRow(item = achievement)
            }
        }
    }
}

@Composable
private fun AchievementRow(item: AchievementItem) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = if (item.unlocked) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant),
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = if (item.unlocked) Icons.Default.EmojiEvents else Icons.Default.Shield,
                contentDescription = item.title,
                tint = if (item.unlocked) CiviQBrandYellow else CiviQTextLow,
                modifier = Modifier.size(30.dp),
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(item.title, style = MaterialTheme.typography.titleMedium, color = Color.White)
                Text(item.description, style = MaterialTheme.typography.bodySmall, color = CiviQTextLow)
            }
            Text(if (item.unlocked) "Unlocked" else "Locked", style = MaterialTheme.typography.labelMedium, color = if (item.unlocked) CiviQSuccess else CiviQTextLow)
        }
    }
}

@Composable
fun CiviQEmptyState(
    title: String,
    message: String,
    icon: ImageVector,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier.fillMaxSize().background(CiviQBackgroundDark)) {
        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .padding(24.dp)
                .clip(RoundedCornerShape(32.dp))
                .background(MaterialTheme.colorScheme.surface)
                .padding(32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Icon(icon, contentDescription = null, tint = CiviQBrandBlue, modifier = Modifier.size(64.dp))
            Text(title, style = MaterialTheme.typography.headlineSmall, color = Color.White, textAlign = TextAlign.Center)
            Text(message, style = MaterialTheme.typography.bodyLarge, color = CiviQTextLow, textAlign = TextAlign.Center)
            if (actionLabel != null && onAction != null) {
                Button(onClick = onAction, shape = RoundedCornerShape(20.dp)) {
                    Text(actionLabel)
                }
            }
        }
    }
}

@Composable
fun CiviQLoadingState(modifier: Modifier = Modifier) {
    Box(modifier = modifier.fillMaxSize().background(CiviQBackgroundDark)) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            ShimmerPlaceholder(width = 260.dp, height = 22.dp)
            ShimmerPlaceholder(width = 320.dp, height = 14.dp)
            Spacer(modifier = Modifier.height(12.dp))
            ShimmerPlaceholder(width = 320.dp, height = 220.dp, shape = RoundedCornerShape(32.dp))
        }
    }
}

@Composable
private fun ShimmerPlaceholder(width: Dp, height: Dp, shape: RoundedCornerShape = RoundedCornerShape(18.dp)) {
    val alphaAnim = rememberInfiniteTransition().animateFloat(
        initialValue = 0.4f,
        targetValue = 0.9f,
        animationSpec = infiniteRepeatable(animation = keyframes {
            durationMillis = 1400
            0.4f at 0
            0.9f at 700
            0.4f at 1400
        }),
    )
    Box(
        modifier = Modifier
            .size(width = width, height = height)
            .clip(shape)
            .background(Color.White.copy(alpha = alphaAnim.value * 0.12f)),
    )
}

@Composable
fun CiviQErrorState(modifier: Modifier = Modifier, onRetry: () -> Unit) {
    Box(modifier = modifier.fillMaxSize().background(CiviQBackgroundDark)) {
        Column(
            modifier = Modifier.align(Alignment.Center),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp),
        ) {
            Text("The politicians lost the data.", style = MaterialTheme.typography.headlineSmall, color = Color.White, textAlign = TextAlign.Center)
            Text("We're looking for it. Tap retry and we'll search the archives.", style = MaterialTheme.typography.bodyLarge, color = CiviQTextLow, textAlign = TextAlign.Center)
            Button(onClick = onRetry, shape = RoundedCornerShape(20.dp)) {
                Text("Retry")
            }
        }
    }
}

@Composable
private fun AnimatedCiviQBackground(
    startColor: Color = CiviQBrandBlue.copy(alpha = 0.95f),
    endColor: Color = CiviQBackgroundDark,
    modifier: Modifier = Modifier,
) {
    val infiniteTransition = rememberInfiniteTransition()
    val offsetX by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = keyframes {
            durationMillis = 6000
            1f at 6000
        }),
    )
    Box(modifier = modifier.fillMaxSize().background(
        brush = Brush.verticalGradient(
            colors = listOf(startColor, endColor),
            startY = offsetX * 1000,
            endY = offsetX * 2000,
        ),
    )) {
        FloatingAccentParticles(count = 18, alpha = 0.18f)
    }
}

@Composable
private fun FloatingAccentParticles(count: Int, alpha: Float = 0.3f) {
    val randomOffsets = remember { List(count) { Offset((20..340).random().toFloat(), (20..780).random().toFloat()) } }
    val transition = rememberInfiniteTransition()
    val movement by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(animation = keyframes {
            durationMillis = 10000
            1f at 10000
        }),
    )

    Canvas(modifier = Modifier.fillMaxSize()) {
        randomOffsets.forEachIndexed { index, offset ->
            drawCircle(
                color = CiviQBrandYellow.copy(alpha = alpha * 0.6f),
                radius = 10f + (index % 3) * 3f,
                center = Offset(offset.x + movement * 10f, (offset.y + movement * 8f) % size.height),
            )
        }
    }
}

@Composable
private fun MascotCoach(message: String, modifier: Modifier = Modifier) {
    val hover = rememberInfiniteTransition().animateFloat(
        initialValue = 0f,
        targetValue = 8f,
        animationSpec = infiniteRepeatable(animation = keyframes {
            durationMillis = 1200
            8f at 600
        }),
    )
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(30.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.9f))
            .padding(16.dp)
            .offset(y = hover.value.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(14.dp),
    ) {
        Box(
            modifier = Modifier
                .size(52.dp)
                .clip(CircleShape)
                .background(CiviQBrandTeal),
            contentAlignment = Alignment.Center,
        ) {
            Icon(Icons.Default.TipsAndUpdates, contentDescription = "Mascot", tint = Color.White)
        }
        Column {
            Text("CiviQ Coach", style = MaterialTheme.typography.labelLarge, color = CiviQBrandYellow)
            Text(message, style = MaterialTheme.typography.bodySmall, color = CiviQTextLow)
        }
    }
}

@Composable
fun CiviQBadgeUnlockAnimation(badgeTitle: String) {
    val scale = rememberInfiniteTransition().animateFloat(
        initialValue = 0.9f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(animation = keyframes {
            durationMillis = 900
            1.05f at 450
        }),
    )
    Card(
        modifier = Modifier
            .size(200.dp)
            .graphicsLayer { scaleX = scale.value; scaleY = scale.value },
        shape = RoundedCornerShape(32.dp),
        colors = CardDefaults.cardColors(containerColor = CiviQBrandYellow),
        elevation = CardDefaults.cardElevation(defaultElevation = 20.dp),
    ) {
        Column(modifier = Modifier.fillMaxSize(), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
            Icon(Icons.Default.EmojiEvents, contentDescription = "Badge", tint = Color.White, modifier = Modifier.size(48.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(badgeTitle, style = MaterialTheme.typography.titleLarge, color = Color.White, textAlign = TextAlign.Center)
        }
    }
}

@Suppress("UnusedPrivateMember")
@Composable
private fun PreviewCiviQScreens() {
    Surface(color = CiviQBackgroundDark) {
        CiviQHomeScreen(state = HomeUiState(), onStartDailyChallenge = {}, onContinueLearning = {})
    }
}
