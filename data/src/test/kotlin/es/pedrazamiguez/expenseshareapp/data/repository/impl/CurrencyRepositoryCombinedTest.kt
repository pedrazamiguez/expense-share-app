package es.pedrazamiguez.expenseshareapp.data.repository.impl

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import es.pedrazamiguez.expenseshareapp.data.source.local.database.AppDatabase
import es.pedrazamiguez.expenseshareapp.data.source.local.datasource.impl.LocalCurrencyDataSourceImpl
import es.pedrazamiguez.expenseshareapp.domain.datasource.remote.RemoteCurrencyDataSource
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRates
import es.pedrazamiguez.expenseshareapp.domain.result.ExchangeRateResult
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.math.BigDecimal
import java.time.Duration
import java.time.Instant

@OptIn(ExperimentalCoroutinesApi::class)
@RunWith(RobolectricTestRunner::class)
class CurrencyRepositoryCombinedTest {

    private lateinit var db: AppDatabase
    private lateinit var localDataSource: LocalCurrencyDataSourceImpl
    private lateinit var remoteDataSource: RemoteCurrencyDataSource
    private lateinit var repository: CurrencyRepositoryImpl

    private val usd = Currency("USD", "$", "US Dollar", 2)
    private val eur = Currency("EUR", "€", "Euro", 2)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java).allowMainThreadQueries()
            .build()
        localDataSource = LocalCurrencyDataSourceImpl(db.currencyDao(), db.exchangeRateDao())
        remoteDataSource = mockk()
        repository = CurrencyRepositoryImpl(localDataSource, remoteDataSource, Duration.ofHours(12))
    }

    @After
    fun tearDown() {
        db.close()
        clearAllMocks()
    }

    @Test
    fun `returns fresh from local when not stale`() = runTest {
        val rates = ExchangeRates(
            baseCurrency = usd,
            rates = listOf(ExchangeRates.Rate(eur, BigDecimal("0.9"))),
            lastUpdated = Instant.now()
        )
        localDataSource.saveExchangeRates(rates)

        val result = repository.getExchangeRates("USD")
        assertTrue(result is ExchangeRateResult.Fresh)
        val freshRates = (result as ExchangeRateResult.Fresh).exchangeRates
        assertEquals(1, freshRates.rates.size)
        assertEquals(BigDecimal("0.9"), freshRates.rates.first().rate)

        coVerify(exactly = 0) { remoteDataSource.fetchExchangeRates(any()) }
    }

    @Test
    fun `fetches remote if local is stale`() = runTest {
        val staleRates = ExchangeRates(
            baseCurrency = usd,
            rates = listOf(ExchangeRates.Rate(eur, BigDecimal("0.8"))),
            lastUpdated = Instant.now().minus(Duration.ofDays(2))
        )
        localDataSource.saveExchangeRates(staleRates)

        val remoteRates = ExchangeRates(
            baseCurrency = usd,
            rates = listOf(ExchangeRates.Rate(eur, BigDecimal("0.9"))),
            lastUpdated = Instant.now()
        )
        coEvery { remoteDataSource.fetchExchangeRates("USD") } returns remoteRates

        val result = repository.getExchangeRates("USD")
        assertTrue(result is ExchangeRateResult.Fresh)
        val freshRates = (result as ExchangeRateResult.Fresh).exchangeRates
        assertEquals(BigDecimal("0.9"), freshRates.rates.first().rate)

        coVerify { remoteDataSource.fetchExchangeRates("USD") }
    }

    @Test
    fun `fetches remote if local empty`() = runTest {
        val remoteRates = ExchangeRates(
            baseCurrency = usd,
            rates = listOf(ExchangeRates.Rate(eur, BigDecimal("0.9"))),
            lastUpdated = Instant.now()
        )
        coEvery { remoteDataSource.fetchExchangeRates("USD") } returns remoteRates

        val result = repository.getExchangeRates("USD")
        assertTrue(result is ExchangeRateResult.Fresh)
        val freshRates = (result as ExchangeRateResult.Fresh).exchangeRates
        assertEquals(BigDecimal("0.9"), freshRates.rates.first().rate)

        coVerify { remoteDataSource.fetchExchangeRates("USD") }
    }
}
