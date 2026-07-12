package es.pedrazamiguez.splittrip.domain.service.calculator.impl

import es.pedrazamiguez.splittrip.domain.converter.CurrencyConverter
import es.pedrazamiguez.splittrip.domain.service.calculator.ExpressionEvaluator
import es.pedrazamiguez.splittrip.domain.service.calculator.ExpressionResult
import java.math.BigDecimal
import java.math.MathContext
import java.math.RoundingMode

@Suppress(
    "CyclomaticComplexMethod",
    "CognitiveComplexMethod",
    "NestedBlockDepth",
    "ComplexCondition",
    "SwallowedException",
    "ThrowsCount",
    "UseRequire"
)
class ExpressionEvaluatorImpl : ExpressionEvaluator {

    override fun evaluate(expression: String): ExpressionResult {
        if (expression.isBlank()) return ExpressionResult.Failure.Empty

        // Replace all multiplication and division signs with standard symbols for parsing
        val normalizedExpr = expression
            .replace("×", "*")
            .replace("÷", "/")
            .replace("−", "-")
            .replace("\\s+".toRegex(), "")

        if (normalizedExpr.isEmpty()) return ExpressionResult.Failure.Empty

        return try {
            val result = evaluateExpression(normalizedExpr)
            ExpressionResult.Success(result)
        } catch (e: ArithmeticException) {
            if (e.message?.contains("Division by zero") == true || e.message?.contains("divide by zero") == true) {
                ExpressionResult.Failure.DivisionByZero
            } else {
                ExpressionResult.Failure.Malformed
            }
        } catch (e: Exception) {
            ExpressionResult.Failure.Malformed
        }
    }

    private fun evaluateExpression(expr: String): BigDecimal {
        // Tokenize
        val tokens = tokenize(expr)
        if (tokens.isEmpty()) throw IllegalArgumentException("Empty tokens")

        // Parse to RPN (Shunting-yard algorithm)
        val rpn = infixToRPN(tokens)

        // Evaluate RPN
        return evaluateRPN(rpn)
    }

    private fun tokenize(expr: String): List<String> {
        val tokens = mutableListOf<String>()
        var currentNumber = StringBuilder()

        for (i in expr.indices) {
            val c = expr[i]
            if (c in listOf('+', '-', '*', '/')) {
                // Check for negative numbers (unary minus) at start or after an operator
                if (c == '-' &&
                    currentNumber.isEmpty() &&
                    (tokens.isEmpty() || tokens.last() in listOf("+", "-", "*", "/"))
                ) {
                    currentNumber.append(c)
                } else {
                    if (currentNumber.isNotEmpty()) {
                        tokens.add(currentNumber.toString())
                        currentNumber.clear()
                    }
                    tokens.add(c.toString())
                }
            } else {
                currentNumber.append(c)
            }
        }
        if (currentNumber.isNotEmpty()) {
            tokens.add(currentNumber.toString())
        }
        return tokens
    }

    private fun infixToRPN(tokens: List<String>): List<String> {
        val output = mutableListOf<String>()
        val operators = ArrayDeque<String>()

        val precedence = mapOf("+" to 1, "-" to 1, "*" to 2, "/" to 2)

        for (token in tokens) {
            if (token in precedence.keys) {
                while (operators.isNotEmpty() && precedence.getOrDefault(operators.last(), 0) >= precedence[token]!!) {
                    output.add(operators.removeLast())
                }
                operators.addLast(token)
            } else {
                // Try parsing the number
                CurrencyConverter.normalizeAmountString(token).toBigDecimalOrNull()
                    ?: throw IllegalArgumentException("Malformed number: $token")
                output.add(token)
            }
        }

        while (operators.isNotEmpty()) {
            output.add(operators.removeLast())
        }

        return output
    }

    private fun evaluateRPN(rpn: List<String>): BigDecimal {
        val stack = ArrayDeque<BigDecimal>()
        val mathContext = MathContext.DECIMAL64

        for (token in rpn) {
            when (token) {
                "+" -> {
                    val b = stack.removeLastOrNull() ?: throw IllegalArgumentException("Malformed expression")
                    val a = stack.removeLastOrNull() ?: throw IllegalArgumentException("Malformed expression")
                    stack.addLast(a.add(b, mathContext))
                }
                "-" -> {
                    val b = stack.removeLastOrNull() ?: throw IllegalArgumentException("Malformed expression")
                    val a = stack.removeLastOrNull() ?: throw IllegalArgumentException("Malformed expression")
                    stack.addLast(a.subtract(b, mathContext))
                }
                "*" -> {
                    val b = stack.removeLastOrNull() ?: throw IllegalArgumentException("Malformed expression")
                    val a = stack.removeLastOrNull() ?: throw IllegalArgumentException("Malformed expression")
                    stack.addLast(a.multiply(b, mathContext))
                }
                "/" -> {
                    val b = stack.removeLastOrNull() ?: throw IllegalArgumentException("Malformed expression")
                    val a = stack.removeLastOrNull() ?: throw IllegalArgumentException("Malformed expression")
                    if (b.compareTo(BigDecimal.ZERO) == 0) throw ArithmeticException("Division by zero")
                    stack.addLast(a.divide(b, 10, RoundingMode.HALF_UP))
                }
                else -> {
                    val normalized = CurrencyConverter.normalizeAmountString(token)
                    stack.addLast(BigDecimal(normalized, mathContext))
                }
            }
        }

        if (stack.size != 1) throw IllegalArgumentException("Malformed expression")
        return stack.first()
    }
}
