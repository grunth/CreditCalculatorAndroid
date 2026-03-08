package com.example.creditcalculator.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.creditcalculator.model.CreditData
import com.example.creditcalculator.model.CreditDataViewModel
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWindow(navController: NavController, creditViewModel: CreditDataViewModel) {
    var creditData by remember { mutableStateOf(creditViewModel.creditData) }
    val units = listOf("Год", "Месяц")
    val repaymentMethods = listOf("Аннуитентные платежи", "Дифференцированные платежи")
    var showDialog by remember { mutableStateOf(false) }
    var expanded1 by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Кредитный калькулятор", fontWeight = FontWeight.Bold) }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    // Поле для ввода суммы кредита
                    OutlinedTextField(
                        value = creditData.loanAmount,
                        onValueChange = { 
                            if (it.all { char -> char.isDigit() }) {
                                creditData = creditData.copy(loanAmount = it)
                            }
                        },
                        label = { Text("Сумма кредита") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                        visualTransformation = ThousandsSeparatorTransformation()
                    )

                    // Поле для ввода процентной ставки
                    OutlinedTextField(
                        value = creditData.interestRate,
                        onValueChange = { creditData = creditData.copy(interestRate = it) },
                        label = { Text("Процентная ставка (%)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) }
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        // Поле для ввода срока кредита
                        OutlinedTextField(
                            value = creditData.loanTerm,
                            onValueChange = { creditData = creditData.copy(loanTerm = it) },
                            label = { Text("Срок") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                        )

                        Spacer(modifier = Modifier.width(8.dp))

                        //поле для выбора единиц измерения срока
                        ExposedDropdownMenuBox(
                            expanded = expanded1,
                            onExpandedChange = { expanded1 = !expanded1 },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = creditData.selectedUnit,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded1) },
                                modifier = Modifier.menuAnchor(),
                                label = { Text("Период") },
                            )

                            ExposedDropdownMenu(
                                expanded = expanded1,
                                onDismissRequest = { expanded1 = false }
                            ) {
                                units.forEach { item ->
                                    DropdownMenuItem(
                                        text = { Text(text = item) },
                                        onClick = {
                                            creditData = creditData.copy(selectedUnit = item)
                                            expanded1 = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    //поле для выбора способа погашения
                    ExposedDropdownMenuBox(
                        expanded = expanded2,
                        onExpandedChange = { expanded2 = !expanded2 },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = creditData.repaymentMethod,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded2) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            label = { Text("Способ погашения") },
                            leadingIcon = { Icon(Icons.Default.List, contentDescription = null) }
                        )

                        ExposedDropdownMenu(
                            expanded = expanded2,
                            onDismissRequest = { expanded2 = false }
                        ) {
                            repaymentMethods.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(text = item) },
                                    onClick = {
                                        creditData = creditData.copy(repaymentMethod = item)
                                        expanded2 = false
                                    }
                                )
                            }
                        }
                    }
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Кнопка "Очистить"
                OutlinedButton(
                    onClick = { creditData = CreditData() },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Очистить")
                }

                // Кнопка "Расчет"
                Button(
                    onClick = {
                        if (validateFields(creditData)) {
                            creditViewModel.creditData = creditData
                            navController.navigate("resultScreen")
                        } else {
                            showDialog = true
                        }
                    },
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Расчет")
                }
            }
            
            Spacer(modifier = Modifier.height(32.dp))
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Ошибка") },
            text = { Text("Пожалуйста, заполните все поля перед расчетом.") },
            confirmButton = {
                Button(onClick = { showDialog = false }) {
                    Text("ОК")
                }
            }
        )
    }
}

private fun validateFields(creditData: CreditData): Boolean {
    return creditData.loanAmount.isNotEmpty() &&
            creditData.interestRate.isNotEmpty() &&
            creditData.loanTerm.isNotEmpty() &&
            creditData.selectedUnit.isNotEmpty() &&
            creditData.repaymentMethod.isNotEmpty()
}

class ThousandsSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) return TransformedText(text, OffsetMapping.Identity)

        val symbols = DecimalFormatSymbols(Locale.US).apply {
            groupingSeparator = ' '
        }
        val formatter = DecimalFormat("#,###", symbols)
        val transformedText = try {
            formatter.format(originalText.toLong())
        } catch (e: Exception) {
            originalText
        }

        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val originalSub = originalText.substring(0, offset)
                val transformedSub = try {
                    formatter.format(originalSub.toLong())
                } catch (e: Exception) {
                    originalSub
                }
                return transformedSub.length
            }

            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val transformedSub = transformedText.substring(0, offset.coerceAtMost(transformedText.length))
                return transformedSub.replace(" ", "").length
            }
        }

        return TransformedText(AnnotatedString(transformedText), offsetMapping)
    }
}
