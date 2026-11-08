package com.smarttracker.app.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.smarttracker.app.MainActivity
import com.smarttracker.app.core.common.IndianCurrencyFormatter

class SmsNotificationService(private val context: Context) {
    private val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val CHANNEL_ID = "detex_transactions"
        const val ACTION_CONFIRM = "com.smarttracker.ACTION_CONFIRM"
        const val ACTION_IGNORE = "com.smarttracker.ACTION_IGNORE"
        const val EXTRA_TX_ID = "extra_tx_id"
    }

    init {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Transaction Alerts",
                NotificationManager.IMPORTANCE_HIGH
            )
            manager.createNotificationChannel(channel)
        }
    }

    fun showConfirmationNotification(id: Long, amount: Double, merchant: String, category: String) {
        val openApp = PendingIntent.getActivity(
            context, id.toInt(),
            Intent(context, MainActivity::class.java),
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val confirmIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_CONFIRM
            putExtra(EXTRA_TX_ID, id)
        }
        val confirmPi = PendingIntent.getBroadcast(
            context, (id * 10 + 1).toInt(), confirmIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val ignoreIntent = Intent(context, NotificationActionReceiver::class.java).apply {
            action = ACTION_IGNORE
            putExtra(EXTRA_TX_ID, id)
        }
        val ignorePi = PendingIntent.getBroadcast(
            context, (id * 10 + 2).toInt(), ignoreIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val formatted = IndianCurrencyFormatter.format(amount)
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("$formatted spent at $merchant")
            .setContentText("Suggested: $category")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(openApp)
            .setAutoCancel(true)
            .addAction(android.R.drawable.ic_input_add, "Confirm", confirmPi)
            .addAction(android.R.drawable.ic_delete, "Ignore", ignorePi)
            .build()

        manager.notify(id.toInt(), notification)
    }
}
