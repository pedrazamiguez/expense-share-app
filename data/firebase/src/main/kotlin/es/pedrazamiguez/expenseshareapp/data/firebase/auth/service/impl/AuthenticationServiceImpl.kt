package es.pedrazamiguez.expenseshareapp.data.firebase.auth.service.impl

import com.google.firebase.auth.FirebaseAuth
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import kotlinx.coroutines.tasks.await

class AuthenticationServiceImpl(
    private val firebaseAuth: FirebaseAuth
) : AuthenticationService {

    override suspend fun signIn(
        email: String,
        password: String
    ): Result<String> = runCatching {
        firebaseAuth.signInWithEmailAndPassword(
            email,
            password
        ).await().user?.uid ?: ""
    }

    override suspend fun signUp(
        email: String,
        password: String
    ): Result<String> = runCatching {
        firebaseAuth.createUserWithEmailAndPassword(
            email,
            password
        ).await().user?.uid ?: ""
    }

}