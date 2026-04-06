package es.pedrazamiguez.splittrip.features.contribution.presentation.viewmodel.state

import es.pedrazamiguez.splittrip.core.designsystem.presentation.model.MemberOptionUiModel
import es.pedrazamiguez.splittrip.core.designsystem.presentation.model.SubunitOptionUiModel
import es.pedrazamiguez.splittrip.domain.enums.PayerType
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf

data class AddContributionUiState(
    val isLoading: Boolean = false,
    val amountInput: String = "",
    val amountError: Boolean = false,
    val groupCurrencyCode: String = "",
    val groupCurrencySymbol: String = "",
    val formattedAmountWithCurrency: String = "",
    val subunitOptions: ImmutableList<SubunitOptionUiModel> = persistentListOf(),
    val contributionScope: PayerType = PayerType.USER,
    val selectedSubunitId: String? = null,

    // ── Member picker (impersonation) ─────────────────────────────────
    val groupMembers: ImmutableList<MemberOptionUiModel> = persistentListOf(),
    val selectedMemberId: String? = null,
    val selectedMemberDisplayName: String = "",

    // ── Wizard ──────────────────────────────────────────────────────────
    val currentStep: AddContributionStep = AddContributionStep.AMOUNT
) {
    val steps: List<AddContributionStep>
        get() = AddContributionStep.entries

    val currentStepIndex: Int
        get() = steps.indexOf(currentStep).coerceAtLeast(0)

    val canGoNext: Boolean
        get() = currentStepIndex < steps.lastIndex

    val isOnReviewStep: Boolean
        get() = currentStep == AddContributionStep.REVIEW

    val isCurrentStepValid: Boolean
        get() = when (currentStep) {
            AddContributionStep.AMOUNT -> amountInput.isNotBlank() && !amountError
            AddContributionStep.SCOPE -> true // always has a default selection
            AddContributionStep.REVIEW ->
                amountInput.isNotBlank() && !amountError
        }
}
