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
    val monthlyIncome: String = "",
    val maxIncomePercent: String = "35",
    val rentInflation: String = "5",
    val propertyAppreciation: String = "4"
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
    val rawPrice: String = "", // Сохраняем цену с валютой для отображения
    val comment: String = ""
)

data class CustomSite(
    val name: String,
    val url: String
)

data class RentVsBuyResult(
    val breakEvenMonth: Int,
    val totalRentPaid: Double,
    val totalMortgagePaid: Double,
    val propertyValueAtEnd: Double,
    val yearlyDetails: List<RentVsBuyYearlyData> = emptyList()
)

data class RentVsBuyYearlyData(
    val year: Int,
    val rentPaid: Double,
    val mortgagePaid: Double,
    val propertyValue: Double,
    val remainingLoan: Double
)
