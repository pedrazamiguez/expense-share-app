package es.pedrazamiguez.expenseshareapp.data.remote.dto

import com.google.gson.annotations.SerializedName

data class CurrencyResponse(
    @SerializedName("currencies") val currencies: Map<String, String> // e.g., {"EUR": "Euro", "USD": "United States Dollar"}
)
