package com.bugsby.companion.character

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import kotlin.random.Random

@Composable
fun SuperSaiyanAura(
    modifier: Modifier = Modifier,
    isSuperSaiyan: Boolean
) {
    if (!isSuperSaiyan) return

    val infiniteTransition = rememberInfiniteTransition(label = "ssj_aura")
    
    // Golden pulse scale
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.25f,
        animationSpec = infiniteRepeatable(
            animation = tween(400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse"
    )

    // Jitter rotation
    val jitterAngle by infiniteTransition.animateFloat(
        initialValue = -3f,
        targetValue = 3f,
        animationSpec = infiniteRepeatable(
            animation = tween(120, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "jitter"
    )

    Canvas(modifier = modifier.fillMaxSize()) {
        val center = Offset(size.width / 2f, size.height / 2f)
        val baseRadius = (size.minDimension / 2f) * pulseScale

        // Multi-layer glowing golden & orange aura
        val auraBrush = Brush.radialGradient(
            colors = listOf(
                Color(0xFFFFF7A0).copy(alpha = 0.85f),
                Color(0xFFFFD700).copy(alpha = 0.65f),
                Color(0xFFFF9100).copy(alpha = 0.40f),
                Color(0xFFFF3D00).copy(alpha = 0.05f),
                Color.Transparent
            ),
            center = center,
            radius = baseRadius * 1.35f
        )
        drawCircle(brush = auraBrush, radius = baseRadius * 1.35f, center = center)

        // Electric lightning sparks (Super Saiyan 2 style arcs)
        drawElectricArcs(center, baseRadius)
    }
}

private fun DrawScope.drawElectricArcs(center: Offset, radius: Float) {
    val rng = Random(System.currentTimeMillis() / 80)
    val arcCount = 6
    for (i in 0 until arcCount) {
        val angle = rng.nextFloat() * 360f
        val rad = Math.toRadians(angle.toDouble())
        val dist1 = radius * (0.6f + rng.nextFloat() * 0.3f)
        val dist2 = radius * (1.0f + rng.nextFloat() * 0.4f)
        
        val p1 = Offset(
            (center.x + dist1 * Math.cos(rad)).toFloat(),
            (center.y + dist1 * Math.sin(rad)).toFloat()
        )
        val p2 = Offset(
            (center.x + dist2 * Math.cos(rad + 0.15)).toFloat(),
            (center.y + dist2 * Math.sin(rad + 0.15)).toFloat()
        )
        
        drawLine(
            color = if (rng.nextBoolean()) Color(0xFF00E5FF) else Color(0xFFFFF9A6),
            start = p1,
            end = p2,
            strokeWidth = 3f + rng.nextFloat() * 3f
        )
    }
}
