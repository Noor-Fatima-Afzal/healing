package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.WifiOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.withTransform
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkEmerald
import com.example.ui.theme.EmeraldGreen
import com.example.ui.theme.Gold
import com.example.ui.theme.PaleGold

@Composable
fun IslamicAppLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(120.dp)
            .clip(CircleShape)
            .background(
                Brush.radialGradient(
                    colors = listOf(LightEmeraldLogo, DarkEmeraldLogo),
                    center = Offset.Unspecified
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize().padding(8.dp)) {
            val center = Offset(size.width / 2, size.height / 2)
            val radius = size.width / 2

            // Golden outline circular border
            drawCircle(
                color = Gold,
                radius = radius,
                style = Stroke(width = 3.dp.toPx())
            )

            // Inner dotted gold border
            drawCircle(
                color = PaleGold,
                radius = radius - 8.dp.toPx(),
                style = Stroke(width = 1.dp.toPx())
            )

            // Rays of Golden light emanating from the center Quran
            val rayCount = 12
            for (i in 0 until rayCount) {
                val angle = (360f / rayCount) * i
                withTransform({
                    rotate(angle, center)
                }) {
                    drawLine(
                        color = Gold.copy(alpha = 0.5f),
                        start = center,
                        end = Offset(center.x, 15.dp.toPx()),
                        strokeWidth = 2.dp.toPx()
                    )
                }
            }

            // Draw the open Quran pages in the center
            val bookWidth = size.width * 0.5f
            val bookHeight = size.height * 0.3f
            val bookLeft = center.x - bookWidth / 2
            val bookTop = center.y - bookHeight / 4

            // Left page
            val leftPagePath = Path().apply {
                moveTo(center.x, bookTop + bookHeight)
                cubicTo(
                    center.x - bookWidth * 0.25f, bookTop + bookHeight * 0.8f,
                    center.x - bookWidth * 0.4f, bookTop + bookHeight * 0.9f,
                    bookLeft, bookTop + bookHeight * 0.7f
                )
                lineTo(bookLeft, bookTop)
                cubicTo(
                    center.x - bookWidth * 0.4f, bookTop + bookHeight * 0.2f,
                    center.x - bookWidth * 0.25f, bookTop + bookHeight * 0.1f,
                    center.x, bookTop + bookHeight * 0.3f
                )
                close()
            }

            // Right page
            val rightPagePath = Path().apply {
                moveTo(center.x, bookTop + bookHeight)
                cubicTo(
                    center.x + bookWidth * 0.25f, bookTop + bookHeight * 0.8f,
                    center.x + bookWidth * 0.4f, bookTop + bookHeight * 0.9f,
                    bookLeft + bookWidth, bookTop + bookHeight * 0.7f
                )
                lineTo(bookLeft + bookWidth, bookTop)
                cubicTo(
                    center.x + bookWidth * 0.4f, bookTop + bookHeight * 0.2f,
                    center.x + bookWidth * 0.25f, bookTop + bookHeight * 0.1f,
                    center.x, bookTop + bookHeight * 0.3f
                )
                close()
            }

            // Draw pages background (warm cream)
            drawPath(leftPagePath, color = Color(0xFFFDFBF7))
            drawPath(rightPagePath, color = Color(0xFFFDFBF7))

            // Draw pages outlines (Gold)
            drawPath(leftPagePath, color = Gold, style = Stroke(width = 2.dp.toPx()))
            drawPath(rightPagePath, color = Gold, style = Stroke(width = 2.dp.toPx()))

            // Draw book spine ribbon/bookmark hanging down
            val ribbonPath = Path().apply {
                moveTo(center.x - 3.dp.toPx(), center.y + bookHeight * 0.3f)
                lineTo(center.x + 3.dp.toPx(), center.y + bookHeight * 0.3f)
                lineTo(center.x + 4.dp.toPx(), center.y + bookHeight * 0.7f)
                lineTo(center.x, center.y + bookHeight * 0.65f)
                lineTo(center.x - 4.dp.toPx(), center.y + bookHeight * 0.7f)
                close()
            }
            drawPath(ribbonPath, color = Gold)

            // Radiant golden central glow
            drawCircle(
                color = Gold.copy(alpha = 0.2f),
                radius = radius * 0.3f,
                center = center
            )
        }
    }
}

val LightEmeraldLogo = Color(0xFF628F35)
val DarkEmeraldLogo = Color(0xFF335114)

@Composable
fun IslamicDecorativeBorder(
    modifier: Modifier = Modifier,
    borderColor: Color = Gold,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .drawBehind {
                val pad = 4.dp.toPx()
                val sizeW = size.width - pad * 2
                val sizeH = size.height - pad * 2

                // Outer border
                drawRoundRect(
                    color = borderColor,
                    topLeft = Offset(pad, pad),
                    size = Size(sizeW, sizeH),
                    cornerRadius = CornerRadius(12.dp.toPx()),
                    style = Stroke(width = 1.dp.toPx())
                )

                // Corner decorative brackets (representing Islamic arches)
                val cornerSize = 12.dp.toPx()
                // Top-Left corner accent
                drawArc(
                    color = borderColor,
                    startAngle = 180f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(pad, pad),
                    size = Size(cornerSize * 2, cornerSize * 2),
                    style = Stroke(width = 2.dp.toPx())
                )
                // Top-Right corner accent
                drawArc(
                    color = borderColor,
                    startAngle = 270f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(pad + sizeW - cornerSize * 2, pad),
                    size = Size(cornerSize * 2, cornerSize * 2),
                    style = Stroke(width = 2.dp.toPx())
                )
                // Bottom-Left corner accent
                drawArc(
                    color = borderColor,
                    startAngle = 90f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(pad, pad + sizeH - cornerSize * 2),
                    size = Size(cornerSize * 2, cornerSize * 2),
                    style = Stroke(width = 2.dp.toPx())
                )
                // Bottom-Right corner accent
                drawArc(
                    color = borderColor,
                    startAngle = 0f,
                    sweepAngle = 90f,
                    useCenter = false,
                    topLeft = Offset(pad + sizeW - cornerSize * 2, pad + sizeH - cornerSize * 2),
                    size = Size(cornerSize * 2, cornerSize * 2),
                    style = Stroke(width = 2.dp.toPx())
                )
            }
            .padding(12.dp),
        content = content
    )
}

@Composable
fun OfflineModeBanner(isOnline: Boolean) {
    AnimatedVisibility(
        visible = !isOnline,
        enter = expandVertically(),
        exit = shrinkVertically()
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 8.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer,
                contentColor = MaterialTheme.colorScheme.onErrorContainer
            ),
            shape = MaterialTheme.shapes.extraSmall
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp, horizontal = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WifiOff,
                    contentDescription = "Offline Mode",
                    modifier = Modifier.size(16.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Offline Mode — Viewing cached data",
                    style = MaterialTheme.typography.labelMedium
                )
            }
        }
    }
}
