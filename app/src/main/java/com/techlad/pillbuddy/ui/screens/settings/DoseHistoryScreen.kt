package com.techlad.pillbuddy.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techlad.pillbuddy.R
import com.techlad.pillbuddy.data.model.DoseHistoryItem
import com.techlad.pillbuddy.data.model.DoseStatus
import com.techlad.pillbuddy.data.repository.DoseHistoryRepository
import com.techlad.pillbuddy.ui.theme.DisplayFont
import com.techlad.pillbuddy.ui.theme.PillBuddyBackground
import com.techlad.pillbuddy.ui.theme.PillBuddyCard
import com.techlad.pillbuddy.ui.theme.PillBuddyDark
import com.techlad.pillbuddy.ui.theme.PillBuddyDarker
import com.techlad.pillbuddy.ui.theme.PillBuddyLighter
import com.techlad.pillbuddy.ui.theme.PillBuddyTheme
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val CardShape = RoundedCornerShape(20.dp)

@Composable
fun DoseHistoryScreen(
    doseHistory: DoseHistoryRepository,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val history by doseHistory.observeRecentTaken().collectAsState(initial = emptyList())
    DoseHistoryContent(
        history = history,
        onBack = onBack,
        modifier = modifier,
    )
}

@Composable
private fun DoseHistoryContent(
    history: List<DoseHistoryItem>,
    onBack: () -> Unit,
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
                text = stringResource(R.string.dose_history),
                modifier = Modifier.padding(start = 12.dp),
                color = PillBuddyDark,
                fontFamily = DisplayFont,
                fontWeight = FontWeight.Bold,
                fontSize = 26.sp,
            )
        }

        if (history.isEmpty()) {
            Text(
                text = stringResource(R.string.no_taken_dose_history),
                modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                color = PillBuddyDarker,
                fontSize = 15.sp,
            )
        } else {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(CardShape)
                    .background(PillBuddyCard),
            ) {
                history.forEachIndexed { index, item ->
                    if (index > 0) {
                        HorizontalDivider(color = PillBuddyLighter)
                    }
                    TakenDoseRow(item)
                }
            }
        }
    }
}

@Composable
private fun TakenDoseRow(item: DoseHistoryItem) {
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
            text = stringResource(R.string.dose_status_taken),
            color = PillBuddyDark,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
        )
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 700)
@Composable
private fun DoseHistoryPreview() {
    PillBuddyTheme {
        DoseHistoryContent(
            history = listOf(
                DoseHistoryItem(
                    id = 1,
                    reminderName = "Nexium",
                    status = DoseStatus.TAKEN,
                    actedAtMillis = System.currentTimeMillis(),
                ),
                DoseHistoryItem(
                    id = 2,
                    reminderName = "Vitamin D",
                    status = DoseStatus.TAKEN,
                    actedAtMillis = System.currentTimeMillis() - 86_400_000L,
                ),
            ),
            onBack = {},
            modifier = Modifier.background(PillBuddyBackground),
        )
    }
}
