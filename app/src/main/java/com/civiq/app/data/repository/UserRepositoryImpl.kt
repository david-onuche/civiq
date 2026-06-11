package com.civiq.app.data.repository

import com.civiq.app.data.mapper.toDomain
import com.civiq.app.data.remote.dto.firestore.UserDto
import com.civiq.app.domain.model.User
import com.civiq.app.domain.repository.UserRepository
import com.civiq.app.utils.FirestoreCollections
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** Reads and writes the `users/{uid}` profile document. */
@Singleton
class UserRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : UserRepository {

    private fun userDoc(userId: String) = firestore.collection(FirestoreCollections.USERS).document(userId)

    override fun observeUser(userId: String): Flow<Resource<User>> = callbackFlow {
        trySend(Resource.Loading())
        val registration = userDoc(userId).addSnapshotListener { snapshot, error ->
            if (error != null) {
                trySend(Resource.Error(UiText.DynamicString(error.localizedMessage ?: "Failed to load profile")))
                return@addSnapshotListener
            }
            val dto = snapshot?.toObject(UserDto::class.java)
            if (dto != null) {
                trySend(Resource.Success(dto.toDomain()))
            } else {
                trySend(Resource.Error(UiText.DynamicString("User not found")))
            }
        }
        awaitClose { registration.remove() }
    }

    override suspend fun getUser(userId: String): Resource<User> = try {
        val snapshot = userDoc(userId).get().await()
        val dto = snapshot.toObject(UserDto::class.java)
        if (dto != null) {
            Resource.Success(dto.toDomain())
        } else {
            Resource.Error(UiText.DynamicString("User not found"))
        }
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to load profile"))
    }

    override suspend fun updateProfile(userId: String, displayName: String, photoUrl: String?): Resource<Unit> = try {
        val updates = mutableMapOf<String, Any?>("displayName" to displayName)
        if (photoUrl != null) updates["photoUrl"] = photoUrl
        userDoc(userId).update(updates).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to update profile"))
    }

    override suspend fun updateCountryCode(userId: String, countryCode: String): Resource<Unit> = try {
        userDoc(userId).update("countryCode", countryCode).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to update country"))
    }

    override suspend fun addFcmToken(userId: String, token: String): Resource<Unit> = try {
        userDoc(userId).update("fcmTokens", FieldValue.arrayUnion(token)).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to register device"))
    }

    override suspend fun removeFcmToken(userId: String, token: String): Resource<Unit> = try {
        userDoc(userId).update("fcmTokens", FieldValue.arrayRemove(token)).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to unregister device"))
    }
}
