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
    var month: Int = 0,
    var d: Int = 0,
    var y: Int = 0,
    var procents: Int = 0,
    var dolg: Int = 0
)
