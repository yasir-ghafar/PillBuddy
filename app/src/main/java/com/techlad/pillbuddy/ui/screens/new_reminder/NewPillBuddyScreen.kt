@file:OptIn(ExperimentalMaterial3Api::class)

package com.techlad.pillbuddy.ui.screens.new_reminder

import androidx.compose.material3.ExperimentalMaterial3Api

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
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
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.techlad.pillbuddy.R
import com.techlad.pillbuddy.data.model.AppSettings
import com.techlad.pillbuddy.data.model.NewReminder
import com.techlad.pillbuddy.data.model.Reminder
import com.techlad.pillbuddy.data.model.formatClock
import com.techlad.pillbuddy.ui.components.PrimaryPillButton
import com.techlad.pillbuddy.ui.theme.DisplayFont
import com.techlad.pillbuddy.ui.theme.PillBuddyTheme
import com.techlad.pillbuddy.ui.theme.PillBuddyCard
import com.techlad.pillbuddy.ui.theme.PillBuddyDark
import com.techlad.pillbuddy.ui.theme.PillBuddyDarker
import com.techlad.pillbuddy.ui.theme.PillBuddyLighter
import com.techlad.pillbuddy.ui.theme.PillBuddyOnPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private enum class ActivePicker { None, Start, End, Time, Notification }

private val FieldShape = RoundedCornerShape(16.dp)
private val FieldErrorColor = Color(0xFFB42318)
private const val SAVE_SUCCESS_VISIBLE_MS = 900L

@Composable
fun NewPillBuddyScreen(
    onBack: () -> Unit,
    onSave: suspend (NewReminder) -> Boolean,
    onSaved: () -> Unit,
    defaults: AppSettings = AppSettings(),
    initial: Reminder? = null,
    modifier: Modifier = Modifier,
) {
    val formKey = initial?.id ?: 0L
    var name by rememberSaveable(formKey) { mutableStateOf(initial?.name.orEmpty()) }
    var instructions by rememberSaveable(formKey) {
        mutableStateOf(initial?.instructions.orEmpty())
    }
    var every by rememberSaveable(formKey, defaults.defaultFrequency) {
        mutableStateOf(
            initial?.frequency?.toScheduleEvery() ?: defaults.defaultFrequency.toScheduleEvery(),
        )
    }
    var startDate by remember(formKey) {
        mutableStateOf(initial?.formStartDate() ?: FormDate.today())
    }
    var endDate by remember(formKey) {
        mutableStateOf(initial?.formEndDate() ?: FormDate.today().plusYears(1))
    }
    var hour by rememberSaveable(formKey, defaults.defaultHour, defaults.defaultMinute) {
        mutableIntStateOf(initial?.hour ?: defaults.defaultHour)
    }
    var minute by rememberSaveable(formKey, defaults.defaultHour, defaults.defaultMinute) {
        mutableIntStateOf(initial?.minute ?: defaults.defaultMinute)
    }
    var notifyMinutes by rememberSaveable(formKey, defaults.notifyMinutesBefore) {
        mutableIntStateOf(initial?.notifyMinutesBefore ?: defaults.notifyMinutesBefore)
    }
    var picker by rememberSaveable(formKey) { mutableStateOf(ActivePicker.None) }
    var nameError by rememberSaveable(formKey) { mutableStateOf(false) }
    var saving by remember(formKey) { mutableStateOf(false) }
    var saveSucceeded by remember(formKey) { mutableStateOf(false) }
    val isEditing = initial != null
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    val saveFailedMessage = stringResource(R.string.reminder_save_failed)

    val todayLabel = stringResource(R.string.today)
    val tomorrowLabel = stringResource(R.string.tomorrow)
    val repeats = upcomingRepeats(every, hour, minute, defaults.hourCycle, todayLabel, tomorrowLabel)
    val timeLabel = formatClock(hour, minute, defaults.hourCycle)

    Box(modifier = modifier.fillMaxSize()) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp)
    ) {

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = PillBuddyDark,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(PillBuddyCard)
                    .clickable(onClick = onBack)
                    .padding(8.dp),
            )
            Text(
                text = stringResource(
                    if (isEditing) R.string.edit_pill_reminder else R.string.new_pill_reminder,
                ),
                modifier = Modifier.padding(start = 12.dp),
                color = PillBuddyDark,
                fontFamily = DisplayFont,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp
            )
        }

        FieldLabel(stringResource(R.string.name_label))
        WhiteField(hasError = nameError) {
            BasicTextField(
                value = name,
                onValueChange = {
                    name = it
                    if (nameError && it.trim().isNotEmpty()) nameError = false
                },
                singleLine = true,
                textStyle = fieldTextStyle(),
                cursorBrush = SolidColor(PillBuddyDark),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Words,
                    imeAction = ImeAction.Next
                ),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    PlaceholderField(
                        showPlaceholder = name.isEmpty(),
                        placeholder = stringResource(R.string.name_placeholder),
                        innerTextField = innerTextField,
                    )
                },
            )
        }
        if (nameError) {
            Text(
                text = stringResource(R.string.name_required),
                modifier = Modifier.padding(start = 4.dp, top = 6.dp),
                color = FieldErrorColor,
                fontSize = 13.sp,
            )
        }

        FieldLabel(stringResource(R.string.instructions_label), modifier = Modifier.padding(top = 16.dp))
        WhiteField {
            BasicTextField(
                value = instructions,
                onValueChange = { instructions = it },
                textStyle = fieldTextStyle(),
                cursorBrush = SolidColor(PillBuddyDark),
                keyboardOptions = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Sentences
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .heightIn(min = 72.dp),
                decorationBox = { innerTextField ->
                    PlaceholderField(
                        showPlaceholder = instructions.isEmpty(),
                        placeholder = stringResource(R.string.instructions_placeholder),
                        innerTextField = innerTextField,
                    )
                },
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
                        .background(if (selected) PillBuddyDark else PillBuddyCard)
                        .clickable { every = option }
                        .padding(horizontal = 18.dp, vertical = 8.dp),
                    color = if (selected) PillBuddyOnPrimary else PillBuddyDark,
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
                color = PillBuddyDark,
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
                .background(PillBuddyCard)
        ) {
            repeats.forEachIndexed { index, row ->
                if (index > 0) {
                    HorizontalDivider(
                        color = PillBuddyLighter,
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
                        color = PillBuddyDark,
                        fontSize = 16.sp
                    )
                    Text(
                        text = row.time,
                        color = PillBuddyDarker,
                        fontWeight = FontWeight.Medium,
                        fontSize = 16.sp
                    )
                }
            }
        }

        PrimaryPillButton(
            text = stringResource(R.string.save_pill_reminder),
            enabled = !saving,
            onClick = {
                val draft = newReminderDraft(
                    name = name,
                    instructions = instructions,
                    every = every,
                    startDate = startDate,
                    endDate = endDate,
                    hour = hour,
                    minute = minute,
                    notifyMinutesBefore = notifyMinutes,
                )
                if (draft == null) {
                    nameError = true
                    return@PrimaryPillButton
                }
                if (saving) return@PrimaryPillButton
                saving = true
                scope.launch {
                    val saved = onSave(draft)
                    if (saved) {
                        saveSucceeded = true
                        delay(SAVE_SUCCESS_VISIBLE_MS)
                        onSaved()
                    } else {
                        saving = false
                        snackbarHostState.showSnackbar(saveFailedMessage)
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp)
        )
        if (saveSucceeded) {
            Text(
                text = stringResource(R.string.reminder_saved),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = PillBuddyDarker,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }
    }

    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier
            .align(Alignment.BottomCenter)
            .padding(16.dp),
    ) { data ->
        Snackbar(
            snackbarData = data,
            containerColor = PillBuddyDark,
            contentColor = PillBuddyOnPrimary,
            shape = RoundedCornerShape(16.dp),
        )
    }
    }

    when (picker) {
        ActivePicker.Start -> DateFieldDialog(
            initial = startDate,
            onDismiss = { picker = ActivePicker.None },
            onConfirm = { picked ->
                val (newStart, newEnd) = applyStartDate(endDate, picked)
                startDate = newStart
                endDate = newEnd
                picker = ActivePicker.None
            }
        )

        ActivePicker.End -> DateFieldDialog(
            initial = endDate,
            onDismiss = { picker = ActivePicker.None },
            onConfirm = { picked ->
                endDate = applyEndDate(startDate, picked)
                picker = ActivePicker.None
            }
        )

        ActivePicker.Time -> TimeFieldDialog(
            hour = hour,
            minute = minute,
            hourCycle = defaults.hourCycle,
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
        color = PillBuddyDarker,
        fontSize = 13.sp
    )
}

@Composable
private fun WhiteField(
    modifier: Modifier = Modifier,
    onClick: (() -> Unit)? = null,
    hasError: Boolean = false,
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
            .background(PillBuddyCard)
            .then(
                if (hasError) {
                    Modifier.border(1.5.dp, FieldErrorColor, FieldShape)
                } else {
                    Modifier
                },
            )
            .then(clickable)
            .padding(horizontal = 16.dp, vertical = verticalPadding)
    ) {
        content()
    }
}

@Composable
private fun PlaceholderField(
    showPlaceholder: Boolean,
    placeholder: String,
    innerTextField: @Composable () -> Unit,
) {
    Box(modifier = Modifier.fillMaxWidth()) {
        if (showPlaceholder) {
            Text(
                text = placeholder,
                style = fieldTextStyle().copy(color = PillBuddyDarker),
            )
        }
        innerTextField()
    }
}

@Composable
private fun fieldTextStyle() = TextStyle(
    color = PillBuddyDark,
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
                Text(stringResource(R.string.ok), color = PillBuddyDark)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = PillBuddyDarker)
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
    hourCycle: Int,
    onDismiss: () -> Unit,
    onConfirm: (Int, Int) -> Unit,
) {
    val state = rememberTimePickerState(
        initialHour = hour,
        initialMinute = minute,
        is24Hour = hourCycle == 24,
    )
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(28.dp), color = PillBuddyCard) {
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
                        Text(stringResource(R.string.cancel), color = PillBuddyDarker)
                    }
                    TextButton(onClick = { onConfirm(state.hour, state.minute) }) {
                        Text(stringResource(R.string.ok), color = PillBuddyDark)
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
        containerColor = PillBuddyCard,
        title = {
            Text(
                text = stringResource(R.string.notification_label),
                color = PillBuddyDark,
                fontFamily = DisplayFont,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column {
                notificationLeadMinutes.forEach { minutes ->
                    Text(
                        text = notificationLabel(minutes),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(minutes) }
                            .padding(vertical = 12.dp),
                        color = if (minutes == selected) PillBuddyDark else PillBuddyDarker,
                        fontWeight = if (minutes == selected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 16.sp
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = PillBuddyDarker)
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

@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun NewPillBuddyPreview() {
    PillBuddyTheme {
        NewPillBuddyScreen(
            onBack = {},
            onSave = { true },
            onSaved = {},
        )
    }
}
