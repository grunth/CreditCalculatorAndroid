package com.example.creditcalculator

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.creditcalculator.model.CreditDataViewModel
import com.example.creditcalculator.ui.screens.MainWindow
import com.example.creditcalculator.ui.screens.ResultScreen
import com.example.creditcalculator.ui.theme.CreditCalculatorTheme

class MainActivity : ComponentActivity() {

    private val creditViewModel by viewModels<CreditDataViewModel>()
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
                            ResultScreen(creditViewModel)
                        }
                    }
                }
            }
        }
    }
}