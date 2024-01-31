package com.example.creditcalculator.service

import com.example.creditcalculator.model.CreditDataViewModel
import com.example.creditcalculator.model.CreditRepaymentData
import kotlin.math.pow

fun calc(creditViewModel: CreditDataViewModel): List<CreditRepaymentData> {
    val result = mutableListOf<CreditRepaymentData>()
    val creditData = creditViewModel.creditData
    val i: Double = creditData.interestRate.toDouble() / 100
    var d: Double = creditData.loanAmount.toDouble()

    val month: Int = if (creditData.selectedUnit == "Год") {
        creditData.loanTerm.toInt() * 12
    } else {
        creditData.loanTerm.toInt()
    }
    var procents = 0.0
    var sumProcents = 0.0
    var sumDolg = 0.0
    var dolg = 0.0
    var y = 0.0
    var ySum = 0.0

    if (creditData.repaymentMethod == "Аннуитентные платежи") {
        y = d * i / 12 / (1 - 1 / (1 + i / 12).pow(month))

        for (temp in 0..month) {
            val creditRepaymentData =
                CreditRepaymentData(temp.toString(), d.toString(), "-", "-", "-")
            if (temp > 0) {
                creditRepaymentData.y = y.toString()
                creditRepaymentData.procents = procents.toString()
                creditRepaymentData.dolg = dolg.toString()
            }
            procents = d * i / 12
            dolg = y - procents
            d -= (y - procents)
            sumProcents += procents
            sumDolg += dolg
            result.add(creditRepaymentData)
        }

        val lastRow = CreditRepaymentData(
            "ИТОГО",
            "",
            (y * month).toString(),
            sumProcents.toString(),
            sumDolg.toString()
        )
        result.add(lastRow)
    } else {
        dolg = d / (month)

        for (temp in 0..month) {
            val creditRepaymentData =
                CreditRepaymentData(temp.toString(), d.toString(), "-", "-", "-")
            if (temp > 0) {
                creditRepaymentData.y = y.toString()
                creditRepaymentData.procents = procents.toString()
                creditRepaymentData.dolg = dolg.toString()
            }
            procents = (d - (temp - 1) * d / (month)) * i / 12
            y = dolg + procents
            d -= dolg
            ySum += y
            sumProcents += procents
            result.add(creditRepaymentData)
        }
        val lastRow = CreditRepaymentData(
            "ИТОГО",
            "",
            (ySum).toString(),
            sumProcents.toString(),
            (dolg * month).toString()
        )
        result.add(lastRow)
    }

    return result
}

