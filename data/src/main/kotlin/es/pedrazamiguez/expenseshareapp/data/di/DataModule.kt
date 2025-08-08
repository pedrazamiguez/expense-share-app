package es.pedrazamiguez.expenseshareapp.data.di

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import es.pedrazamiguez.expenseshareapp.data.repository.AuthRepository
import org.koin.dsl.module

val dataModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }
    single { AuthRepository(get()) }
}
