package es.pedrazamiguez.expenseshareapp.data.firebase.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import es.pedrazamiguez.expenseshareapp.data.firebase.auth.service.impl.AuthenticationServiceImpl
import es.pedrazamiguez.expenseshareapp.data.firebase.firestore.datasource.impl.FirestoreGroupDataSourceImpl
import es.pedrazamiguez.expenseshareapp.domain.datasource.cloud.CloudGroupDataSource
import es.pedrazamiguez.expenseshareapp.domain.service.AuthenticationService
import org.koin.dsl.module

val dataFirebaseModule = module {

    single<FirebaseAuth> { FirebaseAuth.getInstance() }

    single<FirebaseFirestore> { FirebaseFirestore.getInstance() }

    single<FirebaseMessaging> { FirebaseMessaging.getInstance() }

    single<FirebaseStorage> { FirebaseStorage.getInstance() }

    single<AuthenticationService> { AuthenticationServiceImpl(firebaseAuth = get<FirebaseAuth>()) }

    single<CloudGroupDataSource> {
        FirestoreGroupDataSourceImpl(
            firestore = get<FirebaseFirestore>(),
            auth = get<FirebaseAuth>()
        )
    }

}
