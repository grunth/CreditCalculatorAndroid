package com.example.creditcalculator.service

import com.example.creditcalculator.model.CreditDataViewModel
import com.example.creditcalculator.model.CreditRepaymentData
import java.lang.Math.abs
import java.text.NumberFormat
import kotlin.math.pow
import kotlin.math.round

fun calc(creditViewModel: CreditDataViewModel): List<CreditRepaymentData> {
    val result = mutableListOf<CreditRepaymentData>()
    val creditData = creditViewModel.creditData
    val i: Double = creditData.interestRate.toDouble() / 100
    var d: Double = creditData.loanAmount.toDouble()

    val numberFormatter = NumberFormat.getCurrencyInstance()

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
                CreditRepaymentData(
                    temp.toString(),
                    numberFormatter.format(round(abs(d))).toString(),
                    "-",
                    "-",
                    "-"
                )
            if (temp > 0) {
                creditRepaymentData.y = numberFormatter.format(round(y)).toString()
                creditRepaymentData.procents = numberFormatter.format(round(procents)).toString()
                creditRepaymentData.dolg = numberFormatter.format(round(dolg)).toString()
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
            numberFormatter.format(round(y * month)).toString(),
            numberFormatter.format(round(sumProcents)).toString(),
            numberFormatter.format(round(sumDolg)).toString()
        )
        result.add(lastRow)
    } else {
        dolg = d / (month)

        for (temp in 0..month) {
            val creditRepaymentData =
                CreditRepaymentData(
                    temp.toString(),
                    numberFormatter.format(round(abs(d))).toString(),
                    "-",
                    "-",
                    "-"
                )
            if (temp > 0) {
                creditRepaymentData.y = numberFormatter.format(round(y)).toString()
                creditRepaymentData.procents = numberFormatter.format(round(procents)).toString()
                creditRepaymentData.dolg = numberFormatter.format(round(dolg)).toString()
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
            numberFormatter.format(round(ySum)).toString(),
            numberFormatter.format(round(sumProcents)).toString(),
            numberFormatter.format(round(dolg * month)).toString()
        )
        result.add(lastRow)
    }
    return result
}

