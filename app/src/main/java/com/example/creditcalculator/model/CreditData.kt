package com.example.creditcalculator.model

import androidx.lifecycle.ViewModel


class CreditDataViewModel : ViewModel() {
    var creditData: CreditData = CreditData()
}

data class CreditData(
    var loanAmount: String = "",
    var interestRate: String = "",
    var loanTerm: String = "",
    var selectedUnit: String = "Год",
    var repaymentMethod: String = "Аннуитентные платежи"
)

data class CreditRepaymentData(
    var month: String = "",
    var d: String = "",
    var y: String = "",
    var procents: String = "",
    var dolg: String = ""
)
