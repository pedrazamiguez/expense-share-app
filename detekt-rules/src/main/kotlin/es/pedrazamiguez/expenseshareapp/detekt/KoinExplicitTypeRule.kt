package es.pedrazamiguez.expenseshareapp.detekt

import io.gitlab.arturbosch.detekt.api.CodeSmell
import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.Debt
import io.gitlab.arturbosch.detekt.api.Entity
import io.gitlab.arturbosch.detekt.api.Issue
import io.gitlab.arturbosch.detekt.api.Rule
import io.gitlab.arturbosch.detekt.api.Severity
import org.jetbrains.kotlin.psi.KtCallExpression

/**
 * Koin's `get()` must explicitly declare the type argument.
 *
 * **Bad:**  `get()`
 * **Good:** `get<ValidationService>()`
 *
 * This prevents implicit type resolution in Koin DI modules, which can lead to
 * hard-to-debug runtime errors when the wrong type is injected.
 */
class KoinExplicitTypeRule(config: Config) : Rule(config) {

    override val issue = Issue(
        javaClass.simpleName,
        Severity.CodeSmell,
        "Koin's get() must explicitly declare the type argument, e.g., get<ValidationService>().",
        Debt.FIVE_MINS
    )

    override fun visitCallExpression(expression: KtCallExpression) {
        super.visitCallExpression(expression)

        if (expression.calleeExpression?.text == "get" && expression.typeArgumentList == null) {
            report(
                CodeSmell(
                    issue,
                    Entity.from(expression),
                    "Implicit type resolution found in Koin get(). Use an explicit type argument."
                )
            )
        }
    }
}
