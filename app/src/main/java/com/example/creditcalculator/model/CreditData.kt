package com.example.creditcalculator.model

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import org.json.JSONArray
import org.json.JSONObject

class CreditDataViewModel(application: Application) : AndroidViewModel(application) {
    var creditData by mutableStateOf(CreditData())
    
    private val prefs = application.getSharedPreferences("credit_calc_prefs", Context.MODE_PRIVATE)
    
    val customSites = mutableStateListOf<CustomSite>()
    val savedProperties = mutableStateListOf<SavedProperty>()

    init {
        loadData()
    }

    fun addCustomSite(site: CustomSite) {
        customSites.add(site)
        saveData()
    }

    fun removeCustomSite(site: CustomSite) {
        customSites.remove(site)
        saveData()
    }

    fun addSavedProperty(property: SavedProperty) {
        savedProperties.removeAll { it.url == property.url }
        savedProperties.add(0, property)
        saveData()
    }

    fun updatePropertyPrice(url: String, newPrice: String) {
        val index = savedProperties.indexOfFirst { it.url == url }
        if (index != -1) {
            val updated = savedProperties[index].copy(price = newPrice)
            savedProperties[index] = updated
            saveData()
        }
    }

    fun removeSavedProperty(property: SavedProperty) {
        savedProperties.removeAll { it.url == property.url }
        saveData()
    }

    private fun saveData() {
        val sitesArray = JSONArray()
        customSites.forEach {
            val obj = JSONObject()
            obj.put("name", it.name)
            obj.put("url", it.url)
            sitesArray.put(obj)
        }
        
        val propsArray = JSONArray()
        savedProperties.forEach {
            val obj = JSONObject()
            obj.put("title", it.title)
            obj.put("url", it.url)
            obj.put("price", it.price)
            obj.put("siteName", it.siteName)
            propsArray.put(obj)
        }
        
        prefs.edit()
            .putString("custom_sites", sitesArray.toString())
            .putString("saved_properties", propsArray.toString())
            .apply()
    }

    private fun loadData() {
        val sitesJson = prefs.getString("custom_sites", null)
        if (sitesJson != null) {
            val array = JSONArray(sitesJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                customSites.add(CustomSite(obj.getString("name"), obj.getString("url")))
            }
        } else {
            customSites.add(CustomSite("OLX", "https://www.olx.pl/nieruchomosci/mieszkania/sprzedaz/"))
            customSites.add(CustomSite("Otodom", "https://www.otodom.pl/pl/wyniki/sprzedaz/"))
        }

        val propsJson = prefs.getString("saved_properties", null)
        if (propsJson != null) {
            val array = JSONArray(propsJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                savedProperties.add(SavedProperty(
                    obj.getString("title"),
                    obj.getString("url"),
                    obj.getString("price"),
                    obj.optString("siteName", "")
                ))
            }
        }
    }
}

data class CreditData(
    val loanAmount: String = "",
    val interestRate: String = "",
    val loanTerm: String = "",
    val selectedUnit: String = "Год",
    val repaymentMethod: String = "Аннуитентные платежи"
)

data class CreditRepaymentData(
    var month: String = "",
    var d: String = "",
    var y: String = "",
    var procents: String = "",
    var dolg: String = ""
)

data class SavedProperty(
    val title: String,
    val url: String,
    val price: String = "",
    val siteName: String = ""
)

data class CustomSite(
    val name: String,
    val url: String
)
