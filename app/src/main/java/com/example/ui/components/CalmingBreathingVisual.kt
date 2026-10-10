package com.example.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay

enum class BreathingPhase(val label: String, val subtext: String, val durationMs: Int) {
    INHALE("Breathe In", "Inhale smoothly through your nose", 4000),
    HOLD("Hold", "Maintain a gentle, natural pause", 2000),
    EXHALE("Breathe Out", "Release slowly through your mouth", 4000),
    REST("Relax", "Feel your shoulders drop and muscles soften", 2000)
}

@Composable
fun CalmingBreathingVisual(
    modifier: Modifier = Modifier,
    isRelaxing: Boolean = true
) {
    val infiniteTransition = rememberInfiniteTransition(label = "pulseTransition")

    // Rotation angle for atmospheric soothing ripples
    val rotationAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(24000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "rotation"
    )

    // Breathing phase loop
    var currentPhase by remember { mutableStateOf(BreathingPhase.INHALE) }
    val breathScale = remember { Animatable(0.7f) }

    LaunchedEffect(isRelaxing) {
        if (!isRelaxing) {
            breathScale.snapTo(0.85f)
            return@LaunchedEffect
        }
        while (true) {
            // INHALE (4s)
            currentPhase = BreathingPhase.INHALE
            breathScale.animateTo(
                targetValue = 1.0f,
                animationSpec = tween(durationMillis = 4000, easing = FastOutSlowInEasing)
            )

            // HOLD (2s)
            currentPhase = BreathingPhase.HOLD
            delay(2000)

            // EXHALE (4s)
            currentPhase = BreathingPhase.EXHALE
            breathScale.animateTo(
                targetValue = 0.7f,
                animationSpec = tween(durationMillis = 4000, easing = FastOutSlowInEasing)
            )

            // REST (2s)
            currentPhase = BreathingPhase.REST
            delay(2000)
        }
    }

    val primaryColor = MaterialTheme.colorScheme.primary
    val secondaryColor = MaterialTheme.colorScheme.secondary
    val tertiaryColor = MaterialTheme.colorScheme.tertiary

    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(240.dp)
                .clip(CircleShape),
            contentAlignment = Alignment.Center
        ) {
            // Atmospheric canvas with glowing multi-ring breathing orb
            Canvas(modifier = Modifier.fillMaxSize()) {
                val center = Offset(size.width / 2f, size.height / 2f)
                val baseRadius = (size.minDimension / 2.3f) * breathScale.value

                // Outer ambient glow ring
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primaryColor.copy(alpha = 0.28f),
                            secondaryColor.copy(alpha = 0.12f),
                            Color.Transparent
                        ),
                        center = center,
                        radius = baseRadius * 1.35f
                    ),
                    radius = baseRadius * 1.35f,
                    center = center
                )

                // Middle aura pulsing ripple
                drawCircle(
                    color = primaryColor.copy(alpha = 0.18f),
                    radius = baseRadius * 1.15f,
                    center = center,
                    style = Stroke(width = 4.dp.toPx())
                )

                // Secondary soft dashed/colored ripple
                drawCircle(
                    color = secondaryColor.copy(alpha = 0.35f),
                    radius = baseRadius * 0.95f,
                    center = center,
                    style = Stroke(width = 2.dp.toPx())
                )

                // Main soothing center sphere
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            secondaryColor.copy(alpha = 0.45f),
                            primaryColor.copy(alpha = 0.85f)
                        ),
                        center = Offset(center.x - 15f, center.y - 15f),
                        radius = baseRadius * 0.82f
                    ),
                    radius = baseRadius * 0.82f,
                    center = center
                )

                // Inner core highlight
                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            Color.White.copy(alpha = 0.5f),
                            Color.Transparent
                        ),
                        center = Offset(center.x - 20f, center.y - 20f),
                        radius = baseRadius * 0.35f
                    ),
                    radius = baseRadius * 0.35f,
                    center = Offset(center.x - 15f, center.y - 15f)
                )
            }

            // Text overlaid in center of breathing circle
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
                modifier = Modifier.padding(16.dp)
            ) {
                Text(
                    text = if (isRelaxing) currentPhase.label else "Ready",
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    ),
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = if (isRelaxing) currentPhase.subtext else "Press Start to begin guided calm relaxation",
            style = MaterialTheme.typography.bodyMedium.copy(
                fontWeight = FontWeight.Medium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            ),
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        )
    }
}
