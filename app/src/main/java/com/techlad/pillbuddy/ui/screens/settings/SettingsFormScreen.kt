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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techlad.pillbuddy.R
import com.techlad.pillbuddy.data.model.TextScale
import com.techlad.pillbuddy.ui.components.PrimaryPillButton
import com.techlad.pillbuddy.ui.theme.DisplayFont
import com.techlad.pillbuddy.ui.theme.PillBuddyBackground
import com.techlad.pillbuddy.ui.theme.PillBuddyCard
import com.techlad.pillbuddy.ui.theme.PillBuddyDark
import com.techlad.pillbuddy.ui.theme.PillBuddyDarker
import com.techlad.pillbuddy.ui.theme.PillBuddyLighter
import com.techlad.pillbuddy.ui.theme.PillBuddyOnPrimary
import com.techlad.pillbuddy.ui.theme.PillBuddyTheme

private val CardShape = RoundedCornerShape(20.dp)

@Composable
fun SettingsFormScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var textScale by rememberSaveable { mutableStateOf(TextScale.DEFAULT) }
    var haptics by rememberSaveable { mutableStateOf(true) }
    var darkMode by rememberSaveable { mutableStateOf(false) }

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

        SectionLabel(stringResource(R.string.defaults))
        SettingsCard {
            ValueRow(
                label = stringResource(R.string.first_name),
                value = stringResource(R.string.profile_name),
            )
            CardDivider()
            ValueRow(
                label = stringResource(R.string.every_label),
                value = stringResource(R.string.every_day),
            )
            CardDivider()
            ValueRow(
                label = stringResource(R.string.time_label),
                value = stringResource(R.string.default_time),
            )
            CardDivider()
            ValueRow(
                label = stringResource(R.string.hours),
                value = stringResource(R.string.default_hours),
            )
        }

        SettingsCard(modifier = Modifier.padding(top = 14.dp)) {
            SwitchRow(
                title = stringResource(R.string.dark_mode),
                body = stringResource(if (darkMode) R.string.switch_on else R.string.switch_off),
                checked = darkMode,
                onCheckedChange = { darkMode = it },
            )
        }

        SettingsCard(modifier = Modifier.padding(top = 14.dp)) {
            ValueRow(
                label = stringResource(R.string.notification_label),
                value = stringResource(R.string.zero_minutes),
            )
            CardDivider()
            ValueRow(
                label = stringResource(R.string.snooze),
                value = stringResource(R.string.zero_minutes),
            )
            CardDivider()
            ValueRow(
                label = stringResource(R.string.refill_reminder),
                value = stringResource(R.string.refill_reminder_value),
            )
        }

        SectionLabel(
            text = stringResource(R.string.text_scale),
            modifier = Modifier.padding(top = 8.dp),
        )
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            TextScale.entries.forEach { scale ->
                val selected = scale == textScale
                Text(
                    text = stringResource(scale.labelRes),
                    modifier = Modifier
                        .clip(RoundedCornerShape(50))
                        .background(if (selected) PillBuddyDark else PillBuddyCard)
                        .clickable { textScale = scale }
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
            checked = haptics,
            onCheckedChange = { haptics = it },
            modifier = Modifier.padding(top = 8.dp),
        )

        PrimaryPillButton(
            text = stringResource(R.string.save),
            onClick = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 20.dp),
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
private fun ValueRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 16.dp),
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

private val TextScale.labelRes: Int
    get() = when (this) {
        TextScale.DEFAULT -> R.string.text_scale_default
        TextScale.LARGE -> R.string.text_scale_large
        TextScale.EXTRA_LARGE -> R.string.text_scale_extra_large
    }

@Preview(showBackground = true, widthDp = 390, heightDp = 900)
@Composable
private fun SettingsFormPreview() {
    PillBuddyTheme {
        SettingsFormScreen(
            onBack = {},
            modifier = Modifier.background(PillBuddyBackground),
        )
    }
}
