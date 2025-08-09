package es.pedrazamiguez.expenseshareapp.data.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import es.pedrazamiguez.expenseshareapp.data.repository.impl.AuthRepositoryImpl
import es.pedrazamiguez.expenseshareapp.domain.repository.AuthRepository
import org.koin.dsl.module

val dataModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }
    single<AuthRepository> { AuthRepositoryImpl(get()) }
}
