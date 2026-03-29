package com.example.creditcalculator.model

import android.app.Application
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.any
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.atLeastOnce
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.`when`

class CreditDataViewModelTest {

    private lateinit var application: Application
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var editor: SharedPreferences.Editor

    @Before
    fun setup() {
        application = mock(Application::class.java)
        sharedPreferences = mock(SharedPreferences::class.java)
        editor = mock(SharedPreferences.Editor::class.java)

        `when`(application.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences)
        `when`(sharedPreferences.edit()).thenReturn(editor)
        `when`(editor.putString(anyString(), any())).thenReturn(editor)
        `when`(editor.apply()).then {}
    }

    @Test
    fun `saveSitePattern should update map and call shared preferences`() {
        val viewModel = CreditDataViewModel(application)
        val host = "test.com"
        val selector = ".price"
        val separator = ","

        viewModel.saveSitePattern(host, selector, separator)

        val patterns = viewModel.sitePatterns["test.com"]
        assertTrue(patterns != null && patterns.any { it.selector == selector })
        assertEquals(separator, viewModel.siteSeparators["test.com"])
        verify(editor, atLeastOnce()).putString(eq("site_patterns_v2"), any())
    }

    @Test
    fun `cleanPriceText correctly extracts digits with separator`() {
        val viewModel = CreditDataViewModel(application)
        
        assertEquals("1234", viewModel.cleanPriceText("1 234,56 zł", ","))
        assertEquals("123456", viewModel.cleanPriceText("1 234.56", "none"))
        assertEquals("0", viewModel.cleanPriceText("abc", "."))
    }
}
