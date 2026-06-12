# CiviQ — Local Setup Guide

This guide walks through getting CiviQ building and running against your own
Firebase project.

## Prerequisites

- **Android Studio** (latest stable) with Android SDK Platform 35 installed.
- **JDK 17** (bundled with recent Android Studio releases).
- A **Firebase** project (free Spark plan is sufficient for development).
- Optional: API keys for **Gemini** and/or **OpenAI** if you want AI-generated
  quiz questions and the AI Coach to work (the app falls back to the curated
  `questions` collection when these are not configured).

## 1. Clone and open the project

```bash
git clone https://github.com/<your-org>/civiq.git
cd civiq
```

Open the folder in Android Studio and let it sync Gradle. The first sync may
fail (or build with stub Firebase config) until you complete the steps below
— that's expected.

## 2. Create a Firebase project

1. Go to the [Firebase console](https://console.firebase.google.com/) and
   create a new project.
2. Add an Android app with package name **`com.civiq.app`**. If you also want
   to install `debug`/`staging` build variants alongside `prod` on the same
   device, additionally register `com.civiq.app.debug` and
   `com.civiq.app.staging` (see `applicationIdSuffix` in
   `app/build.gradle.kts`).
3. Download the generated **`google-services.json`** and place it at
   `app/google-services.json`. This file is git-ignored — never commit it.

## 3. Enable Firebase products

In the Firebase console, enable:

- **Authentication** → Sign-in method:
  - Email/Password
  - Google (for "Continue with Google" — note the **Web client ID**, you'll
    need it in step 5)
  - Anonymous (powers "Continue as Guest", which maps to `UserRole.GUEST`)
- **Firestore Database** → create in **Native mode**, in a region close to
  your users.
- **Cloud Messaging** (enabled by default) — used for daily reminders, streak
  nudges, achievement unlocks, and weekly challenge pushes.
- **Crashlytics** and **Performance Monitoring** (both already wired via the
  `com.google.firebase.crashlytics` / `firebase.perf` Gradle plugins).
- **Analytics** (recommended; enabled automatically with most Firebase
  projects).

## 4. Deploy Firestore rules and indexes

The repo includes [`firestore.rules`](../firestore.rules) and
[`firestore.indexes.json`](../firestore.indexes.json), documented in
[DATABASE.md](DATABASE.md). Using the
[Firebase CLI](https://firebase.google.com/docs/cli):

```bash
npm install -g firebase-tools
firebase login
firebase use --add          # select your Firebase project
firebase deploy --only firestore:rules,firestore:indexes
```

## 5. Configure `local.properties`

Copy the example file and fill in your own values — `local.properties` is
git-ignored and must never be committed:

```bash
cp local.properties.example local.properties
```

```properties
# AI Provider API Keys
GEMINI_API_KEY=your_gemini_api_key_here
OPENAI_API_KEY=your_openai_api_key_here

# Web client ID for Google Sign-In (Firebase console → Authentication →
# Sign-in method → Google → Web SDK configuration → Web client ID)
GOOGLE_WEB_CLIENT_ID=your_google_web_client_id_here
```

- **Gemini API key**: [Google AI Studio](https://aistudio.google.com/) →
  "Get API key".
- **OpenAI API key**: [OpenAI platform](https://platform.openai.com/api-keys).

These are exposed to the app as `BuildConfig.GEMINI_API_KEY`,
`BuildConfig.OPENAI_API_KEY`, and `BuildConfig.GOOGLE_WEB_CLIENT_ID` (see
`app/build.gradle.kts`). If left blank, AI quiz generation and the AI Coach
gracefully fall back / report `Resource.Error`, but the rest of the app works
normally with curated content.

### Release signing (optional, for `release` builds)

`assembleRelease` falls back to debug signing if these are not set, so they're
only needed when producing a signed release build:

```properties
CIVIQ_RELEASE_STORE_FILE=/path/to/your.keystore
CIVIQ_RELEASE_STORE_PASSWORD=...
CIVIQ_RELEASE_KEY_ALIAS=...
CIVIQ_RELEASE_KEY_PASSWORD=...
```

## 6. Run the app

From Android Studio: select the `devDebug` build variant and run on an
emulator or device (minSdk 26 / Android 8.0+).

From the command line:

```bash
./gradlew assembleDevDebug
./gradlew installDevDebug
```

## 7. Bootstrap an admin account

New accounts default to `UserRole.REGISTERED` (see `users/{uid}.role` in
[DATABASE.md](DATABASE.md)), and role changes normally require an existing
Admin via the in-app Admin Dashboard. To create your **first** Admin:

1. Sign up in the app normally.
2. In the Firebase console, open **Firestore Database** →
   `users/{your-uid}` and change the `role` field from `"REGISTERED"` to
   `"ADMIN"`.
3. Restart the app (or sign out/in) — the **Admin Dashboard** entry now
   appears on the Profile tab, and you can manage other users' roles, the
   question bank, daily challenges, achievements, and feature flags from
   there.

## 8. Seed content (optional)

The app works with an empty `questions` collection, but quizzes will show
"No questions available yet" until content exists. Seed questions either:

- Manually via **Admin Dashboard → Question Bank → Add Question**, or
- By bulk-importing documents into the `questions` collection matching
  `QuestionDto` (see [DATABASE.md](DATABASE.md#questionsquestionid)) via the
  Firebase console or `firebase firestore:` import tooling.

## Troubleshooting

- **`FirebaseApp not initialized`** — `google-services.json` is missing or in
  the wrong location (`app/google-services.json`).
- **Google Sign-In fails immediately** — `GOOGLE_WEB_CLIENT_ID` in
  `local.properties` doesn't match the Web client ID shown under
  Authentication → Sign-in method → Google in the Firebase console.
- **`PERMISSION_DENIED` reading/writing Firestore** — rules haven't been
  deployed (step 4), or you're testing an Admin-only action without having
  promoted your account (step 7).
- **AI features return errors** — `GEMINI_API_KEY`/`OPENAI_API_KEY` are blank
  or invalid; this is expected if you haven't configured them and the app
  will fall back to curated questions.
