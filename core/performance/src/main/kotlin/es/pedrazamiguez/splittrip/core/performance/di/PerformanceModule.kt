package es.pedrazamiguez.splittrip.core.performance.di

import com.google.firebase.perf.FirebasePerformance
import es.pedrazamiguez.splittrip.core.performance.PerformanceMonitor
import es.pedrazamiguez.splittrip.core.performance.PerformanceMonitorImpl
import org.koin.dsl.module

val performanceModule = module {
    single { FirebasePerformance.getInstance() }
    single<PerformanceMonitor> { PerformanceMonitorImpl(get()) }
}
