package es.pedrazamiguez.splittrip.domain.service

import es.pedrazamiguez.splittrip.domain.enums.BiometricCapability

interface BiometricAuthService {
    fun getBiometricCapability(): BiometricCapability
}
