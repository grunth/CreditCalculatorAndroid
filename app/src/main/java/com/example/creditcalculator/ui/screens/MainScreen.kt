package com.example.creditcalculator.ui.screens

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.creditcalculator.model.CreditData
import com.example.creditcalculator.model.CreditDataViewModel
import com.example.creditcalculator.model.CustomSite
import com.example.creditcalculator.model.SavedProperty
import java.net.URLEncoder
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWindow(navController: NavController, viewModel: CreditDataViewModel) {
    val units = listOf("Год", "Месяц")
    val repaymentMethods = listOf("Аннуитентные платежи", "Дифференцированные платежи")
    
    var expanded1 by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }
    
    var showSiteMenu by remember { mutableStateOf(false) }
    var showAddSiteDialog by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    
    var propertyToEdit by remember { mutableStateOf<SavedProperty?>(null) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Кредитный калькулятор", fontWeight = FontWeight.Bold) },
                actions = {
                    Box {
                        IconButton(onClick = { showSiteMenu = true }) {
                            Icon(Icons.Default.Search, contentDescription = "Поиск")
                        }
                        DropdownMenu(expanded = showSiteMenu, onDismissRequest = { showSiteMenu = false }) {
                            viewModel.customSites.forEach { site ->
                                DropdownMenuItem(
                                    text = { 
                                        Row(
                                            modifier = Modifier.fillMaxWidth(),
                                            horizontalArrangement = Arrangement.SpaceBetween,
                                            verticalAlignment = Alignment.CenterVertically
                                        ) {
                                            Text(site.name, modifier = Modifier.weight(1f))
                                            // Кнопка удаления сайта (не для стандартных)
                                            if (site.name != "OLX" && site.name != "Otodom") {
                                                IconButton(
                                                    onClick = { viewModel.removeCustomSite(site) },
                                                    modifier = Modifier.height(24.dp).width(24.dp)
                                                ) {
                                                    Icon(
                                                        Icons.Default.Delete, 
                                                        contentDescription = null, 
                                                        tint = Color.Red.copy(alpha = 0.5f),
                                                        modifier = Modifier.fillMaxSize()
                                                    )
                                                }
                                            }
                                        }
                                    },
                                    onClick = {
                                        showSiteMenu = false
                                        val encodedUrl = URLEncoder.encode(site.url, "UTF-8")
                                        navController.navigate("olxScreen/$encodedUrl")
                                    }
                                )
                            }
                            Divider()
                            DropdownMenuItem(
                                text = { Text("Добавить сайт...", fontWeight = FontWeight.Bold) },
                                leadingIcon = { Icon(Icons.Default.Add, contentDescription = null) },
                                onClick = {
                                    showSiteMenu = false
                                    showAddSiteDialog = true
                                }
                            )
                        }
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
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Сохраненные объекты (Репозиторий)
            if (viewModel.savedProperties.isNotEmpty()) {
                Text(
                    text = "Сохраненные объекты", 
                    style = MaterialTheme.typography.titleSmall, 
                    modifier = Modifier.fillMaxWidth()
                )
                Card(
                    modifier = Modifier.fillMaxWidth(), 
                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                ) {
                    Column(modifier = Modifier.padding(8.dp)) {
                        viewModel.savedProperties.forEach { prop ->
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clickable {
                                        viewModel.creditData = viewModel.creditData.copy(loanAmount = prop.price)
                                    }
                                    .padding(vertical = 8.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        if (prop.siteName.isNotEmpty()) {
                                            Surface(
                                                color = MaterialTheme.colorScheme.primaryContainer,
                                                shape = MaterialTheme.shapes.extraSmall,
                                                modifier = Modifier.padding(end = 6.dp)
                                            ) {
                                                Text(
                                                    text = prop.siteName,
                                                    style = MaterialTheme.typography.labelSmall,
                                                    modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp),
                                                    fontSize = 10.sp
                                                )
                                            }
                                        }
                                        Text(prop.title, maxLines = 1, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    }
                                    Text(
                                        text = prop.price, 
                                        color = MaterialTheme.colorScheme.primary,
                                        modifier = Modifier.clickable { propertyToEdit = prop }
                                    )
                                }
                                
                                Row {
                                    IconButton(onClick = {
                                        viewModel.creditData = viewModel.creditData.copy(loanAmount = prop.price)
                                        val encodedUrl = URLEncoder.encode(prop.url, "UTF-8")
                                        navController.navigate("olxScreen/$encodedUrl")
                                    }) {
                                        Icon(Icons.Default.Language, contentDescription = "Открыть сайт", tint = MaterialTheme.colorScheme.primary)
                                    }
                                    IconButton(onClick = { viewModel.removeSavedProperty(prop) }) {
                                        Icon(Icons.Default.Delete, contentDescription = "Удалить", tint = Color.Red.copy(alpha = 0.6f))
                                    }
                                }
                            }
                            Divider()
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = viewModel.creditData.loanAmount,
                        onValueChange = { 
                            if (it.all { char -> char.isDigit() }) {
                                viewModel.creditData = viewModel.creditData.copy(loanAmount = it)
                            }
                        },
                        label = { Text("Сумма кредита") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.Info, contentDescription = null) },
                        visualTransformation = ThousandsSeparatorTransformation()
                    )

                    OutlinedTextField(
                        value = viewModel.creditData.interestRate,
                        onValueChange = { viewModel.creditData = viewModel.creditData.copy(interestRate = it) },
                        label = { Text("Процентная ставка (%)") },
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.Refresh, contentDescription = null) }
                    )

                    Row(modifier = Modifier.fillMaxWidth()) {
                        OutlinedTextField(
                            value = viewModel.creditData.loanTerm,
                            onValueChange = { viewModel.creditData = viewModel.creditData.copy(loanTerm = it) },
                            label = { Text("Срок") },
                            modifier = Modifier.weight(1f),
                            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
                            leadingIcon = { Icon(Icons.Default.DateRange, contentDescription = null) }
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        ExposedDropdownMenuBox(
                            expanded = expanded1,
                            onExpandedChange = { expanded1 = !expanded1 },
                            modifier = Modifier.weight(1f)
                        ) {
                            OutlinedTextField(
                                value = viewModel.creditData.selectedUnit,
                                onValueChange = {},
                                readOnly = true,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded1) },
                                modifier = Modifier.menuAnchor(),
                                label = { Text("Период") },
                            )
                            ExposedDropdownMenu(expanded = expanded1, onDismissRequest = { expanded1 = false }) {
                                units.forEach { item ->
                                    DropdownMenuItem(text = { Text(text = item) }, onClick = {
                                        viewModel.creditData = viewModel.creditData.copy(selectedUnit = item)
                                        expanded1 = false
                                    })
                                }
                            }
                        }
                    }

                    ExposedDropdownMenuBox(
                        expanded = expanded2,
                        onExpandedChange = { expanded2 = !expanded2 },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        OutlinedTextField(
                            value = viewModel.creditData.repaymentMethod,
                            onValueChange = {},
                            readOnly = true,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded2) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            label = { Text("Способ погашения") },
                            leadingIcon = { Icon(Icons.Default.List, contentDescription = null) }
                        )
                        ExposedDropdownMenu(expanded = expanded2, onDismissRequest = { expanded2 = false }) {
                            repaymentMethods.forEach { item ->
                                DropdownMenuItem(text = { Text(text = item) }, onClick = {
                                    viewModel.creditData = viewModel.creditData.copy(repaymentMethod = item)
                                    expanded2 = false
                                })
                            }
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                OutlinedButton(onClick = { 
                    viewModel.creditData = CreditData()
                }, modifier = Modifier.weight(1f)) {
                    Icon(Icons.Default.Clear, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Очистить")
                }
                Button(
                    onClick = {
                        if (validateFields(viewModel.creditData)) {
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
        }
    }

    if (propertyToEdit != null) {
        var editPrice by remember { mutableStateOf(propertyToEdit?.price ?: "") }
        AlertDialog(
            onDismissRequest = { propertyToEdit = null },
            title = { Text("Изменить цену") },
            text = {
                OutlinedTextField(
                    value = editPrice, 
                    onValueChange = { if (it.all { c -> c.isDigit() }) editPrice = it }, 
                    label = { Text("Цена") },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                )
            },
            confirmButton = {
                Button(onClick = {
                    propertyToEdit?.let { viewModel.updatePropertyPrice(it.url, editPrice) }
                    propertyToEdit = null
                }) { Text("Сохранить") }
            },
            dismissButton = { OutlinedButton(onClick = { propertyToEdit = null }) { Text("Отмена") } }
        )
    }

    if (showAddSiteDialog) {
        var newName by remember { mutableStateOf("") }
        var newUrl by remember { mutableStateOf("https://") }
        AlertDialog(
            onDismissRequest = { showAddSiteDialog = false },
            title = { Text("Добавить сайт") },
            text = {
                Column {
                    OutlinedTextField(value = newName, onValueChange = { newName = it }, label = { Text("Название") }, modifier = Modifier.fillMaxWidth())
                    OutlinedTextField(value = newUrl, onValueChange = { newUrl = it }, label = { Text("URL") }, modifier = Modifier.fillMaxWidth())
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newName.isNotEmpty() && newUrl.length > 8) {
                        viewModel.addCustomSite(CustomSite(newName, newUrl))
                        showAddSiteDialog = false
                    }
                }) { Text("Добавить") }
            },
            dismissButton = { OutlinedButton(onClick = { showAddSiteDialog = false }) { Text("Отмена") } }
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text("Ошибка") },
            text = { Text("Пожалуйста, заполните все поля перед расчетом.") },
            confirmButton = { Button(onClick = { showDialog = false }) { Text("ОК") } }
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
