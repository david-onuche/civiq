package com.civiq.app.domain.repository

import com.civiq.app.domain.model.User
import com.civiq.app.utils.Resource
import kotlinx.coroutines.flow.Flow

/**
 * Authentication boundary. Implementations wrap [com.google.firebase.auth.FirebaseAuth]
 * and keep the corresponding `users/{uid}` Firestore document in sync.
 */
interface AuthRepository {

    /** Emits the currently authenticated [User] (including their gamification profile), or null when signed out. */
    val currentUser: Flow<User?>

    /** Returns the Firebase Auth UID of the signed-in user, or null if signed out. */
    fun getCurrentUserId(): String?

    suspend fun signInWithEmail(email: String, password: String): Resource<User>

    suspend fun registerWithEmail(email: String, password: String, displayName: String): Resource<User>

    /** [idToken] is the Google ID token obtained from the Credential Manager / One Tap UI. */
    suspend fun signInWithGoogle(idToken: String): Resource<User>

    suspend fun sendPasswordResetEmail(email: String): Resource<Unit>

    /** Creates an ephemeral local [UserRole.GUEST] session with no Firestore document. */
    suspend fun continueAsGuest(): Resource<User>

    suspend fun signOut()
}
