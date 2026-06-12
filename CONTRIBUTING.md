# Contributing to CiviQ

Thanks for your interest in CiviQ! We welcome contributions from developers,
designers, civic educators, political scientists, researchers, and anyone
passionate about civic education — code and non-code contributions alike.

## Code of Conduct

This project follows the [Contributor Covenant](CODE_OF_CONDUCT.md). By
participating, you agree to uphold it.

## Ways to contribute

- **Code**: bug fixes, features, performance, accessibility, tests.
- **Quiz content**: new questions (see [Contributing quiz content](#contributing-quiz-content)
  below) — especially country-specific civics content.
- **Design**: UI/UX improvements, illustrations, icons.
- **Docs**: improvements to the `docs/` guides, README, or code comments.
- **Translations**: localizing `strings.xml` (multi-language support is on
  the roadmap).

## Getting started

1. Read [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) to understand the
   project's Clean Architecture layers and conventions.
2. Follow [docs/SETUP.md](docs/SETUP.md) to get a local build running against
   your own Firebase project.
3. Check open issues for something to work on, or open a new issue to discuss
   your idea before starting significant work.

## Development workflow

1. **Fork** the repository and create a feature branch off `main`:

   ```bash
   git checkout -b feature/short-description
   ```

2. Make your changes, following the conventions below.
3. **Run tests** before opening a PR:

   ```bash
   ./gradlew testDevDebugUnitTest
   ```

4. **Format/lint**: this project uses Kotlin's official code style
   (`kotlin.code.style=official`). Use Android Studio's
   *Code → Reformat Code* before committing.
5. Open a **pull request** against `main` with a clear description of the
   change and, for behavior changes, how you tested it (a screen recording or
   screenshots are appreciated for UI changes).

## Code conventions

- **Architecture**: follow the existing layering — `domain/` stays pure
  Kotlin (no Android/Firebase imports), `data/` implements `domain/repository`
  interfaces and maps DTOs via `data/mapper/`, `presentation/` is MVVM with
  one `ViewModel` + `UiState` per screen. See
  [docs/ARCHITECTURE.md](docs/ARCHITECTURE.md) for the full breakdown.
- **Errors & loading state**: data/domain functions return `Resource<T>` (or
  `Flow<Resource<T>>`); user-facing strings go through `UiText` so they can be
  resolved with localization later. Don't throw exceptions across layer
  boundaries or use raw `String` for user-facing error messages.
- **Strings**: all user-visible text belongs in
  `app/src/main/res/values/strings.xml` — no hardcoded UI strings in
  Composables.
- **Navigation**: add new routes to `navigation/Screen.kt` (with a
  `createRoute(...)` factory if it takes arguments) rather than building route
  strings inline.
- **Use cases**: one class per use case with a single `operator fun
  invoke(...)`, added to the relevant `*UseCases` bundle in
  `domain/usecase/<feature>/`.
- **Firestore schema changes**: if you add/change a Firestore field or
  collection, update the corresponding DTO **and**
  [docs/DATABASE.md](docs/DATABASE.md) (and `firestore.rules` /
  `firestore.indexes.json` if access patterns change) in the same PR.

## Commit messages

Use clear, descriptive commit messages in the imperative mood (e.g. "Add
streak milestone achievement check", not "Added" or "Adds"). Reference issue
numbers where relevant (`Fixes #123`).

## Contributing quiz content

New questions can be added two ways:

- **Via the app**: sign in as an Admin and use **Admin Dashboard → Question
  Bank → Add Question**.
- **Via PR**: if proposing a content pack (e.g. a new country's civics
  questions), open an issue first describing the scope (category, difficulty
  spread, country code, source/citations for factual claims) before doing the
  work — this helps avoid duplicate effort and ensures factual accuracy is
  reviewable.

Questions must be factually accurate, politically neutral/non-partisan
(unless explicitly tagged as `tone = SATIRICAL`/`FUNNY` for the Humor & Satire
mode, which still must not promote a specific party or candidate), and include
a clear `explanation`. See `QuestionDto` in
[docs/DATABASE.md](docs/DATABASE.md#questionsquestionid) for the required
shape.

## Reporting bugs / requesting features

Please use the issue templates when opening a new issue — they help us
triage faster. Include reproduction steps, expected vs. actual behavior, and
device/Android version for bugs.

## Security

Please do **not** open public issues for security vulnerabilities. See
[SECURITY.md](SECURITY.md) for how to report them responsibly.
