package com.example.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.BpClassification
import com.example.data.model.BpReading
import com.example.ui.theme.BpHighCoral
import com.example.ui.theme.BpLowBlue
import com.example.ui.theme.BpNormalGreen

@Composable
fun BpStatsSection(
    readings: List<BpReading>,
    modifier: Modifier = Modifier
) {
    if (readings.isEmpty()) return

    val count = readings.size
    val avgSys = (readings.map { it.systolic }.average()).toInt()
    val avgDia = (readings.map { it.diastolic }.average()).toInt()

    val pulses = readings.mapNotNull { it.pulse }
    val avgPulse = if (pulses.isNotEmpty()) pulses.average().toInt() else null

    val normalCount = readings.count { it.getFeedback().classification == BpClassification.NORMAL }
    val normalPercent = (normalCount * 100) / count

    val minSys = readings.minOf { it.systolic }
    val maxSys = readings.maxOf { it.systolic }
    val minDia = readings.minOf { it.diastolic }
    val maxDia = readings.maxOf { it.diastolic }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bp_stats_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "Period Summary & Stats",
                style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = "Based on $count recorded readings",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 2x2 Stats Grid
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMetricBox(
                    title = "Average BP",
                    value = "$avgSys / $avgDia",
                    unit = "mmHg",
                    subtext = "Target: 120/80 (±10)",
                    highlightColor = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.weight(1f)
                )

                StatMetricBox(
                    title = "Within ±10 Standard",
                    value = "$normalPercent%",
                    unit = "$normalCount of $count readings",
                    subtext = "Healthy corridor",
                    highlightColor = BpNormalGreen,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                StatMetricBox(
                    title = "BP Range (Min / Max)",
                    value = "$minSys–$maxSys",
                    unit = "SYS ($minDia–$maxDia DIA)",
                    subtext = "Spread of values",
                    highlightColor = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier.weight(1f)
                )

                StatMetricBox(
                    title = "Avg Heart Rate",
                    value = if (avgPulse != null) "$avgPulse" else "--",
                    unit = "BPM",
                    subtext = "Resting pulse",
                    highlightColor = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun StatMetricBox(
    title: String,
    value: String,
    unit: String,
    subtext: String,
    highlightColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(14.dp),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
    ) {
        Column(
            modifier = Modifier.padding(12.dp)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = value,
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = highlightColor
                    )
                )
            }
            Text(
                text = unit,
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = subtext,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
