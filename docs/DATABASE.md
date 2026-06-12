# CiviQ — Firestore Database Schema

This document is the source of truth for CiviQ's Cloud Firestore data model. It
is generated from, and must be kept in sync with, the DTOs in
`app/src/main/java/com/civiq/app/data/remote/dto/firestore/` and the
collection/field name constants in
`app/src/main/java/com/civiq/app/utils/Constants.kt`
(`FirestoreCollections`, `FirestoreFields`).

## Conventions

- **Database mode:** Cloud Firestore, Native mode.
- **Timestamps:** every `*At` / `*Date` field is a `Long` Unix epoch
  **millisecond** value (`System.currentTimeMillis()`), **not** a Firestore
  `Timestamp`. `lastActiveDate` and `daily_challenges` doc IDs are normalized
  to the start of day in the user's local timezone (see
  `utils/DateTimeExtensions.kt`'s `startOfDay()`).
- **Enums:** stored as their Kotlin enum `name` (UPPER_SNAKE_CASE strings),
  e.g. `"PREMIUM"`, `"MULTIPLE_CHOICE"`. See [Enum reference](#enum-reference)
  for the full set of values per field.
- **Document IDs:** fields annotated `@DocumentId` are populated by the
  Firestore SDK from the document's path and are not stored as a duplicate
  field value.
- **Money/rewards:** `xp` and `coins` (and their `*Reward`/`*Earned`
  variants) are `Long`. `level`, `streakCount`, `criteriaValue`, etc. are
  `Int`.

## Role-based access control (RBAC)

Every user document has a `role` field backed by `UserRole`:

| Role | Description |
| --- | --- |
| `GUEST` | Anonymous Firebase Auth session. Read-only access to public content, limited quiz attempts, no persistence of gamification progress beyond the device. |
| `REGISTERED` | Signed-up user (email/password or Google). Full gamification progress, subject to `FREE_TIER_DAILY_QUIZ_LIMIT`. |
| `PREMIUM` | Active subscription (see `subscriptions`). Unlimited quizzes, AI Coach, exclusive challenges. `User.isPremium` is also cached as a denormalized boolean for fast client checks. |
| `ADMIN` | Staff/moderator. Full read/write access to content collections via the Admin Dashboard (`presentation/admin/*`). |

`User.isAdmin` / `User.isPremium` (domain model computed properties) gate
navigation to `presentation/admin/*` and Premium-only features respectively.
Security must **not** rely on these client-side checks alone — see
[firestore.rules](../firestore.rules).

## Collections at a glance

| Path | Document ID | DTO | Purpose |
| --- | --- | --- | --- |
| `users/{uid}` | Firebase Auth UID | `UserDto` | Profile, role, gamification stats |
| `users/{uid}/user_achievements/{achievementId}` | achievement ID | `UserAchievementDto` | Achievements this user has unlocked |
| `users/{uid}/devices/{token}` | FCM registration token | _(plain map)_ | Registered push-notification devices |
| `users/{uid}/daily_challenge_progress/{date}` | `yyyy-MM-dd` | `DailyChallengeProgressDto` | Per-day daily-challenge completion |
| `questions/{questionId}` | auto-ID | `QuestionDto` | Quiz question bank |
| `quiz_attempts/{attemptId}` | auto-ID | `QuizAttemptDto` | Completed/in-progress quiz sessions |
| `daily_challenges/{date}` | `yyyy-MM-dd` | `DailyChallengeDto` | The featured quiz for a given day |
| `achievements/{achievementId}` | auto-ID | `AchievementDto` | Achievement/badge definitions |
| `leaderboards/{period}_{scope}[_{countryCode}]` | computed, see below | _(none)_ | Leaderboard "bucket" parent docs |
| `leaderboards/{...}/entries/{userId}` | user UID | `LeaderboardEntryDto` | Ranked entries within a bucket |
| `notifications/{notificationId}` | auto-ID | `AppNotificationDto` | In-app notification feed |
| `subscriptions/{subscriptionId}` | auto-ID | `SubscriptionDto` | Premium subscription records |
| `feature_flags/{key}` | flag key | `FeatureFlagDto` | Remote feature toggles |
| `reports/{reportId}` | auto-ID | _(reserved)_ | Content moderation reports (not yet implemented) |

---

## `users/{uid}`

Document ID is the Firebase Auth UID. Created on first sign-in by
`AuthRepositoryImpl`.

| Field | Type | Default | Notes |
| --- | --- | --- | --- |
| `id` | `string` (`@DocumentId`) | — | == Firebase Auth UID |
| `email` | `string` | `""` | |
| `displayName` | `string` | `""` | |
| `photoUrl` | `string?` | `null` | |
| `role` | `string` (`UserRole`) | `"REGISTERED"` | `GUEST`, `REGISTERED`, `PREMIUM`, `ADMIN`. Only mutable by Admins (`AdminRepositoryImpl.updateUserRole`) or a Cloud Function reacting to subscription changes. |
| `xp` | `number` (`Long`) | `0` | Total lifetime XP. |
| `coins` | `number` (`Long`) | `0` | Spendable in-app currency. |
| `level` | `number` (`Int`) | `1` | Derived from `xp` via `LevelThresholds`, cached for display. |
| `streakCount` | `number` (`Int`) | `0` | Current consecutive-day activity streak. |
| `longestStreak` | `number` (`Int`) | `0` | High-water mark of `streakCount`. |
| `lastActiveDate` | `number` (`Long`, epoch ms, start-of-day) | `0` | Used to decide whether a new day's activity continues or resets the streak. |
| `countryCode` | `string?` | `null` | ISO 3166-1 alpha-2. Drives `COUNTRY`-scoped leaderboards and localized AI-generated content. |
| `createdAt` | `number` (`Long`, epoch ms) | `0` | Account creation timestamp. Indexed for `AdminUsersScreen` (newest first). |
| `isPremium` | `boolean` | `false` | Denormalized from the user's active `subscriptions` doc. |
| `premiumExpiresAt` | `number` (`Long`)? | `null` | Denormalized expiry of the active subscription, or `null` if non-expiring/inactive. |
| `fcmTokens` | `array<string>` | `[]` | Legacy/simple multi-token list. The canonical per-device record is the `devices` subcollection below; new code should prefer that. |

### `users/{uid}/user_achievements/{achievementId}`

One document per achievement the user has unlocked. Absence of a document
means the achievement is locked.

| Field | Type | Default | Notes |
| --- | --- | --- | --- |
| `achievementId` | `string` (`@DocumentId`) | — | == `achievements/{achievementId}` document ID. |
| `unlockedAt` | `number` (`Long`, epoch ms) | `0` | When the unlock criteria were met. |

### `users/{uid}/devices/{token}`

Plain field map (no DTO) written by `NotificationRepositoryImpl`. Document ID
is the FCM registration token itself, so re-registering the same token is
idempotent and uninstalling/token-rotation can be cleaned up by deleting the
old token's document.

| Field | Type | Notes |
| --- | --- | --- |
| `token` | `string` | Duplicate of the document ID, kept for query convenience. |
| `registeredAt` | `number` (`Long`, epoch ms) | When this device last (re-)registered. |

### `users/{uid}/daily_challenge_progress/{date}`

Document ID is `yyyy-MM-dd`, matching the corresponding `daily_challenges`
document ID.

| Field | Type | Default | Notes |
| --- | --- | --- | --- |
| `challengeId` | `string` (`@DocumentId`) | — | == `daily_challenges/{date}` document ID (the date string). |
| `userId` | `string` | `""` | Redundant with the parent path; kept for collection-group queries. |
| `isCompleted` | `boolean` | `false` | |
| `attemptId` | `string?` | `null` | Points to the `quiz_attempts` document for this challenge, once completed. |
| `completedAt` | `number` (`Long`)? | `null` | |

---

## `questions/{questionId}`

The curated + AI-generated question bank. Document ID is an auto-generated
Firestore ID.

| Field | Type | Default | Notes |
| --- | --- | --- | --- |
| `id` | `string` (`@DocumentId`) | — | |
| `type` | `string` (`QuestionType`) | `"MULTIPLE_CHOICE"` | `MULTIPLE_CHOICE`, `TRUE_FALSE`, `SCENARIO`. |
| `category` | `string` (`QuizCategory`) | `"DEMOCRACY"` | One of 8 values — see [Enum reference](#enum-reference). |
| `difficulty` | `string` (`QuestionDifficulty`) | `"BEGINNER"` | `BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `EXPERT`. |
| `questionText` | `string` | `""` | |
| `options` | `array<string>` | `[]` | 2 entries for `TRUE_FALSE`, typically 4 for `MULTIPLE_CHOICE`/`SCENARIO`. |
| `correctAnswerIndex` | `number` (`Int`) | `0` | Index into `options`. |
| `explanation` | `string` | `""` | Shown after answering, regardless of correctness. |
| `tone` | `string` (`QuestionTone`) | `"EDUCATIONAL"` | `EDUCATIONAL`, `FUNNY`, `SATIRICAL`. |
| `countryCode` | `string?` | `null` | ISO 3166-1 alpha-2. `null` = applies globally. |
| `tags` | `array<string>` | `[]` | Free-form labels for search/filtering. |
| `source` | `string` (`QuestionSource`) | `"CURATED"` | `CURATED`, `AI_GENERATED`, `COMMUNITY`. |
| `createdAt` | `number` (`Long`, epoch ms) | `0` | |
| `createdBy` | `string?` | `null` | UID of the admin/user who created it, or `null` for seed content. |

---

## `quiz_attempts/{attemptId}`

One document per quiz session (standard quiz or daily challenge). Document ID
is an auto-generated Firestore ID.

| Field | Type | Default | Notes |
| --- | --- | --- | --- |
| `id` | `string` (`@DocumentId`) | — | |
| `userId` | `string` | `""` | Owner. Indexed for `observeQuizHistory` / `getRemainingFreeAttemptsToday`. |
| `category` | `string` (`QuizCategory`) | `"DEMOCRACY"` | |
| `difficulty` | `string` (`QuestionDifficulty`) | `"BEGINNER"` | |
| `questionIds` | `array<string>` | `[]` | Ordered list of `questions` document IDs presented. |
| `answers` | `array<QuestionAnswerDto>` | `[]` | Embedded, **not** a subcollection. See below. |
| `score` | `number` (`Int`) | `0` | Count of correct answers. |
| `totalQuestions` | `number` (`Int`) | `0` | |
| `xpEarned` | `number` (`Long`) | `0` | |
| `coinsEarned` | `number` (`Long`) | `0` | |
| `isDailyChallenge` | `boolean` | `false` | |
| `challengeId` | `string?` | `null` | Set to the `daily_challenges` document ID (`yyyy-MM-dd`) when `isDailyChallenge` is true. |
| `startedAt` | `number` (`Long`, epoch ms) | `0` | Used by `getRemainingFreeAttemptsToday` to count today's attempts. |
| `completedAt` | `number` (`Long`, epoch ms) | `0` | Used to order `observeQuizHistory` (descending). |

### Embedded: `QuestionAnswerDto`

Embedded within `quiz_attempts.answers[]` — never a separate collection.

| Field | Type | Default | Notes |
| --- | --- | --- | --- |
| `questionId` | `string` | `""` | |
| `selectedIndex` | `number` (`Int`) | `-1` | `-1` means unanswered (e.g. session abandoned). |
| `isCorrect` | `boolean` | `false` | |
| `timeTakenMs` | `number` (`Long`) | `0` | |

---

## `daily_challenges/{date}`

Document ID is the challenge date as `yyyy-MM-dd` (local timezone), e.g.
`2026-06-12`. Created/edited via the Admin Dashboard
(`AdminChallengesScreen` → `CreateOrUpdateDailyChallengeUseCase`), which
auto-selects `questionIds` for the chosen `category`/`difficulty`.

| Field | Type | Default | Notes |
| --- | --- | --- | --- |
| `id` | `string` (`@DocumentId`) | — | == the `yyyy-MM-dd` document ID. |
| `date` | `string` | `""` | Duplicate of the document ID, kept for query/display convenience. |
| `title` | `string` | `""` | |
| `description` | `string` | `""` | |
| `category` | `string` (`QuizCategory`) | `"CIVIC_RESPONSIBILITY"` | |
| `difficulty` | `string` (`QuestionDifficulty`) | `"INTERMEDIATE"` | |
| `questionIds` | `array<string>` | `[]` | `DAILY_CHALLENGE_QUESTION_COUNT` (5) questions, drawn from `questions` matching `category`/`difficulty`. |
| `xpReward` | `number` (`Long`) | `0` | Bonus XP for completing, on top of per-question rewards. |
| `coinReward` | `number` (`Long`) | `0` | |
| `createdAt` | `number` (`Long`, epoch ms) | `0` | |

---

## `achievements/{achievementId}`

Achievement/badge **definitions**. Per-user unlock state lives in
`users/{uid}/user_achievements/{achievementId}`. Managed via
`AdminAchievementsScreen`.

| Field | Type | Default | Notes |
| --- | --- | --- | --- |
| `id` | `string` (`@DocumentId`) | — | |
| `title` | `string` | `""` | |
| `description` | `string` | `""` | |
| `iconName` | `string` | `"EmojiEvents"` | One of the 13 names supported by `achievementIcon()` (`presentation/components/AchievementIcon.kt`): `EmojiEvents`, `Star`, `LocalFireDepartment`, `School`, `WorkspacePremium`, `Bolt`, `Public`, `MenuBook`, `Diversity3`, `Verified`, `Gavel`, `HowToVote`, `AutoAwesome`. Unrecognized names fall back to `EmojiEvents`. |
| `category` | `string` (`AchievementCategory`) | `"MILESTONE"` | `MILESTONE`, `STREAK`, `CATEGORY_MASTERY`, `SOCIAL`, `SPECIAL`. |
| `criteriaType` | `string` (`AchievementCriteriaType`) | `"QUIZZES_COMPLETED"` | See [Enum reference](#enum-reference). |
| `criteriaValue` | `number` (`Int`) | `0` | Threshold the `criteriaType` metric must reach to unlock. |
| `xpReward` | `number` (`Long`) | `0` | Awarded once, on unlock. |
| `coinReward` | `number` (`Long`) | `0` | Awarded once, on unlock. |

---

## `leaderboards/{period}_{scope}[_{countryCode}]`

Leaderboard "bucket" documents act purely as parents for the `entries`
subcollection — the bucket document itself currently has no required fields
(it may not even need to exist; Firestore allows subcollections under
non-existent parent documents). The bucket ID is computed by
`LeaderboardRepositoryImpl.leaderboardId()`:

- `GLOBAL` / `FRIENDS` scope: `"{period}_{scope}"`, e.g. `"WEEKLY_GLOBAL"`,
  `"ALL_TIME_FRIENDS"`.
- `COUNTRY` scope: `"{period}_{scope}_{countryCode}"`, e.g.
  `"DAILY_COUNTRY_NG"`.

Recomputing rankings (aggregating `users.xp` into ranked `entries`) is
expected to be done by a scheduled Cloud Function, not the client.

### `leaderboards/{bucketId}/entries/{userId}`

Document ID is the ranked user's UID.

| Field | Type | Default | Notes |
| --- | --- | --- | --- |
| `userId` | `string` (`@DocumentId`) | — | |
| `displayName` | `string` | `""` | Denormalized snapshot at last recompute. |
| `photoUrl` | `string?` | `null` | |
| `xp` | `number` (`Long`) | `0` | XP for this leaderboard's `period` window (not necessarily lifetime `xp`). |
| `level` | `number` (`Int`) | `1` | |
| `rank` | `number` (`Int`) | `0` | 1-based rank within the bucket. Queried with `orderBy("rank")`. |
| `countryCode` | `string?` | `null` | |

---

## `notifications/{notificationId}`

The in-app notification feed (`NotificationsScreen`). Documents are written by
Cloud Functions / server-side logic in response to events (daily reminders,
streak warnings, achievement unlocks, etc.); the client only reads, marks
read, and deep-links.

| Field | Type | Default | Notes |
| --- | --- | --- | --- |
| `id` | `string` (`@DocumentId`) | — | |
| `userId` | `string` | `""` | Recipient. Indexed for `observeNotifications`. |
| `type` | `string` (`NotificationType`) | `"SYSTEM"` | `DAILY_QUIZ`, `STREAK_REMINDER`, `ACHIEVEMENT_UNLOCKED`, `WEEKLY_CHALLENGE`, `SYSTEM`. |
| `title` | `string` | `""` | |
| `body` | `string` | `""` | |
| `isRead` | `boolean` | `false` | |
| `deepLinkRoute` | `string?` | `null` | A `Screen.route`-shaped string (see `navigation/Screen.kt`); `NotificationsScreen` navigates here on tap. |
| `createdAt` | `number` (`Long`, epoch ms) | `0` | Indexed (descending) for `observeNotifications`. |

---

## `subscriptions/{subscriptionId}`

Premium subscription records, one per purchase/renewal cycle. `users/{uid}.isPremium`
and `premiumExpiresAt` are kept in sync with the active subscription by a
Cloud Function.

| Field | Type | Default | Notes |
| --- | --- | --- | --- |
| `id` | `string` (`@DocumentId`) | — | |
| `userId` | `string` | `""` | |
| `tier` | `string` (`SubscriptionTier`) | `"FREE"` | `FREE`, `PREMIUM_MONTHLY`, `PREMIUM_YEARLY`. |
| `provider` | `string?` (`PaymentProvider`) | `null` | `GOOGLE_PLAY`, `STRIPE`, `PAYSTACK`. |
| `startedAt` | `number` (`Long`, epoch ms) | `0` | |
| `expiresAt` | `number` (`Long`)? | `null` | `null` = does not expire (or not yet known). |
| `autoRenew` | `boolean` | `false` | |
| `status` | `string` (`SubscriptionStatus`) | `"ACTIVE"` | `ACTIVE`, `EXPIRED`, `CANCELED`, `GRACE_PERIOD`. `isActive` (domain) is true for `ACTIVE` or `GRACE_PERIOD`. |

---

## `feature_flags/{key}`

Remote feature toggles, managed via `AdminFeatureFlagsScreen`. Document ID is
the flag's key (kebab/camel/snake-case identifier chosen by Admins).

| Field | Type | Default | Notes |
| --- | --- | --- | --- |
| `key` | `string` (`@DocumentId`) | — | |
| `isEnabled` | `boolean` | `false` | |
| `description` | `string` | `""` | Shown to Admins in `AdminFeatureFlagsScreen`. |
| `requiresPremium` | `boolean` | `false` | When true, the flag is only honored for users where `User.isPremium == true`, even if `isEnabled` is true. |

---

## `reports/{reportId}` (reserved)

The `FirestoreCollections.REPORTS` constant is reserved for a future
content-moderation feature (e.g. users flagging a `questions` document as
inaccurate or offensive for Admin review). No DTO, repository, or UI exists
yet. When implemented, the expected shape is:

| Field | Type | Notes |
| --- | --- | --- |
| `id` | `string` (`@DocumentId`) | |
| `reportedBy` | `string` | UID of the reporting user. |
| `targetCollection` | `string` | e.g. `"questions"`. |
| `targetId` | `string` | Document ID within `targetCollection`. |
| `reason` | `string` | |
| `status` | `string` | e.g. `OPEN`, `RESOLVED`, `DISMISSED`. |
| `createdAt` | `number` (`Long`, epoch ms) | |

---

## Enum reference

| Enum | Values |
| --- | --- |
| `UserRole` | `GUEST`, `REGISTERED`, `PREMIUM`, `ADMIN` |
| `QuizCategory` | `DEMOCRACY`, `ELECTIONS`, `GOVERNANCE`, `CONSTITUTIONS`, `PUBLIC_POLICY`, `POLITICAL_HISTORY`, `INTERNATIONAL_RELATIONS`, `CIVIC_RESPONSIBILITY` |
| `QuestionDifficulty` | `BEGINNER`, `INTERMEDIATE`, `ADVANCED`, `EXPERT` |
| `QuestionType` | `MULTIPLE_CHOICE`, `TRUE_FALSE`, `SCENARIO` |
| `QuestionTone` | `EDUCATIONAL`, `FUNNY`, `SATIRICAL` |
| `QuestionSource` | `CURATED`, `AI_GENERATED`, `COMMUNITY` |
| `AchievementCategory` | `MILESTONE`, `STREAK`, `CATEGORY_MASTERY`, `SOCIAL`, `SPECIAL` |
| `AchievementCriteriaType` | `QUIZZES_COMPLETED`, `PERFECT_SCORES`, `STREAK_DAYS`, `XP_EARNED`, `CATEGORY_QUESTIONS_ANSWERED`, `DAILY_CHALLENGES_COMPLETED` |
| `LeaderboardPeriod` | `DAILY`, `WEEKLY`, `MONTHLY`, `ALL_TIME` |
| `LeaderboardScope` | `GLOBAL`, `COUNTRY`, `FRIENDS` |
| `NotificationType` | `DAILY_QUIZ`, `STREAK_REMINDER`, `ACHIEVEMENT_UNLOCKED`, `WEEKLY_CHALLENGE`, `SYSTEM` |
| `SubscriptionTier` | `FREE`, `PREMIUM_MONTHLY`, `PREMIUM_YEARLY` |
| `PaymentProvider` | `GOOGLE_PLAY`, `STRIPE`, `PAYSTACK` |
| `SubscriptionStatus` | `ACTIVE`, `EXPIRED`, `CANCELED`, `GRACE_PERIOD` |

---

## Indexes

Most CiviQ queries are served by Firestore's automatic single-field indexes,
including compound **equality-only** filters (e.g.
`questions.where(category == X).where(difficulty == Y)` and
`notifications.where(userId == X).where(isRead == false)`).

Composite indexes are required wherever a query combines an equality filter
with a range filter or `orderBy` on a **different** field. These are
pre-declared in [`firestore.indexes.json`](../firestore.indexes.json):

| Collection | Fields | Used by |
| --- | --- | --- |
| `quiz_attempts` | `userId` ASC, `completedAt` DESC | `QuizRepositoryImpl.observeQuizHistory` |
| `quiz_attempts` | `userId` ASC, `startedAt` ASC | `QuizRepositoryImpl.getRemainingFreeAttemptsToday` |
| `notifications` | `userId` ASC, `createdAt` DESC | `NotificationRepositoryImpl.observeNotifications` |

Deploy with `firebase deploy --only firestore:indexes`. If a future query
triggers a `FAILED_PRECONDITION: The query requires an index` error, Firestore
provides a direct console link to create it — add the resulting entry to
`firestore.indexes.json` so it's reproducible.

## Security rules

See [`firestore.rules`](../firestore.rules) for the deployable rule set. In
summary:

- **`users/{uid}`**: owners can read/write their own profile, but cannot
  self-modify `role`, `xp`, `coins`, `level`, `isPremium`, or
  `premiumExpiresAt` (those are Admin- or Cloud-Function-only). Admins have
  full read/write.
- **`questions`, `daily_challenges`, `achievements`**: readable by any signed-in
  user (including `GUEST`), writable only by `ADMIN`.
- **`quiz_attempts`**: a user may create/read their own attempts; only Admins
  can update/delete (e.g. for moderation).
- **`leaderboards/**`**: readable by any signed-in user; written only by
  Admins/Cloud Functions.
- **`notifications`**: a user can read and mark-as-read only their own
  documents; creation/deletion is Admin/Cloud-Function-only.
- **`subscriptions`**: a user can read their own records; all writes are
  Admin/Cloud-Function-only (driven by payment provider webhooks).
- **`feature_flags`**: publicly readable (even unauthenticated), writable only
  by Admins.
- **`reports`**: any signed-in user may create a report for themselves;
  only Admins can read/update/delete.

Deploy with `firebase deploy --only firestore:rules`.
