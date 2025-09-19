package es.pedrazamiguez.expenseshareapp.data.di

import android.app.Application
import androidx.room.Room
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import es.pedrazamiguez.expenseshareapp.data.BuildConfig
import es.pedrazamiguez.expenseshareapp.data.repository.AuthRepositoryImpl
import es.pedrazamiguez.expenseshareapp.data.repository.CurrencyRepositoryImpl
import es.pedrazamiguez.expenseshareapp.data.source.local.database.AppDatabase
import es.pedrazamiguez.expenseshareapp.data.source.local.datasource.impl.LocalCurrencyDataSourceImpl
import es.pedrazamiguez.expenseshareapp.data.source.remote.api.OpenExchangeRatesApi
import es.pedrazamiguez.expenseshareapp.data.source.remote.datasource.RemoteCurrencyDataSourceImpl
import es.pedrazamiguez.expenseshareapp.domain.datasource.local.LocalCurrencyDataSource
import es.pedrazamiguez.expenseshareapp.domain.datasource.remote.RemoteCurrencyDataSource
import es.pedrazamiguez.expenseshareapp.domain.repository.AuthRepository
import es.pedrazamiguez.expenseshareapp.domain.repository.CurrencyRepository
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.time.Duration

val dataModule = module {
    single { FirebaseAuth.getInstance() }
    single { FirebaseFirestore.getInstance() }
    single { FirebaseStorage.getInstance() }
    single<AuthRepository> { AuthRepositoryImpl(firebaseAuth = get()) }

    // Room database
    single {
        Room.databaseBuilder(
            context = get<Application>(), klass = AppDatabase::class.java, name = "expense_share_db"
        ).build()
    }

    // Retrofit
    single {
        Retrofit.Builder().baseUrl(BuildConfig.OER_API_BASE_URL)
            .client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create()).build()
    }
}

val currencyDataModule = module {

    // Retrofit API
    single<OpenExchangeRatesApi> {
        get<Retrofit>().create(OpenExchangeRatesApi::class.java)
    }

    // Room DAOs
    single { get<AppDatabase>().currencyDao() }
    single { get<AppDatabase>().exchangeRateDao() }

    // Data sources
    single<LocalCurrencyDataSource> {
        LocalCurrencyDataSourceImpl(
            currencyDao = get(), exchangeRateDao = get()
        )
    }
    single<RemoteCurrencyDataSource> {
        RemoteCurrencyDataSourceImpl(
            api = get(), appId = BuildConfig.OER_APP_ID
        )
    }

    // Repository
    single<CurrencyRepository> {
        CurrencyRepositoryImpl(
            localDataSource = get(),
            remoteDataSource = get(),
            cacheDuration = Duration.ofHours(BuildConfig.EXCHANGE_RATES_CACHE_DURATION_HOURS)
        )
    }
}