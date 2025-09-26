package es.pedrazamiguez.expenseshareapp.data.source.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import es.pedrazamiguez.expenseshareapp.data.source.local.converter.BigDecimalConverter
import es.pedrazamiguez.expenseshareapp.data.source.local.dao.CurrencyDao
import es.pedrazamiguez.expenseshareapp.data.source.local.dao.ExchangeRateDao
import es.pedrazamiguez.expenseshareapp.data.source.local.entity.CurrencyEntity
import es.pedrazamiguez.expenseshareapp.data.source.local.entity.ExchangeRateEntity

@Database(
    entities = [CurrencyEntity::class, ExchangeRateEntity::class], version = 1, exportSchema = true
)
@TypeConverters(BigDecimalConverter::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao
    abstract fun exchangeRateDao(): ExchangeRateDao
}
