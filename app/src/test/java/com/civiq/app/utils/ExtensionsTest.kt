package com.civiq.app.utils

import com.google.common.truth.Truth.assertThat
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone
import java.util.concurrent.TimeUnit
import org.junit.After
import org.junit.Before
import org.junit.Test

class ExtensionsTest {

    private lateinit var originalTimeZone: TimeZone
    private lateinit var originalLocale: Locale

    @Before
    fun setUp() {
        originalTimeZone = TimeZone.getDefault()
        originalLocale = Locale.getDefault()
        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
        Locale.setDefault(Locale.US)
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(originalTimeZone)
        Locale.setDefault(originalLocale)
    }

    // region isValidEmail

    @Test
    fun `isValidEmail accepts well-formed addresses`() {
        assertThat("user@example.com".isValidEmail()).isTrue()
        assertThat("user.name+tag@sub.domain.co".isValidEmail()).isTrue()
    }

    @Test
    fun `isValidEmail rejects malformed addresses`() {
        assertThat("not-an-email".isValidEmail()).isFalse()
        assertThat("user@domain".isValidEmail()).isFalse()
        assertThat("user@.com".isValidEmail()).isFalse()
        assertThat("".isValidEmail()).isFalse()
    }

    // endregion

    // region isValidPassword

    @Test
    fun `isValidPassword requires 8+ chars with a letter and a digit`() {
        assertThat("abc12345".isValidPassword()).isTrue()
        assertThat("Abcdef12".isValidPassword()).isTrue()
    }

    @Test
    fun `isValidPassword rejects passwords missing letters, digits, or length`() {
        assertThat("abcdefgh".isValidPassword()).isFalse() // no digit
        assertThat("12345678".isValidPassword()).isFalse() // no letter
        assertThat("ab12345".isValidPassword()).isFalse() // too short
    }

    // endregion

    // region toPercentString

    @Test
    fun `toPercentString formats fractions as whole-number percentages`() {
        assertThat(0.873.toPercentString()).isEqualTo("87%")
        assertThat(1.0.toPercentString()).isEqualTo("100%")
        assertThat(0.0.toPercentString()).isEqualTo("0%")
    }

    // endregion

    // region toRelativeTimeString

    @Test
    fun `toRelativeTimeString buckets recent timestamps`() {
        val now = System.currentTimeMillis()

        assertThat((now - TimeUnit.SECONDS.toMillis(30)).toRelativeTimeString(now)).isEqualTo("Just now")
        assertThat((now - TimeUnit.MINUTES.toMillis(5)).toRelativeTimeString(now)).isEqualTo("5m ago")
        assertThat((now - TimeUnit.HOURS.toMillis(3)).toRelativeTimeString(now)).isEqualTo("3h ago")
    }

    @Test
    fun `toRelativeTimeString labels yesterday and older days`() {
        val now = System.currentTimeMillis()

        assertThat((now - TimeUnit.HOURS.toMillis(36)).toRelativeTimeString(now)).isEqualTo("Yesterday")
        assertThat((now - TimeUnit.DAYS.toMillis(3)).toRelativeTimeString(now)).isEqualTo("3d ago")
    }

    @Test
    fun `toRelativeTimeString falls back to a formatted date beyond a week`() {
        val now = System.currentTimeMillis()
        val timestamp = now - TimeUnit.DAYS.toMillis(10)

        val expected = SimpleDateFormat("MMM d, yyyy", Locale.getDefault()).format(Date(timestamp))
        assertThat(timestamp.toRelativeTimeString(now)).isEqualTo(expected)
    }

    // endregion

    // region startOfDay / isDayBefore / isSameDayAs

    @Test
    fun `startOfDay zeroes the time-of-day fields`() {
        val now = System.currentTimeMillis()
        val start = now.startOfDay()

        val calendar = Calendar.getInstance().apply { timeInMillis = start }
        assertThat(calendar.get(Calendar.HOUR_OF_DAY)).isEqualTo(0)
        assertThat(calendar.get(Calendar.MINUTE)).isEqualTo(0)
        assertThat(calendar.get(Calendar.SECOND)).isEqualTo(0)
        assertThat(calendar.get(Calendar.MILLISECOND)).isEqualTo(0)
        assertThat(start).isAtMost(now)
    }

    @Test
    fun `isDayBefore is true only for the immediately preceding calendar day`() {
        val today = System.currentTimeMillis()
        val tomorrow = today + TimeUnit.DAYS.toMillis(1)
        val nextWeek = today + TimeUnit.DAYS.toMillis(7)

        assertThat(today.isDayBefore(tomorrow)).isTrue()
        assertThat(today.isDayBefore(nextWeek)).isFalse()
        assertThat(today.isDayBefore(today)).isFalse()
    }

    @Test
    fun `isSameDayAs ignores time-of-day`() {
        val morning = System.currentTimeMillis().startOfDay()
        val lateNight = morning + TimeUnit.HOURS.toMillis(23) + TimeUnit.MINUTES.toMillis(59)
        val nextDay = morning + TimeUnit.DAYS.toMillis(1)

        assertThat(morning.isSameDayAs(lateNight)).isTrue()
        assertThat(morning.isSameDayAs(nextDay)).isFalse()
    }

    // endregion

    // region toDateId / toFormattedDate

    @Test
    fun `toDateId formats as yyyy-MM-dd`() {
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.parse("2024-01-15T00:00:00")!!.time

        assertThat(timestamp.toDateId()).isEqualTo("2024-01-15")
    }

    @Test
    fun `toFormattedDate formats as MMM d, yyyy`() {
        val timestamp = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }.parse("2024-01-15T00:00:00")!!.time

        assertThat(timestamp.toFormattedDate()).isEqualTo("Jan 15, 2024")
    }

    // endregion

    // region safeEnumValueOf

    private enum class Color { RED, GREEN, BLUE }

    @Test
    fun `safeEnumValueOf parses a matching name`() {
        assertThat(safeEnumValueOf("RED", default = Color.BLUE)).isEqualTo(Color.RED)
    }

    @Test
    fun `safeEnumValueOf falls back to default for null, blank, or unknown names`() {
        assertThat(safeEnumValueOf(null, default = Color.BLUE)).isEqualTo(Color.BLUE)
        assertThat(safeEnumValueOf("", default = Color.BLUE)).isEqualTo(Color.BLUE)
        assertThat(safeEnumValueOf("PURPLE", default = Color.BLUE)).isEqualTo(Color.BLUE)
    }

    // endregion
}
