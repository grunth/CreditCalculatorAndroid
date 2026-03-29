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

data class SitePattern(
    val selector: String,
    val separator: String
)

class CreditDataViewModel(application: Application) : AndroidViewModel(application) {
    var creditData by mutableStateOf(CreditData())
    
    private val prefs = application.getSharedPreferences("credit_calc_prefs", Context.MODE_PRIVATE)
    
    val customSites = mutableStateListOf<CustomSite>()
    val savedProperties = mutableStateListOf<SavedProperty>()
    
    // Храним список паттернов для каждого хоста
    val sitePatterns = mutableStateMapOf<String, List<SitePattern>>()
    val siteSeparators = mutableStateMapOf<String, String>() // Дефолтный разделитель для новых находок

    init {
        loadData()
        refreshDefaultSites()
    }

    private fun refreshDefaultSites() {
        val olxUrl = "https://www.olx.pl/nieruchomosci/"
        val otodomUrl = "https://www.otodom.pl/"

        customSites.removeAll { it.name == "OLX" || it.name == "Otodom" }
        
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

    fun updatePropertyPrice(url: String, rawPrice: String) {
        val host = try { android.net.Uri.parse(url).host?.let { normalizeHost(it) } ?: "" } catch(e: Exception) { "" }
        
        // Пытаемся найти подходящий разделитель из существующих паттернов для этого хоста
        // Или используем дефолтный
        val separator = siteSeparators[host] ?: "none"
        val cleanPrice = cleanPriceText(rawPrice, separator)
        
        val index = savedProperties.indexOfFirst { it.url == url }
        if (index != -1) {
            val updated = savedProperties[index].copy(price = cleanPrice, rawPrice = rawPrice)
            savedProperties[index] = updated
            saveData()
        }
    }
    
    fun cleanPriceText(text: String, separator: String): String {
        if (text.isEmpty()) return "0"
        var processed = text.replace("\\s".toRegex(), "").replace("\u00A0", "")
        if (separator != "none" && processed.contains(separator)) {
            processed = processed.substringBefore(separator)
        }
        val digitsOnly = processed.replace(Regex("[^\\d]"), "")
        return digitsOnly.ifEmpty { "0" }
    }

    fun removeSavedProperty(property: SavedProperty) {
        savedProperties.removeAll { it.url == property.url }
        saveData()
    }

    fun addCustomSite(site: CustomSite) {
        customSites.add(site)
        saveData()
    }

    fun removeCustomSite(site: CustomSite) {
        customSites.remove(site)
        saveData()
    }

    fun saveSitePattern(host: String, selector: String, separator: String) {
        val normalized = normalizeHost(host)
        val currentPatterns = sitePatterns[normalized] ?: emptyList()
        
        // Добавляем новый паттерн, если такого еще нет
        if (currentPatterns.none { it.selector == selector }) {
            val newList = currentPatterns + SitePattern(selector, separator)
            sitePatterns[normalized] = newList
        }
        
        siteSeparators[normalized] = separator
        saveData()
    }

    private fun saveData() {
        val propsArray = JSONArray()
        savedProperties.forEach {
            val obj = JSONObject()
            obj.put("title", it.title)
            obj.put("url", it.url)
            obj.put("price", it.price)
            obj.put("siteName", it.siteName)
            obj.put("rawPrice", it.rawPrice)
            propsArray.put(obj)
        }

        val patternsObj = JSONObject()
        sitePatterns.forEach { (host, list) ->
            val arr = JSONArray()
            list.forEach { p ->
                val pObj = JSONObject()
                pObj.put("selector", p.selector)
                pObj.put("separator", p.separator)
                arr.put(pObj)
            }
            patternsObj.put(host, arr)
        }
        
        val separatorsObj = JSONObject()
        siteSeparators.forEach { (host, sep) ->
            separatorsObj.put(host, sep)
        }

        val sitesArray = JSONArray()
        customSites.forEach {
            if (it.name != "OLX" && it.name != "Otodom") {
                val obj = JSONObject()
                obj.put("name", it.name)
                obj.put("url", it.url)
                sitesArray.put(obj)
            }
        }
        
        prefs.edit()
            .putString("saved_properties", propsArray.toString())
            .putString("site_patterns_v2", patternsObj.toString())
            .putString("site_separators", separatorsObj.toString())
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
                    obj.optString("siteName", ""),
                    obj.optString("rawPrice", "")
                ))
            }
        }

        val patternsJson = prefs.getString("site_patterns_v2", null)
        if (patternsJson != null) {
            val obj = JSONObject(patternsJson)
            sitePatterns.clear()
            obj.keys().forEach { host ->
                val arr = obj.getJSONArray(host)
                val list = mutableListOf<SitePattern>()
                for (i in 0 until arr.length()) {
                    val pObj = arr.getJSONObject(i)
                    list.add(SitePattern(pObj.getString("selector"), pObj.getString("separator")))
                }
                sitePatterns[normalizeHost(host)] = list
            }
        } else {
            // Миграция со старого формата, если есть
            val oldPatterns = prefs.getString("site_patterns", null)
            if (oldPatterns != null) {
                val obj = JSONObject(oldPatterns)
                obj.keys().forEach { host ->
                    val sel = obj.getString(host)
                    sitePatterns[normalizeHost(host)] = listOf(SitePattern(sel, "none"))
                }
            }
        }
        
        val separatorsJson = prefs.getString("site_separators", null)
        if (separatorsJson != null) {
            val obj = JSONObject(separatorsJson)
            siteSeparators.clear()
            obj.keys().forEach { host ->
                siteSeparators[normalizeHost(host)] = obj.getString(host)
            }
        }

        val sitesJson = prefs.getString("custom_sites", null)
        if (sitesJson != null) {
            val array = JSONArray(sitesJson)
            for (i in 0 until array.length()) {
                val obj = array.getJSONObject(i)
                val name = obj.getString("name")
                val url = obj.getString("url")
                if (name != "OLX" && name != "Otodom") {
                    customSites.add(CustomSite(name, url))
                }
            }
        }
    }
}
