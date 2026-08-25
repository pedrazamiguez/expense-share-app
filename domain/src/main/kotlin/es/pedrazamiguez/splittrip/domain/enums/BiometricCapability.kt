package es.pedrazamiguez.splittrip.domain.enums

enum class BiometricCapability {
    AVAILABLE,
    NO_HARDWARE,
    NOT_ENROLLED,
    UNAVAILABLE;

    val isAvailable: Boolean
        get() = this == AVAILABLE
}
