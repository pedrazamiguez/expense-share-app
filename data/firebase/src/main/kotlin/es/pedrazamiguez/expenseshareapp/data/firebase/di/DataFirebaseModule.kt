package es.pedrazamiguez.expenseshareapp.data.firebase.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.installations.FirebaseInstallations
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import es.pedrazamiguez.expenseshareapp.data.firebase.auth.service.impl.AuthenticationServiceImpl
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.datasource.impl.FirestoreExpenseDataSourceImpl
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.datasource.impl.FirestoreGroupDataSourceImpl
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.datasource.impl.FirestoreNotificationDataSourceImpl
import es.pedrazamiguez.expenseshareapp.data.firebase.installation.service.impl.CloudMetadataServiceImpl
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.handler.factory.NotificationHandlerFactory
import es.pedrazamiguez.expenseshareapp.data.firebase.messaging.provider.impl.FirebaseDeviceTokenProviderImpl
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudExpenseDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudGroupDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudNotificationDataSource
import es.pedrazamiguez.expenseshareapp.domain.provider.DeviceTokenProvider
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import es.pedrazamiguez.expenseshareapp.domain.service.CloudMetadataService
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val dataFirebaseModule = module {

    single<FirebaseAuth> { FirebaseAuth.getInstance() }

    single<FirebaseFirestore> { FirebaseFirestore.getInstance() }

    single<FirebaseMessaging> { FirebaseMessaging.getInstance() }

    single<FirebaseStorage> { FirebaseStorage.getInstance() }

    single<AuthenticationService> { AuthenticationServiceImpl(firebaseAuth = get<FirebaseAuth>()) }

    single<FirebaseInstallations> { FirebaseInstallations.getInstance() }

    single<CloudGroupDataSource> {
        FirestoreGroupDataSourceImpl(
            firestore = get<FirebaseFirestore>(),
            authenticationService = get<AuthenticationService>()
        )
    }

    single<CloudExpenseDataSource> {
        FirestoreExpenseDataSourceImpl(
            firestore = get<FirebaseFirestore>(),
            authenticationService = get<AuthenticationService>()
        )
    }

    single<CloudMetadataService> { CloudMetadataServiceImpl(firebaseInstallations = get<FirebaseInstallations>()) }

    single<CloudNotificationDataSource> {
        FirestoreNotificationDataSourceImpl(
            context = androidContext(),
            firestore = get<FirebaseFirestore>(),
            authenticationService = get<AuthenticationService>(),
            cloudMetadataService = get<CloudMetadataService>()
        )
    }

    single<NotificationHandlerFactory> {
        NotificationHandlerFactory(
            context = androidContext()
        )
    }

    single<DeviceTokenProvider> {
        FirebaseDeviceTokenProviderImpl(
            firebaseMessaging = get<FirebaseMessaging>()
        )
    }

}
