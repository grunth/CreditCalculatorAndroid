package com.example.creditcalculator.ui.screens

import android.content.Context
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.AddCircleOutline
import androidx.compose.material.icons.filled.Apartment
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Done
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.HomeWork
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Insights
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Savings
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timelapse
import androidx.compose.material.icons.filled.TravelExplore
import androidx.compose.material.icons.filled.Wallet
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExposedDropdownMenuBox
import androidx.compose.material3.ExposedDropdownMenuDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.creditcalculator.R
import com.example.creditcalculator.model.CreditData
import com.example.creditcalculator.model.CreditDataViewModel
import com.example.creditcalculator.model.CustomSite
import com.example.creditcalculator.service.calculateMaxLoan
import com.example.creditcalculator.service.calculateRentVsBuy
import kotlinx.coroutines.launch
import java.net.URLEncoder
import java.text.DecimalFormat
import java.text.DecimalFormatSymbols
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainWindow(navController: NavController, viewModel: CreditDataViewModel) {
    val context = LocalContext.current
    val units = listOf(stringResource(R.string.year), stringResource(R.string.month))
    val methods = listOf(stringResource(R.string.annuity_payment), stringResource(R.string.diff_payment))
    val inputTextStyle = TextStyle(fontSize = 16.sp)
    
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()
    
    // Анимация для логотипа
    val logoInteractionSource = remember { MutableInteractionSource() }
    val isPressed by logoInteractionSource.collectIsPressedAsState()
    val logoScale by animateFloatAsState(
        targetValue = if (isPressed) 0.92f else 1f,
        animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy),
        label = "logoScale"
    )
    
    var expanded1 by remember { mutableStateOf(false) }
    var expanded2 by remember { mutableStateOf(false) }
    
    var showSiteMenu by remember { mutableStateOf(false) }
    var showSettingsMenu by remember { mutableStateOf(false) }
    var showAddSiteDialog by remember { mutableStateOf(false) }
    var showDialog by remember { mutableStateOf(false) }
    
    var showRentCalc by remember { mutableStateOf(false) }
    var showIncomeCalc by remember { mutableStateOf(false) }
    var showInfoDialog by remember { mutableStateOf<String?>(null) }
    var showCommentDialog by remember { mutableStateOf<com.example.creditcalculator.model.SavedProperty?>(null) }
    var editedComment by remember { mutableStateOf("") }

    var newSiteName by remember { mutableStateOf("") }
    var newSiteUrl by remember { mutableStateOf("") }

    val displayUnit = when(viewModel.creditData.selectedUnit) {
        "Год", "Year" -> stringResource(R.string.year)
        "Месяц", "Month" -> stringResource(R.string.month)
        else -> viewModel.creditData.selectedUnit
    }

    val displayMethod = if (viewModel.creditData.repaymentMethod.startsWith("Аннуи") || viewModel.creditData.repaymentMethod.startsWith("Annu")) {
        stringResource(R.string.annuity_payment)
    } else {
        stringResource(R.string.diff_payment)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { 
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .graphicsLayer(scaleX = logoScale, scaleY = logoScale)
                            .clickable(
                                interactionSource = logoInteractionSource,
                                indication = null
                            ) {
                                scope.launch {
                                    scrollState.animateScrollTo(0)
                                }
                            }
                    ) {
                        Icon(
                            painter = painterResource(id = R.drawable.ic_logo),
                            contentDescription = null,
                            tint = Color.Unspecified,
                            modifier = Modifier.size(32.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Text(
                            text = "Mortgage Lab",
                            style = TextStyle(
                                brush = Brush.linearGradient(
                                    colors = listOf(MaterialTheme.colorScheme.primary, MaterialTheme.colorScheme.tertiary)
                                ),
                                fontSize = 22.sp,
                                fontWeight = FontWeight.ExtraBold,
                                letterSpacing = 1.sp
                            )
                        )
                    }
                },
                actions = {
                    IconButton(onClick = { showSiteMenu = true }) {
                        Icon(Icons.Default.TravelExplore, contentDescription = stringResource(R.string.search), tint = MaterialTheme.colorScheme.primary)
                    }
                    DropdownMenu(expanded = showSiteMenu, onDismissRequest = { showSiteMenu = false }) {
                        viewModel.customSites.forEach { site ->
                            DropdownMenuItem(
                                text = { Text(site.name) },
                                onClick = {
                                    showSiteMenu = false
                                    val encodedUrl = URLEncoder.encode(site.url, "UTF-8")
                                    navController.navigate("browserScreen/$encodedUrl")
                                },
                                trailingIcon = {
                                    if (site.name != "OLX" && site.name != "Otodom") {
                                        IconButton(onClick = { viewModel.removeCustomSite(site) }) {
                                            Icon(Icons.Default.Close, contentDescription = null, modifier = Modifier.size(18.dp), tint = Color.Red.copy(alpha = 0.5f))
                                        }
                                    }
                                }
                            )
                        }
                        Divider(modifier = Modifier.padding(vertical = 4.dp))
                        DropdownMenuItem(
                            text = { Text(stringResource(R.string.add_site), fontWeight = FontWeight.Bold) },
                            leadingIcon = { Icon(Icons.Default.AddCircleOutline, contentDescription = null) },
                            onClick = { 
                                showSiteMenu = false
                                newSiteName = ""
                                newSiteUrl = ""
                                showAddSiteDialog = true 
                            }
                        )
                    }

                    IconButton(onClick = { showSettingsMenu = true }) {
                        Icon(Icons.Default.Settings, contentDescription = stringResource(R.string.settings), tint = MaterialTheme.colorScheme.primary)
                    }
                    DropdownMenu(expanded = showSettingsMenu, onDismissRequest = { showSettingsMenu = false }) {
                        DropdownMenuItem(
                            text = { Text("English") },
                            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                            onClick = { 
                                showSettingsMenu = false
                                updateLocale(context, "en")
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Русский") },
                            leadingIcon = { Icon(Icons.Default.Language, contentDescription = null) },
                            onClick = { 
                                showSettingsMenu = false
                                updateLocale(context, "ru")
                            }
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 20.dp)
                .verticalScroll(scrollState),
            verticalArrangement = Arrangement.spacedBy(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            if (viewModel.savedProperties.isNotEmpty()) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.my_options), 
                        style = MaterialTheme.typography.titleMedium, 
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.fillMaxWidth()
                    )
                    viewModel.savedProperties.forEach { prop ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)),
                            onClick = { viewModel.creditData = viewModel.creditData.copy(loanAmount = prop.price) }
                        ) {
                            Row(
                                modifier = Modifier.padding(12.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(RoundedCornerShape(10.dp))
                                        .background(MaterialTheme.colorScheme.primaryContainer),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(Icons.Default.HomeWork, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimaryContainer)
                                }
                                Column(modifier = Modifier.weight(1f).padding(horizontal = 12.dp)) {
                                    Text(prop.title, maxLines = 1, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                                    val displayPrice = prop.rawPrice.ifEmpty { prop.price }
                                    Text(displayPrice, color = MaterialTheme.colorScheme.primary, style = MaterialTheme.typography.bodySmall)
                                    if (prop.comment.isNotEmpty()) {
                                        Text(prop.comment, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f), maxLines = 1, overflow = TextOverflow.Ellipsis)
                                    }
                                }
                                
                                IconButton(onClick = {
                                    editedComment = prop.comment
                                    showCommentDialog = prop
                                }) {
                                    Icon(Icons.Default.Info, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                                }

                                IconButton(onClick = {
                                    val encodedUrl = URLEncoder.encode(prop.url, "UTF-8")
                                    navController.navigate("browserScreen/$encodedUrl")
                                }) {
                                    Icon(Icons.Default.TravelExplore, contentDescription = null, tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f))
                                }

                                IconButton(onClick = { viewModel.removeSavedProperty(prop) }) {
                                    Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = MaterialTheme.colorScheme.error.copy(alpha = 0.6f))
                                }
                            }
                        }
                    }
                }
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                elevation = CardDefaults.cardElevation(defaultElevation = 0.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    OutlinedTextField(
                        value = viewModel.creditData.loanAmount,
                        onValueChange = { if (it.all { c -> c.isDigit() }) viewModel.creditData = viewModel.creditData.copy(loanAmount = it) },
                        label = { Text(stringResource(R.string.property_cost), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        textStyle = inputTextStyle,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.AccountBalanceWallet, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                        visualTransformation = ThousandsSeparatorTransformation()
                    )

                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = viewModel.creditData.downPayment,
                            onValueChange = { input ->
                                if (input.all { c -> c.isDigit() }) {
                                    val total = viewModel.creditData.loanAmount.toDoubleOrNull() ?: 0.0
                                    if (viewModel.creditData.isDownPaymentPercent) {
                                        if ((input.toDoubleOrNull() ?: 0.0) <= 100) viewModel.creditData = viewModel.creditData.copy(downPayment = input)
                                    } else {
                                        if ((input.toDoubleOrNull() ?: 0.0) <= total) viewModel.creditData = viewModel.creditData.copy(downPayment = input)
                                    }
                                }
                            },
                            label = { Text(stringResource(R.string.down_payment), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            textStyle = inputTextStyle,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = { Icon(Icons.Default.Savings, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            visualTransformation = if (viewModel.creditData.isDownPaymentPercent) VisualTransformation.None else ThousandsSeparatorTransformation()
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Button(
                            onClick = { viewModel.creditData = viewModel.creditData.copy(isDownPaymentPercent = !viewModel.creditData.isDownPaymentPercent, downPayment = "0") },
                            modifier = Modifier.height(56.dp).padding(top = 8.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondaryContainer, contentColor = MaterialTheme.colorScheme.onSecondaryContainer)
                        ) {
                            Text(if (viewModel.creditData.isDownPaymentPercent) "%" else "$", fontWeight = FontWeight.Bold)
                        }
                    }

                    OutlinedTextField(
                        value = viewModel.creditData.interestRate,
                        onValueChange = { viewModel.creditData = viewModel.creditData.copy(interestRate = it) },
                        label = { Text(stringResource(R.string.interest_rate), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp),
                        singleLine = true,
                        textStyle = inputTextStyle,
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        leadingIcon = { Icon(Icons.Default.AutoGraph, contentDescription = null, tint = MaterialTheme.colorScheme.primary) }
                    )

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = viewModel.creditData.loanTerm,
                            onValueChange = { if (it.all { c -> c.isDigit() }) viewModel.creditData = viewModel.creditData.copy(loanTerm = it) },
                            label = { Text(stringResource(R.string.loan_term), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.weight(1f),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            textStyle = inputTextStyle,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = { Icon(Icons.Default.Timelapse, contentDescription = null, tint = MaterialTheme.colorScheme.primary) },
                            trailingIcon = {
                                Box(modifier = Modifier.padding(end = 4.dp).size(24.dp).clickable { showInfoDialog = "loanTerm" }, contentAlignment = Alignment.Center) {
                                    Icon(Icons.Default.HelpOutline, contentDescription = null, tint = MaterialTheme.colorScheme.outline, modifier = Modifier.size(18.dp))
                                }
                            }
                        )
                        
                        ExposedDropdownMenuBox(
                            expanded = expanded1,
                            onExpandedChange = { expanded1 = !expanded1 },
                            modifier = Modifier.weight(0.8f)
                        ) {
                            OutlinedTextField(
                                value = displayUnit,
                                onValueChange = {},
                                readOnly = true,
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                textStyle = inputTextStyle,
                                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded1) },
                                modifier = Modifier.menuAnchor().fillMaxWidth(),
                                label = { Text(stringResource(R.string.period), maxLines = 1, overflow = TextOverflow.Ellipsis) }
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
                            value = displayMethod,
                            onValueChange = {},
                            readOnly = true,
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            textStyle = inputTextStyle,
                            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded2) },
                            modifier = Modifier.menuAnchor().fillMaxWidth(),
                            label = { Text(stringResource(R.string.repayment_method), maxLines = 1, overflow = TextOverflow.Ellipsis) }
                        )
                        ExposedDropdownMenu(expanded = expanded2, onDismissRequest = { expanded2 = false }) {
                            methods.forEach { item ->
                                DropdownMenuItem(text = { Text(text = item) }, onClick = {
                                    viewModel.creditData = viewModel.creditData.copy(repaymentMethod = item)
                                    expanded2 = false
                                })
                            }
                        }
                    }

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        FilterChip(
                            selected = showRentCalc,
                            onClick = { showRentCalc = !showRentCalc },
                            label = { Text(stringResource(R.string.rent_vs_buy)) },
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = if (showRentCalc) { { Icon(Icons.Default.Done, contentDescription = null, Modifier.size(16.dp)) } } else null
                        )
                        FilterChip(
                            selected = showIncomeCalc,
                            onClick = { showIncomeCalc = !showIncomeCalc },
                            label = { Text(stringResource(R.string.by_income)) },
                            shape = RoundedCornerShape(12.dp),
                            leadingIcon = if (showIncomeCalc) { { Icon(Icons.Default.Done, contentDescription = null, Modifier.size(16.dp)) } } else null
                        )
                    }

                    AnimatedVisibility(
                        visible = showRentCalc,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        OutlinedTextField(
                            value = viewModel.creditData.monthlyRent,
                            onValueChange = { if (it.all { c -> c.isDigit() }) viewModel.creditData = viewModel.creditData.copy(monthlyRent = it) },
                            label = { Text(stringResource(R.string.monthly_rent), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 8.dp),
                            shape = RoundedCornerShape(16.dp),
                            singleLine = true,
                            textStyle = inputTextStyle,
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            leadingIcon = { Icon(Icons.Default.Apartment, contentDescription = null) },
                            visualTransformation = ThousandsSeparatorTransformation()
                        )
                    }

                    AnimatedVisibility(
                        visible = showIncomeCalc,
                        enter = fadeIn() + expandVertically(),
                        exit = fadeOut() + shrinkVertically()
                    ) {
                        Column {
                            OutlinedTextField(
                                value = viewModel.creditData.monthlyIncome,
                                onValueChange = { if (it.all { c -> c.isDigit() }) viewModel.creditData = viewModel.creditData.copy(monthlyIncome = it) },
                                label = { Text(stringResource(R.string.your_income), maxLines = 1, overflow = TextOverflow.Ellipsis) },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                singleLine = true,
                                textStyle = inputTextStyle,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                                leadingIcon = { Icon(Icons.Default.Wallet, contentDescription = null) },
                                visualTransformation = ThousandsSeparatorTransformation()
                            )
                            if (viewModel.creditData.monthlyIncome.isNotEmpty()) {
                                val t = viewModel.creditData.loanTerm.toDoubleOrNull() ?: 20.0
                                val r = viewModel.creditData.interestRate.toDoubleOrNull() ?: 8.0
                                val isYear = viewModel.creditData.selectedUnit == "Год" || viewModel.creditData.selectedUnit == "Year"
                                val maxL = calculateMaxLoan(viewModel.creditData.monthlyIncome, r.toString(), if (isYear) t else t/12.0)
                                Card(
                                    modifier = Modifier.padding(top = 8.dp).fillMaxWidth(),
                                    colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.4f)),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Column(modifier = Modifier.padding(12.dp)) {
                                        Text(stringResource(R.string.available_loan, DecimalFormat("#,###").format(maxL)), fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
                                        TextButton(onClick = { viewModel.creditData = viewModel.creditData.copy(loanAmount = maxL.toInt().toString()) }) {
                                            Text(stringResource(R.string.use_in_calc), fontSize = 13.sp)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

            if (showRentCalc && viewModel.creditData.monthlyRent.isNotEmpty() && viewModel.creditData.loanAmount.isNotEmpty()) {
                val rResult = calculateRentVsBuy(viewModel)
                if (rResult != null) {
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.tertiaryContainer.copy(alpha = 0.5f))
                    ) {
                        Column(modifier = Modifier.padding(20.dp)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.Insights, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(stringResource(R.string.verdict), fontWeight = FontWeight.ExtraBold, fontSize = 18.sp)
                            }
                            Spacer(modifier = Modifier.height(12.dp))
                            Text(
                                if (rResult.breakEvenMonth > 0) 
                                    stringResource(R.string.buy_payback, rResult.breakEvenMonth / 12)
                                else stringResource(R.string.rent_better),
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(stringResource(R.string.property_growth, DecimalFormat("#,###").format(rResult.propertyValueAtEnd)), style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Row(modifier = Modifier.fillMaxWidth().padding(bottom = 20.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(
                    onClick = { viewModel.creditData = CreditData() },
                    modifier = Modifier.weight(1f).height(56.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.reset))
                }
                Button(
                    onClick = { if (validateFields(viewModel.creditData)) navController.navigate("resultScreen") else showDialog = true },
                    modifier = Modifier.weight(1.5f).height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                ) {
                    Icon(Icons.Default.Calculate, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(stringResource(R.string.calculate), fontWeight = FontWeight.Bold)
                }
            }
        }
    }

    if (showAddSiteDialog) {
        AlertDialog(
            onDismissRequest = { showAddSiteDialog = false },
            title = { Text(stringResource(R.string.add_site)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = newSiteName,
                        onValueChange = { newSiteName = it },
                        label = { Text(stringResource(R.string.site_name_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = newSiteUrl,
                        onValueChange = { newSiteUrl = it },
                        label = { Text(stringResource(R.string.site_url_label)) },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth(),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (newSiteName.isNotBlank() && newSiteUrl.isNotBlank()) {
                        viewModel.addCustomSite(CustomSite(newSiteName, newSiteUrl))
                        showAddSiteDialog = false
                    }
                }) {
                    Text(stringResource(R.string.add))
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddSiteDialog = false }) {
                    Text(stringResource(R.string.cancel))
                }
            }
        )
    }

    if (showInfoDialog != null) {
        AlertDialog(
            onDismissRequest = { showInfoDialog = null },
            confirmButton = { TextButton(onClick = { showInfoDialog = null }) { Text(stringResource(R.string.got_it)) } },
            title = { Text(stringResource(R.string.help)) },
            text = { 
                Text(when(showInfoDialog) {
                    "loanTerm" -> stringResource(R.string.loan_term_help)
                    else -> ""
                })
            },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = { Button(onClick = { showDialog = false }) { Text(stringResource(R.string.ok)) } },
            title = { Text(stringResource(R.string.attention)) },
            text = { Text(stringResource(R.string.fill_fields)) },
            shape = RoundedCornerShape(24.dp)
        )
    }

    if (showCommentDialog != null) {
        AlertDialog(
            onDismissRequest = { showCommentDialog = null },
            title = { Text(stringResource(R.string.comment)) },
            text = {
                OutlinedTextField(
                    value = editedComment,
                    onValueChange = { editedComment = it },
                    modifier = Modifier.fillMaxWidth(),
                    label = { Text(stringResource(R.string.edit_comment)) },
                    shape = RoundedCornerShape(12.dp)
                )
            },
            confirmButton = {
                Button(onClick = {
                    showCommentDialog?.let { prop ->
                        viewModel.updatePropertyComment(prop.url, editedComment)
                    }
                    showCommentDialog = null
                }) {
                    Text(stringResource(R.string.save))
                }
            },
            dismissButton = {
                TextButton(onClick = { showCommentDialog = null }) {
                    Text(stringResource(R.string.cancel))
                }
            },
            shape = RoundedCornerShape(24.dp)
        )
    }
}

private fun updateLocale(context: android.content.Context, lang: String) {
    val prefs = context.getSharedPreferences("credit_calc_prefs", Context.MODE_PRIVATE)
    prefs.edit().putString("app_lang", lang).apply()
    
    val locale = Locale(lang)
    Locale.setDefault(locale)
    val config = context.resources.configuration
    config.setLocale(locale)
    context.resources.updateConfiguration(config, context.resources.displayMetrics)
    
    if (context is android.app.Activity) {
        context.recreate()
    }
}

private fun validateFields(creditData: CreditData): Boolean {
    return creditData.loanAmount.isNotEmpty() && creditData.interestRate.isNotEmpty() && creditData.loanTerm.isNotEmpty()
}

class ThousandsSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val originalText = text.text
        if (originalText.isEmpty()) return TransformedText(text, OffsetMapping.Identity)
        val symbols = DecimalFormatSymbols(Locale.US).apply { groupingSeparator = ' ' }
        val formatter = DecimalFormat("#,###", symbols)
        val transformedText = try { formatter.format(originalText.toLong()) } catch (e: Exception) { originalText }
        val offsetMapping = object : OffsetMapping {
            override fun originalToTransformed(offset: Int): Int {
                if (offset <= 0) return 0
                val originalSub = originalText.substring(0, offset.coerceAtMost(originalText.length))
                return try { formatter.format(originalSub.classToLong()).length } catch (e: Exception) { originalSub.length }
            }
            private fun String.classToLong(): Long = this.replace(" ", "").toLong()
            override fun transformedToOriginal(offset: Int): Int {
                if (offset <= 0) return 0
                val transformedSub = transformedText.substring(0, offset.coerceAtMost(transformedText.length))
                return transformedSub.replace(" ", "").length
            }
        }
        return TransformedText(AnnotatedString(transformedText), offsetMapping)
    }
}
