package es.pedrazamiguez.expenseshareapp

import android.app.Application
import es.pedrazamiguez.expenseshareapp.core.di.coreModule
import es.pedrazamiguez.expenseshareapp.data.di.dataModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(coreModule, dataModule)
        }
    }
}