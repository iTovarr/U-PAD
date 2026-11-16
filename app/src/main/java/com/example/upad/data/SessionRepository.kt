package com.example.upad.data

import android.net.Uri
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

/** Datos del tutor con sesión iniciada que necesita la interfaz. */
data class SessionUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: Uri?
)

/**
 * Único punto de acceso a Firebase Authentication. Las pantallas y ViewModels
 * usan esta clase en lugar de llamar a `FirebaseAuth.getInstance()`.
 */
class SessionRepository(private val auth: FirebaseAuth = FirebaseAuth.getInstance()) {

    /** Tutor con sesión iniciada, o null si no hay sesión. */
    val currentUser: SessionUser?
        get() = auth.currentUser?.toSessionUser()

    /** UID del tutor con sesión iniciada, o cadena vacía si no hay sesión. */
    val currentUserId: String
        get() = auth.currentUser?.uid.orEmpty()

    /** Emite el usuario actual y cada cambio de sesión (inicio o cierre). */
    fun observeSession(): Flow<SessionUser?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { trySend(it.currentUser?.toSessionUser()) }
        auth.addAuthStateListener(listener)
        awaitClose { auth.removeAuthStateListener(listener) }
    }

    suspend fun signIn(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).await()
    }

    /** Crea la cuenta y devuelve el UID del nuevo usuario. */
    suspend fun register(email: String, password: String): String {
        val result = auth.createUserWithEmailAndPassword(email, password).await()
        return result.user?.uid ?: throw IllegalStateException("La cuenta se creó sin usuario asociado")
    }

    suspend fun signInWithGoogleIdToken(idToken: String) {
        auth.signInWithCredential(GoogleAuthProvider.getCredential(idToken, null)).await()
    }

    fun signOut() {
        auth.signOut()
    }

    private fun FirebaseUser.toSessionUser() = SessionUser(uid, email, displayName, photoUrl)
}
