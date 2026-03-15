package es.pedrazamiguez.expenseshareapp.data.local.converter

import androidx.room.TypeConverter
import es.pedrazamiguez.expenseshareapp.domain.model.CashTranche
import org.json.JSONArray
import org.json.JSONObject

/**
 * Room TypeConverter for List<CashTranche>.
 * Stores the list as a JSON string in the database.
 */
class CashTrancheListConverter {

    @TypeConverter
    fun fromCashTrancheList(value: List<CashTranche>?): String? {
        if (value.isNullOrEmpty()) return null
        val jsonArray = JSONArray()
        value.forEach { tranche ->
            val jsonObject = JSONObject().apply {
                put("withdrawalId", tranche.withdrawalId)
                put("amountConsumed", tranche.amountConsumed)
            }
            jsonArray.put(jsonObject)
        }
        return jsonArray.toString()
    }

    @TypeConverter
    fun toCashTrancheList(value: String?): List<CashTranche>? {
        if (value.isNullOrBlank()) return null
        val jsonArray = JSONArray(value)
        val result = mutableListOf<CashTranche>()
        for (i in 0 until jsonArray.length()) {
            val jsonObject = jsonArray.getJSONObject(i)
            result.add(
                CashTranche(
                    withdrawalId = jsonObject.getString("withdrawalId"),
                    amountConsumed = jsonObject.getLong("amountConsumed")
                )
            )
        }
        return result
    }
}
