package com.techlad.pillbuddy.ui.screens.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.techlad.pillbuddy.R
import com.techlad.pillbuddy.data.model.DoseHistoryItem
import com.techlad.pillbuddy.data.model.DoseStatus
import com.techlad.pillbuddy.data.model.EpochDays
import com.techlad.pillbuddy.data.repository.DoseHistoryRepository
import com.techlad.pillbuddy.ui.theme.DisplayFont
import com.techlad.pillbuddy.ui.theme.PillBuddyBackground
import com.techlad.pillbuddy.ui.theme.PillBuddyCard
import com.techlad.pillbuddy.ui.theme.PillBuddyDark
import com.techlad.pillbuddy.ui.theme.PillBuddyDarker
import com.techlad.pillbuddy.ui.theme.PillBuddyTheme
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

private val CardShape = RoundedCornerShape(20.dp)
private val DotOuterSize = 14.dp
private val DotCenterOffset = 21.dp

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
    val days = remember(history) { history.groupedByDay() }
    val today = EpochDays.fromMillis(System.currentTimeMillis())
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

        if (days.isEmpty()) {
            Text(
                text = stringResource(R.string.no_taken_dose_history),
                modifier = Modifier.padding(top = 8.dp, start = 4.dp),
                color = PillBuddyDarker,
                fontSize = 15.sp,
            )
        } else {
            Column(verticalArrangement = Arrangement.spacedBy(20.dp)) {
                days.forEach { day ->
                    DoseHistoryDaySection(day = day, today = today)
                }
            }
        }
    }
}

@Composable
private fun DoseHistoryDaySection(
    day: DoseHistoryDay,
    today: Long,
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = historyDateLabel(day.epochDay, today),
            modifier = Modifier
                .padding(start = 4.dp, bottom = 8.dp)
                .semantics { heading() },
            color = PillBuddyDarker,
            fontWeight = FontWeight.SemiBold,
            fontSize = 13.sp,
        )
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(CardShape)
                .background(PillBuddyCard)
                .padding(horizontal = 16.dp, vertical = 4.dp),
        ) {
            day.items.forEachIndexed { index, item ->
                TimelineDoseRow(
                    item = item,
                    isFirst = index == 0,
                    isLast = index == day.items.lastIndex,
                )
            }
        }
    }
}

@Composable
private fun TimelineDoseRow(
    item: DoseHistoryItem,
    isFirst: Boolean,
    isLast: Boolean,
) {
    val pattern = stringResource(R.string.dose_history_clock)
    val timeLabel = SimpleDateFormat(pattern, Locale.getDefault()).format(Date(item.actedAtMillis))
    val lineColor = PillBuddyDarker
    val dotColor = PillBuddyDark
    val ringColor = PillBuddyCard
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(IntrinsicSize.Min),
        verticalAlignment = Alignment.Top,
    ) {
        Box(
            modifier = Modifier
                .width(24.dp)
                .fillMaxHeight(),
        ) {
            if (!isFirst) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .width(2.dp)
                        .height(DotCenterOffset)
                        .background(lineColor),
                )
            }
            if (!isLast) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopCenter)
                        .padding(top = DotCenterOffset)
                        .width(2.dp)
                        .fillMaxHeight()
                        .background(lineColor),
                )
            }
            Box(
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(top = DotCenterOffset - DotOuterSize / 2)
                    .size(DotOuterSize)
                    .border(2.dp, ringColor, CircleShape)
                    .padding(2.dp)
                    .clip(CircleShape)
                    .background(dotColor),
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp, top = 12.dp, bottom = 12.dp),
        ) {
            Text(
                text = item.reminderName,
                color = PillBuddyDark,
                fontWeight = FontWeight.SemiBold,
                fontSize = 16.sp,
            )
            Text(
                text = timeLabel,
                modifier = Modifier.padding(top = 2.dp),
                color = PillBuddyDarker,
                fontSize = 13.sp,
            )
        }
        Text(
            text = stringResource(R.string.dose_status_taken),
            modifier = Modifier.padding(top = 14.dp),
            color = PillBuddyDark,
            fontWeight = FontWeight.Medium,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun historyDateLabel(epochDay: Long, today: Long): String {
    return when (epochDay) {
        today -> stringResource(R.string.today)
        today - 1L -> stringResource(R.string.yesterday)
        else -> {
            val pattern = if (calendarYear(epochDay) == calendarYear(today)) {
                stringResource(R.string.dose_history_date)
            } else {
                stringResource(R.string.dose_history_date_with_year)
            }
            SimpleDateFormat(pattern, Locale.getDefault())
                .format(Date(EpochDays.atLocalTime(epochDay, 12, 0)))
        }
    }
}

private data class DoseHistoryDay(
    val epochDay: Long,
    val items: List<DoseHistoryItem>,
)

private fun List<DoseHistoryItem>.groupedByDay(): List<DoseHistoryDay> {
    return groupBy { EpochDays.fromMillis(it.actedAtMillis) }
        .map { (epochDay, items) ->
            DoseHistoryDay(
                epochDay = epochDay,
                items = items.sortedByDescending { it.actedAtMillis },
            )
        }
        .sortedByDescending { it.epochDay }
}

private fun calendarYear(epochDay: Long): Int {
    return Calendar.getInstance().apply {
        timeInMillis = EpochDays.atLocalTime(epochDay, 12, 0)
    }.get(Calendar.YEAR)
}

@Preview(showBackground = true, widthDp = 390, heightDp = 700)
@Composable
private fun DoseHistoryPreview() {
    val today = EpochDays.fromMillis(System.currentTimeMillis())
    fun at(dayOffset: Long, hour: Int, minute: Int) =
        EpochDays.atLocalTime(today - dayOffset, hour, minute)

    PillBuddyTheme {
        DoseHistoryContent(
            history = listOf(
                DoseHistoryItem(
                    id = 1,
                    reminderName = "Nexium",
                    status = DoseStatus.TAKEN,
                    actedAtMillis = at(0, 9, 0),
                ),
                DoseHistoryItem(
                    id = 2,
                    reminderName = "Vitamin D",
                    status = DoseStatus.TAKEN,
                    actedAtMillis = at(0, 8, 0),
                ),
                DoseHistoryItem(
                    id = 3,
                    reminderName = "Nexium",
                    status = DoseStatus.TAKEN,
                    actedAtMillis = at(1, 9, 0),
                ),
                DoseHistoryItem(
                    id = 4,
                    reminderName = "Fish oil",
                    status = DoseStatus.TAKEN,
                    actedAtMillis = at(3, 21, 30),
                ),
            ),
            onBack = {},
            modifier = Modifier.background(PillBuddyBackground),
        )
    }
}
