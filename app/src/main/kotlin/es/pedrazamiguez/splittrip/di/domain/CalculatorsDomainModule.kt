package es.pedrazamiguez.splittrip.di.domain

import es.pedrazamiguez.splittrip.domain.service.calculator.ExpressionCalculatorService
import es.pedrazamiguez.splittrip.domain.service.calculator.impl.ExpressionCalculatorServiceImpl
import org.koin.dsl.module

val calculatorsDomainModule = module {
    single<ExpressionCalculatorService> { ExpressionCalculatorServiceImpl() }
}
