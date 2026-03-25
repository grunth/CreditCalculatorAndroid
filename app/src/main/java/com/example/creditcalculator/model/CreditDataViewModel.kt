package com.example.creditcalculator.model

import android.app.Application
import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
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
    val sitePatterns = mutableStateMapOf<String, String>()

    init {
        loadData()
        // Принудительно обновляем дефолтные сайты правильными ссылками на недвижимость
        refreshDefaultSites()
    }

    private fun refreshDefaultSites() {
        val olxUrl = "https://www.olx.pl/nieruchomosci/"
        val otodomUrl = "https://www.otodom.pl/"

        // Удаляем старые или дублирующиеся записи с такими именами
        customSites.removeAll { it.name == "OLX" || it.name == "Otodom" }
        
        // Добавляем актуальные версии в начало списка
        customSites.add(0, CustomSite("OLX", olxUrl))
        customSites.add(1, CustomSite("Otodom", otodomUrl))
    }

    private fun normalizeHost(host: String): String {
        return host.lowercase().removePrefix("www.")
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

    fun addCustomSite(site: CustomSite) {
        // Избегаем дубликатов по URL
        if (customSites.none { it.url == site.url }) {
            customSites.add(site)
            saveData()
        }
    }

    fun removeCustomSite(site: CustomSite) {
        customSites.remove(site)
        saveData()
    }

    fun saveSitePattern(host: String, selector: String) {
        val normalized = normalizeHost(host)
        sitePatterns[normalized] = selector
        saveData()
    }

    fun getPatternForHost(host: String): String? {
        return sitePatterns[normalizeHost(host)]
    }

    private fun saveData() {
        val propsArray = JSONArray()
        savedProperties.forEach {
            val obj = JSONObject()
            obj.put("title", it.title)
            obj.put("url", it.url)
            obj.put("price", it.price)
            obj.put("siteName", it.siteName)
            propsArray.put(obj)
        }

        val patternsObj = JSONObject()
        sitePatterns.forEach { (host, selector) ->
            patternsObj.put(host, selector)
        }

        val sitesArray = JSONArray()
        customSites.forEach {
            // Сохраняем только пользовательские сайты
            if (it.name != "OLX" && it.name != "Otodom") {
                val obj = JSONObject()
                obj.put("name", it.name)
                obj.put("url", it.url)
                sitesArray.put(obj)
            }
        }
        
        prefs.edit()
            .putString("saved_properties", propsArray.toString())
            .putString("site_patterns", patternsObj.toString())
            .putString("custom_sites", sitesArray.toString())
            .apply()
    }

    private fun loadData() {
        val propsJson = prefs.getString("saved_properties", null)
        if (propsJson != null) {
            val array = JSONArray(propsJson)
            savedProperties.clear()
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

        val patternsJson = prefs.getString("site_patterns", null)
        if (patternsJson != null) {
            val obj = JSONObject(patternsJson)
            sitePatterns.clear()
            obj.keys().forEach { host ->
                sitePatterns[normalizeHost(host)] = obj.getString(host)
            }
        }

        val sitesJson = prefs.getString("custom_sites", null)
        if (sitesJson != null) {
            val array = JSONArray(sitesJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val name = obj.getString("name")
                val url = obj.getString("url")
                // Загружаем только те, что не являются дефолтными (чтобы не дублировать логику refresh)
                if (name != "OLX" && name != "Otodom") {
                    customSites.add(CustomSite(name, url))
                }
            }
        }
    }
}
