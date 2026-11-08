package com.smarttracker.app.data.csv

import android.content.Context
import android.net.Uri
import com.smarttracker.app.data.local.TransactionEntity
import com.smarttracker.app.data.sms.SmsParserEngine
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object CsvManager {

    /**
     * Exports a list of transactions into CSV format via Android Storage Access Framework.
     */
    suspend fun exportTransactionsToUri(
        context: Context,
        uri: Uri,
        transactions: List<TransactionEntity>
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            context.contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    // Standard CSV Header
                    writer.write("ID,Date,Merchant,Category,Amount,Type,Account,Reference\n")
                    val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())

                    for (tx in transactions) {
                        val formattedDate = dateFormat.format(Date(tx.timestamp))
                        val safeMerchant = tx.merchant.replace(",", " ")
                        val safeCategory = tx.category.replace(",", " ")
                        val safeAccount = tx.accountOrCard.replace(",", " ")
                        val safeRef = (tx.referenceNumber ?: "").replace(",", " ")

                        writer.write(
                            "${tx.id}," +
                            "\"$formattedDate\"," +
                            "\"$safeMerchant\"," +
                            "\"$safeCategory\"," +
                            "${tx.amount}," +
                            "${tx.type}," +
                            "\"$safeAccount\"," +
                            "\"$safeRef\"\n"
                        )
                    }
                    writer.flush()
                }
            }
            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    /**
     * Reads and parses bank/credit-card statements or previously exported CSV files.
     * Automatically maps header variations like Amount/Debit/Withdrawal and Merchant/Description/Narrative.
     */
    suspend fun importTransactionsFromUri(
        context: Context,
        uri: Uri
    ): List<TransactionEntity> = withContext(Dispatchers.IO) {
        val importedList = mutableListOf<TransactionEntity>()

        try {
            context.contentResolver.openInputStream(uri)?.use { inputStream ->
                BufferedReader(InputStreamReader(inputStream)).use { reader ->
                    val headerLine = reader.readLine() ?: return@withContext emptyList()
                    val headers = headerLine.split(",").map { it.trim().lowercase().replace("\"", "") }

                    var dateIdx = headers.indexOfFirst { it.contains("date") || it.contains("txn date") }
                    var descIdx = headers.indexOfFirst { 
                        it.contains("merchant") || it.contains("description") || it.contains("narration") || it.contains("particulars")
                    }
                    var amountIdx = headers.indexOfFirst { 
                        it == "amount" || it.contains("debit") || it.contains("withdrawal") || it.contains("spent")
                    }
                    var catIdx = headers.indexOfFirst { it.contains("category") }
                    var refIdx = headers.indexOfFirst { it.contains("reference") || it.contains("ref") || it.contains("rrn") }

                    if (amountIdx == -1) amountIdx = 4 // Fallback default column index
                    if (descIdx == -1) descIdx = 2
                    if (dateIdx == -1) dateIdx = 1

                    var line: String?
                    val simpleDate = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                    while (reader.readLine().also { line = it } != null) {
                        val rawRow = line!!.trim()
                        if (rawRow.isBlank()) continue

                        // Regex handles CSV tokens containing commas within quotes
                        val tokens = rawRow.split(",(?=(?:[^\"]*\"[^\"]*\")*[^\"]*$)".toRegex())
                            .map { it.trim().replace("\"", "") }

                        if (tokens.size <= amountIdx) continue

                        val rawAmount = tokens.getOrNull(amountIdx)
                            ?.replace("[^0-9.]".toRegex(), "")
                            ?.toDoubleOrNull() ?: continue

                        val merchant = tokens.getOrNull(descIdx)?.ifBlank { "Statement Import" } ?: "Statement Import"
                        val refNo = if (refIdx != -1 && refIdx < tokens.size) tokens[refIdx] else null
                        val category = if (catIdx != -1 && catIdx < tokens.size && tokens[catIdx].isNotBlank()) {
                            tokens[catIdx]
                        } else {
                            SmsParserEngine.predictCategory(merchant, merchant)
                        }

                        val parsedDate = if (dateIdx != -1 && dateIdx < tokens.size) {
                            try {
                                simpleDate.parse(tokens[dateIdx])?.time ?: System.currentTimeMillis()
                            } catch (_: Exception) {
                                System.currentTimeMillis()
                            }
                        } else {
                            System.currentTimeMillis()
                        }

                        importedList.add(
                            TransactionEntity(
                                amount = rawAmount,
                                type = "EXPENSE",
                                category = category,
                                merchant = merchant.take(28),
                                accountOrCard = "CSV Import",
                                balanceAfterTx = null,
                                referenceNumber = refNo,
                                timestamp = parsedDate,
                                isConfirmed = true,
                                rawSmsBody = "Imported from CSV statement"
                            )
                        )
                    }
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        importedList
    }
}
