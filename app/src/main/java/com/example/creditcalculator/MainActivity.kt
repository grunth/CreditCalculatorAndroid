package com.example.creditcalculator

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.creditcalculator.model.CreditDataViewModel
import com.example.creditcalculator.ui.screens.MainWindow
import com.example.creditcalculator.ui.screens.ResultScreen
import com.example.creditcalculator.ui.screens.SiteBrowserScreen
import com.example.creditcalculator.ui.theme.CreditCalculatorTheme
import java.net.URLDecoder
import java.util.Locale

class MainActivity : ComponentActivity() {

    private val creditViewModel by viewModels<CreditDataViewModel>()
    
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("credit_calc_prefs", Context.MODE_PRIVATE)
        val lang = prefs.getString("app_lang", Locale.getDefault().language) ?: Locale.getDefault().language
        val locale = Locale(lang)
        Locale.setDefault(locale)
        val config = newBase.resources.configuration
        config.setLocale(locale)
        super.attachBaseContext(newBase.createConfigurationContext(config))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            CreditCalculatorTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "mainScreen") {
                        composable("mainScreen") {
                            MainWindow(navController, creditViewModel)
                        }
                        composable("resultScreen") {
                            ResultScreen(
                                creditViewModel = creditViewModel,
                                onBackClick = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "browserScreen/{url}",
                            arguments = listOf(navArgument("url") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val encodedUrl = backStackEntry.arguments?.getString("url") ?: ""
                            val decodedUrl = URLDecoder.decode(encodedUrl, "UTF-8")
                            SiteBrowserScreen(navController, creditViewModel, decodedUrl)
                        }
                    }
                }
            }
        }
    }
}
