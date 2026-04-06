package es.pedrazamiguez.splittrip.data.firebase.installation.service.impl

import com.google.firebase.installations.FirebaseInstallations
import es.pedrazamiguez.splittrip.domain.service.CloudMetadataService
import kotlinx.coroutines.tasks.await

class CloudMetadataServiceImpl(private val firebaseInstallations: FirebaseInstallations) : CloudMetadataService {

    override suspend fun getAppInstallationId(): Result<String> = runCatching {
        firebaseInstallations.id.await()
    }
}
