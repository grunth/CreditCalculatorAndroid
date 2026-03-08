package com.example.creditcalculator.ui.screens

import android.content.Context
import android.print.PrintAttributes
import android.print.PrintManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.ExperimentalFoundationApi
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
import androidx.compose.material.icons.filled.Print
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.creditcalculator.model.CreditData
import com.example.creditcalculator.model.CreditDataViewModel
import com.example.creditcalculator.model.CreditRepaymentData
import com.example.creditcalculator.service.calc
import java.text.NumberFormat
import kotlin.math.roundToLong

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun ResultScreen(creditViewModel: CreditDataViewModel, onBackClick: () -> Unit) {
    val context = LocalContext.current
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
                },
                actions = {
                    IconButton(onClick = { printResults(context, creditData, data) }) {
                        Icon(Icons.Default.Print, contentDescription = "Печать")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                item {
                    Spacer(modifier = Modifier.height(16.dp))
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
                }

                stickyHeader {
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
                }

                itemsIndexed(tableData) { index, item ->
                    val bgColor = if (index % 2 == 0) Color.Transparent else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .background(bgColor)
                            .padding(vertical = 12.dp, horizontal = 4.dp)
                    ) {
                        TableCell(text = item.month, weight = 0.15f)
                        TableCell(text = formatValue(item.d), weight = 0.25f)
                        TableCell(text = formatValue(item.y), weight = 0.25f)
                        TableCell(text = formatValue(item.procents), weight = 0.15f)
                        TableCell(text = formatValue(item.dolg), weight = 0.2f)
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
                        TableCell(text = formatValue(item.d), weight = 0.25f, fontWeight = FontWeight.Bold)
                        TableCell(text = formatValue(item.y), weight = 0.25f, fontWeight = FontWeight.Bold)
                        TableCell(text = formatValue(item.procents), weight = 0.15f, fontWeight = FontWeight.Bold)
                        TableCell(text = formatValue(item.dolg), weight = 0.2f, fontWeight = FontWeight.Bold)
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
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

fun printResults(context: Context, creditData: CreditData, results: List<CreditRepaymentData>) {
    val webView = WebView(context)
    val integerFormatter = NumberFormat.getIntegerInstance()
    
    fun formatVal(value: String): String {
        return value.toDoubleOrNull()?.let { integerFormatter.format(it.roundToLong()) } ?: value
    }

    val htmlContent = StringBuilder()
    htmlContent.append("<html><head><style>")
    htmlContent.append("table { width: 100%; border-collapse: collapse; }")
    htmlContent.append("th, td { border: 1px solid black; padding: 8px; text-align: center; font-size: 12px; }")
    htmlContent.append("th { background-color: #f2f2f2; }")
    htmlContent.append(".summary { margin-bottom: 20px; }")
    htmlContent.append("</style></head><body>")
    
    htmlContent.append("<h1>Результаты расчета кредита</h1>")
    htmlContent.append("<div class='summary'>")
    htmlContent.append("<p><b>Сумма кредита:</b> ${formatVal(creditData.loanAmount)}</p>")
    htmlContent.append("<p><b>Процентная ставка:</b> ${creditData.interestRate}%</p>")
    htmlContent.append("<p><b>Срок:</b> ${creditData.loanTerm} ${creditData.selectedUnit}</p>")
    htmlContent.append("<p><b>Тип платежей:</b> ${creditData.repaymentMethod}</p>")
    htmlContent.append("</div>")

    htmlContent.append("<table><thead><tr>")
    htmlContent.append("<th>№</th><th>Остаток</th><th>Платеж</th><th>Проценты</th><th>Долг</th>")
    htmlContent.append("</tr></thead><tbody>")

    results.forEach { item ->
        val isTotal = item.month == "ИТОГО"
        val style = if (isTotal) "style='font-weight: bold; background-color: #e0e0e0;'" else ""
        htmlContent.append("<tr $style>")
        htmlContent.append("<td>${item.month}</td>")
        htmlContent.append("<td>${formatVal(item.d)}</td>")
        htmlContent.append("<td>${formatVal(item.y)}</td>")
        htmlContent.append("<td>${formatVal(item.procents)}</td>")
        htmlContent.append("<td>${formatVal(item.dolg)}</td>")
        htmlContent.append("</tr>")
    }

    htmlContent.append("</tbody></table>")
    htmlContent.append("</body></html>")

    webView.webViewClient = object : WebViewClient() {
        override fun onPageFinished(view: WebView, url: String) {
            val printManager = context.getSystemService(Context.PRINT_SERVICE) as PrintManager
            val printAdapter = webView.createPrintDocumentAdapter("CreditReport")
            val jobName = "Credit Calculation Report"
            printManager.print(jobName, printAdapter, PrintAttributes.Builder().build())
        }
    }

    webView.loadDataWithBaseURL(null, htmlContent.toString(), "text/HTML", "UTF-8", null)
}
