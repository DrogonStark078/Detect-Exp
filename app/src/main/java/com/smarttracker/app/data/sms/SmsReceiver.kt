package com.smarttracker.app.data.sms

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.provider.Telephony
import com.smarttracker.app.SmartExpenseApp
import com.smarttracker.app.data.local.TransactionEntity
import com.smarttracker.app.service.SmsNotificationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class SmsReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Telephony.Sms.Intents.SMS_RECEIVED_ACTION) return

        val messages = Telephony.Sms.Intents.getMessagesFromIntent(intent) ?: return
        val db = SmartExpenseApp.instance.database.transactionDao()
        val notifier = SmsNotificationService(context)

        for (sms in messages) {
            val sender = sms.originatingAddress ?: ""
            val body = sms.messageBody ?: ""

            val parsed = SmsParserEngine.parse(sender, body) ?: continue
            val predictedCat = SmsParserEngine.predictCategory(parsed.merchant, body)

            CoroutineScope(Dispatchers.IO).launch {
                val entity = TransactionEntity(
                    amount = parsed.amount,
                    type = parsed.type.name,
                    category = predictedCat,
                    merchant = parsed.merchant,
                    accountOrCard = parsed.accountOrCard,
                    balanceAfterTx = parsed.balance,
                    referenceNumber = parsed.referenceNumber,
                    timestamp = parsed.timestamp,
                    isConfirmed = false,
                    rawSmsBody = parsed.rawBody
                )
                val id = db.insert(entity)
                notifier.showConfirmationNotification(id, parsed.amount, parsed.merchant, predictedCat)
            }
        }
    }
}
