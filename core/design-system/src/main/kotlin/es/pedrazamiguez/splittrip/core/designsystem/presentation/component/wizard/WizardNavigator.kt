package es.pedrazamiguez.splittrip.core.designsystem.presentation.component.wizard

/**
 * Pure, stateless utility for wizard step navigation.
 *
 * Encapsulates the finite-state machine logic shared across all wizard-enabled
 * ViewModels (`navigateNext`, `navigatePrevious`, `navigateToReview`) so the
 * logic does not need to be copy-pasted per feature.
 *
 * **Usage:** Instantiate as a private `val` inside each ViewModel — no DI needed
 * since this class has no dependencies and no mutable state.
 *
 * ```kotlin
 * private val wizardNavigator = WizardNavigator()
 *
 * private fun navigateNext() {
 *     val next = wizardNavigator.navigateNext(state.currentStep, state.applicableSteps) ?: return
 *     _uiState.update { it.copy(currentStep = next) }
 * }
 * ```
 *
 * **`navigateToReview` integration note:** [navigateToReview] returns the target
 * review step only. The caller is responsible for also recording the departure
 * step in state (`jumpedFromStep = state.currentStep`).
 *
 * **Future-proof:** A `jumpToStep(targetIndex, applicableSteps)` method (issue #992)
 * can be added here without changing the existing API.
 */
class WizardNavigator {

    /**
     * Returns the step that follows [currentStep] in [applicableSteps],
     * or `null` if [currentStep] is not found in the list or is already the last step.
     */
    fun <S : WizardStep> navigateNext(
        currentStep: S,
        applicableSteps: List<S>
    ): S? {
        val index = applicableSteps.indexOf(currentStep)
        if (index < 0 || index == applicableSteps.lastIndex) return null
        return applicableSteps[index + 1]
    }

    /**
     * Returns the appropriate [NavigationResult] for a back-press on [currentStep].
     *
     * Priority order:
     * 1. If [jumpedFromStep] is non-null **and** [currentStep] is the review step
     *    (`isReview == true`) → [NavigationResult.JumpBack] to the departure step.
     * 2. If a step before [currentStep] exists in [applicableSteps] →
     *    [NavigationResult.Step] to that step.
     * 3. If [currentStep] is already the first step → [NavigationResult.ExitWizard].
     *
     * **ViewModel integration note:** Both [NavigationResult.Step] and
     * [NavigationResult.JumpBack] result in the same state update — navigate to
     * the returned step and clear `jumpedFromStep`. The two subtypes exist for
     * semantic clarity and isolated unit-testability of this class.
     */
    fun <S : WizardStep> navigatePrevious(
        currentStep: S,
        jumpedFromStep: S?,
        applicableSteps: List<S>
    ): NavigationResult<S> {
        if (jumpedFromStep != null && currentStep.isReview) {
            return NavigationResult.JumpBack(jumpedFromStep)
        }
        val index = applicableSteps.indexOf(currentStep)
        if (index < 0) return NavigationResult.ExitWizard
        val prevStep = applicableSteps.getOrNull(index - 1)
        return if (prevStep != null) NavigationResult.Step(prevStep) else NavigationResult.ExitWizard
    }

    /**
     * Returns the review step from [applicableSteps] when [currentStep] is optional
     * (i.e. the user may skip ahead to review), or `null` when the jump is not allowed.
     *
     * Returns `null` when:
     * - [currentStep] is not optional (`isOptional == false`), or
     * - no step with `isReview == true` exists in [applicableSteps].
     *
     * **ViewModel integration note:** When a non-null step is returned the caller
     * must also record the departure step in state:
     * `_uiState.update { it.copy(currentStep = reviewStep, jumpedFromStep = state.currentStep) }`
     */
    fun <S : WizardStep> navigateToReview(
        currentStep: S,
        applicableSteps: List<S>
    ): S? {
        if (!currentStep.isOptional) return null
        return applicableSteps.firstOrNull { it.isReview }
    }

    /** Typed outcome of a [navigatePrevious] call. */
    sealed interface NavigationResult<out S> {

        /** Navigate sequentially to the previous [step]. */
        data class Step<S>(val step: S) : NavigationResult<S>

        /** Return to the [step] the user jumped from after a "Skip to Review" action. */
        data class JumpBack<S>(val step: S) : NavigationResult<S>

        /** No previous step exists — the wizard should be dismissed (back-stack pop). */
        data object ExitWizard : NavigationResult<Nothing>
    }
}
