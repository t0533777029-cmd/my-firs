package com.proporit.app.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proporit.app.ui.components.GlassCard
import com.proporit.app.ui.theme.Blue400
import com.proporit.app.ui.theme.CardBorder
import com.proporit.app.ui.theme.Cyan400
import com.proporit.app.ui.theme.Green400
import com.proporit.app.ui.theme.Ink0
import com.proporit.app.ui.theme.Ink300
import com.proporit.app.ui.theme.Ink500
import com.proporit.app.ui.theme.Navy950

private val dowLabels = listOf("Пн", "Вт", "Ср", "Чт", "Пт", "Сб", "Вс")

@Composable
fun CalendarScreen(viewModel: CalendarViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
    ) {
        Spacer(Modifier.height(18.dp))
        Text("История и план", color = Ink300, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text("Календарь", color = Ink0, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)

        Spacer(Modifier.height(18.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(state.monthLabel, color = Ink0, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
            Spacer(Modifier.height(14.dp))

            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                dowLabels.forEach {
                    Text(it, color = Ink500, fontSize = 11.sp, fontWeight = FontWeight.Bold, modifier = Modifier.weight(1f), textAlign = androidx.compose.ui.text.style.TextAlign.Center)
                }
            }
            Spacer(Modifier.height(6.dp))

            val totalCells = state.leadingBlanks + state.daysInMonth
            val rows = (totalCells + 6) / 7
            var dayCounter = 1
            for (row in 0 until rows) {
                Row(modifier = Modifier.fillMaxWidth()) {
                    for (col in 0 until 7) {
                        val cellIndex = row * 7 + col
                        Box(modifier = Modifier.weight(1f).aspectRatio(1f).padding(3.dp), contentAlignment = Alignment.Center) {
                            if (cellIndex >= state.leadingBlanks && dayCounter <= state.daysInMonth) {
                                val day = dayCounter
                                DayCell(
                                    day = day,
                                    done = day in state.doneDays,
                                    due = day == state.dueDay,
                                    today = day == state.today,
                                )
                                dayCounter++
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(14.dp))
            Row {
                LegendItem(color = Green400, text = "Заменено")
                Spacer(Modifier.size(16.dp))
                LegendItem(color = Blue400, text = "Плановая замена")
            }
        }

        Spacer(Modifier.height(22.dp))
        Text("ПОСЛЕДНИЕ ЗАМЕНЫ", color = Ink500, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
        Spacer(Modifier.height(8.dp))

        if (state.history.isEmpty()) {
            Text("Пока нет истории замен", color = Ink500, fontSize = 13.sp)
        } else {
            Column {
                state.history.forEachIndexed { index, row ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (row.onTime) Green400 else Color(0xFFFFB547))
                        )
                        Spacer(Modifier.size(12.dp))
                        Text(row.dateText, color = Ink0, fontSize = 13.5.sp, fontWeight = FontWeight.SemiBold, modifier = Modifier.weight(1f))
                        Text(row.timeText, color = Ink500, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                    if (index != state.history.lastIndex) {
                        HorizontalDivider(color = CardBorder)
                    }
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun DayCell(day: Int, done: Boolean, due: Boolean, today: Boolean) {
    val bg = when {
        due -> Brush.linearGradient(listOf(Blue400, Cyan400))
        done -> Brush.linearGradient(listOf(Green400.copy(alpha = 0.16f), Green400.copy(alpha = 0.16f)))
        else -> Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
    }
    val textColor = when {
        due -> Navy950
        done -> Green400
        else -> Ink300
    }
    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(bg),
        contentAlignment = Alignment.Center
    ) {
        Text(
            day.toString(),
            color = textColor,
            fontSize = 13.sp,
            fontWeight = if (done || due) FontWeight.ExtraBold else FontWeight.SemiBold
        )
    }
}

@Composable
private fun LegendItem(color: Color, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(9.dp).clip(CircleShape).background(color))
        Spacer(Modifier.size(6.dp))
        Text(text, color = Ink300, fontSize = 11.5.sp, fontWeight = FontWeight.SemiBold)
    }
}
