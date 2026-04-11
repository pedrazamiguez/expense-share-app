package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.wizard

/**
 * Classification of wizard skip behaviour.
 *
 * Used by feature-level step enums to declare whether a step is required or
 * optional. The strategy does **not** affect [WizardNavigationBar] directly;
 * instead, it guides the [WizardStepIndicator]'s visual treatment and the
 * ViewModel's jump-to-review logic.
 *
 * | Strategy         | Behaviour                                             |
 * |------------------|-------------------------------------------------------|
 * | [REQUIRED]       | Must be completed before advancing.                   |
 * | [OPTIONAL]       | May be skipped — visual cue in the step indicator.    |
 */
enum class WizardSkipStrategy {
    /** Step must be completed — default for all steps. */
    REQUIRED,

    /** Step may be skipped; shown with a distinct visual style. */
    OPTIONAL
}
