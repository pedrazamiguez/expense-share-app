package es.pedrazamiguez.expenseshareapp

import android.app.Application
import es.pedrazamiguez.expenseshareapp.core.config.di.coreConfigModule
import es.pedrazamiguez.expenseshareapp.data.di.currencyDataModule
import es.pedrazamiguez.expenseshareapp.data.di.dataModule
import es.pedrazamiguez.expenseshareapp.domain.di.useCaseModule
import es.pedrazamiguez.expenseshareapp.ui.auth.di.authModule
import es.pedrazamiguez.expenseshareapp.ui.balance.di.balanceModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(
                coreConfigModule,
                dataModule,
                useCaseModule,
                currencyDataModule,
                authModule,
                balanceModule
            )
        }
    }
}