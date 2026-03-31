package es.pedrazamiguez.expenseshareapp.features.settings.presentation.viewmodel

import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.GetUserDefaultCurrencyUseCase
import es.pedrazamiguez.expenseshareapp.domain.usecase.setting.SetUserDefaultCurrencyUseCase
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class DefaultCurrencyViewModelTest {

    private val testDispatcher = StandardTestDispatcher()

    private lateinit var getUserDefaultCurrencyUseCase: GetUserDefaultCurrencyUseCase
    private lateinit var setUserDefaultCurrencyUseCase: SetUserDefaultCurrencyUseCase
    private lateinit var viewModel: DefaultCurrencyViewModel

    @BeforeEach
    fun setUp() {
        Dispatchers.setMain(testDispatcher)
        getUserDefaultCurrencyUseCase = mockk()
        setUserDefaultCurrencyUseCase = mockk(relaxed = true)
        every { getUserDefaultCurrencyUseCase() } returns flowOf("EUR")
        viewModel = DefaultCurrencyViewModel(
            getUserDefaultCurrencyUseCase = getUserDefaultCurrencyUseCase,
            setUserDefaultCurrencyUseCase = setUserDefaultCurrencyUseCase
        )
    }

    @AfterEach
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Nested
    inner class SelectedCurrencyCode {

        @Test
        fun `initial value is null before flow emits`() {
            assertNull(viewModel.selectedCurrencyCode.value)
        }

        @Test
        fun `emits currency code from use case flow`() = runTest(testDispatcher) {
            val collectJob = launch { viewModel.selectedCurrencyCode.collect {} }
            advanceUntilIdle()

            assertEquals("EUR", viewModel.selectedCurrencyCode.value)
            collectJob.cancel()
        }
    }

    @Nested
    inner class AvailableCurrencies {

        @Test
        fun `exposes all Currency entries`() {
            assertTrue(viewModel.availableCurrencies.isNotEmpty())
        }
    }

    @Nested
    inner class OnCurrencySelected {

        @Test
        fun `delegates to set use case`() = runTest(testDispatcher) {
            viewModel.onCurrencySelected("USD")
            advanceUntilIdle()

            coVerify { setUserDefaultCurrencyUseCase("USD") }
        }
    }
}
