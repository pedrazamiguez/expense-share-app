package es.pedrazamiguez.expenseshareapp.domain.usecase.setting

import es.pedrazamiguez.expenseshareapp.domain.repository.PreferenceRepository

class SetGroupLastUsedPaymentMethodUseCase(
    private val preferenceRepository: PreferenceRepository
) {

    suspend operator fun invoke(groupId: String, paymentMethodId: String) {
        preferenceRepository.setGroupLastUsedPaymentMethod(groupId, paymentMethodId)
    }

}

