package es.pedrazamiguez.splittrip.core.designsystem.biometric

import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_STRONG
import androidx.biometric.BiometricManager.Authenticators.BIOMETRIC_WEAK
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity

object BiometricPromptHelper {

    fun authenticate(
        activity: FragmentActivity,
        title: String,
        subtitle: String? = null,
        negativeButtonText: String,
        onSuccess: () -> Unit,
        onError: (errorCode: Int, errString: CharSequence) -> Unit = { _, _ -> },
        onFailed: () -> Unit = {}
    ) {
        val executor = ContextCompat.getMainExecutor(activity)
        val prompt = BiometricPrompt(
            activity,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errorCode, errString)
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    onFailed()
                }
            }
        )

        val promptInfoBuilder = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setNegativeButtonText(negativeButtonText)
            .setAllowedAuthenticators(BIOMETRIC_STRONG or BIOMETRIC_WEAK)

        if (!subtitle.isNullOrBlank()) {
            promptInfoBuilder.setSubtitle(subtitle)
        }

        prompt.authenticate(promptInfoBuilder.build())
    }
}
