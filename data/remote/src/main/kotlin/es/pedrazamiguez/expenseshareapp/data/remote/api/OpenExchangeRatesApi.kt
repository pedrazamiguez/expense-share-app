package es.pedrazamiguez.expenseshareapp.data.remote.api

import es.pedrazamiguez.expenseshareapp.data.remote.dto.ExchangeRateResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface OpenExchangeRatesApi {
    @GET("currencies.json")
    suspend fun getCurrencies(
        @Query("app_id")
        appId: String
    ): Map<String, String>

    @GET("latest.json")
    suspend fun getExchangeRates(
        @Query("app_id")
        appId: String,
        @Query("base")
        baseCurrency: String
    ): ExchangeRateResponse
}
