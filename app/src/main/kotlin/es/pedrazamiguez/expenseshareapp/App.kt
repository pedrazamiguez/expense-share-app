package es.pedrazamiguez.expenseshareapp

import android.app.Application
import es.pedrazamiguez.expenseshareapp.core.di.coreModule
import es.pedrazamiguez.expenseshareapp.data.di.dataModule
import es.pedrazamiguez.expenseshareapp.ui.auth.di.AuthModule
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.startKoin
import org.koin.ksp.generated.module

class App : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@App)
            modules(coreModule, dataModule, AuthModule().module)
        }
    }
}