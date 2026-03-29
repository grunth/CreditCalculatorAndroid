package com.example.creditcalculator.model

data class CreditData(
    val loanAmount: String = "",
    val downPayment: String = "0",
    val isDownPaymentPercent: Boolean = true,
    val interestRate: String = "",
    val loanTerm: String = "",
    val selectedUnit: String = "Год",
    val repaymentMethod: String = "Аннуитентные платежи",
    val monthlyRent: String = "",
    val monthlyIncome: String = ""
)

data class CreditRepaymentData(
    val month: String,
    val d: String,
    val y: String,
    val procents: String,
    val dolg: String
)

data class SavedProperty(
    val title: String,
    val url: String,
    val price: String = "",
    val siteName: String = "",
    val rawPrice: String = "" // Сохраняем цену с валютой для отображения
)

data class CustomSite(
    val name: String,
    val url: String
)

data class RentVsBuyResult(
    val breakEvenMonth: Int,
    val totalRentPaid: Double,
    val totalMortgagePaid: Double,
    val propertyValueAtEnd: Double
)
