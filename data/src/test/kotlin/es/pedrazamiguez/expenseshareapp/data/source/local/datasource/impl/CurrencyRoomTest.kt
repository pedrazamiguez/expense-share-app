package es.pedrazamiguez.expenseshareapp.data.source.local.datasource.impl

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import es.pedrazamiguez.expenseshareapp.data.source.local.dao.CurrencyDao
import es.pedrazamiguez.expenseshareapp.data.source.local.dao.ExchangeRateDao
import es.pedrazamiguez.expenseshareapp.data.source.local.database.AppDatabase
import es.pedrazamiguez.expenseshareapp.domain.model.Currency
import es.pedrazamiguez.expenseshareapp.domain.model.ExchangeRates
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.math.BigDecimal
import java.time.Instant

@RunWith(RobolectricTestRunner::class)
@OptIn(ExperimentalCoroutinesApi::class)
class CurrencyRoomTest {

    private lateinit var db: AppDatabase
    private lateinit var currencyDao: CurrencyDao
    private lateinit var exchangeRateDao: ExchangeRateDao
    private lateinit var localDataSource: LocalCurrencyDataSourceImpl

    private val usd = Currency("USD", "$", "US Dollar", 2)
    private val eur = Currency("EUR", "€", "Euro", 2)

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries() // okay for tests
            .build()
        currencyDao = db.currencyDao()
        exchangeRateDao = db.exchangeRateDao()
        localDataSource = LocalCurrencyDataSourceImpl(currencyDao, exchangeRateDao)
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun saveAndGetCurrencies() = runTest {
        val currencies = listOf(usd, eur)
        localDataSource.saveCurrencies(currencies)

        val result = localDataSource.getCurrencies()
        assertEquals(2, result.size)
        assertEquals(usd.code, result.first().code)
    }

    @Test
    fun saveAndGetExchangeRates() = runTest {
        val exchangeRates = ExchangeRates(
            baseCurrency = usd, rates = listOf(
                ExchangeRates.Rate(eur, BigDecimal("0.9"))
            ), lastUpdated = Instant.now()
        )

        localDataSource.saveExchangeRates(exchangeRates)

        val result = localDataSource.getExchangeRates("USD")
        assertEquals(1, result.rates.size)
        assertEquals(eur.code, result.rates.first().currency.code)
        assertEquals(BigDecimal("0.9"), result.rates.first().rate)
    }

    @Test
    fun getLastUpdated() = runTest {
        val now = Instant.now()
        val exchangeRates = ExchangeRates(
            baseCurrency = usd,
            rates = listOf(ExchangeRates.Rate(eur, BigDecimal("0.9"))),
            lastUpdated = now
        )
        localDataSource.saveExchangeRates(exchangeRates)

        val lastUpdated = localDataSource.getLastUpdated("USD")
        assertEquals(now.epochSecond, lastUpdated)
    }
}
