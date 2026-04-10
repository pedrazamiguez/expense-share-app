package es.pedrazamiguez.splittrip.domain.usecase.setting

import es.pedrazamiguez.splittrip.domain.repository.GroupPreferenceRepository

class SetGroupLastUsedPaymentMethodUseCase(private val preferenceRepository: GroupPreferenceRepository) {

    suspend operator fun invoke(groupId: String, paymentMethodId: String) {
        preferenceRepository.setGroupLastUsedPaymentMethod(groupId, paymentMethodId)
    }
}
