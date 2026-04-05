package es.pedrazamiguez.expenseshareapp.core.designsystem.presentation.snackbar

import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
@DisplayName("SnackbarController")
class SnackbarControllerTest {

    private lateinit var testScope: TestScope
    private lateinit var hostState: SnackbarHostState
    private lateinit var controller: SnackbarController

    @BeforeEach
    fun setUp() {
        testScope = TestScope()
        hostState = mockk(relaxed = true)
        controller = SnackbarController(hostState, testScope)
    }

    @Nested
    @DisplayName("showSnackbar")
    inner class ShowSnackbar {

        @Test
        fun `uses withDismissAction true by default`() = testScope.runTest {
            // Given
            coEvery {
                hostState.showSnackbar(any(), any(), any(), any())
            } returns SnackbarResult.Dismissed

            // When
            controller.showSnackbar(message = "Test message")
            advanceUntilIdle()

            // Then
            coVerify {
                hostState.showSnackbar(
                    message = "Test message",
                    actionLabel = null,
                    withDismissAction = true,
                    duration = SnackbarDuration.Short
                )
            }
        }

        @Test
        fun `allows overriding withDismissAction to false`() = testScope.runTest {
            // Given
            coEvery {
                hostState.showSnackbar(any(), any(), any(), any())
            } returns SnackbarResult.Dismissed

            // When
            controller.showSnackbar(
                message = "Test message",
                withDismissAction = false
            )
            advanceUntilIdle()

            // Then
            coVerify {
                hostState.showSnackbar(
                    message = "Test message",
                    actionLabel = null,
                    withDismissAction = false,
                    duration = SnackbarDuration.Short
                )
            }
        }

        @Test
        fun `passes all parameters to hostState`() = testScope.runTest {
            // Given
            coEvery {
                hostState.showSnackbar(any(), any(), any(), any())
            } returns SnackbarResult.ActionPerformed

            // When
            controller.showSnackbar(
                message = "Error occurred",
                actionLabel = "Retry",
                withDismissAction = true,
                duration = SnackbarDuration.Long
            )
            advanceUntilIdle()

            // Then
            coVerify {
                hostState.showSnackbar(
                    message = "Error occurred",
                    actionLabel = "Retry",
                    withDismissAction = true,
                    duration = SnackbarDuration.Long
                )
            }
        }

        @Test
        fun `invokes onResult callback with Dismissed result`() = testScope.runTest {
            // Given
            coEvery {
                hostState.showSnackbar(any(), any(), any(), any())
            } returns SnackbarResult.Dismissed

            var capturedResult: SnackbarResult? = null

            // When
            controller.showSnackbar(
                message = "Test",
                onResult = { capturedResult = it }
            )
            advanceUntilIdle()

            // Then
            assertEquals(SnackbarResult.Dismissed, capturedResult)
        }

        @Test
        fun `invokes onResult callback with ActionPerformed result`() = testScope.runTest {
            // Given
            coEvery {
                hostState.showSnackbar(any(), any(), any(), any())
            } returns SnackbarResult.ActionPerformed

            var capturedResult: SnackbarResult? = null

            // When
            controller.showSnackbar(
                message = "Test",
                actionLabel = "Undo",
                onResult = { capturedResult = it }
            )
            advanceUntilIdle()

            // Then
            assertEquals(SnackbarResult.ActionPerformed, capturedResult)
        }

        @Test
        fun `does not invoke onResult when callback is null`() = testScope.runTest {
            // Given
            coEvery {
                hostState.showSnackbar(any(), any(), any(), any())
            } returns SnackbarResult.Dismissed

            // When / Then — no exception thrown
            controller.showSnackbar(message = "Test", onResult = null)
            advanceUntilIdle()
        }

        @Test
        fun `uses Short duration by default`() = testScope.runTest {
            // Given
            coEvery {
                hostState.showSnackbar(any(), any(), any(), any())
            } returns SnackbarResult.Dismissed

            // When
            controller.showSnackbar(message = "Test")
            advanceUntilIdle()

            // Then
            coVerify {
                hostState.showSnackbar(
                    message = any(),
                    actionLabel = any(),
                    withDismissAction = any(),
                    duration = SnackbarDuration.Short
                )
            }
        }

        @Test
        fun `exposes hostState for Scaffold integration`() {
            // Then
            assertTrue(controller.hostState === hostState)
        }
    }
}
