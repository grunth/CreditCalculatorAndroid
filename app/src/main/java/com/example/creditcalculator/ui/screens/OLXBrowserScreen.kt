package com.example.creditcalculator.ui.screens

import android.view.ViewGroup
import android.webkit.JavascriptInterface
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.activity.compose.BackHandler
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.viewinterop.AndroidView
import androidx.navigation.NavController
import com.example.creditcalculator.model.CreditDataViewModel
import com.example.creditcalculator.model.SavedProperty

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun OLXBrowserScreen(navController: NavController, viewModel: CreditDataViewModel, initialUrl: String) {
    val context = LocalContext.current
    var webView: WebView? by remember { mutableStateOf(null) }
    var currentUrl by remember { mutableStateOf(initialUrl) }
    
    BackHandler(enabled = webView?.canGoBack() == true) {
        webView?.goBack()
    }

    val isSaved = viewModel.savedProperties.any { it.url == currentUrl }
    
    val fabScale by animateFloatAsState(
        targetValue = if (isSaved) 1.1f else 1f,
        animationSpec = spring(dampingRatio = 0.5f),
        label = "fabScale"
    )
    
    val heartColor by animateColorAsState(
        targetValue = if (isSaved) Color.Red else MaterialTheme.colorScheme.onSecondaryContainer,
        label = "heartColor"
    )

    val jsInterface = remember {
        object {
            @JavascriptInterface
            fun onPriceExtracted(price: String, url: String) {
                // Извлекаем только цифры. Если пусто - "0"
                val cleanPrice = price.replace(Regex("[^\\d]"), "").ifEmpty { "0" }
                android.os.Handler(android.os.Looper.getMainLooper()).post {
                    viewModel.updatePropertyPrice(url, cleanPrice)
                }
            }
        }
    }

    Scaffold(
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
            FloatingActionButton(
                onClick = {
                    val url = webView?.url ?: ""
                    val isAlreadySaved = viewModel.savedProperties.any { it.url == url }
                    
                    if (isAlreadySaved) {
                        val property = viewModel.savedProperties.find { it.url == url }
                        property?.let { viewModel.removeSavedProperty(it) }
                    } else {
                        // Мгновенно создаем запись с ценой "0"
                        val title = webView?.title ?: "Объект"
                        val siteName = when {
                            url.contains("olx.pl") -> "OLX"
                            url.contains("otodom.pl") -> "Otodom"
                            else -> ""
                        }
                        viewModel.addSavedProperty(SavedProperty(title, url, "0", siteName))
                        
                        // Запускаем парсинг цены в фоне
                        val script = getPriceExtractionScript(url)
                        webView?.evaluateJavascript(script, null)
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
    ) { padding ->
        Box(modifier = Modifier.padding(padding).fillMaxSize()) {
            AndroidView(
                factory = {
                    WebView(it).apply {
                        layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        webViewClient = object : WebViewClient() {
                            override fun onPageFinished(view: WebView?, url: String?) {
                                super.onPageFinished(view, url)
                                url?.let { currentUrl = it }
                            }
                        }
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
                modifier = Modifier.fillMaxSize()
            )
        }
    }
}

private fun getPriceExtractionScript(url: String): String {
    return """
        (function() {
            var price = "";
            var selectors = [
                '[data-testid="ad-price"]',
                'strong[data-cy="ad-price"]',
                '[data-testid="ad-price-container"] h3',
                '.css-1wnm74r',
                '.css-12v969',
                '[data-cy="ad-price-container"]',
                'h2[data-testid="ad-price"]'
            ];
            
            for (var s of selectors) {
                var el = document.querySelector(s);
                if (el && el.innerText) {
                    var raw = el.innerText;
                    if (raw.includes('zł')) {
                        price = raw.split('zł')[0];
                    } else if (raw.includes('PLN')) {
                        price = raw.split('PLN')[0];
                    } else {
                        price = raw;
                    }
                    break;
                }
            }
            Android.onPriceExtracted(price, "$url");
        })()
    """.trimIndent()
}
