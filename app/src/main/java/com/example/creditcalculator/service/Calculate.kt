package com.example.creditcalculator.service

import com.example.creditcalculator.model.CreditDataViewModel
import com.example.creditcalculator.model.CreditRepaymentData
import com.example.creditcalculator.model.RentVsBuyResult
import kotlin.math.pow

fun calc(creditViewModel: CreditDataViewModel): List<CreditRepaymentData> {
    val result = mutableListOf<CreditRepaymentData>()
    val creditData = creditViewModel.creditData
    
    val totalAmount = creditData.loanAmount.replace(" ", "").toDoubleOrNull() ?: 0.0
    val downPaymentValue = creditData.downPayment.replace(" ", "").toDoubleOrNull() ?: 0.0
    
    val downPaymentAmount = if (creditData.isDownPaymentPercent) {
        totalAmount * (downPaymentValue / 100.0)
    } else {
        downPaymentValue
    }
    
    val loanAmount = (totalAmount - downPaymentAmount).coerceAtLeast(0.0)
    
    val interestRate = creditData.interestRate.toDoubleOrNull() ?: 0.0
    val i = interestRate / 100.0
    val term = creditData.loanTerm.toDoubleOrNull() ?: 0.0
    
    val months = if (creditData.selectedUnit == "Год") (term * 12).toInt() else term.toInt()
    if (months <= 0 || loanAmount <= 0) return result

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

fun calculateRentVsBuy(creditViewModel: CreditDataViewModel): RentVsBuyResult? {
    val creditData = creditViewModel.creditData
    val totalAmount = creditData.loanAmount.replace(" ", "").toDoubleOrNull() ?: return null
    val rentInput = creditData.monthlyRent.toDoubleOrNull() ?: return null
    
    val downPaymentValue = creditData.downPayment.replace(" ", "").toDoubleOrNull() ?: 0.0
    val downPaymentAmount = if (creditData.isDownPaymentPercent) {
        totalAmount * (downPaymentValue / 100.0)
    } else {
        downPaymentValue
    }
    
    val loanAmount = (totalAmount - downPaymentAmount).coerceAtLeast(0.0)
    val interestRate = creditData.interestRate.toDoubleOrNull() ?: 0.0
    val i = interestRate / 100.0
    val termYears = if (creditData.selectedUnit == "Год") creditData.loanTerm.toDoubleOrNull() ?: 0.0 else (creditData.loanTerm.toDoubleOrNull() ?: 0.0) / 12.0
    val totalMonths = (termYears * 12).toInt()
    
    if (totalMonths <= 0) return null

    val monthlyRate = i / 12.0
    val mortgagePayment = if (monthlyRate > 0) {
        loanAmount * monthlyRate / (1 - (1 + monthlyRate).pow(-totalMonths))
    } else {
        loanAmount / totalMonths
    }

    val annualInflation = 0.05 // 5% инфляция аренды
    val annualAppreciation = 0.04 // 4% рост недв
    
    var currentRent = rentInput
    var totalRentPaid = 0.0
    var totalMortgagePaid = 0.0
    var currentBalance = loanAmount
    var currentPropertyValue = totalAmount
    
    var breakEvenMonth = -1

    for (m in 1..360) { // Считаем до 30 лет макс
        // Расходы на аренду
        totalRentPaid += currentRent
        if (m % 12 == 0) currentRent *= (1 + annualInflation)
        
        // Расходы на ипотеку и изменение капитала
        val interest = currentBalance * monthlyRate
        val principal = (mortgagePayment - interest).coerceAtMost(currentBalance)
        totalMortgagePaid += mortgagePayment
        currentBalance -= principal
        
        if (m % 12 == 0) currentPropertyValue *= (1 + annualAppreciation)
        
        // Капитал при покупке = Стоимость - Остаток долга - Потрачено на ипотеку
        val buyEquity = currentPropertyValue - currentBalance - totalMortgagePaid - downPaymentAmount
        // Капитал при аренде = -Потрачено на аренду
        val rentEquity = -totalRentPaid
        
        if (breakEvenMonth == -1 && buyEquity > rentEquity) {
            breakEvenMonth = m
        }
        
        if (m == totalMonths && m <= 360) {
             // закончили расчет на сроке кредита если он меньше 30 лет
        }
    }

    return RentVsBuyResult(
        breakEvenMonth = breakEvenMonth,
        totalRentPaid = totalRentPaid,
        totalMortgagePaid = totalMortgagePaid,
        propertyValueAtEnd = currentPropertyValue
    )
}

fun calculateMaxLoan(monthlyIncome: String, interestRate: String, termYears: Double): Double {
    val income = monthlyIncome.replace(" ", "").toDoubleOrNull() ?: 0.0
    val rate = interestRate.toDoubleOrNull() ?: 0.0
    if (income <= 0 || rate <= 0 || termYears <= 0) return 0.0
    
    val maxMonthlyPayment = income * 0.35 // 35% от дохода
    val monthlyRate = (rate / 100.0) / 12.0
    val totalMonths = termYears * 12
    
    return maxMonthlyPayment * (1 - (1 + monthlyRate).pow(-totalMonths)) / monthlyRate
}
