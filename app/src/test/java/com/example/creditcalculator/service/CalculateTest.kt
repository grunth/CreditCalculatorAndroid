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
        return this.replace("\\s".toRegex(), "").replace("\u00a0", "").replace(",", ".")
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

        // Первая строка (начальный баланс)
        assertTrue(result[0].d.clean().toDouble() == 100000.0)
        
        // 12 месяцев + начальное состояние + итоговая строка = 14
        assertEquals(14, result.size)

        val totalRow = result.last()
        assertEquals("ИТОГО", totalRow.month)
        // Общая сумма выплат при 12% годовых на 100000 на 12 месяцев (аннуитет) ~ 106618.55
        assertTrue(totalRow.y.clean().toDouble() > 106000.0)
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
        // Дифф: 100000 + (12% от среднего остатка). 100000 + 6500 = 106500
        assertEquals(106500.0, totalRow.y.clean().toDouble(), 0.1)
    }
}
