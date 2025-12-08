package es.pedrazamiguez.expenseshareapp.data.firebase.messaging.provider.impl

import com.google.firebase.messaging.FirebaseMessaging
import es.pedrazamiguez.expenseshareapp.domain.provider.DeviceTokenProvider
import kotlinx.coroutines.tasks.await

class FirebaseDeviceTokenProviderImpl(
    private val firebaseMessaging: FirebaseMessaging
) : DeviceTokenProvider {

    override suspend fun getDeviceToken(): Result<String> = runCatching {
        firebaseMessaging.token.await()
    }

}
