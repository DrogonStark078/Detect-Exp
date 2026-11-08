package com.smarttracker.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.smarttracker.app.core.theme.DetExTheme
import com.smarttracker.app.data.model.CategoryShare
import com.smarttracker.app.data.model.TransactionItemUiModel
import com.smarttracker.app.ui.screens.AnalyticsScreen
import com.smarttracker.app.ui.screens.ExpenseConfirmationSheet
import com.smarttracker.app.ui.screens.HomeScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Permissions evaluated */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()

        setContent {
            DetExTheme {
                var selectedTab by remember { mutableIntStateOf(0) }
                val db = (application as SmartExpenseApp).database.transactionDao()
                val scope = rememberCoroutineScope()

                val confirmedTxs by db.getConfirmedTransactions().collectAsState(initial = emptyList())
                val totalSpent by db.getTotalSpent().collectAsState(initial = 0.0)

                var showConfirmSheet by remember { mutableStateOf(false) }

                val uiTransactions = confirmedTxs.map {
                    TransactionItemUiModel(
                        id = it.id,
                        merchant = it.merchant,
                        category = it.category,
                        amount = it.amount,
                        isExpense = it.type == "EXPENSE",
                        timeAgo = "Recently"
                    )
                }

                val analyticsShares = listOf(
                    CategoryShare("Food", (totalSpent ?: 0.0) * 0.45, MaterialTheme.colorScheme.primary),
                    CategoryShare("Groceries", (totalSpent ?: 0.0) * 0.35, MaterialTheme.colorScheme.error),
                    CategoryShare("Other", (totalSpent ?: 0.0) * 0.20, MaterialTheme.colorScheme.surfaceVariant)
                )

                Scaffold(
                    bottomBar = {
                        NavigationBar {
                            NavigationBarItem(
                                selected = selectedTab == 0,
                                onClick = { selectedTab = 0 },
                                icon = { Icon(Icons.Filled.Home, contentDescription = "Home") },
                                label = { Text("Home") }
                            )
                            NavigationBarItem(
                                selected = selectedTab == 1,
                                onClick = { selectedTab = 1 },
                                icon = { Icon(Icons.Filled.Analytics, contentDescription = "Analytics") },
                                label = { Text("Analytics") }
                            )
                        }
                    }
                ) { innerPadding ->
                    Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                        if (selectedTab == 0) {
                            HomeScreen(
                                totalSpentMonth = totalSpent ?: 0.0,
                                monthlyBudget = 50000.0,
                                recentTransactions = uiTransactions,
                                onAddExpenseClick = { showConfirmSheet = true }
                            )
                        } else {
                            AnalyticsScreen(shares = analyticsShares)
                        }

                        if (showConfirmSheet) {
                            ExpenseConfirmationSheet(
                                amount = 450.0,
                                merchant = "Swiggy",
                                suggestedCategory = "Food & Dining",
                                onConfirm = { selectedCat ->
                                    scope.launch {
                                        db.insert(
                                            com.smarttracker.app.data.local.TransactionEntity(
                                                amount = 450.0,
                                                type = "EXPENSE",
                                                category = selectedCat,
                                                merchant = "Swiggy",
                                                accountOrCard = "Default",
                                                balanceAfterTx = null,
                                                referenceNumber = null,
                                                timestamp = System.currentTimeMillis(),
                                                isConfirmed = true
                                            )
                                        )
                                    }
                                    showConfirmSheet = false
                                },
                                onDismiss = { showConfirmSheet = false }
                            )
                        }
                    }
                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        val permissions = mutableListOf(
            Manifest.permission.RECEIVE_SMS,
            Manifest.permission.READ_SMS
        )
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            permissions.add(Manifest.permission.POST_NOTIFICATIONS)
        }

        val missing = permissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (missing.isNotEmpty()) {
            requestPermissionLauncher.launch(missing.toTypedArray())
        }
    }
}
