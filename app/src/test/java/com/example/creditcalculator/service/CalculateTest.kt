package com.example.creditcalculator.service

import com.example.creditcalculator.model.CreditData
import com.example.creditcalculator.model.CreditDataViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class CalculateTest {

    private fun String.clean(): String {
        // Remove all whitespace characters (including non-breaking spaces)
        return this.replace("\\s".toRegex(), "").replace("\u00a0", "")
    }

    @Test
    fun `annuity repayment calculation is correct`() {
        val viewModel = CreditDataViewModel().apply {
            creditData = CreditData(
                loanAmount = "100000",
                interestRate = "12",
                loanTerm = "12",
                selectedUnit = "Месяц",
                repaymentMethod = "Аннуитентные платежи"
            )
        }

        val result = calc(viewModel)

        // Month 0: Just the initial amount. Cleaning to avoid locale issues with currency/spaces
        assertTrue(result[0].d.clean().contains("100000"))

        // Check if the number of rows is loanTerm + 1 (month 0) + 1 (TOTAL)
        assertEquals(14, result.size)

        val totalRow = result.last()
        assertEquals("ИТОГО", totalRow.month)
        
        // Total payment for 100k at 12% for 1 year is approx 106,619
        assertTrue(totalRow.y.clean().contains("10661"))
    }

    @Test
    fun `differentiated repayment calculation is correct`() {
        val viewModel = CreditDataViewModel().apply {
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

        // Total: 106500
        assertTrue(totalRow.y.clean().contains("106500"))
    }
}
