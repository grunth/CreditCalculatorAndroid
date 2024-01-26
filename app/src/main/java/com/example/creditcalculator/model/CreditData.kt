package com.example.creditcalculator.model

data class CreditData(
    var loanAmount: String = "",
    var interestRate: String = "",
    var loanTerm: String = "",
    var selectedUnit: String = "Год",
    var repaymentMethod: String = "Аннуитентные платежи"
)
