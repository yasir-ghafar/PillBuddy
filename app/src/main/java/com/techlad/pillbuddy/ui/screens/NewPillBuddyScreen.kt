@file:OptIn(ExperimentalMaterial3Api::class)

package com.techlad.pillbuddy.ui.screens

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.techlad.pillbuddy.R
import com.techlad.pillbuddy.data.model.Remed
import com.techlad.pillbuddy.ui.components.PrimaryPillButton
import com.techlad.pillbuddy.ui.theme.DisplayFont
import com.techlad.pillbuddy.ui.theme.PillBuddyTheme
import com.techlad.pillbuddy.ui.theme.ReMedCard
import com.techlad.pillbuddy.ui.theme.ReMedDark
import com.techlad.pillbuddy.ui.theme.ReMedDarker
import com.techlad.pillbuddy.ui.theme.ReMedLighter
import com.techlad.pillbuddy.ui.theme.ReMedOnPrimary
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.TimeZone

private enum class ScheduleEvery { Day, Week, Month }

private enum class ActivePicker { None, Start, End, Time, Notification }

private val FieldShape = RoundedCornerShape(16.dp)
private val notificationChoices = listOf(0, 5, 15, 30, 60)

@Composable
fun NewPillBuddyScreen(
    onBack: () -> Unit,
    onCreate: (Remed) -> Unit,
    modifier: Modifier = Modifier,
) {
    var name by rememberSaveable { mutableStateOf("Nexium") }
    val defaultInstructions = stringResource(R.string.default_instructions)
    var instructions by rememberSaveable { mutableStateOf(defaultInstructions) }
    var every by rememberSaveable { mutableStateOf(ScheduleEvery.Day) }
    var startDate by remember { mutableStateOf(FormDate(2021, Calendar.JANUARY, 15)) }
    var endDate by remember { mutableStateOf(FormDate(2021, Calendar.JANUARY, 15)) }
    var hour by rememberSaveable { mutableIntStateOf(6) }
    var minute by rememberSaveable { mutableIntStateOf(0) }
    var notifyMinutes by rememberSaveable { mutableIntStateOf(0) }
    var picker by rememberSaveable { mutableStateOf(ActivePicker.None) }

    val todayLabel = stringResource(R.string.today)
    val tomorrowLabel = stringResource(R.string.tomorrow)
    val repeats = upcomingRepeats(every, hour, minute, todayLabel, tomorrowLabel)
    val timeLabel = formatTime(hour, minute)

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = (-12).dp)
                .padding(top = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = ReMedDark
                )
            }
            Text(
                text = stringResource(R.string.new_remed_title),
                color = ReMedDark,
                fontFamily = DisplayFont,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }

        FieldLabel(stringResource(R.string.name_label))
        WhiteField {
            BasicTextField(
                value = name,
                onValueChange = { name = it },
                singleLine = true,
                textStyle = fieldTextStyle(),
                cursorBrush = SolidColor(ReMedDark),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth()
            )
        }

        FieldLabel(stringResource(R.string.instructions_label), modifier = Modifier.padding(top = 16.dp))
        WhiteField {
            BasicTextField(
                value = instructions,
                onValueChange = { instructions = it },
                textStyle = fieldTextStyle(),
                cursorBrush = SolidColor(ReMedDark),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp)
            )
        }

        FieldLabel(stringResource(R.string.every_label), modifier = Modifier.padding(top = 16.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ScheduleEvery.entries.forEach { option ->
                val selected = option == every
                Text(
                    text = stringResource(option.labelRes),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) ReMedDark else ReMedCard)
                        .clickable { every = option }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    color = if (selected) ReMedOnPrimary else ReMedDark,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp
                )
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Column(modifier = Modifier.weight(1f)) {
                FieldLabel(stringResource(R.string.start_date))
                WhiteField(onClick = { picker = ActivePicker.Start }) {
                    Text(text = startDate.format(), style = fieldTextStyle())
                }
            }
            Column(modifier = Modifier.weight(1f)) {
                FieldLabel(stringResource(R.string.end_date))
                WhiteField(onClick = { picker = ActivePicker.End }) {
                    Text(text = endDate.format(), style = fieldTextStyle())
                }
            }
        }

        FieldLabel(stringResource(R.string.time_label), modifier = Modifier.padding(top = 16.dp))
        WhiteField(onClick = { picker = ActivePicker.Time }, verticalPadding = 18.dp) {
            Text(
                text = timeLabel,
                color = ReMedDark,
                fontFamily = DisplayFont,
                fontWeight = FontWeight.Bold,
                fontSize = 32.sp
            )
        }

        FieldLabel(stringResource(R.string.notification_label), modifier = Modifier.padding(top = 16.dp))
        WhiteField(onClick = { picker = ActivePicker.Notification }) {
            Text(text = notificationLabel(notifyMinutes), style = fieldTextStyle())
        }

        FieldLabel(stringResource(R.string.repeats_label), modifier = Modifier.padding(top = 16.dp))
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(FieldShape)
                .background(ReMedCard)
        ) {
            repeats.forEachIndexed { index, row ->
                if (index > 0) {
                    HorizontalDivider(
                        color = ReMedLighter,
                        modifier = Modifier.padding(horizontal = 16.dp)
                    )
                }
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = row.label,
                        color = ReMedDark,
                        fontSize = 16.sp
                    )
                    Text(
                        text = row.time,
                        color = ReMedDarker,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }

        PrimaryPillButton(
            text = stringResource(R.string.create_pillbuddy),
            onClick = {
                val trimmed = name.trim()
                if (trimmed.isEmpty()) return@PrimaryPillButton
                onCreate(
                    Remed(
                        id = 0,
                        name = trimmed,
                        instructions = instructions.trim(),
                        time = timeLabel
                    )
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        )
    }

    when (picker) {
        ActivePicker.Start -> DateFieldDialog(
            initial = startDate,
            onDismiss = { picker = ActivePicker.None },
            onConfirm = { picked ->
                startDate = picked
                if (endDate.toUtcMillis() < picked.toUtcMillis()) endDate = picked
                picker = ActivePicker.None
            }
        )

        ActivePicker.End -> DateFieldDialog(
            initial = endDate,
            onDismiss = { picker = ActivePicker.None },
            onConfirm = { picked ->
                endDate = if (picked.toUtcMillis() < startDate.toUtcMillis()) startDate else picked
                picker = ActivePicker.None
            }
        )

        ActivePicker.Time -> TimeFieldDialog(
            hour = hour,
            minute = minute,
            onDismiss = { picker = ActivePicker.None },
            onConfirm = { pickedHour, pickedMinute ->
                hour = pickedHour
                minute = pickedMinute
                picker = ActivePicker.None
            }
        )

        ActivePicker.Notification -> NotificationDialog(
            selected = notifyMinutes,
            onDismiss = { picker = ActivePicker.None },
            onSelect = {
                notifyMinutes = it
                picker = ActivePicker.None
            }
        )

        ActivePicker.None -> Unit
    }
}

@Composable
private fun FieldLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(start = 4.dp, bottom = 6.dp),
        color = ReMedDarker,
        fontSize = 13.sp
    )
}

@Composable
private fun WhiteField(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    verticalPadding: androidx.compose.ui.unit.Dp = 16.dp,
    content: @Composable () -> Unit,
) {
    val clickable = if (onClick != null) {
        Modifier.clickable(role = Role.Button, onClick = onClick)
    } else {
        Modifier
    }
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(FieldShape)
            .background(ReMedCard)
            .then(clickable)
            .padding(horizontal = 16.dp, vertical = verticalPadding)
    ) {
        content()
    }
}

@Composable
private fun fieldTextStyle() = TextStyle(
    color = ReMedDark,
    fontSize = 16.sp,
    fontWeight = FontWeight.Normal
)

@Composable
private fun notificationLabel(minutes: Int): String {
    return if (minutes >= 60) {
        stringResource(R.string.hour_before)
    } else {
        stringResource(R.string.minutes_before, minutes)
    }
}

@Composable
private fun DateFieldDialog(
    initial: FormDate,
    onDismiss: () -> Unit,
    onConfirm: (FormDate) -> Unit,
) {
    val state = rememberDatePickerState(initialSelectedDateMillis = initial.toUtcMillis())
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = {
                    val millis = state.selectedDateMillis
                    if (millis != null) onConfirm(FormDate.fromUtcMillis(millis)) else onDismiss()
                }
            ) {
                Text(stringResource(R.string.ok), color = ReMedDark)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = ReMedDarker)
            }
        }
    ) {
        DatePicker(state = state)
    }
}

@Composable
private fun TimeFieldDialog(
    hour: Int,
    minute: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    val state = rememberTimePickerState(initialHour = hour, initialMinute = minute, is24Hour = false)
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = ReMedCard) {
            Column(
                modifier = Modifier.padding(top = 24.dp, bottom = 12.dp, start = 16.dp, end = 16.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                TimePicker(state = state)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismiss) {
                        Text(stringResource(R.string.cancel), color = ReMedDarker)
                    }
                    TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                        Text(stringResource(R.string.ok), color = ReMedDark)
                    }
                }
            }
        }
    }
}

@Composable
private fun NotificationDialog(
    selected: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = ReMedCard,
        title = {
            Text(
                text = stringResource(R.string.notification_label),
                color = ReMedDark,
                fontFamily = DisplayFont,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                notificationChoices.forEach { minutes ->
                    Text(
                        text = notificationLabel(minutes),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(minutes) }
                            .padding(vertical = 12.dp),
                        color = if (minutes == selected) ReMedDark else ReMedDarker,
                        fontWeight = if (minutes == selected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 16.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = ReMedDarker)
            }
        }
    )
}

private val ScheduleEvery.labelRes: Int
    get() = when (this) {
        ScheduleEvery.Day -> R.string.every_day
        ScheduleEvery.Week -> R.string.every_week
        ScheduleEvery.Month -> R.string.every_month
    }

private data class FormDate(val year: Int, val month: Int, val day: Int) {
    fun toUtcMillis(): Long {
        val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
        utc.clear()
        utc.set(year, month, day)
        return utc.timeInMillis
    }

    fun format(): String {
        val local = Calendar.getInstance()
        local.clear()
        local.set(year, month, day)
        return SimpleDateFormat("d MMM yyyy", Locale.US).format(local.time)
    }

    companion object {
        fun fromUtcMillis(millis: Long): FormDate {
            val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC"))
            utc.timeInMillis = millis
            return FormDate(
                utc.get(Calendar.YEAR),
                utc.get(Calendar.MONTH),
                utc.get(Calendar.DAY_OF_MONTH)
            )
        }
    }
}

private data class RepeatRow(val label: String, val time: String)

private fun formatTime(hour: Int, minute: Int): String {
    val calendar = Calendar.getInstance()
    calendar.set(Calendar.HOUR_OF_DAY, hour)
    calendar.set(Calendar.MINUTE, minute)
    return SimpleDateFormat("h:mm a", Locale.US).format(calendar.time)
}

private fun upcomingRepeats(
    every: ScheduleEvery,
    hour: Int,
    minute: Int,
    todayLabel: String,
    tomorrowLabel: String,
): List<RepeatRow> {
    val timeText = formatTime(hour, minute)
    val dayFormat = SimpleDateFormat("EEE, MMM d", Locale.US)
    val start = Calendar.getInstance().apply {
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }
    val tomorrow = (start.clone() as Calendar).apply { add(Calendar.DAY_OF_YEAR, 1) }
    return List(3) { index ->
        val occurrence = (start.clone() as Calendar).apply {
            when (every) {
                ScheduleEvery.Day -> add(Calendar.DAY_OF_YEAR, index)
                ScheduleEvery.Week -> add(Calendar.WEEK_OF_YEAR, index)
                ScheduleEvery.Month -> add(Calendar.MONTH, index)
            }
        }
        val label = when {
            isSameDay(occurrence, start) -> todayLabel
            isSameDay(occurrence, tomorrow) -> tomorrowLabel
            else -> dayFormat.format(occurrence.time)
        }
        RepeatRow(label, timeText)
    }
}

private fun isSameDay(first: Calendar, second: Calendar): Boolean {
    return first.get(Calendar.YEAR) == second.get(Calendar.YEAR) &&
        first.get(Calendar.DAY_OF_YEAR) == second.get(Calendar.DAY_OF_YEAR)
}

@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun NewPillBuddyPreview() {
    PillBuddyTheme {
        NewPillBuddyScreen(onBack = {}, onCreate = {})
    }
}
