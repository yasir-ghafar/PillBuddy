package com.techlad.pillbuddy.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
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
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.techlad.pillbuddy.R
import com.techlad.pillbuddy.data.model.AppSettings
import com.techlad.pillbuddy.data.model.Frequency
import com.techlad.pillbuddy.data.model.TextScale
import com.techlad.pillbuddy.data.model.formatClock
import com.techlad.pillbuddy.data.settings.PreferencesStore
import com.techlad.pillbuddy.ui.components.PrimaryPillButton
import com.techlad.pillbuddy.ui.theme.DisplayFont
import com.techlad.pillbuddy.ui.theme.PillBuddyCard
import com.techlad.pillbuddy.ui.theme.PillBuddyDark
import com.techlad.pillbuddy.ui.theme.PillBuddyDarker
import com.techlad.pillbuddy.ui.theme.PillBuddyLighter
import com.techlad.pillbuddy.ui.theme.PillBuddyOnPrimary
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val CardShape = RoundedCornerShape(20.dp)
private val notificationChoices = listOf(0, 5, 15, 30, 60)
private val snoozeChoices = listOf(0, 5, 10, 15, 30)
private val refillChoices = listOf(0, 1, 3, 7, 14)
private val hourCycles = listOf(12, 24)

private enum class SettingsEditor {
    None,
    Name,
    Every,
    Time,
    Hours,
    Notification,
    Snooze,
    Refill,
}

@Composable
fun SettingsFormScreen(
    preferences: PreferencesStore,
    onBack: () -> Unit,
    onOpenDoseHistory: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val stored by preferences.settings.collectAsState(initial = null as AppSettings?)
    var defaultsReady by rememberSaveable { mutableStateOf(false) }
    var firstName by rememberSaveable { mutableStateOf(AppSettings().firstName) }
    var frequency by rememberSaveable { mutableStateOf(Frequency.DAY) }
    var hour by rememberSaveable { mutableIntStateOf(AppSettings().defaultHour) }
    var minute by rememberSaveable { mutableIntStateOf(AppSettings().defaultMinute) }
    var hourCycle by rememberSaveable { mutableIntStateOf(12) }
    var notifyMinutes by rememberSaveable { mutableIntStateOf(0) }
    var snoozeMinutes by rememberSaveable { mutableIntStateOf(AppSettings().snoozeMinutes) }
    var refillDays by rememberSaveable { mutableIntStateOf(0) }
    var editor by rememberSaveable { mutableStateOf(SettingsEditor.None) }
    var savedVisible by rememberSaveable { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    LaunchedEffect(stored) {
        val current = stored ?: return@LaunchedEffect
        if (!defaultsReady) {
            firstName = current.firstName
            frequency = current.defaultFrequency
            hour = current.defaultHour
            minute = current.defaultMinute
            hourCycle = current.hourCycle
            notifyMinutes = current.notifyMinutesBefore
            snoozeMinutes = current.snoozeMinutes
            refillDays = current.refillDaysBefore
            defaultsReady = true
        }
    }
    LaunchedEffect(savedVisible) {
        if (savedVisible) {
            delay(2_000)
            savedVisible = false
        }
    }

    val baseline = stored
    if (baseline == null || !defaultsReady) {
        SettingsHeader(onBack = onBack, modifier = modifier.padding(horizontal = 20.dp))
        return
    }

    val shown = baseline.copy(
        firstName = firstName,
        defaultFrequency = frequency,
        defaultHour = hour,
        defaultMinute = minute,
        hourCycle = hourCycle,
        notifyMinutesBefore = notifyMinutes,
        snoozeMinutes = snoozeMinutes,
        refillDaysBefore = refillDays,
    )

    SettingsContent(
        settings = shown,
        savedVisible = savedVisible,
        onBack = onBack,
        onOpenDoseHistory = onOpenDoseHistory,
        onEditName = { editor = SettingsEditor.Name },
        onEditFrequency = { editor = SettingsEditor.Every },
        onEditTime = { editor = SettingsEditor.Time },
        onEditHourCycle = { editor = SettingsEditor.Hours },
        onEditNotification = { editor = SettingsEditor.Notification },
        onEditSnooze = { editor = SettingsEditor.Snooze },
        onEditRefill = { editor = SettingsEditor.Refill },
        onTextScale = { scale -> scope.launch { preferences.setTextScale(scale) } },
        onHaptics = { enabled -> scope.launch { preferences.setHaptics(enabled) } },
        onSave = {
            scope.launch {
                preferences.saveDefaults(shown)
                savedVisible = true
            }
        },
        modifier = modifier,
    )

    when (editor) {
        SettingsEditor.Name -> NameDialog(
            initial = firstName,
            onDismiss = { editor = SettingsEditor.None },
            onConfirm = { updated ->
                firstName = updated
                editor = SettingsEditor.None
            },
        )

        SettingsEditor.Every -> ChoiceDialog(
            title = stringResource(R.string.every_label),
            choices = frequencyChoiceLabels(),
            selectedIndex = Frequency.entries.indexOf(frequency),
            onDismiss = { editor = SettingsEditor.None },
            onSelect = { index ->
                frequency = Frequency.entries[index]
                editor = SettingsEditor.None
            },
        )

        SettingsEditor.Time -> DefaultTimeDialog(
            hour = hour,
            minute = minute,
            hourCycle = hourCycle,
            onDismiss = { editor = SettingsEditor.None },
            onConfirm = { pickedHour, pickedMinute ->
                hour = pickedHour
                minute = pickedMinute
                editor = SettingsEditor.None
            },
        )

        SettingsEditor.Hours -> ChoiceDialog(
            title = stringResource(R.string.hours),
            choices = hourCycles.map { it.toString() },
            selectedIndex = hourCycles.indexOf(hourCycle).coerceAtLeast(0),
            onDismiss = { editor = SettingsEditor.None },
            onSelect = { index ->
                hourCycle = hourCycles[index]
                editor = SettingsEditor.None
            },
        )

        SettingsEditor.Notification -> {
            val options = minuteOptions(notificationChoices, notifyMinutes)
            ChoiceDialog(
                title = stringResource(R.string.notification_label),
                choices = minuteChoiceLabels(options),
                selectedIndex = options.indexOf(notifyMinutes),
                onDismiss = { editor = SettingsEditor.None },
                onSelect = { index ->
                    notifyMinutes = options[index]
                    editor = SettingsEditor.None
                },
            )
        }

        SettingsEditor.Snooze -> {
            val options = minuteOptions(snoozeChoices, snoozeMinutes)
            ChoiceDialog(
                title = stringResource(R.string.snooze),
                choices = minuteChoiceLabels(options),
                selectedIndex = options.indexOf(snoozeMinutes),
                onDismiss = { editor = SettingsEditor.None },
                onSelect = { index ->
                    snoozeMinutes = options[index]
                    editor = SettingsEditor.None
                },
            )
        }

        SettingsEditor.Refill -> {
            val options = dayOptions(refillDays)
            ChoiceDialog(
                title = stringResource(R.string.refill_reminder),
                choices = refillChoiceLabels(options),
                selectedIndex = options.indexOf(refillDays),
                onDismiss = { editor = SettingsEditor.None },
                onSelect = { index ->
                    refillDays = options[index]
                    editor = SettingsEditor.None
                },
            )
        }

        SettingsEditor.None -> Unit
    }
}


@Composable
private fun SettingsContent(
    settings: AppSettings,
    savedVisible: Boolean,
    onBack: () -> Unit,
    onOpenDoseHistory: () -> Unit,
    onEditName: () -> Unit,
    onEditFrequency: () -> Unit,
    onEditTime: () -> Unit,
    onEditHourCycle: () -> Unit,
    onEditNotification: () -> Unit,
    onEditSnooze: () -> Unit,
    onEditRefill: () -> Unit,
    onTextScale: (TextScale) -> Unit,
    onHaptics: (Boolean) -> Unit,
    onSave: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),)
    {
        SettingsHeader(onBack = onBack)

        SectionLabel(stringResource(R.string.defaults))
        SettingsCard {
            ValueRow(
                label = stringResource(R.string.first_name),
                value = settings.firstName,
                onClick = onEditName,
            )
            CardDivider()
            ValueRow(
                label = stringResource(R.string.every_label),
                value = frequencyLabel(settings.defaultFrequency),
                onClick = onEditFrequency,
            )
            CardDivider()
            ValueRow(
                label = stringResource(R.string.time_label),
                value = formatClock(settings.defaultHour, settings.defaultMinute, settings.hourCycle),
                onClick = onEditTime,
            )
            CardDivider()
            ValueRow(
                label = stringResource(R.string.hours),
                value = settings.hourCycle.toString(),
                onClick = onEditHourCycle,
            )
        }

        SettingsCard(modifier = Modifier.padding(top = 14.dp)) {
            /*SwitchRow(
                title = stringResource(R.string.dark_mode),
                body = stringResource(if (settings) R.string.switch_on else R.string.switch_off),
                checked = darkMode,
                onCheckedChange = { darkMode = it },
            )*/
        }

        SettingsCard(modifier = Modifier.padding(top = 14.dp)) {
            ValueRow(
                label = stringResource(R.string.notification_label),
                value = minutesLabel(settings.notifyMinutesBefore),
                onClick = onEditNotification,
            )
            CardDivider()
            ValueRow(
                label = stringResource(R.string.snooze),
                value = minutesLabel(settings.snoozeMinutes),
                onClick = onEditSnooze,
            )
            CardDivider()
            ValueRow(
                label = stringResource(R.string.refill_reminder),
                value = refillLabel(settings.refillDaysBefore),
                onClick = onEditRefill,
            )
        }

        SectionLabel(
            text = stringResource(R.string.text_scale),
            modifier = Modifier.padding(top = 8.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextScale.entries.forEach { scale ->
                val selected = scale == settings.textScale
                Text(
                    text = stringResource(scale.labelRes),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) PillBuddyDark else PillBuddyCard)
                        .clickable { onTextScale(scale) }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    color = if (selected) PillBuddyOnPrimary else PillBuddyDark,
                    fontWeight = FontWeight.Medium,
                    fontSize = 15.sp,
                )
            }
        }

        SettingSwitch(
            title = stringResource(R.string.haptics),
            body = stringResource(R.string.haptics_description),
            checked = settings.haptics,
            onCheckedChange = onHaptics,
            modifier = Modifier.padding(top = 8.dp),
        )

        SettingsCard(modifier = Modifier.padding(top = 14.dp)) {
            NavigationRow(
                label = stringResource(R.string.view_dose_history),
                onClick = onOpenDoseHistory,
            )
        }

        PrimaryPillButton(
            text = stringResource(R.string.save),
            onClick = onSave,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
        )
        if (savedVisible) {
            Text(
                text = stringResource(R.string.settings_saved),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp),
                color = PillBuddyDarker,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun SettingsHeader(onBack: () -> Unit, modifier: Modifier = Modifier) {
    Row(
        modifier = modifier
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
            text = stringResource(R.string.settings),
            modifier = Modifier.padding(start = 12.dp),
            color = PillBuddyDark,
            fontFamily = DisplayFont,
            fontWeight = FontWeight.Bold,
            fontSize = 26.sp,
        )
    }
}
@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(start = 4.dp, bottom = 8.dp),
        color = PillBuddyDarker,
        fontSize = 13.sp,
    )
}

@Composable
private fun SettingsCard(
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(PillBuddyCard),
        content = content,
    )
}

@Composable
private fun CardDivider() {
    HorizontalDivider(color = PillBuddyLighter)
}

@Composable
private fun ValueRow(label: String, value: String, onClick: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            modifier = Modifier.padding(end = 12.dp),
            color = PillBuddyDark,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
        )
        Text(
            text = value,
            color = PillBuddyDarker,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
        )
    }
}

@Composable
private fun NavigationRow(
    label: String,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = label,
            color = PillBuddyDark,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
        )
        Icon(
            imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
            contentDescription = null,
            tint = PillBuddyDarker,
        )
    }
}

@Composable
private fun SwitchRow(
    title: String,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = PillBuddyDark,
                fontWeight = FontWeight.Medium,
                fontSize = 16.sp,
            )
            Text(
                text = body,
                modifier = Modifier.padding(top = 2.dp),
                color = PillBuddyDarker,
                fontSize = 13.sp,
            )
        }
        SettingsSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingSwitch(
    title: String,
    body: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(top = 12.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(PillBuddyCard)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = PillBuddyDark,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
            Text(
                text = body,
                modifier = Modifier.padding(top = 2.dp),
                color = PillBuddyDarker,
                fontSize = 13.sp,
            )
        }
        SettingsSwitch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun SettingsSwitch(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
) {
    Switch(
        checked = checked,
        onCheckedChange = onCheckedChange,
        colors = SwitchDefaults.colors(
            checkedThumbColor = PillBuddyCard,
            checkedTrackColor = PillBuddyDark,
            uncheckedThumbColor = PillBuddyCard,
            uncheckedTrackColor = PillBuddyLighter,
            uncheckedBorderColor = PillBuddyLighter,
        ),
    )
}

@Composable
private fun NameDialog(
    initial: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
) {
    var value by remember { mutableStateOf(initial) }
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PillBuddyCard,
        title = {
            Text(
                text = stringResource(R.string.first_name),
                color = PillBuddyDark,
                fontFamily = DisplayFont,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            BasicTextField(
                value = value,
                onValueChange = { value = it },
                singleLine = true,
                textStyle = TextStyle(
                    color = PillBuddyDark,
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Medium,
                ),
                cursorBrush = SolidColor(PillBuddyDark),
                keyboardOptions = KeyboardOptions(capitalization = KeyboardCapitalization.Words),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(PillBuddyLighter)
                    .padding(horizontal = 12.dp, vertical = 12.dp),
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    val trimmed = value.trim()
                    if (trimmed.isNotEmpty()) onConfirm(trimmed)
                },
            ) {
                Text(stringResource(R.string.ok), color = PillBuddyDark)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = PillBuddyDarker)
            }
        },
    )
}

@Composable
private fun ChoiceDialog(
    title: String,
    choices: List<String>,
    selectedIndex: Int,
    onDismiss: () -> Unit,
    onSelect: (Int) -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PillBuddyCard,
        title = {
            Text(
                text = title,
                color = PillBuddyDark,
                fontFamily = DisplayFont,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column {
                choices.forEachIndexed { index, label ->
                    val selected = index == selectedIndex
                    Text(
                        text = label,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onSelect(index) }
                            .padding(vertical = 12.dp),
                        color = if (selected) PillBuddyDark else PillBuddyDarker,
                        fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                        fontSize = 16.sp,
                    )
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.cancel), color = PillBuddyDarker)
            }
        },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DefaultTimeDialog(
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
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                TimePicker(state = state)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
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
private fun frequencyChoiceLabels(): List<String> {
    val labels = ArrayList<String>(Frequency.entries.size)
    for (option in Frequency.entries) {
        labels += frequencyLabel(option)
    }
    return labels
}

@Composable
private fun minuteChoiceLabels(values: List<Int>): List<String> {
    val labels = ArrayList<String>(values.size)
    for (value in values) {
        labels += minutesLabel(value)
    }
    return labels
}

@Composable
private fun refillChoiceLabels(values: List<Int>): List<String> {
    val labels = ArrayList<String>(values.size)
    for (value in values) {
        labels += refillLabel(value)
    }
    return labels
}

@Composable
private fun frequencyLabel(frequency: Frequency): String {
    val res = when (frequency) {
        Frequency.DAY -> R.string.every_day
        Frequency.WEEK -> R.string.every_week
        Frequency.MONTH -> R.string.every_month
    }
    return stringResource(res)
}

@Composable
private fun minutesLabel(minutes: Int): String {
    return if (minutes == 0) {
        stringResource(R.string.zero_minutes)
    } else {
        stringResource(R.string.minutes_short, minutes)
    }
}

@Composable
private fun refillLabel(days: Int): String {
    return if (days == 0) {
        stringResource(R.string.refill_reminder_value)
    } else {
        stringResource(R.string.refill_days_before, days)
    }
}

private fun minuteOptions(presets: List<Int>, selected: Int): List<Int> {
    return (presets + selected).distinct().sorted()
}

private fun dayOptions(selected: Int): List<Int> {
    return (refillChoices + selected).distinct().sorted()
}

private val TextScale.labelRes: Int
    get() = when (this) {
        TextScale.DEFAULT -> R.string.text_scale_default
        TextScale.LARGE -> R.string.text_scale_large
        TextScale.EXTRA_LARGE -> R.string.text_scale_extra_large
    }

/*@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun SettingsFormPreview() {
    PillBuddyTheme {
        SettingsFormScreen(
            onBack = {},
            onOpenDoseHistory = {},
            modifier = Modifier.background(PillBuddyBackground),
        )
    }
}*/
