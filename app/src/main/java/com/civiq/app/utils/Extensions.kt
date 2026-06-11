package com.civiq.app.utils

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

/** Basic email format validation for auth forms. */
fun String.isValidEmail(): Boolean =
    Pattern.compile(
        "[a-zA-Z0-9+._%\\-]{1,256}@[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
    ).matcher(this).matches()

/** Minimum password strength: at least 8 characters, one letter and one digit. */
fun String.isValidPassword(): Boolean =
    length >= 8 && any { it.isDigit() } && any { it.isLetter() }

/** Formats a percentage (0.0..1.0) as a whole-number string, e.g. "0.873" -> "87%". */
fun Double.toPercentString(): String = "${(this * 100).toInt()}%"

/** Formats epoch millis as a relative "time ago" string, e.g. "3h ago", "Yesterday". */
fun Long.toRelativeTimeString(now: Long = System.currentTimeMillis()): String {
    val diff = now - this
    return when {
        diff < TimeUnit.MINUTES.toMillis(1) -> "Just now"
        diff < TimeUnit.HOURS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toMinutes(diff)}m ago"
        diff < TimeUnit.DAYS.toMillis(1) -> "${TimeUnit.MILLISECONDS.toHours(diff)}h ago"
        diff < TimeUnit.DAYS.toMillis(2) -> "Yesterday"
        diff < TimeUnit.DAYS.toMillis(7) -> "${TimeUnit.MILLISECONDS.toDays(diff)}d ago"
        else -> SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(this))
    }
}

/** Returns the start-of-day timestamp (midnight, device timezone) for [this] epoch millis. */
fun Long.startOfDay(): Long {
    val calendar = java.util.Calendar.getInstance()
    calendar.timeInMillis = this
    calendar.set(java.util.Calendar.HOUR_OF_DAY, 0)
    calendar.set(java.util.Calendar.MINUTE, 0)
    calendar.set(java.util.Calendar.SECOND, 0)
    calendar.set(java.util.Calendar.MILLISECOND, 0)
    return calendar.timeInMillis
}

/** True if [this] (epoch millis) falls on the calendar day immediately before [other]. */
fun Long.isDayBefore(other: Long): Boolean {
    val oneDayMillis = TimeUnit.DAYS.toMillis(1)
    return this.startOfDay() + oneDayMillis == other.startOfDay()
}

/** True if [this] (epoch millis) falls on the same calendar day as [other]. */
fun Long.isSameDayAs(other: Long): Boolean = this.startOfDay() == other.startOfDay()

/** Formats today's date as "yyyy-MM-dd" for use as a Firestore document ID (e.g. daily challenges). */
fun Long.toDateId(): String = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date(this))

/**
 * Parses [name] as an enum constant of [T], falling back to [default] if
 * [name] is blank or doesn't match a constant. Used when mapping Firestore
 * string fields (which may be missing/legacy) to domain enums.
 */
inline fun <reified T : Enum<T>> safeEnumValueOf(name: String?, default: T): T {
    if (name.isNullOrBlank()) return default
    return try {
        enumValueOf<T>(name)
    } catch (e: IllegalArgumentException) {
        default
    }
}
