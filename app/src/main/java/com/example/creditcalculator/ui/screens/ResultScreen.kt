package com.example.creditcalculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.creditcalculator.model.CreditDataViewModel
import com.example.creditcalculator.service.calc

@Composable
fun ResultScreen(creditViewModel: CreditDataViewModel) {
    var creditData = creditViewModel.creditData

    val data = calc(creditViewModel);

    LazyColumn(
        Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Here is the header
        item {
            Row(Modifier.background(Color.Gray)) {
                TableCell(text = "Сумма кредита", weight = .25f)
                TableCell(text = creditData.loanAmount, weight = .25f)
                TableCell(text = "Процентная ставка", weight = .25f)
                TableCell(text = creditData.interestRate, weight = .25f)
            }
            Row(Modifier.background(Color.Gray)) {
                TableCell(text = "Срок погашения", weight = .25f)
                TableCell(text = creditData.loanTerm, weight = .25f)
                TableCell(text = "Способ погашения", weight = .25f)
                TableCell(text = creditData.repaymentMethod, weight = .25f)
            }
            Row(Modifier.background(Color.LightGray)) {
                TableCell(text = "Месяц", weight = .1f)
                TableCell(text = "Непогашенная сумма основного долга", weight = .225f)
                TableCell(text = "Сумма месячного погашенного взноса", weight = .225f)
                TableCell(text = "Процентные платежи", weight = .225f)
                TableCell(text = "Месячная выплата основного долга", weight = .225f)
            }
        }

        items(data) {
            Row(Modifier.fillMaxWidth()) {
                TableCell(text = it.month, weight = .1f)
                TableCell(text = it.d, weight = .2f)
                TableCell(text = it.y, weight = .2f)
                TableCell(text = it.procents, weight = .2f)
                TableCell(text = it.dolg, weight = .2f)
            }
        }
    }
}

@Composable
fun RowScope.TableCell(
    text: String,
    weight: Float
) {
    Text(
        text = text,
        Modifier
            .weight(weight)
            .padding(8.dp)
    )
}