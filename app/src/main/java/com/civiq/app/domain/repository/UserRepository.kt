package com.civiq.app.domain.repository

import com.civiq.app.domain.model.User
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow

/** Reads and writes the `users/{uid}` profile document (excluding auth credentials). */
interface UserRepository {

    /** Real-time stream of the user's profile document. */
    fun observeUser(userId: String): Flow<Resource<User>>

    suspend fun getUser(userId: String): Resource<User>

    suspend fun updateProfile(userId: String, displayName: String, photoUrl: String?): Resource<Unit>

    suspend fun updateCountryCode(userId: String, countryCode: String): Resource<Unit>

    suspend fun addFcmToken(userId: String, token: String): Resource<Unit>

    suspend fun removeFcmToken(userId: String, token: String): Resource<Unit>
}
