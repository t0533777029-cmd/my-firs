package com.proporit.app.ui.components

import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proporit.app.ui.theme.Amber400
import com.proporit.app.ui.theme.Blue400
import com.proporit.app.ui.theme.Cyan400
import com.proporit.app.ui.theme.Gold400
import com.proporit.app.ui.theme.GoldDeep
import com.proporit.app.ui.theme.Green400
import com.proporit.app.ui.theme.Ink0
import com.proporit.app.ui.theme.Ink300
import com.proporit.app.ui.theme.Navy950
import com.proporit.app.ui.theme.Red400

enum class RingStatus { OK, WARNING, OVERDUE }

@Composable
fun CountdownRing(
    progress: Float,
    mainLabel: String,
    subLabel: String,
    status: RingStatus,
    modifier: Modifier = Modifier,
) {
    val infinite = rememberInfiniteTransition(label = "pulse")
    val pulse by infinite.animateFloat(
        initialValue = if (status == RingStatus.OVERDUE) 0.75f else 1f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(900), RepeatMode.Reverse),
        label = "pulseAlpha"
    )

    val gradientColors = when (status) {
        RingStatus.OK -> listOf(Blue400, Cyan400)
        RingStatus.WARNING -> listOf(Blue400, Amber400)
        RingStatus.OVERDUE -> listOf(Amber400, Red400)
    }

    Box(
        modifier = modifier.size(206.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(206.dp)) {
            val strokeWidth = 14.dp.toPx()
            val trackColor = Color.White.copy(alpha = 0.08f)
            val diameter = size.minDimension - strokeWidth
            val topLeft = Offset(
                (size.width - diameter) / 2f,
                (size.height - diameter) / 2f
            )
            val arcSize = Size(diameter, diameter)

            drawArc(
                color = trackColor,
                startAngle = -90f,
                sweepAngle = 360f,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth)
            )
            val sweep = 360f * progress.coerceIn(0f, 1f) * (if (status == RingStatus.OVERDUE) pulse else 1f)
            drawArc(
                brush = Brush.sweepGradient(gradientColors),
                startAngle = -90f,
                sweepAngle = sweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round)
            )
        }

        Box(
            modifier = Modifier
                .size(182.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(Color(0xFF101C40), Navy950),
                        radius = 260f
                    )
                ),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(mainLabel, color = Ink0, fontSize = 40.sp, fontWeight = FontWeight.ExtraBold)
                Text(subLabel, color = Ink300, fontSize = 12.5.sp, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

private data class PillSpec(val bg: Color, val fg: Color, val text: String, val icon: ImageVector)

@Composable
fun StatusPill(status: RingStatus, modifier: Modifier = Modifier) {
    val spec = when (status) {
        RingStatus.OK -> PillSpec(Green400.copy(alpha = 0.14f), Green400, "Всё под контролем", Icons.Filled.Check)
        RingStatus.WARNING -> PillSpec(Amber400.copy(alpha = 0.16f), Amber400, "Скоро пора менять", Icons.Filled.Schedule)
        RingStatus.OVERDUE -> PillSpec(Red400.copy(alpha = 0.16f), Red400, "Просрочено — меняй", Icons.Filled.Warning)
    }
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(spec.bg)
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(spec.icon, contentDescription = null, tint = spec.fg, modifier = Modifier.size(14.dp))
            Spacer(Modifier.size(6.dp))
            Text(spec.text, color = spec.fg, fontSize = 12.5.sp, fontWeight = FontWeight.Bold)
        }
    }
}

/** Gold accent badge — shows the streak of on-time changes. Only the badge on Home is gold. */
@Composable
fun StreakBadge(streak: Int, modifier: Modifier = Modifier) {
    if (streak <= 0) return
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(100.dp))
            .background(Brush.horizontalGradient(listOf(Gold400, GoldDeep)))
            .padding(horizontal = 14.dp, vertical = 7.dp),
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Filled.LocalFireDepartment, contentDescription = null, tint = Navy950, modifier = Modifier.size(14.dp))
            Spacer(Modifier.size(6.dp))
            Text("$streak подряд вовремя", color = Navy950, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}
