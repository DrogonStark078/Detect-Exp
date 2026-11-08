package com.smarttracker.app.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.smarttracker.app.core.common.IndianCurrencyFormatter

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExpenseConfirmationSheet(
    amount: Double,
    merchant: String,
    suggestedCategory: String,
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var selectedCat by remember { mutableStateOf(suggestedCategory) }
    val categories = listOf("Food & Dining", "Groceries", "Shopping", "Transportation", "Bills & Utilities", "Other Expenses")

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text("Verify Transaction", style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text(IndianCurrencyFormatter.format(amount), style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold)
            Text("Merchant: $merchant", style = MaterialTheme.typography.bodyMedium)

            Spacer(modifier = Modifier.height(16.dp))
            Text("Select Category", style = MaterialTheme.typography.labelLarge)
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(categories) { cat ->
                    FilterChip(
                        selected = cat == selectedCat,
                        onClick = { selectedCat = cat },
                        label = { Text(cat) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedButton(onClick = onDismiss, modifier = Modifier.weight(1f)) { Text("Ignore") }
                Button(onClick = { onConfirm(selectedCat) }, modifier = Modifier.weight(1f)) { Text("Confirm") }
            }
        }
    }
}
