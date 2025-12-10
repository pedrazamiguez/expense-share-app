package es.pedrazamiguez.expenseshareapp.data.firebase.firestore.datasource.impl

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.document.DeviceDocument
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudNotificationDataSource
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.CloudMetadataService
import kotlinx.coroutines.tasks.await
import java.util.UUID

class FirestoreNotificationDataSourceImpl(
    private val context: Context,
    private val firestore: FirebaseFirestore,
    private val authenticationService: AuthenticationService,
    private val cloudMetadataService: CloudMetadataService
) : CloudNotificationDataSource {

    override suspend fun registerDeviceToken(token: String) {
        val userId = authenticationService.requireUserId()
        val installationIdResult = cloudMetadataService.getAppInstallationId()
        val deviceId = installationIdResult.getOrDefault(
            UUID.randomUUID()
                .toString()
        )

        val packageInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                context.packageManager.getPackageInfo(
                    context.packageName, PackageManager.PackageInfoFlags.of(0)
                )
            } else {
                context.packageManager.getPackageInfo(context.packageName, 0)
            }
        } catch (_: Exception) {
            null
        }

        val versionName = packageInfo?.versionName ?: "Unknown"
        val versionCode = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            packageInfo?.longVersionCode ?: 0L
        } else {
            // @formatter:off
            @Suppress("DEPRECATION")
            packageInfo?.versionCode?.toLong() ?: 0L
            // @formatter:on
        }

        val isEmulator =
            Build.FINGERPRINT.contains("generic") || Build.MODEL.contains("sdk") || Build.PRODUCT.contains(
                "sdk"
            )


        val deviceDoc = DeviceDocument(
            deviceId = deviceId,
            token = token,
            model = "${Build.MANUFACTURER} ${Build.MODEL}",
            androidVersion = Build.VERSION.RELEASE,
            appVersionName = versionName,
            appVersionCode = versionCode,
            isEmulator = isEmulator
        )

        firestore.collection(DeviceDocument.collectionPath(userId))
            .document(deviceId)
            .set(deviceDoc, SetOptions.merge())
            .await()
    }

    override suspend fun unregisterDeviceToken(token: String) {
        val userId = authenticationService.requireUserId()
        val devicesCollection = firestore.collection(DeviceDocument.collectionPath(userId))

        // Find the document containing this token to delete it
        val snapshot = devicesCollection.whereEqualTo(
            DeviceDocument.TOKEN_FIELD, token
        )
            .get()
            .await()

        for (document in snapshot.documents) {
            document.reference.delete()
                .await()
        }
    }

}
