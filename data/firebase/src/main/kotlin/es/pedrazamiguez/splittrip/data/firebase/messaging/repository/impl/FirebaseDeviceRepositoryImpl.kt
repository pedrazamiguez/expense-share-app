package es.pedrazamiguez.splittrip.data.firebase.messaging.repository.impl

import com.google.firebase.messaging.FirebaseMessaging
import es.pedrazamiguez.splittrip.domain.repository.DeviceRepository
import kotlinx.coroutines.tasks.await

class FirebaseDeviceRepositoryImpl(private val firebaseMessaging: FirebaseMessaging) : DeviceRepository {

    override suspend fun getDeviceToken(): Result<String> = runCatching {
        firebaseMessaging.token.await()
    }
}
