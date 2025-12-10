package es.pedrazamiguez.expenseshareapp.provider.impl

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import es.pedrazamiguez.expenseshareapp.domain.provider.AppMetadataProvider
import timber.log.Timber

class AppMetadataProviderImpl(private val context: Context) : AppMetadataProvider {

    override val appVersionName: String by lazy {
        getPackageInfo()?.versionName ?: "Unknown"
    }

    override val appVersionCode: Long by lazy {
        val info = getPackageInfo()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            info?.longVersionCode ?: 0L
        } else {
            // @formatter:off
            @Suppress("DEPRECATION")
            info?.versionCode?.toLong() ?: 0L
            // @formatter:on
        }
    }

    // @formatter:off
    override val isEmulator: Boolean by lazy {
        Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MODEL.contains("sdk_gphone")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT
                || Build.HARDWARE.contains("goldfish")
                || Build.HARDWARE.contains("ranchu")
                || Build.PRODUCT.contains("sdk_google")
                || Build.PRODUCT.contains("vbox86p")
    }
    // @formatter:on

    override val deviceModel: String
        get() = "${Build.MANUFACTURER} ${Build.MODEL}"

    override val androidVersion: String
        get() = Build.VERSION.SDK_INT.toString()

    private fun getPackageInfo() = try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.packageManager.getPackageInfo(
                context.packageName, PackageManager.PackageInfoFlags.of(0)
            )
        } else {
            context.packageManager.getPackageInfo(context.packageName, 0)
        }
    } catch (e: Exception) {
        Timber.w(e, "Failed to get package info, using default values")
        null
    }
}
