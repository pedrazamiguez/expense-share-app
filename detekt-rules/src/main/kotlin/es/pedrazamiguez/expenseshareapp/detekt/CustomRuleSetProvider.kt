package es.pedrazamiguez.expenseshareapp.detekt

import io.gitlab.arturbosch.detekt.api.Config
import io.gitlab.arturbosch.detekt.api.RuleSet
import io.gitlab.arturbosch.detekt.api.RuleSetProvider

class CustomRuleSetProvider : RuleSetProvider {

    override val ruleSetId: String = "KoinRules"

    override fun instance(config: Config): RuleSet = RuleSet(
        ruleSetId,
        listOf(KoinExplicitTypeRule(config))
    )
}
