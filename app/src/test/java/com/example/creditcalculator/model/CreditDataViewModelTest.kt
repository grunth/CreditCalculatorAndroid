package com.example.creditcalculator.model

import android.app.Application
import android.content.SharedPreferences
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify
import org.mockito.Mockito.atLeastOnce
import org.mockito.ArgumentMatchers.any

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
    }

    @Test
    fun `saveSitePattern should update map and call shared preferences`() {
        val viewModel = CreditDataViewModel(application)
        val host = "test.com"
        val selector = ".price"

        viewModel.saveSitePattern(host, selector)

        assertEquals(selector, viewModel.sitePatterns[host])
        verify(editor, atLeastOnce()).putString(eq("site_patterns"), any())
        verify(editor, atLeastOnce()).apply()
    }

    @Test
    fun `removeSitePattern should update map and call shared preferences`() {
        val viewModel = CreditDataViewModel(application)
        val host = "test.com"
        viewModel.sitePatterns[host] = ".price"

        viewModel.removeSitePattern(host)

        assertTrue(viewModel.sitePatterns.isEmpty())
        verify(editor, atLeastOnce()).apply()
    }
}
