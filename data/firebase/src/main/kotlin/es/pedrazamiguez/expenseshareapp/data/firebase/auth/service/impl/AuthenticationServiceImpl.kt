package es.pedrazamiguez.expenseshareapp.data.firebase.auth.service.impl

import com.google.firebase.auth.FirebaseAuth
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

}
