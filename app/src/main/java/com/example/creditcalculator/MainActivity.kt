package com.example.creditcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.creditcalculator.ui.theme.CreditCalculatorTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CreditCalculatorTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainWindow()
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWindow(modifier: Modifier = Modifier) {
    var loanAmount by remember { mutableStateOf("") }
    var interestRate by remember { mutableStateOf("") }
    var loanTerm by remember { mutableStateOf("") }
    var selectedUnit by remember { mutableStateOf("Год") }
    var unitLabel by remember { mutableStateOf("Единица измерения") }
    var selectedRepaymentMethod by remember { mutableStateOf("Аннуитентные платежи") }

    // Список значений для комбо-боксов
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
        modifier = modifier.padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        // Поле для ввода суммы кредита
        TextField(
            value = loanAmount,
            onValueChange = { loanAmount = it },
            label = { Text("Сумма кредита") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        // Поле для ввода процентной ставки
        TextField(
            value = interestRate,
            onValueChange = { interestRate = it },
            label = { Text("Процентная ставка") },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number)
        )

        // Поле для ввода срока кредита
        TextField(
            value = loanTerm,
            onValueChange = { loanTerm = it },
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
                value = unitLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded1) },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded1,
                onDismissRequest = { expanded1 = false }
            ) {
                units.forEach() { item ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            expanded1 = false
                        }
                    )
                }
            }
        }


        //поле для выбора срока погашения
        ExposedDropdownMenuBox(
            expanded = expanded2,
            onExpandedChange = {
                expanded2 = !expanded2
            }
        ) {
            TextField(
                value = repaymentMethodsLabel,
                onValueChange = {},
                readOnly = true,
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded2) },
                modifier = Modifier.menuAnchor()
            )

            ExposedDropdownMenu(
                expanded = expanded2,
                onDismissRequest = { expanded2 = false }
            ) {
                repaymentMethods.forEach() { item ->
                    DropdownMenuItem(
                        text = { Text(text = item) },
                        onClick = {
                            expanded2 = false
                        }
                    )
                }
            }
        }

        Column(
            modifier = modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            // ... (ваш предыдущий код)

            // Кнопка "Очистить"
            Button(onClick = {
                // Очистить все введенные данные
                loanAmount = ""
                interestRate = ""
                loanTerm = ""
                selectedUnit = "Год"
                unitLabel = "Единица измерения"
                selectedRepaymentMethod = "Аннуитентные платежи"
                repaymentMethodsLabel = "Способ погашения"
            }) {
                Text("Очистить")
            }

            // Кнопка "Расчет"
            Button(onClick = {
                // Ваш код для расчета на основе введенных данных
                // ...

                // Пример вывода результатов в консоль
                println("Сумма кредита: $loanAmount")
                println("Процентная ставка: $interestRate")
                println("Срок кредита: $loanTerm $selectedUnit")
                println("Способ погашения: $selectedRepaymentMethod")
            }) {
                Text("Расчет")
            }
        }


    }
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    CreditCalculatorTheme {
        MainWindow()
    }
}