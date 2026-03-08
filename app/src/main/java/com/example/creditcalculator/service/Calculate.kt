package com.example.creditcalculator.service

import com.example.creditcalculator.model.CreditDataViewModel
import com.example.creditcalculator.model.CreditRepaymentData
import kotlin.math.pow

fun calc(creditViewModel: CreditDataViewModel): List<CreditRepaymentData> {
    val result = mutableListOf<CreditRepaymentData>()
    val creditData = creditViewModel.creditData
    
    val loanAmount = creditData.loanAmount.replace(" ", "").toDoubleOrNull() ?: 0.0
    val interestRate = creditData.interestRate.toDoubleOrNull() ?: 0.0
    val i = interestRate / 100.0
    val term = creditData.loanTerm.toDoubleOrNull() ?: 0.0
    
    val months = if (creditData.selectedUnit == "Год") (term * 12).toInt() else term.toInt()
    if (months <= 0) return result

    var balance = loanAmount
    var totalInterest = 0.0
    var totalPrincipal = 0.0

    // Начальное состояние (месяц 0)
    result.add(CreditRepaymentData("0", balance.toString(), "-", "-", "-"))

    if (creditData.repaymentMethod == "Аннуитентные платежи") {
        val monthlyRate = i / 12.0
        if (monthlyRate > 0) {
            val monthlyPayment = loanAmount * monthlyRate / (1 - (1 + monthlyRate).pow(-months))
            for (m in 1..months) {
                val interest = balance * monthlyRate
                var principal = monthlyPayment - interest
                
                // В последний месяц корректируем остаток, чтобы выйти в 0
                if (m == months) {
                    principal = balance
                }
                
                val payment = interest + principal
                balance -= principal
                
                totalInterest += interest
                totalPrincipal += principal
                
                result.add(CreditRepaymentData(
                    m.toString(),
                    balance.toString(),
                    payment.toString(),
                    interest.toString(),
                    principal.toString()
                ))
            }
        } else {
            // Если ставка 0%
            val monthlyPayment = loanAmount / months
            for (m in 1..months) {
                balance -= monthlyPayment
                totalPrincipal += monthlyPayment
                result.add(CreditRepaymentData(m.toString(), balance.toString(), monthlyPayment.toString(), "0", monthlyPayment.toString()))
            }
        }
    } else {
        // Дифференцированные платежи
        val monthlyPrincipal = loanAmount / months
        val monthlyRate = i / 12.0
        for (m in 1..months) {
            val interest = balance * monthlyRate
            val payment = monthlyPrincipal + interest
            
            balance -= monthlyPrincipal
            totalInterest += interest
            totalPrincipal += monthlyPrincipal
            
            result.add(CreditRepaymentData(
                m.toString(),
                balance.toString(),
                payment.toString(),
                interest.toString(),
                monthlyPrincipal.toString()
            ))
        }
    }

    // Итоговая строка
    result.add(CreditRepaymentData(
        "ИТОГО",
        "",
        (totalInterest + totalPrincipal).toString(),
        totalInterest.toString(),
        totalPrincipal.toString()
    ))
    
    return result
}
