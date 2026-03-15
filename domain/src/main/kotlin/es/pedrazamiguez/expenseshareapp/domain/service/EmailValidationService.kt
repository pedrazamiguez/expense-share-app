package es.pedrazamiguez.expenseshareapp.domain.service

/**
 * Domain service responsible for validating email addresses.
 * Validation logic belongs in services, NOT in UseCases or ViewModels.
 *
 * Uses a pure Kotlin regex — no Android dependencies.
 */
class EmailValidationService {

    fun isValidEmail(email: String): Boolean = email.trim().matches(EMAIL_REGEX)

    companion object {
        private val EMAIL_REGEX = Regex(
            "[a-zA-Z0-9+._%\\-]{1,256}" +
                "@" +
                "[a-zA-Z0-9][a-zA-Z0-9\\-]{0,64}" +
                "(\\.[a-zA-Z0-9][a-zA-Z0-9\\-]{0,25})+"
        )
    }
}
