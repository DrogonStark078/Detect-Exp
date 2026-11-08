package com.smarttracker.app.service

import android.app.NotificationManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.smarttracker.app.SmartExpenseApp
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class NotificationActionReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val txId = intent.getLongExtra(SmsNotificationService.EXTRA_TX_ID, -1L)
        if (txId == -1L) return

        val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.cancel(txId.toInt())

        val db = SmartExpenseApp.instance.database.transactionDao()
        CoroutineScope(Dispatchers.IO).launch {
            when (intent.action) {
                SmsNotificationService.ACTION_CONFIRM -> {
                    db.confirmTransaction(txId, "Food & Dining")
                }
                SmsNotificationService.ACTION_IGNORE -> {
                    db.deleteById(txId)
                }
            }
        }
    }
}
