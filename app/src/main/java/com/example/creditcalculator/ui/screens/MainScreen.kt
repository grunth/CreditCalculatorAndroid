package com.example.creditcalculator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.creditcalculator.model.CreditData
import com.example.creditcalculator.model.CreditDataViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWindow(navController: NavController, creditViewModel: CreditDataViewModel) {
    var creditData by remember { mutableStateOf(creditViewModel.creditData) }
    var unitLabel by remember { mutableStateOf("Единица измерения") }
    val units = listOf("Год", "Месяц")
    var repaymentMethodsLabel by remember { mutableStateOf("Способ погашения") }
    val repaymentMethods = listOf("Аннуитентные платежи", "Дифференцированные платежи")
    var expanded1 by remember {
        mutableStateOf(false)
    }
    var expanded2 by remember {
        mutableStateOf(false)
    }
    Column(
        modifier = Modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Поле для ввода суммы кредита
        TextField(
            value = creditData.loanAmount,
            onValueChange = { creditData = creditData.copy(loanAmount = it) },
            label = { Text("Сумма кредита") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        // Поле для ввода процентной ставки
        TextField(
            value = creditData.interestRate,
            onValueChange = { creditData = creditData.copy(interestRate = it) },
            label = { Text("Процентная ставка") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        // Поле для ввода срока кредита
        TextField(
            value = creditData.loanTerm,
            onValueChange = { creditData = creditData.copy(loanTerm = it) },
            label = { Text("Срок кредита") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        //поле для выбора единиц измерения срока
        ExposedDropdownMenuBox(
            expanded = expanded1,
            onExpandedChange = {
                expanded1 = !expanded1
            }
        ) {
            TextField(
                value = creditData.selectedUnit,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded1) },
                modifier = Modifier.menuAnchor(),
                label = { Text("Единица измерения") },
            )

            ExposedDropdownMenu(
                expanded = expanded1,
                onDismissRequest = { expanded1 = false }
            ) {
                units.forEach() { item ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            creditData = creditData.copy(selectedUnit = item)
                            unitLabel = item
                            expanded1 = false
                        }
                    )
                }
            }
        }

        //поле для выбора способа погашения
        ExposedDropdownMenuBox(
            expanded = expanded2,
            onExpandedChange = {
                expanded2 = !expanded2
            }
        ) {
            TextField(
                value = creditData.repaymentMethod,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded2) },
                modifier = Modifier.menuAnchor(),
                label = { Text("Способ погашения") },
            )

            ExposedDropdownMenu(
                expanded = expanded2,
                onDismissRequest = { expanded2 = false }
            ) {
                repaymentMethods.forEach() { item ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            creditData = creditData.copy(repaymentMethod = item)
                            repaymentMethodsLabel = item
                            expanded2 = false
                        }
                    )
                }
            }
        }

        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // Кнопка "Очистить"
            Button(onClick = {
                creditData = CreditData()
            }) {
                Text("Очистить")
            }

            // Кнопка "Расчет"
            Button(onClick = {
                navController.navigate("resultScreen")
                creditViewModel.creditData = creditData
                //TODO Метод расчета кредита
            }) {
                Text("Расчет")
            }
        }
    }
}