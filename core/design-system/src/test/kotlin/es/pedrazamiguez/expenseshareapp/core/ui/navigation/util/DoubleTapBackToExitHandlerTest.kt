package es.pedrazamiguez.expenseshareapp.core.ui.navigation.util

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class DoubleBackToExitHandlerTest {
    private var fakeTime = 0L
    private lateinit var handler: DoubleTapBackToExitHandler

    @BeforeEach
    fun setup() {
        fakeTime = 0L
        handler = DoubleTapBackToExitHandler(intervalMillis = 2000) { fakeTime }
    }

    @Test
    fun `first press should not exit`() {
        assertFalse(handler.shouldExit())
    }

    @Test
    fun `second press within interval should exit`() {
        handler.shouldExit() // first press at t=0
        fakeTime += 1500     // move time forward 1.5s
        assertTrue(handler.shouldExit()) // second press
    }

    @Test
    fun `second press after interval should not exit`() {
        handler.shouldExit() // first press at t=0
        fakeTime += 3000     // move time forward 3s
        assertFalse(handler.shouldExit()) // too late
    }

    @Test
    fun `reset clears state`() {
        handler.shouldExit() // first press at t=0
        fakeTime += 1000
        handler.reset()
        fakeTime += 1000
        assertFalse(handler.shouldExit()) // behaves as fresh
    }
}