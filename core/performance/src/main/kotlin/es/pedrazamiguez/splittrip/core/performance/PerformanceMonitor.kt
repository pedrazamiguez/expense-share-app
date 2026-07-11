package es.pedrazamiguez.splittrip.core.performance

import com.google.firebase.perf.FirebasePerformance

interface PerformanceMonitor {
    fun <T> trace(name: String, block: () -> T): T
    suspend fun <T> traceAsync(name: String, block: suspend () -> T): T
}

class PerformanceMonitorImpl(
    private val firebasePerformance: FirebasePerformance
) : PerformanceMonitor {
    override fun <T> trace(name: String, block: () -> T): T {
        val trace = firebasePerformance.newTrace(name)
        trace.start()
        return try {
            block()
        } finally {
            trace.stop()
        }
    }

    override suspend fun <T> traceAsync(name: String, block: suspend () -> T): T {
        val trace = firebasePerformance.newTrace(name)
        trace.start()
        return try {
            block()
        } finally {
            trace.stop()
        }
    }
}
