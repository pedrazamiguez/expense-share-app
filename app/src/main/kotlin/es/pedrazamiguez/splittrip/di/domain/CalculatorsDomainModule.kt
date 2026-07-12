package es.pedrazamiguez.splittrip.di.domain

import es.pedrazamiguez.splittrip.domain.service.calculator.ExpressionEvaluator
import es.pedrazamiguez.splittrip.domain.service.calculator.impl.ExpressionEvaluatorImpl
import org.koin.dsl.module

val calculatorsDomainModule = module {
    single<ExpressionEvaluator> { ExpressionEvaluatorImpl() }
}
