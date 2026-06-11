package com.civiq.app.data.repository

import com.civiq.app.data.mapper.toDomain
import com.civiq.app.data.remote.dto.firestore.AppNotificationDto
import com.civiq.app.domain.model.AppNotification
import com.civiq.app.domain.repository.NotificationRepository
import com.civiq.app.utils.FirestoreCollections
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

/** In-app notification feed (`notifications`) and FCM device token registration. */
@Singleton
class NotificationRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : NotificationRepository {

    override fun observeNotifications(userId: String, limit: Int): Flow<Resource<List<AppNotification>>> = callbackFlow {
        trySend(Resource.Loading())
        val registration = firestore.collection(FirestoreCollections.NOTIFICATIONS)
            .whereEqualTo("userId", userId)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit(limit.toLong())
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(UiText.DynamicString(error.localizedMessage ?: "Failed to load notifications")))
                    return@addSnapshotListener
                }
                val notifications = snapshot?.documents
                    ?.mapNotNull { it.toObject(AppNotificationDto::class.java)?.toDomain() }
                    ?: emptyList()
                trySend(Resource.Success(notifications))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun markAsRead(notificationId: String): Resource<Unit> = try {
        firestore.collection(FirestoreCollections.NOTIFICATIONS).document(notificationId)
            .update("isRead", true)
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to update notification"))
    }

    override suspend fun markAllAsRead(userId: String): Resource<Unit> = try {
        val snapshot = firestore.collection(FirestoreCollections.NOTIFICATIONS)
            .whereEqualTo("userId", userId)
            .whereEqualTo("isRead", false)
            .get()
            .await()
        if (!snapshot.isEmpty) {
            val batch = firestore.batch()
            snapshot.documents.forEach { batch.update(it.reference, "isRead", true) }
            batch.commit().await()
        }
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to update notifications"))
    }

    override suspend fun registerDeviceToken(userId: String, token: String): Resource<Unit> = try {
        firestore.collection(FirestoreCollections.USERS)
            .document(userId)
            .collection(FirestoreCollections.USER_DEVICES)
            .document(token)
            .set(mapOf("token" to token, "registeredAt" to System.currentTimeMillis()))
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to register device"))
    }

    override suspend fun unregisterDeviceToken(userId: String, token: String): Resource<Unit> = try {
        firestore.collection(FirestoreCollections.USERS)
            .document(userId)
            .collection(FirestoreCollections.USER_DEVICES)
            .document(token)
            .delete()
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to unregister device"))
    }
}
