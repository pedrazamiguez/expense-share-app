package es.pedrazamiguez.expenseshareapp.data.firebase.auth.service.impl

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import es.pedrazamiguez.expenseshareapp.domain.model.User
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class AuthenticationServiceImpl(
    private val firebaseAuth: FirebaseAuth
) : AuthenticationService {

    override fun currentUserId(): String? = firebaseAuth.currentUser?.uid

    override fun requireUserId(): String =
        currentUserId() ?: throw IllegalStateException("User not logged in")

    override val authState: Flow<Boolean> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser != null)
        }
        firebaseAuth.addAuthStateListener(listener)
        awaitClose { firebaseAuth.removeAuthStateListener(listener) }
    }

    override suspend fun signIn(
        email: String,
        password: String
    ): Result<String> = runCatching {
        firebaseAuth
            .signInWithEmailAndPassword(
                email,
                password
            )
            .await().user?.uid ?: ""
    }

    override suspend fun signUp(
        email: String,
        password: String
    ): Result<String> = runCatching {
        firebaseAuth
            .createUserWithEmailAndPassword(
                email,
                password
            )
            .await().user?.uid ?: ""
    }

    override suspend fun signOut(): Result<Unit> = runCatching {
        firebaseAuth.signOut()
    }

    override suspend fun signInWithGoogle(idToken: String): Result<User> = runCatching {
        val credential = GoogleAuthProvider.getCredential(idToken, null)
        val firebaseUser = firebaseAuth.signInWithCredential(credential).await().user
            ?: throw IllegalStateException("Google sign-in succeeded but Firebase user is null")
        User(
            userId = firebaseUser.uid,
            email = firebaseUser.email ?: "",
            displayName = firebaseUser.displayName,
            profileImagePath = firebaseUser.photoUrl?.toString()
        )
    }

}
