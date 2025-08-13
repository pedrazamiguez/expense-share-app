package es.pedrazamiguez.expenseshareapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class ExchangeRateResponse(
    @SerializedName("base") val base: String,
    @SerializedName("rates") val rates: Map<String, Double>, // e.g., {"USD": 1.18, "MXN": 21.5}
    @SerializedName("timestamp") val timestamp: Long
)
