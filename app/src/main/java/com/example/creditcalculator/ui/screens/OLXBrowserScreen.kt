package com.example.creditcalculator.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdsClick
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.creditcalculator.model.CreditDataViewModel
import com.example.creditcalculator.model.SavedProperty
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OLXBrowserScreen(navController: NavController, viewModel: CreditDataViewModel, initialUrl: String) {
    val context = LocalContext.current
    var webView: WebView? by remember { mutableStateOf(null) }
    var currentUrl by remember { mutableStateOf(initialUrl) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    
    var showSuggestDialog by remember { mutableStateOf(false) }
    var suggestedSelector by remember { mutableStateOf("") }
    var suggestedText by remember { mutableStateOf("") }
    var nextSelectorIndex by remember { mutableStateOf(0) }

    // Normalize host to avoid www. issues
    val host = remember(currentUrl) { 
        try { 
            Uri.parse(currentUrl).host?.lowercase()?.removePrefix("www.") ?: "" 
        } catch(e: Exception) { "" } 
    }
    
    val isSaved = viewModel.savedProperties.any { it.url == currentUrl }

    // Update currentUrl periodically to handle SPA navigation
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
                val cleanPrice = price.replace(Regex("[^\\d]"), "").ifEmpty { "0" }
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    viewModel.updatePropertyPrice(url, cleanPrice)
                }
            }

            @JavascriptInterface
            fun onPatternDefined(selector: String, text: String) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    suggestedSelector = selector
                    suggestedText = text
                    showSuggestDialog = true
                }
            }
            
            @JavascriptInterface
            fun onAutoPatternFound(selector: String, text: String, index: Int) {
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    if (!viewModel.sitePatterns.containsKey(host)) {
                        suggestedSelector = selector
                        suggestedText = text
                        nextSelectorIndex = index + 1
                        showSuggestDialog = true
                    }
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
                title = { Text("Браузер") },
                navigationIcon = {
                    IconButton(onClick = { 
                        if (webView?.canGoBack() == true) {
                            webView?.goBack()
                        } else {
                            navController.popBackStack()
                        }
                    }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Назад")
                    }
                }
            )
        },
        floatingActionButton = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                FloatingActionButton(
                    onClick = {
                        webView?.evaluateJavascript(getDefinePatternScript(), null)
                    },
                    containerColor = MaterialTheme.colorScheme.tertiaryContainer
                ) {
                    Icon(Icons.Default.AdsClick, contentDescription = "Задать паттерн")
                }

                FloatingActionButton(
                    onClick = {
                        val url = webView?.url ?: currentUrl
                        if (viewModel.savedProperties.any { it.url == url }) {
                            val property = viewModel.savedProperties.find { it.url == url }
                            property?.let { viewModel.removeSavedProperty(it) }
                        } else {
                            val title = webView?.title ?: "Объект"
                            val siteName = when {
                                url.contains("olx.pl") -> "OLX"
                                url.contains("otodom.pl") -> "Otodom"
                                else -> host
                            }
                            viewModel.addSavedProperty(SavedProperty(title, url, "0", siteName))
                            
                            val pattern = viewModel.sitePatterns[host]
                            if (pattern != null) {
                                webView?.evaluateJavascript(getExtractPriceByPatternScript(pattern, url), null)
                            } else {
                                scope.launch {
                                    snackbarHostState.showSnackbar("Паттерн не задан. Используется стандартный поиск.")
                                }
                                webView?.evaluateJavascript(getOldPriceExtractionScript(url), null)
                            }
                        }
                    },
                    containerColor = MaterialTheme.colorScheme.secondaryContainer,
                    modifier = Modifier.scale(fabScale)
                ) {
                    Icon(
                        imageVector = if (isSaved) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                        contentDescription = "Сохранить",
                        tint = heartColor
                    )
                }
            }
        }
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
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
                                    if (!viewModel.sitePatterns.containsKey(currentHost)) {
                                        evaluateJavascript(getAutoFindPriceScript(0), null)
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

    if (showSuggestDialog) {
        var editedSelector by remember { mutableStateOf(suggestedSelector) }
        AlertDialog(
            onDismissRequest = { showSuggestDialog = false },
            title = { Text("Найден элемент цены") },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text("Система определила цену: $suggestedText")
                    Text("Вы можете отредактировать CSS-паттерн ниже:", style = MaterialTheme.typography.labelSmall)
                    OutlinedTextField(
                        value = editedSelector,
                        onValueChange = { editedSelector = it },
                        modifier = Modifier.fillMaxWidth(),
                        label = { Text("CSS Селектор") }
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    viewModel.saveSitePattern(host, editedSelector)
                    showSuggestDialog = false
                    Toast.makeText(context, "Паттерн для $host сохранен!", Toast.LENGTH_SHORT).show()
                    // Try to extract price immediately after saving pattern
                    webView?.let {
                        it.evaluateJavascript(getExtractPriceByPatternScript(editedSelector, it.url ?: currentUrl), null)
                    }
                }) { Text("Сохранить") }
            },
            dismissButton = {
                OutlinedButton(onClick = { 
                    showSuggestDialog = false
                    // Try next search method
                    webView?.evaluateJavascript(getAutoFindPriceScript(nextSelectorIndex), null)
                }) { Text("Не то, искать еще") }
            }
        )
    }
}

private fun getAutoFindPriceScript(startIndex: Int): String {
    return """
        (function() {
            var selectors = [
                '[data-testid="ad-price"]', 
                'strong[data-cy="ad-price"]', 
                '[data-testid="ad-price-container"] h3', 
                '.css-1wnm74r',
                'h2[data-testid="ad-price"]',
                '.price-label strong',
                '.ad-price',
                '[data-testid="price-value"]',
                '.offer-price__number',
                '.aria-price'
            ];
            
            function getSelector(e) {
                if (e.id) return '#' + e.id;
                var path = [];
                while (e && e.nodeType === Node.ELEMENT_NODE) {
                    var sel = e.nodeName.toLowerCase();
                    if (e.className && typeof e.className === 'string') {
                        var classes = e.className.trim().split(/\s+/).filter(c => c && !c.includes(':') && !c.includes('[')).join('.');
                        if (classes) sel += '.' + classes;
                    }
                    path.unshift(sel);
                    e = e.parentNode;
                    if (!e || e.nodeName === 'BODY' || e.nodeName === 'HTML') break;
                }
                return path.join(' > ');
            }

            for (var i = $startIndex; i < selectors.length; i++) {
                var el = document.querySelector(selectors[i]);
                if (el && el.innerText && el.innerText.length < 40 && /\d/.test(el.innerText)) {
                    Android.onAutoPatternFound(getSelector(el), el.innerText, i);
                    return;
                }
            }
            
            // Если ничего не нашли из списка, ищем по валюте
            if ($startIndex < 100) { 
                var all = document.querySelectorAll('h1, h2, h3, strong, span.price, div.price');
                for (var i = 0; i < all.length; i++) {
                    var txt = all[i].innerText;
                    if ((txt.includes('zł') || txt.includes('PLN') || txt.includes('€') || txt.includes('$')) && txt.length < 25 && /\d/.test(txt)) {
                        Android.onAutoPatternFound(getSelector(all[i]), txt, 100);
                        return;
                    }
                }
            }
        })()
    """.trimIndent()
}

private fun getDefinePatternScript(): String {
    return """
        (function() {
            // Remove old overlay if exists
            var old = document.getElementById('price-picker-overlay');
            if (old) old.remove();

            var overlay = document.createElement('div');
            overlay.id = 'price-picker-overlay';
            overlay.style.position = 'fixed';
            overlay.style.top = '0'; overlay.style.left = '0';
            overlay.style.width = '100%'; overlay.style.height = '100%';
            overlay.style.backgroundColor = 'rgba(0,0,0,0.3)';
            overlay.style.zIndex = '999999';
            overlay.style.display = 'flex';
            overlay.style.flexDirection = 'column';
            overlay.style.alignItems = 'center';
            overlay.style.justifyContent = 'center';
            
            var msg = document.createElement('div');
            msg.innerText = 'Нажмите точно на цену товара';
            msg.style.background = 'white';
            msg.style.padding = '15px';
            msg.style.borderRadius = '10px';
            msg.style.fontWeight = 'bold';
            overlay.appendChild(msg);

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

            overlay.onclick = function(e) {
                overlay.style.display = 'none';
                var el = document.elementFromPoint(e.clientX, e.clientY);
                overlay.remove();
                if (el) {
                    var selector = getSelector(el);
                    Android.onPatternDefined(selector, el.innerText);
                } else {
                    Android.showToast("Элемент не найден");
                }
            };
            
            document.body.appendChild(overlay);
        })()
    """.trimIndent()
}

private fun getExtractPriceByPatternScript(selector: String, url: String): String {
    return """
        (function() {
            var price = "";
            try {
                var el = document.querySelector('$selector');
                if (el) {
                    price = el.innerText;
                } else {
                    // Fallback to last part of selector
                    var parts = '$selector'.split(' > ');
                    var last = parts[parts.length - 1];
                    var fallback = document.querySelector(last);
                    if (fallback) price = fallback.innerText;
                }
            } catch(e) {}
            Android.onPriceExtracted(price, "$url");
        })()
    """.trimIndent()
}

private fun getOldPriceExtractionScript(url: String): String {
    return """
        (function() {
            var price = "";
            var selectors = ['[data-testid="ad-price"]', 'strong[data-cy="ad-price"]', '[data-testid="ad-price-container"] h3', '.css-1wnm74r'];
            for (var s of selectors) {
                var el = document.querySelector(s);
                if (el && el.innerText) {
                    price = el.innerText;
                    break;
                }
            }
            Android.onPriceExtracted(price, "$url");
        })()
    """.trimIndent()
}
