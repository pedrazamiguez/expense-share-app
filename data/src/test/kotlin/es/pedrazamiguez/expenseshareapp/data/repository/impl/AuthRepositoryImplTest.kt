package es.pedrazamiguez.expenseshareapp.data.repository.impl

import com.google.android.gms.tasks.TaskCompletionSource
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthRepositoryImplTest {

    private val firebaseAuth = mockk<FirebaseAuth>()

    private val repository = AuthRepositoryImpl(firebaseAuth)

    @Test
    fun `signIn returns user uid on success`() = runTest {
        val uid = "test-uid"

        val fakeTask = TaskCompletionSource<AuthResult>()
        fakeTask.setResult(
            mockk<AuthResult> {
                every { user?.uid } returns uid
            })

        coEvery { firebaseAuth.signInWithEmailAndPassword(any(), any()) } returns fakeTask.task

        val result = repository.signIn("test@example.com", "password")

        assertEquals(uid, result.getOrThrow())
    }

    @Test
    fun `signIn returns failure on exception`() = runTest {
        val exception = Exception("SignIn failed")

        val fakeTask = TaskCompletionSource<AuthResult>()
        fakeTask.setException(exception)

        coEvery { firebaseAuth.signInWithEmailAndPassword(any(), any()) } returns fakeTask.task

        val result = repository.signIn("test@example.com", "password")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }

    @Test
    fun `signUp returns user uid on success`() = runTest {
        val uid = "new-user-uid"

        val fakeTask = TaskCompletionSource<AuthResult>()
        fakeTask.setResult(
            mockk<AuthResult> {
                every { user?.uid } returns uid
            })

        coEvery { firebaseAuth.createUserWithEmailAndPassword(any(), any()) } returns fakeTask.task

        val result = repository.signUp("new@example.com", "password")

        assertEquals(uid, result.getOrThrow())
    }

    @Test
    fun `signUp returns failure on exception`() = runTest {
        val exception = Exception("SignUp failed")

        val fakeTask = TaskCompletionSource<AuthResult>()
        fakeTask.setException(exception)

        coEvery { firebaseAuth.createUserWithEmailAndPassword(any(), any()) } returns fakeTask.task

        val result = repository.signUp("new@example.com", "password")

        assertTrue(result.isFailure)
        assertEquals(exception, result.exceptionOrNull())
    }
}
