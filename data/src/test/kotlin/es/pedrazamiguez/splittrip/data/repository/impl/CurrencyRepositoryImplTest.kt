package es.pedrazamiguez.splittrip.data.repository.impl

import es.pedrazamiguez.splittrip.domain.datasource.local.LocalCurrencyDataSource
import es.pedrazamiguez.splittrip.domain.datasource.remote.RemoteCurrencyDataSource
import es.pedrazamiguez.splittrip.domain.model.Currency
import es.pedrazamiguez.splittrip.domain.model.ExchangeRate
import es.pedrazamiguez.splittrip.domain.model.ExchangeRates
import es.pedrazamiguez.splittrip.domain.result.ExchangeRateResult
import io.mockk.Runs
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.just
import io.mockk.mockk
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CurrencyRepositoryImplTest {

    private val local = mockk<LocalCurrencyDataSource>(relaxed = true)
    private val remote = mockk<RemoteCurrencyDataSource>(relaxed = true)
    private lateinit var currencyRepositoryImpl: CurrencyRepositoryImpl

    private val usd = Currency(
        "USD",
        "$",
        "US Dollar",
        2
    )
    private val eur = Currency(
        "EUR",
        "€",
        "Euro",
        2
    )

    private val freshRates = ExchangeRates(
        baseCurrency = usd,
        exchangeRates = listOf(
            ExchangeRate(
                eur,
                BigDecimal("0.9")
            )
        ),
        lastUpdated = Instant.now()
    )

    @BeforeEach
    fun setUp() {
        currencyRepositoryImpl = CurrencyRepositoryImpl(
            local,
            remote,
            Duration.ofHours(12)
        )
    }

    @AfterEach
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `return fresh local rates`() = runTest {
        coEvery { local.getExchangeRates("USD") } returns freshRates
        coEvery { local.getLastUpdated("USD") } returns freshRates.lastUpdated.epochSecond

        val result = currencyRepositoryImpl.getExchangeRates("USD")
        assertTrue(result is ExchangeRateResult.Fresh)
        coVerify(exactly = 0) { remote.fetchExchangeRates(any()) }
    }

    @Test
    fun `fallback to stale when remote fails`() = runTest {
        val staleRates = freshRates.copy(
            lastUpdated = Instant
                .now()
                .minusSeconds(86_400)
        )

        coEvery { local.getExchangeRates("USD") } returns staleRates
        coEvery { local.getLastUpdated("USD") } returns staleRates.lastUpdated.epochSecond
        coEvery { remote.fetchExchangeRates("USD") } throws RuntimeException("network error")

        val result = currencyRepositoryImpl.getExchangeRates("USD")
        assertTrue(result is ExchangeRateResult.Stale)
    }

    @Test
    fun `fetch remote when local empty`() = runTest {
        val emptyRates = ExchangeRates(
            usd,
            emptyList(),
            Instant.EPOCH
        )

        coEvery { local.getExchangeRates("USD") } returns emptyRates
        coEvery { local.getLastUpdated("USD") } returns null
        coEvery { remote.fetchExchangeRates("USD") } returns freshRates
        coEvery { local.saveExchangeRates(freshRates) } just Runs

        val result = currencyRepositoryImpl.getExchangeRates("USD")
        assertTrue(result is ExchangeRateResult.Fresh)
        coVerify { local.saveExchangeRates(freshRates) }
    }
}
