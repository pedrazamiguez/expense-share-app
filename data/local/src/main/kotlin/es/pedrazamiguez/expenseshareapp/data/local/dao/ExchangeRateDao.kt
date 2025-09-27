package es.pedrazamiguez.expenseshareapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import es.pedrazamiguez.expenseshareapp.data.local.entity.ExchangeRateEntity

@Dao
interface ExchangeRateDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertExchangeRates(rates: List<ExchangeRateEntity>)

    @Query("SELECT * FROM exchange_rates WHERE baseCurrencyCode = :base")
    suspend fun getExchangeRates(base: String): List<ExchangeRateEntity>

    @Query("SELECT MAX(lastUpdated) FROM exchange_rates WHERE baseCurrencyCode = :base")
    suspend fun getLastUpdated(base: String): Long?

}
