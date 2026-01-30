package es.pedrazamiguez.expenseshareapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import es.pedrazamiguez.expenseshareapp.data.local.converter.BigDecimalConverter
import es.pedrazamiguez.expenseshareapp.data.local.converter.StringListConverter
import es.pedrazamiguez.expenseshareapp.data.local.dao.CurrencyDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.ExchangeRateDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.GroupDao
import es.pedrazamiguez.expenseshareapp.data.local.entity.CurrencyEntity
import es.pedrazamiguez.expenseshareapp.data.local.entity.ExchangeRateEntity
import es.pedrazamiguez.expenseshareapp.data.local.entity.GroupEntity

@Database(
    entities = [
        CurrencyEntity::class,
        ExchangeRateEntity::class,
        GroupEntity::class
    ],
    version = 2,
    exportSchema = true
)
@TypeConverters(
    BigDecimalConverter::class,
    StringListConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao
    abstract fun exchangeRateDao(): ExchangeRateDao
    abstract fun groupDao(): GroupDao
}
