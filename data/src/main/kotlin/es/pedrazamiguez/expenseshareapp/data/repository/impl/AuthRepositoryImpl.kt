package es.pedrazamiguez.expenseshareapp.data.repository.impl

import com.google.firebase.auth.FirebaseAuth
import es.pedrazamiguez.expenseshareapp.domain.repository.AuthRepository
import kotlinx.coroutines.tasks.await

class AuthRepositoryImpl(
    private val firebaseAuth: FirebaseAuth
) : AuthRepository {

    override suspend fun signIn(email: String, password: String): Result<String> = runCatching {
        firebaseAuth.signInWithEmailAndPassword(email, password).await().user?.uid ?: ""
    }

    override suspend fun signUp(email: String, password: String): Result<String> = runCatching {
        firebaseAuth.createUserWithEmailAndPassword(email, password).await().user?.uid ?: ""
    }

}