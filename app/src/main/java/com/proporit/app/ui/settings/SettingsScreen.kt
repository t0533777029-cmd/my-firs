package com.proporit.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShieldMoon
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import com.proporit.app.alarm.AlarmScheduler
import com.proporit.app.ui.components.GlassCard
import com.proporit.app.ui.theme.Amber400
import com.proporit.app.ui.theme.Blue400
import com.proporit.app.ui.theme.CardBorder
import com.proporit.app.ui.theme.Cyan400
import com.proporit.app.ui.theme.Ink0
import com.proporit.app.ui.theme.Ink300
import com.proporit.app.ui.theme.Ink500
import com.proporit.app.ui.util.dayLabel
import com.proporit.app.ui.util.hourLabel

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val settings by viewModel.settings.collectAsState()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var exactAlarmGranted by remember { mutableStateOf(AlarmScheduler.canScheduleExact(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                exactAlarmGranted = AlarmScheduler.canScheduleExact(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 22.dp)
    ) {
        Spacer(Modifier.height(18.dp))
        Text("Твои правила", color = Ink300, fontSize = 13.sp, fontWeight = FontWeight.SemiBold)
        Text("Настройки", color = Ink0, fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)

        if (!exactAlarmGranted) {
            Spacer(Modifier.height(16.dp))
            GlassCard(modifier = Modifier.fillMaxWidth()) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.ShieldMoon, contentDescription = null, tint = Amber400)
                    Spacer(Modifier.size(10.dp))
                    Text(
                        "Разреши точные напоминания в настройках Android, иначе уведомления могут приходить с опозданием.",
                        color = Ink300, fontSize = 12.5.sp
                    )
                }
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { AlarmScheduler.openExactAlarmSettings(context) },
                    colors = ButtonDefaults.buttonColors(containerColor = Amber400)
                ) { Text("Открыть настройки", fontWeight = FontWeight.Bold) }
            }
        }

        SectionTitle("Интервал замены")
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingRow(
                title = "Менять каждые",
                subtitle = "По умолчанию — 3 дня",
            ) {
                Stepper(
                    valueLabel = dayLabel(settings.intervalDays),
                    onDec = { viewModel.setIntervalDays(settings.intervalDays - 1) },
                    onInc = { viewModel.setIntervalDays(settings.intervalDays + 1) },
                )
            }
        }

        SectionTitle("Уведомления")
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingRow(
                title = "Напоминать каждые",
                subtitle = "Когда срок наступил, а замены ещё не было",
            ) {
                Stepper(
                    valueLabel = hourLabel(settings.frequencyHours),
                    onDec = { viewModel.setFrequencyHours(settings.frequencyHours - 1) },
                    onInc = { viewModel.setFrequencyHours(settings.frequencyHours + 1) },
                )
            }
            HorizontalDivider(color = CardBorder)
            SettingRow(title = "Звук", subtitle = "Звуковой сигнал при напоминании") {
                Switch(
                    checked = settings.soundEnabled,
                    onCheckedChange = { viewModel.setSound(it) },
                    colors = switchColors(),
                )
            }
            HorizontalDivider(color = CardBorder)
            SettingRow(title = "Вибрация", subtitle = "Дублировать вибрацией") {
                Switch(
                    checked = settings.vibrationEnabled,
                    onCheckedChange = { viewModel.setVibration(it) },
                    colors = switchColors(),
                )
            }
            HorizontalDivider(color = CardBorder)
            SettingRow(title = "Тихие часы", subtitle = "Не напоминать с ${settings.quietStartHour}:00 до ${settings.quietEndHour}:00") {
                Switch(
                    checked = settings.quietHoursEnabled,
                    onCheckedChange = { viewModel.setQuietHours(it, settings.quietStartHour, settings.quietEndHour) },
                    colors = switchColors(),
                )
            }
            if (settings.quietHoursEnabled) {
                HorizontalDivider(color = CardBorder)
                SettingRow(title = "Начало тихих часов", subtitle = null) {
                    Stepper(
                        valueLabel = "${settings.quietStartHour}:00",
                        onDec = { viewModel.setQuietHours(true, (settings.quietStartHour - 1 + 24) % 24, settings.quietEndHour) },
                        onInc = { viewModel.setQuietHours(true, (settings.quietStartHour + 1) % 24, settings.quietEndHour) },
                    )
                }
                HorizontalDivider(color = CardBorder)
                SettingRow(title = "Конец тихих часов", subtitle = null) {
                    Stepper(
                        valueLabel = "${settings.quietEndHour}:00",
                        onDec = { viewModel.setQuietHours(true, settings.quietStartHour, (settings.quietEndHour - 1 + 24) % 24) },
                        onInc = { viewModel.setQuietHours(true, settings.quietStartHour, (settings.quietEndHour + 1) % 24) },
                    )
                }
            }
        }

        SectionTitle("Надёжность")
        GlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                "Если поменял пропорит не по расписанию (раньше или позже) — весь график на ${settings.intervalDays} дн. вперёд пересчитается автоматически от момента нажатия кнопки. Это работает, даже если телефон перезагружался или приложение закрывалось.",
                color = Ink300, fontSize = 12.5.sp, lineHeight = 18.sp,
            )
        }

        Spacer(Modifier.height(24.dp))
    }
}

@Composable
private fun SectionTitle(text: String) {
    Spacer(Modifier.height(22.dp))
    Text(text.uppercase(), color = Ink500, fontSize = 12.sp, fontWeight = FontWeight.ExtraBold)
    Spacer(Modifier.height(10.dp))
}

@Composable
private fun SettingRow(title: String, subtitle: String?, trailing: @Composable () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(title, color = Ink0, fontSize = 14.sp, fontWeight = FontWeight.Bold)
            if (subtitle != null) {
                Spacer(Modifier.height(2.dp))
                Text(subtitle, color = Ink500, fontSize = 11.5.sp)
            }
        }
        Spacer(Modifier.size(10.dp))
        trailing()
    }
}

@Composable
private fun Stepper(valueLabel: String, onDec: () -> Unit, onInc: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepButton("–", onDec)
        Text(
            valueLabel,
            color = Cyan400,
            fontSize = 14.5.sp,
            fontWeight = FontWeight.ExtraBold,
            modifier = Modifier.padding(horizontal = 10.dp)
        )
        StepButton("+", onInc)
    }
}

@Composable
private fun StepButton(symbol: String, onClick: () -> Unit) {
    Box(
        modifier = Modifier
            .size(30.dp)
            .clip(RoundedCornerShape(10.dp))
            .background(Ink0.copy(alpha = 0.06f))
            .then(Modifier),
        contentAlignment = Alignment.Center
    ) {
        androidx.compose.material3.TextButton(onClick = onClick, contentPadding = androidx.compose.foundation.layout.PaddingValues(0.dp)) {
            Text(symbol, color = Ink0, fontSize = 16.sp, fontWeight = FontWeight.ExtraBold)
        }
    }
}

@Composable
private fun switchColors() = SwitchDefaults.colors(
    checkedThumbColor = Ink0,
    checkedTrackColor = Blue400,
    uncheckedThumbColor = Ink0,
    uncheckedTrackColor = Ink0.copy(alpha = 0.15f),
)
