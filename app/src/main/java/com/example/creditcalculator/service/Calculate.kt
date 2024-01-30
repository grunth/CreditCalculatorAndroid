package com.example.creditcalculator.service

import com.example.creditcalculator.model.CreditDataViewModel
import com.example.creditcalculator.model.CreditRepaymentData

fun calc(creditViewModel: CreditDataViewModel): List<CreditRepaymentData> {
    val result = mutableListOf<CreditRepaymentData>()
    var creditData = creditViewModel.creditData
    val month: Int
    val y: Float

    if (creditData.selectedUnit == "Год") {
        month = creditData.loanTerm.toInt() * 12
    } else {
        month = creditData.loanTerm.toInt()
    }

    for (temp in 0..month) {
        val creditRepaymentData = CreditRepaymentData(temp, 12, 13, 14, 15)
        result.add(creditRepaymentData)
    }

    return result
}

