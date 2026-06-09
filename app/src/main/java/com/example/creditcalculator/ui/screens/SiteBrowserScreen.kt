package com.example.creditcalculator.ui.screens

import android.content.Intent
import android.net.Uri
import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.creditcalculator.R
import com.example.creditcalculator.model.CreditDataViewModel
import com.example.creditcalculator.model.SavedProperty
import kotlinx.coroutines.delay
import org.json.JSONArray

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SiteBrowserScreen(navController: NavController, viewModel: CreditDataViewModel, initialUrl: String) {
    val context = LocalContext.current
    var webView: WebView? by remember { mutableStateOf(null) }
    var currentUrl by remember { mutableStateOf(initialUrl) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showSaveDialog by remember { mutableStateOf(false) }
    var suggestedSelector by remember { mutableStateOf("") }
    var suggestedText by remember { mutableStateOf("") }
    
    val pickTip = stringResource(R.string.pick_price_tip)

    val host = remember(currentUrl) { 
        try { 
            Uri.parse(currentUrl).host?.lowercase()?.removePrefix("www.") ?: "" 
        } catch(e: Exception) { "" } 
    }
    
    val isSaved = viewModel.savedProperties.any { it.url == currentUrl }

    LaunchedEffect(webView) {
        while(true) {
            webView?.let {
                if (it.url != null && it.url != currentUrl) {
                    currentUrl = it.url!!
                }
            }
            delay(1000)
        }
    }

    BackHandler(enabled = webView?.canGoBack() == true) {
        webView?.goBack()
    }

    val fabScale by animateFloatAsState(
        targetValue = if (isSaved) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "fabScale"
    )
    
    val heartColor by animateColorAsState(
        targetValue = if (isSaved) Color.Red else MaterialTheme.colorScheme.onSecondaryContainer,
        label = "heartColor"
    )

    val jsInterface = remember(host) {
        object {
            @JavascriptInterface
            fun onPriceExtracted(price: String, url: String) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    suggestedText = price
                }
            }

            @JavascriptInterface
            fun onPatternDefined(selector: String, text: String) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    suggestedSelector = selector
                    suggestedText = text
                    showSaveDialog = true
                    webView?.evaluateJavascript(getHighlightScript(selector), null)
                }
            }
            
            @JavascriptInterface
            fun onAutoPatternFound(selector: String, text: String, index: Int) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    suggestedSelector = selector
                    suggestedText = text
                    webView?.evaluateJavascript(getHighlightScript(selector), null)
                }
            }

            @JavascriptInterface
            fun showToast(message: String) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.browser_title)) },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (webView?.canGoBack() == true) {
                            webView?.goBack()
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = stringResource(R.string.back))
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val url = webView?.url ?: currentUrl
                        val sendIntent: Intent = Intent().apply {
                            action = Intent.ACTION_SEND
                            putExtra(Intent.EXTRA_TEXT, url)
                            type = "text/plain"
                        }
                        val shareIntent = Intent.createChooser(sendIntent, null)
                        context.startActivity(shareIntent)
                    }) {
                        Icon(Icons.Default.Share, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val url = webView?.url ?: currentUrl
                    val existing = viewModel.savedProperties.find { it.url == url }
                    if (existing != null) {
                        suggestedSelector = viewModel.sitePatterns[host]?.lastOrNull()?.selector ?: ""
                        suggestedText = existing.rawPrice
                        showSaveDialog = true
                    } else {
                        // Если объект новый, сразу просим выбрать цену
                        webView?.evaluateJavascript(getDefinePatternScript(pickTip), null)
                    }
                },
                containerColor = MaterialTheme.colorScheme.secondaryContainer,
                modifier = Modifier.scale(fabScale)
            ) {
                Icon(
                    imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                    contentDescription = stringResource(R.string.save_property),
                    tint = heartColor
                )
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.padding(paddingValues).fillMaxSize()) {
            AndroidView(
                factory = { ctx ->
                    WebView(ctx).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                url?.let { 
                                    currentUrl = it 
                                    val currentHost = Uri.parse(it).host?.lowercase()?.removePrefix("www.") ?: ""
                                    val patterns = viewModel.sitePatterns[currentHost]
                                    if (!patterns.isNullOrEmpty()) {
                                        // Пробуем извлечь цену только если паттерн уже известен для этого хоста
                                        evaluateJavascript(getExtractPriceByMultiPatternScript(patterns.map { it.selector }), null)
                                    }
                                }
                            }
                        }
                        webChromeClient = WebChromeClient()
                        settings.apply {
                            javaScriptEnabled = true
                            domStorageEnabled = true
                            userAgentString = "Mozilla/5.0 (Linux; Android 10; Mobile) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/83.0.4103.106 Mobile Safari/537.36"
                        }
                        addJavascriptInterface(jsInterface, "Android")
                        loadUrl(initialUrl)
                        webView = this
                    }
                },
                modifier = Modifier.fillMaxSize(),
                update = {
                    webView = it
                }
            )
        }
    }

    if (showSaveDialog) {
        var editedTitle by remember { mutableStateOf(webView?.title ?: context.getString(R.string.property_default_title)) }
        var editedPriceText by remember { mutableStateOf(suggestedText) }
        var selectedSeparator by remember { mutableStateOf(viewModel.siteSeparators[host] ?: "none") }
        var showAdvanced by remember { mutableStateOf(false) }
        var editedSelector by remember { mutableStateOf(suggestedSelector) }
        
        val numericPrice = remember(editedPriceText, selectedSeparator) { 
            viewModel.cleanPriceText(editedPriceText, selectedSeparator)
        }

        AlertDialog(
            onDismissRequest = { 
                showSaveDialog = false 
                webView?.evaluateJavascript(getRemoveHighlightScript(), null)
            },
            title = { Text(stringResource(R.string.save_property)) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    OutlinedTextField(
                        value = editedTitle,
                        onValueChange = { editedTitle = it },
                        label = { Text(stringResource(R.string.property_title_label)) },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        OutlinedTextField(
                            value = editedPriceText,
                            onValueChange = { editedPriceText = it },
                            label = { Text(stringResource(R.string.price_on_site)) },
                            modifier = Modifier.weight(1f),
                            supportingText = { Text(stringResource(R.string.recognized_price, numericPrice)) }
                        )
                        IconButton(
                            onClick = {
                                showSaveDialog = false
                                webView?.evaluateJavascript(getDefinePatternScript(pickTip), null)
                            },
                            modifier = Modifier.padding(top = 8.dp)
                        ) {
                            Icon(Icons.Default.AdsClick, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        }
                    }
                    
                    Text(stringResource(R.string.decimal_separator), style = MaterialTheme.typography.labelMedium)
                    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                        FilterChip(
                            selected = selectedSeparator == "none",
                            onClick = { selectedSeparator = "none" },
                            label = { Text(stringResource(R.string.sep_none)) }
                        )
                        FilterChip(
                            selected = selectedSeparator == ",",
                            onClick = { selectedSeparator = "," },
                            label = { Text(",") }
                        )
                        FilterChip(
                            selected = selectedSeparator == ".",
                            onClick = { selectedSeparator = "." },
                            label = { Text(".") }
                        )
                    }

                    TextButton(onClick = { showAdvanced = !showAdvanced }) {
                        Text(if (showAdvanced) stringResource(R.string.hide_tech_data) else stringResource(R.string.show_tech_data))
                    }
                    
                    if (showAdvanced) {
                        OutlinedTextField(
                            value = editedSelector,
                            onValueChange = { editedSelector = it },
                            modifier = Modifier.fillMaxWidth(),
                            label = { Text(stringResource(R.string.css_selector)) },
                            textStyle = MaterialTheme.typography.labelSmall
                        )
                        if (isSaved) {
                            Button(
                                onClick = {
                                    val url = webView?.url ?: currentUrl
                                    val property = viewModel.savedProperties.find { it.url == url }
                                    property?.let { viewModel.removeSavedProperty(it) }
                                    showSaveDialog = false
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.errorContainer, contentColor = MaterialTheme.colorScheme.onErrorContainer),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, Modifier.size(18.dp))
                                Spacer(Modifier.width(8.dp))
                                Text(stringResource(R.string.delete))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(onClick = {
                    val url = webView?.url ?: currentUrl
                    val siteName = when {
                        url.contains("olx.pl") -> "OLX"
                        url.contains("otodom.pl") -> "Otodom"
                        else -> host
                    }
                    
                    if (editedSelector.isNotEmpty()) {
                        viewModel.saveSitePattern(host, editedSelector, selectedSeparator)
                    }
                    
                    viewModel.addSavedProperty(SavedProperty(
                        title = editedTitle,
                        url = url,
                        price = numericPrice,
                        siteName = siteName,
                        rawPrice = editedPriceText
                    ))
                    
                    showSaveDialog = false
                    Toast.makeText(context, context.getString(R.string.property_saved), Toast.LENGTH_SHORT).show()
                }) { Text(stringResource(R.string.save)) }
            },
            dismissButton = {
                OutlinedButton(onClick = { 
                    showSaveDialog = false
                    webView?.evaluateJavascript(getRemoveHighlightScript(), null)
                }) { Text(stringResource(R.string.cancel)) }
            }
        )
    }
}

private fun getHighlightScript(selector: String): String {
    return """
        (function() {
            var existing = document.querySelectorAll('.mortgage-lab-highlight');
            existing.forEach(el => {
                el.style.outline = '';
                el.style.backgroundColor = '';
                el.classList.remove('mortgage-lab-highlight');
            });

            try {
                var el = document.querySelector('$selector');
                if (el) {
                    el.style.outline = '3px solid #4CAF50';
                    el.style.outlineOffset = '2px';
                    el.style.backgroundColor = 'rgba(76, 175, 80, 0.15)';
                    el.classList.add('mortgage-lab-highlight');
                    el.scrollIntoView({behavior: "smooth", block: "center"});
                }
            } catch(e) {}
        })()
    """.trimIndent()
}

private fun getRemoveHighlightScript(): String {
    return """
        (function() {
            var existing = document.querySelectorAll('.mortgage-lab-highlight');
            existing.forEach(el => {
                el.style.outline = '';
                el.style.backgroundColor = '';
                el.classList.remove('mortgage-lab-highlight');
            });
        })()
    """.trimIndent()
}

private fun getExtractPriceByMultiPatternScript(selectors: List<String>): String {
    val selectorsJson = JSONArray(selectors).toString()
    return """
        (function() {
            var selectors = $selectorsJson;
            for (var s of selectors) {
                var el = document.querySelector(s);
                if (el && el.innerText && /\d/.test(el.innerText)) {
                    Android.onAutoPatternFound(s, el.innerText, 0);
                    return;
                }
            }
        })()
    """.trimIndent()
}

private fun getAutoFindPriceScript(startIndex: Int): String {
    return "" // Disabled as per user request to avoid performance issues
}

private fun getDefinePatternScript(tipText: String): String {
    return """
        (function() {
            var lastEl = null;
            var originalOutline = '';
            
            var tip = document.createElement('div');
            tip.innerText = '$tipText';
            tip.style.position = 'fixed';
            tip.style.bottom = '80px';
            tip.style.left = '50%';
            tip.style.transform = 'translateX(-50%)';
            tip.style.background = 'rgba(0,0,0,0.8)';
            tip.style.color = 'white';
            tip.style.padding = '12px 24px';
            tip.style.borderRadius = '25px';
            tip.style.zIndex = '1000000';
            tip.style.fontSize = '16px';
            tip.style.boxShadow = '0 4px 15px rgba(0,0,0,0.3)';
            tip.id = 'price-picker-tip';
            document.body.appendChild(tip);

            function getSelector(el) {
                var path = [];
                while (el && el.nodeType === Node.ELEMENT_NODE) {
                    var sel = el.nodeName.toLowerCase();
                    if (el.id) {
                        sel += '#' + el.id;
                        path.unshift(sel);
                        break;
                    }
                    if (el.className && typeof el.className === 'string') {
                        var cls = el.className.trim().split(/\s+/).filter(c => c && !c.includes(':') && !c.includes('[')).join('.');
                        if (cls) sel += '.' + cls;
                    }
                    path.unshift(sel);
                    el = el.parentNode;
                    if (!el || el.nodeName === 'BODY' || el.nodeName === 'HTML') break;
                }
                return path.join(' > ');
            }

            function onMouseMove(e) {
                var el = document.elementFromPoint(e.clientX, e.clientY);
                if (el && el !== lastEl && el.id !== 'price-picker-tip') {
                    if (lastEl) lastEl.style.outline = originalOutline;
                    lastEl = el;
                    originalOutline = el.style.outline;
                    el.style.outline = '2px solid #2196F3';
                    el.style.outlineOffset = '1px';
                }
            }

            function onClick(e) {
                e.preventDefault();
                e.stopPropagation();
                
                document.removeEventListener('mousemove', onMouseMove, true);
                document.removeEventListener('click', onClick, true);
                
                if (lastEl) lastEl.style.outline = originalOutline;
                if (tip) tip.remove();
                
                var el = document.elementFromPoint(e.clientX, e.clientY);
                if (el) {
                    var selector = getSelector(el);
                    Android.onPatternDefined(selector, el.innerText);
                }
                return false;
            }

            document.addEventListener('mousemove', onMouseMove, true);
            document.addEventListener('click', onClick, true);
        })()
    """.trimIndent()
}
