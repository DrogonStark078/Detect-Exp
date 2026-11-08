package com.smarttracker.app.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.smarttracker.app.core.common.IndianCurrencyFormatter
import com.smarttracker.app.data.model.CategoryShare

@Composable
fun AnalyticsScreen(shares: List<CategoryShare>) {
    val total = shares.sumOf { it.amount }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
    ) {
        Text("Expense Analytics", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(220.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.size(180.dp)) {
                var startAngle = -90f
                val strokeWidth = 32.dp.toPx()

                shares.forEach { share ->
                    val sweep = if (total > 0) ((share.amount / total) * 360f).toFloat() else 0f
                    drawArc(
                        color = share.color,
                        startAngle = startAngle,
                        sweepAngle = sweep,
                        useCenter = false,
                        style = Stroke(width = strokeWidth)
                    )
                    startAngle += sweep
                }
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Total", style = MaterialTheme.typography.labelSmall)
                Text(IndianCurrencyFormatter.format(total), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}
