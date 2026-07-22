package com.proporit.app.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bloodtype
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.proporit.app.ui.components.CountdownRing
import com.proporit.app.ui.components.GlassCard
import com.proporit.app.ui.components.StatusPill
import com.proporit.app.ui.components.StreakBadge
import com.proporit.app.ui.theme.Blue400
import com.proporit.app.ui.theme.Cyan400
import com.proporit.app.ui.theme.Ink0
import com.proporit.app.ui.theme.Ink300
import com.proporit.app.ui.theme.Ink500
import com.proporit.app.ui.theme.Navy950

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
    ) {
        Spacer(Modifier.height(18.dp))
        Text("Твой помощник", color = Ink300, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text(
            "Пропорит",
            color = Ink0,
            fontSize = 26.sp,
            fontWeight = FontWeight.ExtraBold,
        )

        Spacer(Modifier.height(18.dp))

        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                CountdownRing(
                    progress = state.progress,
                    mainLabel = state.mainLabel,
                    subLabel = state.subLabel,
                    status = state.status,
                )
                Spacer(Modifier.height(16.dp))
                StatusPill(status = state.status)

                if (state.streak > 0) {
                    Spacer(Modifier.height(10.dp))
                    StreakBadge(streak = state.streak)
                }

                Spacer(Modifier.height(18.dp))
                Row(modifier = Modifier.fillMaxWidth()) {
                    InfoBox(label = "Последняя замена", value = state.lastChangedText, modifier = Modifier.weight(1f))
                    InfoBox(label = "Следующая", value = state.nextDueText, modifier = Modifier.weight(1f))
                }

                Spacer(Modifier.height(20.dp))
                Button(
                    onClick = { viewModel.onMarkChanged() },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(58.dp),
                    shape = RoundedCornerShape(20.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Blue400, contentColor = Navy950),
                ) {
                    Icon(Icons.Filled.Bloodtype, contentDescription = null)
                    Spacer(Modifier.size(10.dp))
                    Text("Поменял пропорит", fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
                }
            }
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun InfoBox(label: String, value: String, modifier: Modifier = Modifier) {
    Column(modifier = modifier, horizontalAlignment = Alignment.CenterHorizontally) {
        Text(label.uppercase(), color = Ink500, fontSize = 10.5.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(4.dp))
        Text(value, color = Ink0, fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
    }
}
