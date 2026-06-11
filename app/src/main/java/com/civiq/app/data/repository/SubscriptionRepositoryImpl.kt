package com.civiq.app.data.repository

import com.civiq.app.data.mapper.toDomain
import com.civiq.app.data.mapper.toDto
import com.civiq.app.data.remote.dto.firestore.SubscriptionDto
import com.civiq.app.domain.model.PaymentProvider
import com.civiq.app.domain.model.Subscription
import com.civiq.app.domain.model.SubscriptionStatus
import com.civiq.app.domain.model.SubscriptionTier
import com.civiq.app.domain.repository.SubscriptionRepository
import com.civiq.app.utils.FirestoreCollections
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/** Premium subscription lifecycle, stored at `subscriptions/{subscriptionId}` (queried by `userId`). */
@Singleton
class SubscriptionRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
) : SubscriptionRepository {

    private fun subscriptionsCollection() = firestore.collection(FirestoreCollections.SUBSCRIPTIONS)

    override fun observeSubscription(userId: String): Flow<Resource<Subscription?>> = callbackFlow {
        trySend(Resource.Loading())
        val registration = subscriptionsCollection()
            .whereEqualTo("userId", userId)
            .limit(1)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    trySend(Resource.Error(UiText.DynamicString(error.localizedMessage ?: "Failed to load subscription")))
                    return@addSnapshotListener
                }
                val dto = snapshot?.documents?.firstOrNull()?.toObject(SubscriptionDto::class.java)
                trySend(Resource.Success(dto?.toDomain()))
            }
        awaitClose { registration.remove() }
    }

    override suspend fun startSubscription(
        userId: String,
        tier: SubscriptionTier,
        provider: PaymentProvider,
    ): Resource<Subscription> = try {
        val existing = subscriptionsCollection()
            .whereEqualTo("userId", userId)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
        val docRef = existing?.reference ?: subscriptionsCollection().document()
        val now = System.currentTimeMillis()
        val expiresAt = when (tier) {
            SubscriptionTier.PREMIUM_MONTHLY -> now + TimeUnit.DAYS.toMillis(30)
            SubscriptionTier.PREMIUM_YEARLY -> now + TimeUnit.DAYS.toMillis(365)
            SubscriptionTier.FREE -> null
        }
        val subscription = Subscription(
            id = docRef.id,
            userId = userId,
            tier = tier,
            provider = provider,
            startedAt = now,
            expiresAt = expiresAt,
            autoRenew = tier != SubscriptionTier.FREE,
            status = SubscriptionStatus.ACTIVE,
        )
        docRef.set(subscription.toDto()).await()
        firestore.collection(FirestoreCollections.USERS).document(userId)
            .update(mapOf("isPremium" to (tier != SubscriptionTier.FREE), "premiumExpiresAt" to expiresAt))
            .await()
        Resource.Success(subscription)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to start subscription"))
    }

    override suspend fun cancelSubscription(userId: String): Resource<Unit> = try {
        val existing = subscriptionsCollection()
            .whereEqualTo("userId", userId)
            .limit(1)
            .get()
            .await()
            .documents
            .firstOrNull()
        existing?.reference?.update(
            mapOf("status" to SubscriptionStatus.CANCELED.name, "autoRenew" to false),
        )?.await()
        firestore.collection(FirestoreCollections.USERS).document(userId)
            .update("isPremium", false)
            .await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to cancel subscription"))
    }
}
