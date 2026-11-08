package com.smarttracker.app.data.model

enum class TransactionType {
    EXPENSE, INCOME, TRANSFER, REFUND, IGNORED
}

data class ParsedTransaction(
    val amount: Double,
    val type: TransactionType,
    val merchant: String,
    val accountOrCard: String,
    val balance: Double?,
    val referenceNumber: String?,
    val timestamp: Long = System.currentTimeMillis(),
    val rawSender: String,
    val rawBody: String
)

data class TransactionItemUiModel(
    val id: Long,
    val merchant: String,
    val category: String,
    val amount: Double,
    val isExpense: Boolean,
    val timeAgo: String
)

data class CategoryShare(
    val name: String,
    val amount: Double,
    val color: androidx.compose.ui.graphics.Color
)
