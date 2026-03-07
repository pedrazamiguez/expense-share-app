package es.pedrazamiguez.expenseshareapp.data.local.database

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import es.pedrazamiguez.expenseshareapp.data.local.converter.BigDecimalConverter
import es.pedrazamiguez.expenseshareapp.data.local.converter.CashTrancheListConverter
import es.pedrazamiguez.expenseshareapp.data.local.converter.StringListConverter
import es.pedrazamiguez.expenseshareapp.data.local.dao.CashWithdrawalDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.ContributionDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.CurrencyDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.ExchangeRateDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.ExpenseDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.ExpenseSplitDao
import es.pedrazamiguez.expenseshareapp.data.local.dao.GroupDao
import es.pedrazamiguez.expenseshareapp.data.local.entity.CashWithdrawalEntity
import es.pedrazamiguez.expenseshareapp.data.local.entity.ContributionEntity
import es.pedrazamiguez.expenseshareapp.data.local.entity.CurrencyEntity
import es.pedrazamiguez.expenseshareapp.data.local.entity.ExchangeRateEntity
import es.pedrazamiguez.expenseshareapp.data.local.entity.ExpenseEntity
import es.pedrazamiguez.expenseshareapp.data.local.entity.ExpenseSplitEntity
import es.pedrazamiguez.expenseshareapp.data.local.entity.GroupEntity

@Database(
    entities = [
        CurrencyEntity::class,
        ExchangeRateEntity::class,
        GroupEntity::class,
        ExpenseEntity::class,
        ExpenseSplitEntity::class,
        ContributionEntity::class,
        CashWithdrawalEntity::class
    ],
    version = 9,
    exportSchema = true
)
@TypeConverters(
    BigDecimalConverter::class,
    StringListConverter::class,
    CashTrancheListConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun currencyDao(): CurrencyDao
    abstract fun exchangeRateDao(): ExchangeRateDao
    abstract fun groupDao(): GroupDao
    abstract fun expenseDao(): ExpenseDao
    abstract fun expenseSplitDao(): ExpenseSplitDao
    abstract fun contributionDao(): ContributionDao
    abstract fun cashWithdrawalDao(): CashWithdrawalDao
}
