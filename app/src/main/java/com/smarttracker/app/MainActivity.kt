package com.smarttracker.app

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Analytics
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.core.content.ContextCompat
import com.smarttracker.app.core.auth.SessionManager
import com.smarttracker.app.core.theme.DetExTheme
import com.smarttracker.app.data.csv.CsvManager
import com.smarttracker.app.data.local.TransactionEntity
import com.smarttracker.app.data.model.CategoryShare
import com.smarttracker.app.data.model.TransactionItemUiModel
import com.smarttracker.app.ui.screens.AnalyticsScreen
import com.smarttracker.app.ui.screens.AuthScreen
import com.smarttracker.app.ui.screens.ExpenseConfirmationSheet
import com.smarttracker.app.ui.screens.HomeScreen
import com.smarttracker.app.ui.screens.SettingsScreen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val sessionManager by lazy { SessionManager(this) }

    private val permissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestMultiplePermissions()
    ) { /* Handled gracefully */ }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkAndRequestPermissions()

        setContent {
            DetExTheme {
                var isAuthenticated by remember { mutableStateOf(sessionManager.isLoggedIn()) }

                if (!isAuthenticated) {
                    AuthScreen(
                        onAuthSuccess = { phone, code ->
                            sessionManager.setLoggedIn(phone, code)
                            isAuthenticated = true
                            Toast.makeText(this, "Welcome to DetEx!", Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    MainAppScaffold(
                        onLogout = {
                            sessionManager.logout()
                            isAuthenticated = false
                        }
                    )
                }
            }
        }
    }

    @Composable
    private fun MainAppScaffold(onLogout: () -> Unit) {
        val context = this@MainActivity
        val scope = rememberCoroutineScope()
        val db = (application as SmartExpenseApp).database.transactionDao()

        var selectedTab by remember { mutableIntStateOf(0) }
        val confirmedTxs by db.getConfirmedTransactions().collectAsState(initial = emptyList())
        val allTxs by db.getAllTransactions().collectAsState(initial = emptyList())
        val totalSpent by db.getTotalSpent().collectAsState(initial = 0.0)

        var showConfirmSheet by remember { mutableStateOf(false) }

        // Storage Access Framework Launcher for CSV Export
        val exportLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.CreateDocument("text/csv")
        ) { uri ->
            if (uri != null) {
                scope.launch {
                    val success = CsvManager.exportTransactionsToUri(context, uri, allTxs)
                    Toast.makeText(
                        context,
                        if (success) "Exported ${allTxs.size} transactions to CSV!" else "Failed to export CSV",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        // Storage Access Framework Launcher for CSV Import
        val importLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.OpenDocument()
        ) { uri ->
            if (uri != null) {
                scope.launch {
                    val imported = CsvManager.importTransactionsFromUri(context, uri)
                    if (imported.isNotEmpty()) {
                        for (item in imported) {
                            db.insert(item)
                        }
                        Toast.makeText(
                            context,
                            "Successfully imported ${imported.size} historical expenses!",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        Toast.makeText(
                            context,
                            "No valid transactions found in selected file",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        }

        val uiTransactions = confirmedTxs.map {
            TransactionItemUiModel(
                id = it.id,
                merchant = it.merchant,
                category = it.category,
                amount = it.amount,
                isExpense = it.type == "EXPENSE",
                timeAgo = "Confirmed"
            )
        }

        val analyticsShares = listOf(
            CategoryShare("Food & Dining", (totalSpent ?: 0.0) * 0.42, MaterialTheme.colorScheme.primary),
            CategoryShare("Groceries", (totalSpent ?: 0.0) * 0.33, MaterialTheme.colorScheme.error),
            CategoryShare("Transportation", (totalSpent ?: 0.0) * 0.15, MaterialTheme.colorScheme.secondary),
            CategoryShare("Other", (totalSpent ?: 0.0) * 0.10, MaterialTheme.colorScheme.surfaceVariant)
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
                    NavigationBarItem(
                        selected = selectedTab == 2,
                        onClick = { selectedTab = 2 },
                        icon = { Icon(Icons.Filled.Settings, contentDescription = "Settings") },
                        label = { Text("Settings") }
                    )
                }
            }
        ) { innerPadding ->
            Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
                when (selectedTab) {
                    0 -> HomeScreen(
                        totalSpentMonth = totalSpent ?: 0.0,
                        monthlyBudget = 50000.0,
                        recentTransactions = uiTransactions,
                        onAddExpenseClick = { showConfirmSheet = true }
                    )
                    1 -> AnalyticsScreen(shares = analyticsShares)
                    2 -> SettingsScreen(
                        phoneNumber = sessionManager.getPhoneNumber(),
                        totalTransactionCount = allTxs.size,
                        onImportCsv = {
                            importLauncher.launch(
                                arrayOf(
                                    "text/csv",
                                    "text/comma-separated-values",
                                    "application/csv",
                                    "text/plain"
                                )
                            )
                        },
                        onExportCsv = {
                            exportLauncher.launch("detex_backup_${System.currentTimeMillis()}.csv")
                        },
                        onLogout = onLogout
                    )
                }

                if (showConfirmSheet) {
                    ExpenseConfirmationSheet(
                        amount = 450.0,
                        merchant = "Swiggy",
                        suggestedCategory = "Food & Dining",
                        onConfirm = { selectedCat ->
                            scope.launch {
                                db.insert(
                                    TransactionEntity(
                                        amount = 450.0,
                                        type = "EXPENSE",
                                        category = selectedCat,
                                        merchant = "Swiggy",
                                        accountOrCard = "Default Card",
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
            permissionLauncher.launch(missing.toTypedArray())
        }
    }
}
