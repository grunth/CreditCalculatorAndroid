package com.example.creditcalculator.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.creditcalculator.model.CreditDataViewModel
import com.example.creditcalculator.service.calc
import java.text.NumberFormat
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ResultScreen(creditViewModel: CreditDataViewModel, onBackClick: () -> Unit) {
    val creditData = creditViewModel.creditData
    val data = calc(creditViewModel)
    
    val tableData = data.filter { it.month != "ИТОГО" }
    val footerRow = data.find { it.month == "ИТОГО" }

    val integerFormatter = NumberFormat.getIntegerInstance()

    fun formatValue(value: String): String {
        return value.toDoubleOrNull()?.let { integerFormatter.format(it.roundToLong()) } ?: value
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Результаты расчета", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
        ) {
            // Summary Card
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.secondaryContainer
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SummaryItem(label = "Сумма:", value = formatValue(creditData.loanAmount))
                        SummaryItem(label = "Ставка:", value = "${creditData.interestRate}%")
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        SummaryItem(label = "Срок:", value = "${creditData.loanTerm} ${creditData.selectedUnit}")
                        SummaryItem(label = "Тип:", value = if(creditData.repaymentMethod.startsWith("Аннуи")) "Анн." else "Дифф.")
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "График платежей",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            // Table Header
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = MaterialTheme.shapes.small,
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(vertical = 12.dp, horizontal = 4.dp)
                ) {
                    TableHeaderCell(text = "№", weight = 0.15f)
                    TableHeaderCell(text = "Остаток", weight = 0.25f)
                    TableHeaderCell(text = "Платеж", weight = 0.25f)
                    TableHeaderCell(text = "%", weight = 0.15f)
                    TableHeaderCell(text = "Долг", weight = 0.2f)
                }
            }

            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                itemsIndexed(tableData) { index, item ->
                    val bgColor = if (index % 2 == 0) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(bgColor)
                            .padding(vertical = 12.dp, horizontal = 4.dp)
                    ) {
                        TableCell(text = item.month, weight = 0.15f)
                        TableCell(text = item.d, weight = 0.25f)
                        TableCell(text = item.y, weight = 0.25f)
                        TableCell(text = item.procents, weight = 0.15f)
                        TableCell(text = item.dolg, weight = 0.2f)
                    }
                    Divider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 0.5.dp)
                }
            }
            
            // Sticky Footer (Total)
            footerRow?.let { item ->
                Spacer(modifier = Modifier.height(8.dp))
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = MaterialTheme.shapes.small,
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)
                ) {
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp, horizontal = 4.dp)
                    ) {
                        TableCell(text = item.month, weight = 0.15f, fontWeight = FontWeight.Bold)
                        TableCell(text = item.d, weight = 0.25f, fontWeight = FontWeight.Bold)
                        TableCell(text = item.y, weight = 0.25f, fontWeight = FontWeight.Bold)
                        TableCell(text = item.procents, weight = 0.15f, fontWeight = FontWeight.Bold)
                        TableCell(text = item.dolg, weight = 0.2f, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
fun SummaryItem(label: String, value: String) {
    Row {
        Text(text = label, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSecondaryContainer)
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = value, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.onSecondaryContainer)
    }
}

@Composable
fun RowScope.TableHeaderCell(text: String, weight: Float) {
    Text(
        text = text,
        modifier = Modifier.width(0.dp).weight(weight),
        fontWeight = FontWeight.Bold,
        fontSize = 12.sp,
        textAlign = TextAlign.Center
    )
}

@Composable
fun RowScope.TableCell(text: String, weight: Float, fontWeight: FontWeight = FontWeight.Normal) {
    Text(
        text = text,
        modifier = Modifier.width(0.dp).weight(weight),
        fontSize = 12.sp,
        fontWeight = fontWeight,
        textAlign = TextAlign.Center
    )
}
