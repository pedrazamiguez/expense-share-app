package es.pedrazamiguez.splittrip.di

import es.pedrazamiguez.splittrip.domain.service.AuthenticationService
import es.pedrazamiguez.splittrip.domain.usecase.currency.WarmCurrencyCacheUseCase
import es.pedrazamiguez.splittrip.domain.usecase.setting.IsOnboardingCompleteUseCase
import es.pedrazamiguez.splittrip.domain.usecase.setting.SetOnboardingCompleteUseCase
import es.pedrazamiguez.splittrip.features.main.navigation.DeepLinkHolder
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flowOf
import org.koin.dsl.module

// ═══════════════════════════════════════════════════════════════════════
//  Reusable Koin test modules for instrumentation tests.
//
//  These modules provide MockK fakes for all domain-layer interfaces
//  that AppNavHost resolves via getKoin(). Each test can customise
//  the flows before calling setContent().
// ═══════════════════════════════════════════════════════════════════════

/**
 * Creates a Koin module for [AppNavHost] navigation tests.
 *
 * @param authStateFlow Flow emitted by [AuthenticationService.authState].
 *   Use `MutableSharedFlow<Boolean>()` (never emits, simulates loading),
 *   `flowOf(false)` (logged out), or `flowOf(true)` (logged in).
 * @param onboardingFlow Flow returned by [IsOnboardingCompleteUseCase].
 *   Use `flowOf(true)` / `flowOf(false)` to control onboarding status.
 */
fun createAppNavHostTestModule(
    authStateFlow: Flow<Boolean> = MutableStateFlow(false),
    onboardingFlow: Flow<Boolean> = flowOf(false)
) = module {
    // ── Domain services ───────────────────────────────────────────────
    single<AuthenticationService> {
        mockk<AuthenticationService>(relaxed = true).apply {
            every { authState } returns authStateFlow
        }
    }

    // ── Use cases consumed directly by AppNavHost ─────────────────────
    factory<IsOnboardingCompleteUseCase> {
        mockk<IsOnboardingCompleteUseCase>().apply {
            every { this@apply.invoke() } returns onboardingFlow
        }
    }

    factory<SetOnboardingCompleteUseCase> {
        mockk<SetOnboardingCompleteUseCase>(relaxed = true).apply {
            coEvery { this@apply.invoke() } returns Unit
        }
    }

    // ── Currency cache warm-up (fire-and-forget, no-op in tests) ────
    factory<WarmCurrencyCacheUseCase> { mockk(relaxed = true) }

    // ── Deep link holder for cold start replay ──────────────────────
    single { DeepLinkHolder() }
}
