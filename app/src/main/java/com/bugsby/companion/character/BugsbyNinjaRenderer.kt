package com.bugsby.companion.character

import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun BugsbyNinjaCharacter(
    modifier: Modifier = Modifier,
    sizeDp: Dp = 110.dp,
    expression: Expression = Expression.IDLE,
    mode: CompanionMode = CompanionMode.NORMAL_2D,
    furTint: Color = Color(0xFF1E1E24)
) {
    val isSuperSaiyan = mode == CompanionMode.SUPER_SAIYAN_3D

    // Breathing Bob Animation
    val infiniteTransition = rememberInfiniteTransition(label = "ninja_idle")
    val breathingOffset by infiniteTransition.animateFloat(
        initialValue = -2.5f,
        targetValue = 2.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(1400, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breath"
    )

    // Periodic Blink
    val blinkProgress by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = keyframes {
                durationMillis = 3500
                1f at 0
                1f at 3100
                0.05f at 3200 // Eye closed
                1f at 3300 // Eye opened
                1f at 3500
            },
            repeatMode = RepeatMode.Restart
        ),
        label = "blink"
    )

    // Jitter for Super Saiyan energy
    val energyJitter by infiniteTransition.animateFloat(
        initialValue = -1.5f,
        targetValue = 1.5f,
        animationSpec = infiniteRepeatable(
            animation = tween(60, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "ssj_shake"
    )

    Box(
        modifier = modifier.size(sizeDp),
        contentAlignment = Alignment.Center
    ) {
        // Super Saiyan Aura layer behind the ninja
        SuperSaiyanAura(
            modifier = Modifier.fillMaxSize(),
            isSuperSaiyan = isSuperSaiyan
        )

        // Ninja Body & Katana
        Canvas(modifier = Modifier.fillMaxSize()) {
            val jitter = if (isSuperSaiyan) energyJitter else 0f
            val verticalShift = breathingOffset + jitter
            val center = Offset(size.width / 2f, (size.height / 2f) + verticalShift)
            val headRadius = size.minDimension * 0.32f

            // 1. Draw Katana strapped on back (Left side, angled up-left)
            drawKatana(center, headRadius)

            // 2. Draw Fluffy Hood / Ninja Body Silhouette
            drawNinjaBodyAndHood(center, headRadius, furTint, isSuperSaiyan)

            // 3. Draw Rope Sash (diagonal across chest)
            drawRopeSash(center, headRadius)

            // 4. Draw Face Slit (peeking peach/tan skin)
            drawFaceOpening(center, headRadius)

            // 5. Draw Cowl Mask (covering mouth up to eyes)
            drawCowlMask(center, headRadius, furTint, isSuperSaiyan)

            // 6. Draw Big Glossy Eyes & Expressions
            drawNinjaEyes(center, headRadius, expression, blinkProgress, isSuperSaiyan)
        }
    }
}

private fun DrawScope.drawKatana(center: Offset, radius: Float) {
    // Katana hilt & guard peaking from behind left shoulder
    val swordAngle = -42f
    val hiltOrigin = Offset(center.x - radius * 0.75f, center.y - radius * 0.35f)

    rotate(degrees = swordAngle, pivot = hiltOrigin) {
        // Blade Collar / Habaki
        drawRect(
            color = Color(0xFFC0C0C0),
            topLeft = Offset(hiltOrigin.x - 4f, hiltOrigin.y - 12f),
            size = Size(8f, 12f)
        )
        // Guard (Tsuba)
        drawRoundRect(
            color = Color(0xFF2A2A2A),
            topLeft = Offset(hiltOrigin.x - 12f, hiltOrigin.y - 16f),
            size = Size(24f, 6f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(3f, 3f)
        )
        // Handle / Tsuka (Black wrapped hilt)
        drawRoundRect(
            color = Color(0xFF151515),
            topLeft = Offset(hiltOrigin.x - 5f, hiltOrigin.y - 56f),
            size = Size(10f, 40f),
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(2f, 2f)
        )
        // Diamond wrapping texture (Ito)
        for (i in 0..3) {
            drawLine(
                color = Color(0xFF383838),
                start = Offset(hiltOrigin.x - 4f, hiltOrigin.y - 50f + i * 9f),
                end = Offset(hiltOrigin.x + 4f, hiltOrigin.y - 44f + i * 9f),
                strokeWidth = 2.5f
            )
        }
        // Pommel (Kashira)
        drawCircle(
            color = Color(0xFFC0C0C0),
            radius = 5f,
            center = Offset(hiltOrigin.x, hiltOrigin.y - 57f)
        )
    }
}

private fun DrawScope.drawNinjaBodyAndHood(
    center: Offset,
    radius: Float,
    baseTint: Color,
    isSuperSaiyan: Boolean
) {
    // Shoulders / Torso
    val bodyCenter = Offset(center.x, center.y + radius * 0.65f)
    val bodyWidth = radius * 1.85f
    val bodyHeight = radius * 1.25f

    val bodyBrush = Brush.radialGradient(
        colors = listOf(
            baseTint.copy(alpha = 0.95f),
            Color(0xFF0F0F14),
            Color(0xFF050508)
        ),
        center = bodyCenter,
        radius = radius * 1.4f
    )

    drawOval(
        brush = bodyBrush,
        topLeft = Offset(bodyCenter.x - bodyWidth / 2f, bodyCenter.y - bodyHeight / 2f),
        size = Size(bodyWidth, bodyHeight)
    )

    // Fluffy Head Hood
    val headBrush = Brush.radialGradient(
        colors = if (isSuperSaiyan) {
            listOf(
                Color(0xFFFFEA75),
                baseTint,
                Color(0xFF09090C)
            )
        } else {
            listOf(
                baseTint,
                Color(0xFF121216),
                Color(0xFF050507)
            )
        },
        center = Offset(center.x, center.y - radius * 0.25f),
        radius = radius * 1.2f
    )

    drawCircle(
        brush = headBrush,
        radius = radius,
        center = center
    )

    // 3D Rim Lighting
    if (isSuperSaiyan) {
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(Color.Transparent, Color(0xFFFFD700).copy(alpha = 0.8f)),
                center = center,
                radius = radius
            ),
            radius = radius,
            center = center,
            style = Stroke(width = 6f)
        )
    }
}

private fun DrawScope.drawRopeSash(center: Offset, radius: Float) {
    // Twisted grey rope running diagonally from right shoulder to bottom left
    val start = Offset(center.x + radius * 0.65f, center.y + radius * 0.35f)
    val end = Offset(center.x - radius * 0.75f, center.y + radius * 0.85f)

    drawLine(
        color = Color(0xFF6B6E70),
        start = start,
        end = end,
        strokeWidth = 9f,
        cap = StrokeCap.Round
    )
    drawLine(
        color = Color(0xFF9E9E9E),
        start = start,
        end = end,
        strokeWidth = 3f,
        cap = StrokeCap.Round
    )
}

private fun DrawScope.drawFaceOpening(center: Offset, radius: Float) {
    // Skin oval behind mask
    val faceCenter = Offset(center.x, center.y - radius * 0.05f)
    val faceWidth = radius * 1.25f
    val faceHeight = radius * 0.65f

    drawOval(
        color = Color(0xFFD7A781), // Warm peach / ninja skin
        topLeft = Offset(faceCenter.x - faceWidth / 2f, faceCenter.y - faceHeight / 2f),
        size = Size(faceWidth, faceHeight)
    )
}

private fun DrawScope.drawCowlMask(
    center: Offset,
    radius: Float,
    tint: Color,
    isSuperSaiyan: Boolean
) {
    // Mask covering bottom half of face and neck
    val maskCenter = Offset(center.x, center.y + radius * 0.2f)
    val maskWidth = radius * 1.35f
    val maskHeight = radius * 0.55f

    val maskBrush = Brush.verticalGradient(
        colors = listOf(
            tint.copy(alpha = 0.95f),
            Color(0xFF0A0A0E)
        )
    )

    drawOval(
        brush = maskBrush,
        topLeft = Offset(maskCenter.x - maskWidth / 2f, maskCenter.y - maskHeight / 2f),
        size = Size(maskWidth, maskHeight)
    )

    // Top trim line of the mask
    drawArc(
        color = Color(0xFF2C2C34),
        startAngle = 180f,
        sweepAngle = 180f,
        useCenter = false,
        topLeft = Offset(maskCenter.x - maskWidth * 0.45f, maskCenter.y - maskHeight * 0.5f),
        size = Size(maskWidth * 0.9f, maskHeight * 0.4f),
        style = Stroke(width = 3.5f)
    )
}

private fun DrawScope.drawNinjaEyes(
    center: Offset,
    radius: Float,
    expression: Expression,
    blinkProgress: Float,
    isSuperSaiyan: Boolean
) {
    val eyeSpacing = radius * 0.32f
    val eyeY = center.y - radius * 0.12f
    val eyeRadius = radius * 0.22f

    val leftEyeCenter = Offset(center.x - eyeSpacing, eyeY)
    val rightEyeCenter = Offset(center.x + eyeSpacing, eyeY)

    when (expression) {
        Expression.HAPPY -> {
            // Cheerful happy curved slits (^ ^)
            drawArc(
                color = if (isSuperSaiyan) Color(0xFFFFD700) else Color(0xFF0F0F14),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(leftEyeCenter.x - eyeRadius, leftEyeCenter.y - eyeRadius * 0.6f),
                size = Size(eyeRadius * 2f, eyeRadius * 1.2f),
                style = Stroke(width = 7f, cap = StrokeCap.Round)
            )
            drawArc(
                color = if (isSuperSaiyan) Color(0xFFFFD700) else Color(0xFF0F0F14),
                startAngle = 180f,
                sweepAngle = 180f,
                useCenter = false,
                topLeft = Offset(rightEyeCenter.x - eyeRadius, rightEyeCenter.y - eyeRadius * 0.6f),
                size = Size(eyeRadius * 2f, eyeRadius * 1.2f),
                style = Stroke(width = 7f, cap = StrokeCap.Round)
            )
        }
        else -> {
            // Default Round glossy ninja eyes with blinking
            val currentHeight = eyeRadius * 2f * blinkProgress
            val eyeColor = if (isSuperSaiyan) Color(0xFF00E5FF) else Color(0xFF0A0A0C)

            // Left Eye
            drawOval(
                color = eyeColor,
                topLeft = Offset(leftEyeCenter.x - eyeRadius, leftEyeCenter.y - currentHeight / 2f),
                size = Size(eyeRadius * 2f, currentHeight)
            )
            // Right Eye
            drawOval(
                color = eyeColor,
                topLeft = Offset(rightEyeCenter.x - eyeRadius, rightEyeCenter.y - currentHeight / 2f),
                size = Size(eyeRadius * 2f, currentHeight)
            )

            if (blinkProgress > 0.4f) {
                // Large Specular Highlights (Upper Left of each eye)
                val highlightRadius = eyeRadius * 0.42f
                val hOffset = Offset(-eyeRadius * 0.28f, -eyeRadius * 0.25f)
                val highlightColor = if (isSuperSaiyan) Color(0xFFFFFFFF) else Color(0xFFFFFFFF)

                drawCircle(
                    color = highlightColor,
                    radius = highlightRadius,
                    center = leftEyeCenter + hOffset
                )
                drawCircle(
                    color = highlightColor,
                    radius = highlightRadius,
                    center = rightEyeCenter + hOffset
                )

                // Secondary small highlight (Bottom Right)
                val subHRadius = eyeRadius * 0.18f
                val subHOffset = Offset(eyeRadius * 0.32f, eyeRadius * 0.3f)
                drawCircle(
                    color = highlightColor.copy(alpha = 0.85f),
                    radius = subHRadius,
                    center = leftEyeCenter + subHOffset
                )
                drawCircle(
                    color = highlightColor.copy(alpha = 0.85f),
                    radius = subHRadius,
                    center = rightEyeCenter + subHOffset
                )
            }
        }
    }
}
