package es.pedrazamiguez.expenseshareapp.data.source.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import es.pedrazamiguez.expenseshareapp.data.source.local.entity.CurrencyEntity

@Dao
interface CurrencyDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCurrencies(currencies: List<CurrencyEntity>)

    @Query("SELECT * FROM currencies")
    suspend fun getCurrencies(): List<CurrencyEntity>
}
