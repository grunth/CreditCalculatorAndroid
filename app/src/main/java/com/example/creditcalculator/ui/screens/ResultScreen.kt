package com.example.creditcalculator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment.Companion.CenterHorizontally
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.creditcalculator.model.CreditDataViewModel

@Composable
fun ResultScreen(creditViewModel: CreditDataViewModel) {
    val creditData = creditViewModel.creditData
    println(creditData)
    val resultText by remember { mutableStateOf("Результат расчета") }
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = resultText, modifier = Modifier.align(CenterHorizontally))
    }
}