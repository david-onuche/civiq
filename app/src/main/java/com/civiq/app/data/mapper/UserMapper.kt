package com.civiq.app.data.mapper

import com.civiq.app.data.remote.dto.firestore.UserDto
import com.civiq.app.domain.model.User
import com.civiq.app.domain.model.UserRole
import com.civiq.app.utils.safeEnumValueOf

fun UserDto.toDomain(): User = User(
    id = id,
    email = email,
    displayName = displayName,
    photoUrl = photoUrl,
    role = safeEnumValueOf(role, UserRole.REGISTERED),
    xp = xp,
    coins = coins,
    level = level,
    streakCount = streakCount,
    longestStreak = longestStreak,
    lastActiveDate = lastActiveDate,
    countryCode = countryCode,
    createdAt = createdAt,
    isPremium = isPremium,
    premiumExpiresAt = premiumExpiresAt,
    fcmTokens = fcmTokens,
)

fun User.toDto(): UserDto = UserDto(
    id = id,
    email = email,
    displayName = displayName,
    photoUrl = photoUrl,
    role = role.name,
    xp = xp,
    coins = coins,
    level = level,
    streakCount = streakCount,
    longestStreak = longestStreak,
    lastActiveDate = lastActiveDate,
    countryCode = countryCode,
    createdAt = createdAt,
    isPremium = isPremium,
    premiumExpiresAt = premiumExpiresAt,
    fcmTokens = fcmTokens,
)
