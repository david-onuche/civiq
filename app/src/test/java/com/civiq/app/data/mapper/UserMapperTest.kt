package com.civiq.app.data.mapper

import com.civiq.app.data.remote.dto.firestore.UserDto
import com.civiq.app.domain.model.User
import com.civiq.app.domain.model.UserRole
import com.google.common.truth.Truth.assertThat
import org.junit.Test

class UserMapperTest {

    @Test
    fun `toDomain maps all fields and parses role`() {
        val dto = UserDto(
            id = "user-1",
            email = "user@example.com",
            displayName = "Ada Lovelace",
            photoUrl = "https://example.com/photo.jpg",
            role = "PREMIUM",
            xp = 1_200,
            coins = 340,
            level = 5,
            streakCount = 7,
            longestStreak = 21,
            lastActiveDate = 1_700_000_000_000L,
            countryCode = "NG",
            createdAt = 1_690_000_000_000L,
            isPremium = true,
            premiumExpiresAt = 1_800_000_000_000L,
            fcmTokens = listOf("token-a", "token-b"),
        )

        val domain = dto.toDomain()

        assertThat(domain).isEqualTo(
            User(
                id = "user-1",
                email = "user@example.com",
                displayName = "Ada Lovelace",
                photoUrl = "https://example.com/photo.jpg",
                role = UserRole.PREMIUM,
                xp = 1_200,
                coins = 340,
                level = 5,
                streakCount = 7,
                longestStreak = 21,
                lastActiveDate = 1_700_000_000_000L,
                countryCode = "NG",
                createdAt = 1_690_000_000_000L,
                isPremium = true,
                premiumExpiresAt = 1_800_000_000_000L,
                fcmTokens = listOf("token-a", "token-b"),
            ),
        )
    }

    @Test
    fun `toDomain falls back to REGISTERED for an unrecognized role`() {
        val dto = UserDto(id = "user-2", role = "SUPER_ADMIN")

        assertThat(dto.toDomain().role).isEqualTo(UserRole.REGISTERED)
    }

    @Test
    fun `toDto round-trips a domain User`() {
        val user = User(
            id = "user-3",
            email = "admin@example.com",
            displayName = "Admin",
            role = UserRole.ADMIN,
            xp = 50,
            coins = 10,
            level = 2,
            streakCount = 1,
            longestStreak = 1,
            lastActiveDate = 100L,
            countryCode = "KE",
            createdAt = 50L,
            isPremium = false,
            premiumExpiresAt = null,
            fcmTokens = emptyList(),
        )

        val dto = user.toDto()

        assertThat(dto.role).isEqualTo("ADMIN")
        assertThat(dto.toDomain()).isEqualTo(user)
    }
}
