package com.example.creditcalculator.service

import android.app.Application
import android.content.SharedPreferences
import com.example.creditcalculator.model.CreditData
import com.example.creditcalculator.model.CreditDataViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.ArgumentMatchers.anyInt
import org.mockito.ArgumentMatchers.anyString
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock

class CalculateTest {

    private lateinit var application: Application
    private lateinit var sharedPreferences: SharedPreferences

    @Before
    fun setup() {
        application = mock(Application::class.java)
        sharedPreferences = mock(SharedPreferences::class.java)
        `when`(application.getSharedPreferences(anyString(), anyInt())).thenReturn(sharedPreferences)
    }

    private fun String.clean(): String {
        return this.replace("\\s".toRegex(), "").replace("\u00a0", "")
    }

    @Test
    fun `annuity repayment calculation is correct`() {
        val viewModel = CreditDataViewModel(application).apply {
            creditData = CreditData(
                loanAmount = "100000",
                interestRate = "12",
                loanTerm = "12",
                selectedUnit = "Месяц",
                repaymentMethod = "Аннуитентные платежи"
            )
        }

        val result = calc(viewModel)

        assertTrue(result[0].d.clean().contains("100000"))
        assertEquals(14, result.size)

        val totalRow = result.last()
        assertEquals("ИТОГО", totalRow.month)
        assertTrue(totalRow.y.clean().contains("10661"))
    }

    @Test
    fun `differentiated repayment calculation is correct`() {
        val viewModel = CreditDataViewModel(application).apply {
            creditData = CreditData(
                loanAmount = "100000",
                interestRate = "12",
                loanTerm = "12",
                selectedUnit = "Месяц",
                repaymentMethod = "Дифференцированные платежи"
            )
        }

        val result = calc(viewModel)

        assertEquals(14, result.size)
        
        val totalRow = result.last()
        assertEquals("ИТОГО", totalRow.month)
        assertTrue(totalRow.y.clean().contains("106500"))
    }
}
