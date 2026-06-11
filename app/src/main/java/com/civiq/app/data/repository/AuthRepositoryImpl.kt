package com.civiq.app.data.repository

import com.civiq.app.data.mapper.toDomain
import com.civiq.app.data.mapper.toDto
import com.civiq.app.data.remote.dto.firestore.UserDto
import com.civiq.app.domain.model.User
import com.civiq.app.domain.model.UserRole
import com.civiq.app.domain.repository.AuthRepository
import com.civiq.app.utils.FirestoreCollections
import com.civiq.app.utils.Resource
import com.civiq.app.utils.UiText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

private const val GUEST_USER_ID = "guest"

/**
 * Wraps [FirebaseAuth] for credential management and keeps the corresponding
 * `users/{uid}` Firestore document in sync. Guest sessions are purely local
 * (no Firebase Auth user, no Firestore document) and are tracked via
 * [guestUser].
 */
@OptIn(ExperimentalCoroutinesApi::class)
@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val firebaseAuth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
) : AuthRepository {

    private val guestUser = MutableStateFlow<User?>(null)

    override val currentUser: Flow<User?> = authStateFlow().flatMapLatest { firebaseUser ->
        if (firebaseUser == null) {
            guestUser
        } else {
            observeUserDocument(firebaseUser.uid)
        }
    }

    private fun authStateFlow() = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth -> trySend(auth.currentUser) }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    private fun observeUserDocument(uid: String): Flow<User?> = callbackFlow {
        val registration = firestore.collection(FirestoreCollections.USERS)
            .document(uid)
            .addSnapshotListener { snapshot, _ ->
                val dto = snapshot?.toObject(UserDto::class.java)
                trySend(dto?.toDomain())
            }
        awaitClose { registration.remove() }
    }

    override fun getCurrentUserId(): String? = firebaseAuth.currentUser?.uid

    override suspend fun signInWithEmail(email: String, password: String): Resource<User> = try {
        val result = firebaseAuth.signInWithEmailAndPassword(email, password).await()
        val uid = result.user?.uid
        if (uid == null) {
            Resource.Error(UiText.DynamicString("Sign in failed"))
        } else {
            guestUser.value = null
            fetchUser(uid)
        }
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Sign in failed"))
    }

    override suspend fun registerWithEmail(email: String, password: String, displayName: String): Resource<User> = try {
        val result = firebaseAuth.createUserWithEmailAndPassword(email, password).await()
        val firebaseUser = result.user
        if (firebaseUser == null) {
            Resource.Error(UiText.DynamicString("Registration failed"))
        } else {
            firebaseUser.updateProfile(
                UserProfileChangeRequest.Builder().setDisplayName(displayName).build(),
            ).await()
            val newUser = User(
                id = firebaseUser.uid,
                email = email,
                displayName = displayName,
                role = UserRole.REGISTERED,
                createdAt = System.currentTimeMillis(),
            )
            firestore.collection(FirestoreCollections.USERS).document(firebaseUser.uid)
                .set(newUser.toDto())
                .await()
            guestUser.value = null
            Resource.Success(newUser)
        }
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Registration failed"))
    }

    override suspend fun signInWithGoogle(idToken: String): Resource<User> = try {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val result = firebaseAuth.signInWithCredential(credential).await()
        val firebaseUser = result.user
        if (firebaseUser == null) {
            Resource.Error(UiText.DynamicString("Sign in failed"))
        } else {
            guestUser.value = null
            if (result.additionalUserInfo?.isNewUser == true) {
                val newUser = User(
                    id = firebaseUser.uid,
                    email = firebaseUser.email ?: "",
                    displayName = firebaseUser.displayName ?: "",
                    photoUrl = firebaseUser.photoUrl?.toString(),
                    role = UserRole.REGISTERED,
                    createdAt = System.currentTimeMillis(),
                )
                firestore.collection(FirestoreCollections.USERS).document(firebaseUser.uid)
                    .set(newUser.toDto())
                    .await()
                Resource.Success(newUser)
            } else {
                fetchUser(firebaseUser.uid)
            }
        }
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Sign in failed"))
    }

    override suspend fun sendPasswordResetEmail(email: String): Resource<Unit> = try {
        firebaseAuth.sendPasswordResetEmail(email).await()
        Resource.Success(Unit)
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to send reset email"))
    }

    override suspend fun continueAsGuest(): Resource<User> {
        val guest = User(
            id = GUEST_USER_ID,
            displayName = "Guest",
            role = UserRole.GUEST,
            createdAt = System.currentTimeMillis(),
        )
        guestUser.value = guest
        return Resource.Success(guest)
    }

    override suspend fun signOut() {
        guestUser.value = null
        firebaseAuth.signOut()
    }

    private suspend fun fetchUser(uid: String): Resource<User> = try {
        val snapshot = firestore.collection(FirestoreCollections.USERS).document(uid).get().await()
        val dto = snapshot.toObject(UserDto::class.java)
        if (dto != null) {
            Resource.Success(dto.toDomain())
        } else {
            Resource.Error(UiText.DynamicString("User profile not found"))
        }
    } catch (e: Exception) {
        Resource.Error(UiText.DynamicString(e.localizedMessage ?: "Failed to load profile"))
    }
}
