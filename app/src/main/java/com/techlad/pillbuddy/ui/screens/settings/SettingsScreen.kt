package com.techlad.pillbuddy.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techlad.pillbuddy.R
import com.techlad.pillbuddy.data.model.AppSettings
import com.techlad.pillbuddy.data.model.DoseHistoryItem
import com.techlad.pillbuddy.data.model.DoseStatus
import com.techlad.pillbuddy.data.model.TextScale
import com.techlad.pillbuddy.data.repository.DoseHistoryRepository
import com.techlad.pillbuddy.data.settings.PreferencesStore
import com.techlad.pillbuddy.ui.theme.DisplayFont
import com.techlad.pillbuddy.ui.theme.PillBuddyCard
import com.techlad.pillbuddy.ui.theme.PillBuddyDark
import com.techlad.pillbuddy.ui.theme.PillBuddyDarker
import com.techlad.pillbuddy.ui.theme.PillBuddyLighter
import com.techlad.pillbuddy.ui.theme.PillBuddyOnPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val FieldShape = RoundedCornerShape(16.dp)

@Composable
fun SettingsScreen(
    preferences: PreferencesStore,
    doseHistory: DoseHistoryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val settings by preferences.settings.collectAsState(initial = AppSettings())
    val history by doseHistory.observeRecent().collectAsState(initial = emptyList())
    val scope = rememberCoroutineScope()
    SettingsContent(
        settings = settings,
        history = history,
        onBack = onBack,
        onTextScale = { scale -> scope.launch { preferences.setTextScale(scale) } },
        onHighContrast = { enabled -> scope.launch { preferences.setHighContrast(enabled) } },
        onHaptics = { enabled -> scope.launch { preferences.setHaptics(enabled) } },
        onSpokenConfirmation = { enabled ->
            scope.launch { preferences.setSpokenConfirmation(enabled) }
        },
        modifier = modifier,
    )
}

@Composable
private fun SettingsContent(
    settings: AppSettings,
    history: List<DoseHistoryItem>,
    onBack: () -> Unit,
    onTextScale: (TextScale) -> Unit,
    onHighContrast: (Boolean) -> Unit,
    onHaptics: (Boolean) -> Unit,
    onSpokenConfirmation: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 20.dp)
            .padding(bottom = 24.dp),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .offset(x = (-12).dp)
                .padding(top = 4.dp, bottom = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onBack) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = stringResource(R.string.back),
                    tint = PillBuddyDark,
                )
            }
            Text(
                text = stringResource(R.string.settings),
                color = PillBuddyDark,
                fontFamily = DisplayFont,
                fontWeight = FontWeight.Bold,
                fontSize = 22.sp,
            )
        }

        ProfileRow(
            label = stringResource(R.string.first_name),
            value = stringResource(R.string.profile_name),
        )

        SectionTitle(stringResource(R.string.text_scale))
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
            title = stringResource(R.string.high_contrast),
            body = stringResource(R.string.high_contrast_description),
            checked = settings.highContrast,
            onCheckedChange = onHighContrast,
            modifier = Modifier.padding(top = 8.dp),
        )
        SettingSwitch(
            title = stringResource(R.string.haptics),
            body = stringResource(R.string.haptics_description),
            checked = settings.haptics,
            onCheckedChange = onHaptics,
        )
        SettingSwitch(
            title = stringResource(R.string.spoken_confirmation),
            body = stringResource(R.string.spoken_confirmation_description),
            checked = settings.spokenConfirmation,
            onCheckedChange = onSpokenConfirmation,
        )

        SectionTitle(
            text = stringResource(R.string.dose_history),
            modifier = Modifier.padding(top = 12.dp),
        )
        if (history.isEmpty()) {
            Text(
                text = stringResource(R.string.no_dose_history),
                color = PillBuddyDarker,
                fontSize = 15.sp,
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(FieldShape)
                    .background(PillBuddyCard),
            ) {
                history.forEachIndexed { index, item ->
                    if (index > 0) {
                        HorizontalDivider(
                            color = PillBuddyLighter,
                            modifier = Modifier.padding(horizontal = 16.dp),
                        )
                    }
                    HistoryRow(item)
                }
            }
        }
    }
}

@Composable
private fun ProfileRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(FieldShape)
            .background(PillBuddyCard)
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
        Text(
            text = value,
            color = PillBuddyDark,
            fontWeight = FontWeight.Medium,
            fontSize = 16.sp,
        )
    }
}

@Composable
private fun SectionTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier.padding(start = 4.dp, top = 16.dp, bottom = 8.dp),
        color = PillBuddyDarker,
        fontSize = 13.sp,
    )
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
            .clip(FieldShape)
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
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun HistoryRow(item: DoseHistoryItem) {
    val pattern = stringResource(R.string.dose_history_time)
    val whenLabel = SimpleDateFormat(pattern, Locale.getDefault()).format(Date(item.actedAtMillis))
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = item.reminderName,
                color = PillBuddyDark,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
            Text(
                text = whenLabel,
                color = PillBuddyDarker,
                fontSize = 13.sp,
            )
        }
        Text(
            text = stringResource(item.status.labelRes),
            color = PillBuddyDark,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
        )
    }
}

private val TextScale.labelRes: Int
    get() = when (this) {
        TextScale.DEFAULT -> R.string.text_scale_default
        TextScale.LARGE -> R.string.text_scale_large
        TextScale.EXTRA_LARGE -> R.string.text_scale_extra_large
    }

private val DoseStatus.labelRes: Int
    get() = when (this) {
        DoseStatus.TAKEN -> R.string.dose_status_taken
        DoseStatus.SNOOZED -> R.string.dose_status_snoozed
        DoseStatus.MISSED -> R.string.dose_status_missed
    }
