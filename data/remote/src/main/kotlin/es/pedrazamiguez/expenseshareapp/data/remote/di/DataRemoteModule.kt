package es.pedrazamiguez.expenseshareapp.data.remote.di

import es.pedrazamiguez.expenseshareapp.data.remote.BuildConfig
import es.pedrazamiguez.expenseshareapp.data.remote.api.OpenExchangeRatesApi
import es.pedrazamiguez.expenseshareapp.data.remote.datasource.RemoteCurrencyDataSourceImpl
import es.pedrazamiguez.expenseshareapp.domain.datasource.remote.RemoteCurrencyDataSource
import okhttp3.OkHttpClient
import org.koin.dsl.module
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

val dataRemoteModule = module {

    single {
        Retrofit.Builder().baseUrl(BuildConfig.OER_API_BASE_URL).client(OkHttpClient.Builder().build())
            .addConverterFactory(GsonConverterFactory.create()).build()
    }

    single<OpenExchangeRatesApi> {
        get<Retrofit>().create(OpenExchangeRatesApi::class.java)
    }

    single<RemoteCurrencyDataSource> {
        RemoteCurrencyDataSourceImpl(
            api = get<OpenExchangeRatesApi>(),
            appId = BuildConfig.OER_APP_ID
        )
    }

}
