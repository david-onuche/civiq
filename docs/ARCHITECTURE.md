# CiviQ — App Architecture

CiviQ is a single-module Android app built with **Kotlin**, **Jetpack
Compose**, and **Material 3**, following **Clean Architecture** with an
**MVVM** presentation layer and **Hilt** for dependency injection.

This document describes how the codebase is organized and the conventions
used throughout. For the Firestore data model, see
[DATABASE.md](DATABASE.md).

## Layers

```
app/src/main/java/com/civiq/app/
├── data/            # Implementation details: Firestore, Retrofit, DataStore
│   ├── local/datastore/   # UserPreferencesDataSource (DataStore<Preferences>)
│   ├── mapper/             # DTO <-> domain model mappers (toDomain()/toDto())
│   ├── remote/
│   │   ├── ai/             # AiPromptTemplates (Gemini/OpenAI prompt builders)
│   │   ├── api/             # Retrofit service interfaces (Gemini, OpenAI)
│   │   └── dto/
│   │       ├── ai/          # Request/response DTOs for AI providers
│   │       └── firestore/    # @DocumentId-annotated Firestore document DTOs
│   └── repository/         # *RepositoryImpl - implements domain/repository interfaces
├── domain/           # Pure Kotlin, no Android/Firebase imports
│   ├── model/        # Domain models + enums (User, Question, Achievement, ...)
│   ├── repository/   # Repository interfaces (contracts)
│   └── usecase/      # One class per use case, grouped into *UseCases bundles
│       ├── admin/ aicoach/ auth/ challenge/ gamification/
│       └── home/ notification/ premium/ quiz/
├── presentation/     # Jetpack Compose UI + ViewModels, one package per feature
│   ├── achievements/ admin/ aicoach/ auth/ challenges/
│   ├── components/   # Shared composables (CiviQButton, CiviQTextField, StateViews, ...)
│   ├── home/ leaderboard/ notifications/ premium/ profile/ quiz/
│   └── theme/         # Material3 theme + ExtendedColorScheme
├── navigation/        # Screen routes, NavHost graphs, bottom nav
├── di/                 # Hilt modules
├── services/
│   ├── fcm/            # CiviQFirebaseMessagingService
│   └── notifications/  # NotificationScheduler, DailyReminderWorker, BootCompletedReceiver
├── utils/              # Resource, UiText, Constants, extension functions
├── CiviQApplication.kt # @HiltAndroidApp, notification channels, WorkManager config
└── MainActivity.kt     # Single-activity host for Compose Navigation
```

### `domain/` — business rules

Pure Kotlin with **no** Android, Firebase, or Compose imports — fully unit
testable on the JVM.

- **`model/`** — immutable data classes and enums (e.g. `User`, `UserRole`,
  `Question`, `QuizCategory`, `Achievement`, `DailyChallenge`,
  `LeaderboardEntry`, `Subscription`, `FeatureFlag`). Domain models are the
  types ViewModels and Compose UI work with — never the Firestore DTOs
  directly.
- **`repository/`** — interfaces only (`AuthRepository`, `QuizRepository`,
  `GamificationRepository`, `AiQuizRepository`, `AiCoachRepository`,
  `NotificationRepository`, `SubscriptionRepository`, `FeatureFlagRepository`,
  `LeaderboardRepository`, `DailyChallengeRepository`,
  `PreferencesRepository`, `UserRepository`). Implementations live in
  `data/repository/` and are bound in `di/RepositoryModule.kt`.
- **`usecase/`** — one class per use case (single `operator fun invoke(...)`),
  grouped by feature into a bundle data class injected as a single
  dependency, e.g.:

  ```kotlin
  data class QuizUseCases @Inject constructor(
      val getQuizQuestions: GetQuizQuestionsUseCase,
      val getQuestionsByIds: GetQuestionsByIdsUseCase,
      val completeQuiz: CompleteQuizUseCase,
      val observeQuizHistory: ObserveQuizHistoryUseCase,
      val getQuizAttempt: GetQuizAttemptUseCase,
      val getRemainingFreeAttempts: GetRemainingFreeAttemptsUseCase,
  )
  ```

  ViewModels inject one bundle (or a few) instead of half a dozen individual
  use cases. Bundles: `AuthUseCases`, `HomeUseCases`, `QuizUseCases`,
  `GamificationUseCases`, `ChallengeUseCases`, `NotificationUseCases`,
  `PremiumUseCases`, `AiCoachUseCases`, `AdminUseCases`.

### `data/` — implementation details

- **`remote/dto/firestore/`** — one `@DocumentId`-annotated data class per
  Firestore document shape (`UserDto`, `QuestionDto`, `QuizAttemptDto`, ...).
  Field names and defaults mirror [DATABASE.md](DATABASE.md) exactly.
- **`remote/dto/ai/` + `remote/api/`** — Retrofit `@Serializable` request/
  response models and service interfaces for the Gemini and OpenAI REST APIs.
- **`remote/ai/AiPromptTemplates.kt`** — builds provider-agnostic prompts for
  AI quiz generation and the AI Coach from a `AiQuestionRequest` (category,
  difficulty, tone, country code, count).
- **`mapper/`** — pure `DTO.toDomain()` / `DomainModel.toDto()` extension
  functions. This is the **only** place DTOs and domain models interact.
- **`repository/`** — `*RepositoryImpl` classes. Firestore-backed
  repositories use `callbackFlow` + `addSnapshotListener` for `observe*()`
  functions (real-time updates) and suspend functions + `.await()` for
  one-shot reads/writes. Every public function returns `Resource<T>` (or
  `Flow<Resource<T>>`), catching exceptions and mapping them to
  `Resource.Error(UiText...)`.
- **`local/datastore/UserPreferencesDataSource.kt`** — Jetpack DataStore
  wrapper for local-only settings (dark mode, onboarding flag, last-synced
  FCM token, etc. — see `DataStoreKeys` in `utils/Constants.kt`).

### `presentation/` — MVVM + Compose

Each feature package (`home/`, `quiz/`, `admin/`, ...) follows the same
shape:

- `XyzScreen.kt` — `@Composable fun XyzScreen(..., viewModel: XyzViewModel = hiltViewModel())`.
- `XyzViewModel.kt` — `@HiltViewModel class XyzViewModel @Inject constructor(...)`.
- `XyzUiState.kt` — immutable data class consumed via
  `viewModel.uiState.collectAsStateWithLifecycle()`.

ViewModels compose multiple `Flow`s with `combine()` /
`flatMapLatest()` and expose a single `StateFlow<XyzUiState>` via:

```kotlin
val uiState: StateFlow<XyzUiState> = combine(...) { ... }
    .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), XyzUiState())
```

**Shared building blocks** (`presentation/components/`):

- `Resource<T>` consumers use `ResourceContent(resource, onRetry, onLoading,
  onError, onSuccess)` to render `LoadingState` / `ErrorState` /
  `OfflineState` / the success content uniformly.
- `EmptyState(title, subtitle, icon)` for empty lists.
- `CiviQTopAppBar`, `CiviQButton`, `CiviQTextField` / `CiviQPasswordField`.
- Errors surface via `SnackbarHostState` + a `LaunchedEffect(uiState.errorMessage)`
  that calls `message.asString(context)` and then `viewModel.dismissError()`.

**Theming** (`presentation/theme/`): standard Material 3
`lightColorScheme`/`darkColorScheme` plus a custom `ExtendedColorScheme`
(accessed via `MaterialTheme.extendedColors`) for gamification-specific
colors not in the Material palette — `xp`/`xpContainer`,
`coin`/`coinContainer`, `streak`/`streakContainer`.

### `navigation/`

- `Screen.kt` — sealed class, single source of truth for every route.
  Routes with arguments expose `createRoute(...)` factories and `ARG_*`
  constants for `navArgument(...)`.
- `MainScreen.kt` — the bottom-navigation `NavHost` (Home, Quiz, Challenges,
  Leaderboard, Profile, and all secondary/detail/admin screens reachable from
  them).
- `BottomNavItem.kt` — enum driving the bottom navigation bar.
- Auth/onboarding (`Splash`, `Onboarding`, `Login`, `Register`,
  `ForgotPassword`) and the main graph are switched at the top level based on
  `AuthRepository.currentUser` / onboarding-complete state.

### `di/` — Hilt modules

| Module | Provides |
| --- | --- |
| `AppModule` | App-wide singletons not covered by other modules |
| `RepositoryModule` | Binds every `*Repository` interface to its `*RepositoryImpl` |
| `FirebaseModule` | `FirebaseAuth`, `FirebaseFirestore`, `FirebaseMessaging`, etc. |
| `NetworkModule` | `OkHttpClient`, `Retrofit` instances for Gemini/OpenAI, JSON config |
| `AiModule` | AI provider bindings (Gemini/OpenAI services + prompt templates) |
| `DataStoreModule` | `DataStore<Preferences>` instance |
| `DispatcherModule` | `@IoDispatcher` / `@MainDispatcher` / `@DefaultDispatcher` + `@ApplicationScope` `CoroutineScope` (see `Qualifiers.kt`) |

### `services/`

- **`fcm/CiviQFirebaseMessagingService`** — handles incoming FCM data/
  notification messages and renders them via `CiviQNotificationBuilder` into
  the channels declared in `utils/Constants.kt#NotificationChannels`
  (general, daily quiz, streak, achievement, weekly challenge).
- **`notifications/NotificationScheduler`** + **`DailyReminderWorker`** —
  WorkManager (`HiltWorkerFactory`, configured in `CiviQApplication`)
  schedules a recurring daily-reminder notification.
- **`notifications/BootCompletedReceiver`** — re-schedules the daily reminder
  after device reboot (`RECEIVE_BOOT_COMPLETED`).

### `utils/`

- **`Resource<T>`** — sealed result type used end-to-end:
  `Success<T>(data)`, `Error<T>(message: UiText, data: T? = null)`,
  `Loading<T>(data: T? = null)`.
- **`UiText`** — sealed class for deferring string resolution out of the
  data/domain layers: `DynamicString(value)` and
  `StringResource(@StringRes resId, args = emptyList())`, both with
  `asString()` (Composable) / `asString(context)` overloads.
- **`Constants.kt`** — `FirestoreCollections`, `FirestoreFields`,
  `DataStoreKeys`, `GamificationConfig` (XP/coin tuning, streak milestones,
  free-tier limits), `LevelThresholds`, `NetworkConfig`, `AiConfig`,
  `NotificationChannels`.
- Extension functions: `safeEnumValueOf<T>()` (falls back to a default
  instead of throwing on unknown enum strings — important for
  forward-compatible Firestore documents), `Long.startOfDay()`, etc.

## AI quiz generation

`AiQuizRepositoryImpl` (and `AiCoachRepositoryImpl` for the Premium AI Coach)
abstract over two providers — **Gemini** (`gemini-1.5-flash`) and **OpenAI**
(`gpt-4o-mini`), configured in `utils/Constants.kt#AiConfig`. Prompts are
built by `AiPromptTemplates` from an `AiQuestionRequest` (category,
difficulty, tone, optional country code, question count, capped at
`MAX_QUESTION_COUNT`). Responses are parsed into `QuestionDto`s; on
parse/network failure the repository returns `Resource.Error`, and calling
ViewModels fall back to the curated `questions` collection
(`source = "AI_GENERATED"` vs `"CURATED"` distinguishes the two in Firestore).

## Build configuration

- **Flavors:** `dev` / `prod` (`environment` dimension) — sets
  `BuildConfig.ENVIRONMENT`.
- **Build types:** `debug` (`.debug` app ID suffix), `staging` (debuggable,
  falls back to `debug` matching), `release` (minified, shrunk resources,
  signed via `local.properties`/env-var secrets when present, else falls
  back to debug signing for local builds).
- **Secrets:** `GEMINI_API_KEY`, `OPENAI_API_KEY`, `GOOGLE_WEB_CLIENT_ID`, and
  release-signing credentials are read from `local.properties` (git-ignored;
  see `local.properties.example`) or environment variables, and exposed via
  generated `BuildConfig` fields. See [SETUP.md](SETUP.md).
- **Core library desugaring** is enabled (`minSdk = 26`,
  `compileSdk = 35`/`targetSdk = 35`), which is why `java.time.*` (e.g.
  `LocalDate`) can be used freely throughout the app.

## Testing

- **Unit tests** (`app/src/test/`): JUnit4 + MockK + Turbine (for `Flow`
  assertions) + Google Truth, targeting ViewModels, use cases, and mappers in
  isolation — repositories are mocked via their domain interfaces.
- **Instrumented tests** (`app/src/androidTest/`): Compose UI tests +
  Hilt testing (`HiltTestRunner`, `@HiltAndroidTest`).
