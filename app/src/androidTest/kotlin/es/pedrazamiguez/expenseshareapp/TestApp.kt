package es.pedrazamiguez.expenseshareapp

import android.app.Application

/**
 * Lightweight Application for instrumentation tests.
 *
 * Unlike the production [App] class, this does NOT call [startKoin] with
 * production modules. Each test sets up its own Koin context via
 * [KoinApplication] in the Compose tree, providing only the mocks it needs.
 */
class TestApp : Application()

