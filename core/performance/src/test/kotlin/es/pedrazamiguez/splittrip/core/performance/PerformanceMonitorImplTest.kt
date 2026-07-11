package es.pedrazamiguez.splittrip.core.performance

import com.google.firebase.perf.FirebasePerformance
import com.google.firebase.perf.metrics.Trace
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test

class PerformanceMonitorImplTest {

    private lateinit var firebasePerformance: FirebasePerformance
    private lateinit var trace: Trace
    private lateinit var performanceMonitor: PerformanceMonitorImpl

    @BeforeEach
    fun setUp() {
        firebasePerformance = mockk()
        trace = mockk(relaxed = true)

        every { firebasePerformance.newTrace(any()) } returns trace

        performanceMonitor = PerformanceMonitorImpl(firebasePerformance)
    }

    @Test
    fun `trace executes block and records trace`() {
        // Arrange
        val expectedResult = "result"

        // Act
        val actualResult = performanceMonitor.trace("test-trace") {
            expectedResult
        }

        // Assert
        assertEquals(expectedResult, actualResult)
        verify { firebasePerformance.newTrace("test-trace") }
        verify { trace.start() }
        verify { trace.stop() }
    }

    @Test
    fun `traceAsync executes block and records trace`() = runTest {
        // Arrange
        val expectedResult = "async-result"

        // Act
        val actualResult = performanceMonitor.traceAsync("test-async-trace") {
            expectedResult
        }

        // Assert
        assertEquals(expectedResult, actualResult)
        verify { firebasePerformance.newTrace("test-async-trace") }
        verify { trace.start() }
        verify { trace.stop() }
    }
}
