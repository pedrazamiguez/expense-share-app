package es.pedrazamiguez.expenseshareapp

import android.app.Application
import android.content.Context
import androidx.test.runner.AndroidJUnitRunner

/**
 * Custom test runner that replaces the production [App] with [TestApp].
 *
 * This prevents the production Koin graph (Firebase, Room, Retrofit, etc.)
 * from being initialised during instrumentation tests. Each test provides
 * its own minimal Koin modules via the Compose `KoinApplication` wrapper.
 */
class TestRunner : AndroidJUnitRunner() {

    override fun newApplication(
        cl: ClassLoader?,
        name: String?,
        context: Context?
    ): Application {
        return super.newApplication(cl, TestApp::class.java.name, context)
    }
}

