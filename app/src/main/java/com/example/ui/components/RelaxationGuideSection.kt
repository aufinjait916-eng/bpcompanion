package com.example.ui.components

import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AirlineSeatReclineNormal
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.HourglassBottom
import androidx.compose.material.icons.filled.HourglassEmpty
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelfImprovement
import androidx.compose.material.icons.filled.SkipNext
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import java.util.Locale

enum class TimerState {
    IDLE,
    RUNNING,
    PAUSED,
    COMPLETED
}

@Composable
fun RelaxationGuideSection(
    modifier: Modifier = Modifier,
    onReadyToLog: () -> Unit = {},
    resetKey: Int = 0
) {
    val context = LocalContext.current
    var selectedMinutes by remember { mutableIntStateOf(2) } // default 2 minutes (options 1, 2, 3, 5)
    var timerState by remember { mutableStateOf(TimerState.IDLE) }
    var remainingSeconds by remember { mutableIntStateOf(selectedMinutes * 60) }

    // Synchronize timer reset if minutes changed while idle
    LaunchedEffect(selectedMinutes) {
        if (timerState == TimerState.IDLE) {
            remainingSeconds = selectedMinutes * 60
        }
    }

    // Reset timer when resetKey changes
    LaunchedEffect(resetKey) {
        if (resetKey > 0) {
            timerState = TimerState.IDLE
            remainingSeconds = selectedMinutes * 60
        }
    }

    // Countdown timer logic
    LaunchedEffect(timerState) {
        if (timerState == TimerState.RUNNING) {
            while (remainingSeconds > 0 && timerState == TimerState.RUNNING) {
                delay(1000)
                remainingSeconds -= 1
            }
            if (remainingSeconds <= 0 && timerState == TimerState.RUNNING) {
                timerState = TimerState.COMPLETED
                triggerGentleHaptic(context)
                onReadyToLog()
            }
        }
    }

    val totalDurationSeconds = selectedMinutes * 60
    val progress = if (totalDurationSeconds > 0) {
        (totalDurationSeconds - remainingSeconds).toFloat() / totalDurationSeconds.toFloat()
    } else 0f

    Card(
        modifier = modifier
            .fillMaxWidth()
            .animateContentSize()
            .testTag("relaxation_guide_card"),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Header Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .background(
                                MaterialTheme.colorScheme.primaryContainer,
                                shape = CircleShape
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.SelfImprovement,
                            contentDescription = "Relaxation",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "Pre-Reading Relaxation",
                            style = MaterialTheme.typography.titleMedium.copy(fontWeight = FontWeight.Bold)
                        )
                        Text(
                            text = "Clinical practice recommends resting 1–5 min",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Sit straight posture instructions
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Text(
                        text = "Posture Checklist before measuring:",
                        style = MaterialTheme.typography.labelLarge.copy(
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.primary
                        )
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    PostureItem(
                        icon = Icons.Default.AirlineSeatReclineNormal,
                        text = "Sit straight in a relaxed position with back supported"
                    )
                    PostureItem(
                        icon = Icons.Default.CheckCircle,
                        text = "Keep both feet flat down on the floor (do not cross legs)"
                    )
                    PostureItem(
                        icon = Icons.Default.CheckCircle,
                        text = "Rest your arm on a table at heart level, palm facing up"
                    )
                    PostureItem(
                        icon = Icons.Default.CheckCircle,
                        text = "Breathe normally and stay still & quiet during rest"
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Timer length selection chips (when idle or completed)
            if (timerState == TimerState.IDLE) {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Choose relaxation duration:",
                        style = MaterialTheme.typography.bodyMedium.copy(fontWeight = FontWeight.Medium),
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        listOf(1, 2, 3, 5).forEach { mins ->
                            val isSelected = selectedMinutes == mins
                            FilterChip(
                                selected = isSelected,
                                onClick = {
                                    selectedMinutes = mins
                                    remainingSeconds = mins * 60
                                },
                                label = {
                                    Text(
                                        text = "$mins min",
                                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = MaterialTheme.colorScheme.primary,
                                    selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                                ),
                                modifier = Modifier.testTag("timer_chip_${mins}m")
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(16.dp))
            }

            // Calming Animated Breathing Visual
            CalmingBreathingVisual(
                modifier = Modifier.fillMaxWidth(),
                isRelaxing = timerState == TimerState.RUNNING
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Timer Display (MM:SS)
            val minutesStr = String.format(Locale.US, "%02d", remainingSeconds / 60)
            val secondsStr = String.format(Locale.US, "%02d", remainingSeconds % 60)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = if (timerState == TimerState.RUNNING) Icons.Default.HourglassBottom else Icons.Default.HourglassEmpty,
                    contentDescription = "Timer",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "$minutesStr:$secondsStr",
                    style = MaterialTheme.typography.headlineMedium.copy(
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                )
            }

            if (timerState == TimerState.RUNNING || timerState == TimerState.PAUSED) {
                Spacer(modifier = Modifier.height(8.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier
                        .fillMaxWidth(0.8f)
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = MaterialTheme.colorScheme.primary,
                    trackColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Action Buttons
            when (timerState) {
                TimerState.IDLE -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = {
                                remainingSeconds = selectedMinutes * 60
                                timerState = TimerState.RUNNING
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(50.dp)
                                .testTag("start_relaxation_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Start Calm Timer (${selectedMinutes}m)")
                        }

                        OutlinedButton(
                            onClick = { onReadyToLog() },
                            modifier = Modifier
                                .height(50.dp)
                                .testTag("skip_to_log_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Text("Log Now")
                        }
                    }
                }

                TimerState.RUNNING -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        OutlinedButton(
                            onClick = { timerState = TimerState.PAUSED },
                            modifier = Modifier
                                .weight(0.33f)
                                .height(48.dp)
                                .testTag("pause_timer_button"),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 4.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Pause, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Pause")
                        }

                        Button(
                            onClick = {
                                timerState = TimerState.COMPLETED
                                onReadyToLog()
                            },
                            modifier = Modifier
                                .weight(0.67f)
                                .height(48.dp)
                                .testTag("skip_to_measure_button"),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 8.dp, vertical = 0.dp),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.SkipNext, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "I'm Relaxed, Measure",
                                style = MaterialTheme.typography.labelLarge.copy(fontWeight = FontWeight.SemiBold),
                                maxLines = 1,
                                softWrap = false
                            )
                        }
                    }
                }

                TimerState.PAUSED -> {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = { timerState = TimerState.RUNNING },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("resume_timer_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Resume")
                        }

                        OutlinedButton(
                            onClick = {
                                timerState = TimerState.IDLE
                                remainingSeconds = selectedMinutes * 60
                            },
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("reset_timer_button"),
                            shape = RoundedCornerShape(14.dp)
                        ) {
                            Icon(Icons.Default.Refresh, contentDescription = null)
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Reset")
                        }
                    }
                }

                TimerState.COMPLETED -> {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(14.dp),
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(24.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Relaxation Completed!",
                                    style = MaterialTheme.typography.titleMedium.copy(
                                        fontWeight = FontWeight.Bold,
                                        color = MaterialTheme.colorScheme.onPrimaryContainer
                                    )
                                )
                            }
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "Your body is relaxed. Turn on your BP monitor, take your reading, and log the values below.",
                                style = MaterialTheme.typography.bodyMedium,
                                textAlign = TextAlign.Center,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Row(
                                horizontalArrangement = Arrangement.spacedBy(10.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Button(
                                    onClick = { onReadyToLog() },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Log Reading Now")
                                }
                                OutlinedButton(
                                    onClick = {
                                        timerState = TimerState.IDLE
                                        remainingSeconds = selectedMinutes * 60
                                    },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Reset Timer")
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
private fun PostureItem(icon: androidx.compose.ui.graphics.vector.ImageVector, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.Normal),
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

private fun triggerGentleHaptic(context: Context) {
    try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
            vibratorManager?.defaultVibrator?.vibrate(
                VibrationEffect.createWaveform(longArrayOf(0, 150, 100, 200), -1)
            )
        } else {
            @Suppress("DEPRECATION")
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            @Suppress("DEPRECATION")
            vibrator?.vibrate(250)
        }
    } catch (_: Exception) {
        // Safe fallback
    }
}
