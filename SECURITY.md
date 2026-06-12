# Security Policy

## Supported Versions

CiviQ is currently pre-1.0 (MVP). Security fixes are applied to the `main`
branch only.

| Version | Supported |
| --- | --- |
| `main` / latest | ✅ |

## Reporting a Vulnerability

**Please do not report security vulnerabilities through public GitHub
issues.**

If you discover a security vulnerability — for example, a Firestore security
rule that allows unauthorized read/write access, an authentication bypass, or
exposure of API keys/secrets — please report it privately:

- Email **security@civiq.app** with details of the vulnerability and steps to
  reproduce it, or
- Use GitHub's
  [private vulnerability reporting](https://docs.github.com/en/code-security/security-advisories/guidance-on-reporting-and-writing/privately-reporting-a-security-vulnerability)
  feature on this repository (Security tab → Report a vulnerability).

We will acknowledge your report as soon as possible and work with you to
understand and address the issue. Please give us a reasonable amount of time
to fix the issue before any public disclosure.

## Scope

Areas of particular interest for this project:

- **Firestore security rules** (`firestore.rules`) — improper access control
  to `users`, `quiz_attempts`, `subscriptions`, or Admin-only collections.
- **Authentication flows** (`presentation/auth/`, `AuthRepositoryImpl`) —
  privilege escalation, e.g. a non-Admin gaining `UserRole.ADMIN`.
- **Secrets handling** — API keys (`GEMINI_API_KEY`, `OPENAI_API_KEY`,
  signing credentials) accidentally bundled into the APK or committed to the
  repository.
- **Dependency vulnerabilities** in `app/build.gradle.kts`.

Out of scope: issues that only affect a developer's own local
`local.properties`/`google-services.json` (these are git-ignored by design —
see `.gitignore`).
