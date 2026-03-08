package com.example.creditcalculator.ui

import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performTextInput
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.creditcalculator.model.CreditDataViewModel
import com.example.creditcalculator.ui.screens.MainWindow
import com.example.creditcalculator.ui.screens.ResultScreen
import org.junit.Rule
import org.junit.Test

class NavigationTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun testNavigationToResultAndBack() {
        val viewModel = CreditDataViewModel()

        composeTestRule.setContent {
            val navController = rememberNavController()
            NavHost(navController = navController, startDestination = "main") {
                composable("main") {
                    MainWindow(navController, viewModel)
                }
                composable("resultScreen") {
                    ResultScreen(viewModel, onBackClick = { navController.popBackStack() })
                }
            }
        }

        // Fill data
        composeTestRule.onNodeWithText("Сумма кредита").performTextInput("100000")
        composeTestRule.onNodeWithText("Процентная ставка (%)").performTextInput("10")
        composeTestRule.onNodeWithText("Срок").performTextInput("12")

        // Click Calculate
        composeTestRule.onNodeWithText("Расчет").performClick()

        // Check if on Result Screen
        composeTestRule.onNodeWithText("Результаты расчета").assertExists()
        composeTestRule.onNodeWithText("График платежей").assertExists()

        // Click Back
        composeTestRule.onNodeWithText("Назад").performClick()

        // Check if back on Main Screen
        composeTestRule.onNodeWithText("Кредитный калькулятор").assertExists()
    }

    @Test
    fun testValidationErrorShowDialog() {
        val viewModel = CreditDataViewModel()

        composeTestRule.setContent {
            val navController = rememberNavController()
            MainWindow(navController, viewModel)
        }

        // Click Calculate without filling fields
        composeTestRule.onNodeWithText("Расчет").performClick()

        // Check if error dialog is shown
        composeTestRule.onNodeWithText("Ошибка").assertExists()
        composeTestRule.onNodeWithText("Пожалуйста, заполните все поля перед расчетом.").assertExists()
        
        // Close dialog
        composeTestRule.onNodeWithText("ОК").performClick()
        composeTestRule.onNodeWithText("Ошибка").assertDoesNotExist()
    }
}
