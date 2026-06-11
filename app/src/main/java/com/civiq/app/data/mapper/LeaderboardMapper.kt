package com.civiq.app.data.mapper

import com.civiq.app.data.remote.dto.firestore.LeaderboardEntryDto
import com.civiq.app.domain.model.LeaderboardEntry

fun LeaderboardEntryDto.toDomain(): LeaderboardEntry = LeaderboardEntry(
    userId = userId,
    displayName = displayName,
    photoUrl = photoUrl,
    xp = xp,
    level = level,
    rank = rank,
    countryCode = countryCode,
)

fun LeaderboardEntry.toDto(): LeaderboardEntryDto = LeaderboardEntryDto(
    userId = userId,
    displayName = displayName,
    photoUrl = photoUrl,
    xp = xp,
    level = level,
    rank = rank,
    countryCode = countryCode,
)
