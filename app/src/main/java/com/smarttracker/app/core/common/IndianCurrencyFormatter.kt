package com.smarttracker.app.core.common

import java.text.DecimalFormat

object IndianCurrencyFormatter {
    fun format(amount: Double): String {
        val formatter = DecimalFormat("##,##,##0.00")
        return "₹" + formatter.format(amount)
    }
}
