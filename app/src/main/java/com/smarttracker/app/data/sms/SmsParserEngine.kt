package com.smarttracker.app.data.sms

import com.smarttracker.app.data.model.ParsedTransaction
import com.smarttracker.app.data.model.TransactionType
import java.util.regex.Pattern

object SmsParserEngine {
    private val EXCLUSIONS = listOf(
        Pattern.compile("(?i)\\b(otp|verification code|secret code)\\b"),
        Pattern.compile("(?i)\\b(pre-approved|apply now|congratulations)\\b")
    )

    private val AMOUNT_PATTERN = Pattern.compile("(?i)(?:rs\\.?|inr)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)")
    private val DEBIT_PATTERN = Pattern.compile("(?i)\\b(debited|spent|paid|charged|transferred to|sent to)\\b")
    private val CREDIT_PATTERN = Pattern.compile("(?i)\\b(credited|received|deposited|refunded)\\b")
    private val CARD_ACC_PATTERN = Pattern.compile("(?i)(?:a/c|acct|ac|card|ending)\\s*(?:no\\.?)?\\s*[x*]*([0-9]{3,4})")
    private val BALANCE_PATTERN = Pattern.compile("(?i)(?:bal|balance)\\s*(?:is)?\\s*(?:rs\\.?|inr)\\s*([0-9,]+(?:\\.[0-9]{1,2})?)")
    private val VENDOR_PATTERN = Pattern.compile("(?i)(?:at|to|vpa)\\s+([A-Za-z0-9.\\-_@]{3,24})")

    fun parse(sender: String, messageBody: String): ParsedTransaction? {
        if (EXCLUSIONS.any { it.matcher(messageBody).find() }) return null

        val amountMatcher = AMOUNT_PATTERN.matcher(messageBody)
        if (!amountMatcher.find()) return null
        val amount = amountMatcher.group(1)?.replace(",", "")?.toDoubleOrNull() ?: return null

        val isDebit = DEBIT_PATTERN.matcher(messageBody).find()
        val isCredit = CREDIT_PATTERN.matcher(messageBody).find()
        val isRefund = messageBody.contains("refund", ignoreCase = true)

        val txType = when {
            isRefund -> TransactionType.REFUND
            isDebit -> TransactionType.EXPENSE
            isCredit -> TransactionType.INCOME
            else -> return null
        }

        val accountMatcher = CARD_ACC_PATTERN.matcher(messageBody)
        val account = if (accountMatcher.find()) "XX" + accountMatcher.group(1) else "Bank Account"

        val balMatcher = BALANCE_PATTERN.matcher(messageBody)
        val balance = if (balMatcher.find()) balMatcher.group(1)?.replace(",", "")?.toDoubleOrNull() else null

        val vendorMatcher = VENDOR_PATTERN.matcher(messageBody)
        val merchant = if (vendorMatcher.find()) vendorMatcher.group(1)?.trim() ?: "Merchant" else "Merchant"

        val refMatcher = Pattern.compile("(?i)(?:ref|rrn|upi ref)[:\\s]*([0-9]{6,16})").matcher(messageBody)
        val refNo = if (refMatcher.find()) refMatcher.group(1) else null

        return ParsedTransaction(
            amount = amount,
            type = txType,
            merchant = merchant,
            accountOrCard = account,
            balance = balance,
            referenceNumber = refNo,
            rawSender = sender,
            rawBody = messageBody
        )
    }

    fun predictCategory(merchant: String, rawBody: String): String {
        val content = "${merchant.lowercase()} ${rawBody.lowercase()}"
        return when {
            listOf("swiggy", "zomato", "restaurant", "cafe", "dhaba").any { content.contains(it) } -> "Food & Dining"
            listOf("blinkit", "zepto", "instamart", "bigbasket", "dmart").any { content.contains(it) } -> "Groceries"
            listOf("uber", "ola", "rapido", "metro", "fuel", "petrol").any { content.contains(it) } -> "Transportation"
            listOf("amazon", "flipkart", "myntra", "ajio").any { content.contains(it) } -> "Shopping"
            listOf("bescom", "airtel", "jio", "electricity", "broadband").any { content.contains(it) } -> "Bills & Utilities"
            else -> "Other Expenses"
        }
    }
}
