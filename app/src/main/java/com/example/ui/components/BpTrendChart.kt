package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BpReading
import com.example.ui.theme.BpHighCoral
import com.example.ui.theme.BpLowBlue
import com.example.ui.theme.BpNormalGreen
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.abs

@Composable
fun BpTrendChart(
    readings: List<BpReading>,
    modifier: Modifier = Modifier
) {
    // Readings sorted chronologically
    val sortedReadings = remember(readings) {
        readings.sortedBy { it.timestamp }
    }

    var selectedReadingIndex by remember { mutableStateOf<Int?>(null) }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("bp_trend_chart_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header with title & Legend
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "Blood Pressure Trends",
                        style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "Tap any data point to inspect details",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Legend Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = MaterialTheme.colorScheme.primary, text = "SYS (Systolic)")
                LegendItem(color = MaterialTheme.colorScheme.secondary, text = "DIA (Diastolic)")
                LegendItem(color = Color(0x334CAF50), text = "Target ±10 Zone", isBox = true)
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (sortedReadings.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "No readings logged in this period yet.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                val primaryColor = MaterialTheme.colorScheme.primary
                val secondaryColor = MaterialTheme.colorScheme.secondary
                val gridColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f)
                val targetZoneColor = Color(0x224CAF50)
                val onSurfaceColor = MaterialTheme.colorScheme.onSurface

                // Canvas Chart
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(220.dp)
                ) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(sortedReadings) {
                                detectTapGestures { tapOffset ->
                                    val paddingLeft = 40.dp.toPx()
                                    val paddingRight = 16.dp.toPx()
                                    val chartWidth = size.width - paddingLeft - paddingRight

                                    if (sortedReadings.size == 1) {
                                        selectedReadingIndex = 0
                                    } else {
                                        val stepX = chartWidth / (sortedReadings.size - 1)
                                        var closestIdx = 0
                                        var minDiff = Float.MAX_VALUE

                                        sortedReadings.indices.forEach { i ->
                                            val ptX = paddingLeft + (i * stepX)
                                            val diff = abs(tapOffset.x - ptX)
                                            if (diff < minDiff) {
                                                minDiff = diff
                                                closestIdx = i
                                            }
                                        }
                                        selectedReadingIndex = if (minDiff < stepX * 0.8f) closestIdx else null
                                    }
                                }
                            }
                    ) {
                        val paddingLeft = 40.dp.toPx()
                        val paddingRight = 16.dp.toPx()
                        val paddingTop = 16.dp.toPx()
                        val paddingBottom = 30.dp.toPx()

                        val chartWidth = size.width - paddingLeft - paddingRight
                        val chartHeight = size.height - paddingTop - paddingBottom

                        // Y-scale: 50 mmHg to 180 mmHg
                        val minY = 50f
                        val maxY = 180f
                        fun yToPx(v: Float): Float {
                            val ratio = (v - minY) / (maxY - minY)
                            return paddingTop + chartHeight * (1f - ratio.coerceIn(0f, 1f))
                        }

                        // Draw Target Corridor:
                        // Systolic standard target zone: 110 to 130
                        val sysTargetTop = yToPx(130f)
                        val sysTargetBottom = yToPx(110f)
                        drawRect(
                            color = targetZoneColor,
                            topLeft = Offset(paddingLeft, sysTargetTop),
                            size = Size(chartWidth, sysTargetBottom - sysTargetTop)
                        )

                        // Diastolic standard target zone: 70 to 90
                        val diaTargetTop = yToPx(90f)
                        val diaTargetBottom = yToPx(70f)
                        drawRect(
                            color = targetZoneColor,
                            topLeft = Offset(paddingLeft, diaTargetTop),
                            size = Size(chartWidth, diaTargetBottom - diaTargetTop)
                        )

                        // Draw Grid lines (60, 80, 100, 120, 140, 160)
                        val yLevels = listOf(60, 80, 100, 120, 140, 160)
                        val textPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.GRAY
                            textSize = 26f
                            isAntiAlias = true
                        }

                        yLevels.forEach { lvl ->
                            val y = yToPx(lvl.toFloat())
                            val isStandardLine = lvl == 120 || lvl == 80
                            drawLine(
                                color = if (isStandardLine) Color(0xFF4CAF50).copy(alpha = 0.5f) else gridColor,
                                start = Offset(paddingLeft, y),
                                end = Offset(size.width - paddingRight, y),
                                strokeWidth = if (isStandardLine) 2.dp.toPx() else 1.dp.toPx(),
                                pathEffect = if (isStandardLine) PathEffect.dashPathEffect(floatArrayOf(10f, 8f)) else null
                            )

                            drawContext.canvas.nativeCanvas.drawText(
                                lvl.toString(),
                                6.dp.toPx(),
                                y + 8f,
                                textPaint
                            )
                        }

                        val stepX = if (sortedReadings.size > 1) {
                            chartWidth / (sortedReadings.size - 1)
                        } else chartWidth / 2f

                        // Plot Systolic & Diastolic lines
                        val sysPath = Path()
                        val diaPath = Path()

                        val sysPoints = mutableListOf<Offset>()
                        val diaPoints = mutableListOf<Offset>()

                        sortedReadings.forEachIndexed { i, r ->
                            val x = if (sortedReadings.size == 1) {
                                paddingLeft + chartWidth / 2f
                            } else {
                                paddingLeft + (i * stepX)
                            }

                            val ySys = yToPx(r.systolic.toFloat())
                            val yDia = yToPx(r.diastolic.toFloat())

                            sysPoints.add(Offset(x, ySys))
                            diaPoints.add(Offset(x, yDia))

                            if (i == 0) {
                                sysPath.moveTo(x, ySys)
                                diaPath.moveTo(x, yDia)
                            } else {
                                sysPath.lineTo(x, ySys)
                                diaPath.lineTo(x, yDia)
                            }
                        }

                        if (sortedReadings.size > 1) {
                            // Draw continuous lines
                            drawPath(
                                path = sysPath,
                                color = primaryColor,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                            drawPath(
                                path = diaPath,
                                color = secondaryColor,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Draw point markers
                        sysPoints.forEachIndexed { i, pt ->
                            val isSelected = selectedReadingIndex == i
                            drawCircle(
                                color = Color.White,
                                radius = if (isSelected) 7.dp.toPx() else 5.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = primaryColor,
                                radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                                center = pt
                            )
                        }

                        diaPoints.forEachIndexed { i, pt ->
                            val isSelected = selectedReadingIndex == i
                            drawCircle(
                                color = Color.White,
                                radius = if (isSelected) 7.dp.toPx() else 5.dp.toPx(),
                                center = pt
                            )
                            drawCircle(
                                color = secondaryColor,
                                radius = if (isSelected) 5.dp.toPx() else 3.5.dp.toPx(),
                                center = pt
                            )
                        }

                        // Selected indicator vertical bar
                        selectedReadingIndex?.let { selIdx ->
                            if (selIdx in sysPoints.indices) {
                                val x = sysPoints[selIdx].x
                                drawLine(
                                    color = primaryColor.copy(alpha = 0.5f),
                                    start = Offset(x, paddingTop),
                                    end = Offset(x, paddingTop + chartHeight),
                                    strokeWidth = 2.dp.toPx(),
                                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(6f, 6f))
                                )
                            }
                        }
                    }
                }

                // Selected point details card
                selectedReadingIndex?.let { selIdx ->
                    if (selIdx in sortedReadings.indices) {
                        val reading = sortedReadings[selIdx]
                        val feedback = reading.getFeedback()
                        val dateFormat = SimpleDateFormat("MMM d, yyyy • h:mm a", Locale.getDefault())

                        Spacer(modifier = Modifier.height(10.dp))
                        Surface(
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(12.dp),
                            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.65f)
                        ) {
                            Column(modifier = Modifier.padding(12.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = dateFormat.format(Date(reading.timestamp)),
                                        style = MaterialTheme.typography.labelMedium,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                    Text(
                                        text = feedback.title,
                                        style = MaterialTheme.typography.labelMedium.copy(
                                            fontWeight = FontWeight.Bold,
                                            color = when (feedback.classification) {
                                                com.example.data.model.BpClassification.NORMAL -> BpNormalGreen
                                                com.example.data.model.BpClassification.HIGH -> BpHighCoral
                                                com.example.data.model.BpClassification.LOW -> BpLowBlue
                                                com.example.data.model.BpClassification.MIXED -> BpHighCoral
                                            }
                                        )
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Row(
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                                ) {
                                    Text(
                                        text = "SYS: ${reading.systolic} mmHg",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.primary
                                    )
                                    Text(
                                        text = "DIA: ${reading.diastolic} mmHg",
                                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Bold),
                                        color = MaterialTheme.colorScheme.secondary
                                    )
                                    reading.pulse?.let {
                                        Text(
                                            text = "Pulse: $it bpm",
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                    }
                                    Text(
                                        text = "Arm: ${reading.arm}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (reading.notes.isNotBlank()) {
                                    Spacer(modifier = Modifier.height(4.dp))
                                    Text(
                                        text = "Note: ${reading.notes}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LegendItem(
    color: Color,
    text: String,
    isBox: Boolean = false
) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        if (isBox) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .background(color, shape = RoundedCornerShape(2.dp))
            )
        } else {
            Box(
                modifier = Modifier
                    .size(10.dp)
                    .background(color, shape = CircleShape)
            )
        }
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
