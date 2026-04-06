package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.repository.PreferenceRepository

class SetGroupLastUsedPaymentMethodUseCase(private val preferenceRepository: PreferenceRepository) {

    suspend operator fun invoke(groupId: String, paymentMethodId: String) {
        preferenceRepository.setGroupLastUsedPaymentMethod(groupId, paymentMethodId)
    }
}
