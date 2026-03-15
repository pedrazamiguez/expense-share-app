package es.pedrazamiguez.expenseshareapp.domain.model

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(val message: String) : ValidationResult
}
