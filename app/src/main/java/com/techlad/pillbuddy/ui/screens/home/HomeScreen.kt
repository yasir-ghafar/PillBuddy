package com.techlad.pillbuddy.ui.screens.home

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.techlad.pillbuddy.R
import com.techlad.pillbuddy.data.model.AppSettings
import com.techlad.pillbuddy.data.model.Remed
import com.techlad.pillbuddy.data.model.sampleRemeds
import com.techlad.pillbuddy.ui.feedback.performConfirmHaptic
import com.techlad.pillbuddy.ui.feedback.rememberDoseSpeaker
import com.techlad.pillbuddy.ui.components.DailyProgress
import com.techlad.pillbuddy.ui.components.DoneButton
import com.techlad.pillbuddy.ui.components.NewReminderButton
import com.techlad.pillbuddy.ui.components.SnoozeButton
import com.techlad.pillbuddy.ui.theme.PillBuddyBackground
import com.techlad.pillbuddy.ui.theme.PillBuddyTheme
import com.techlad.pillbuddy.ui.theme.PillBuddyCard
import com.techlad.pillbuddy.ui.theme.PillBuddyDark
import com.techlad.pillbuddy.ui.theme.PillBuddyDarker
import com.techlad.pillbuddy.ui.theme.PillBuddyLighter
import com.techlad.pillbuddy.ui.theme.PillBuddyMedium
import com.techlad.pillbuddy.ui.theme.PillBuddyOnPrimary
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    onAdd: () -> Unit,
    onOpenSettings: () -> Unit,
    settings: AppSettings,
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.uiState.collectAsState()
    val view = LocalView.current
    val context = LocalContext.current
    val speak = rememberDoseSpeaker()
    LaunchedEffect(Unit) {
        viewModel.refresh()
    }
    fun confirm(id: Long, spokenRes: Int) {
        val name = state.doses().firstOrNull { it.id == id }?.name.orEmpty()
        if (settings.haptics) performConfirmHaptic(view)
        if (settings.spokenConfirmation && name.isNotEmpty()) {
            speak(context.getString(spokenRes, name))
        }
    }
    HomeScreenContent(
        state = state,
        onAdd = onAdd,
        onOpenSettings = onOpenSettings,
        onDone = { id ->
            confirm(id, R.string.spoken_dose_done)
            viewModel.markDone(id)
        },
        onSnooze = { id ->
            confirm(id, R.string.spoken_dose_snoozed)
            viewModel.snooze(id)
        },
        modifier = modifier,
    )
}

private fun HomeUiState.doses(): List<Remed> {
    return listOfNotNull(current) + missed + upcoming
}

@Composable
private fun HomeScreenContent(
    state: HomeUiState,
    onAdd: () -> Unit,
    onOpenSettings: () -> Unit,
    onDone: (Long) -> Unit,
    onSnooze: (Long) -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snoozedMessage = stringResource(R.string.snoozed)
    val context = LocalContext.current
    val current = state.current
    val missed = state.missed
    val upcoming = state.upcoming
    val shareSummary = stringResource(
        R.string.share_pill_reminder_summary,
        state.totalCount,
        state.completedCount,
    )
    val shareChooser = stringResource(R.string.share_chooser)

    Column(modifier = modifier.fillMaxSize()) {
        Box(modifier = Modifier.weight(1f)) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 16.dp)
        ) {
            Text(
                text = state.dateLabel,
                style = MaterialTheme.typography.labelMedium,
                color = PillBuddyDarker
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.greeting),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.headlineMedium,
                    color = PillBuddyDark
                )
                IconButton(
                    onClick = onOpenSettings,
                    modifier = Modifier
                        .padding(end = 8.dp)
                        .size(44.dp)
                        .border(1.dp, PillBuddyDarker.copy(alpha = 0.35f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Settings,
                        contentDescription = stringResource(R.string.open_settings),
                        tint = PillBuddyDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
                IconButton(
                    onClick = {
                        val send = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareSummary)
                        }
                        context.startActivity(Intent.createChooser(send, shareChooser))
                    },
                    modifier = Modifier
                        .size(44.dp)
                        .border(1.dp, PillBuddyDarker.copy(alpha = 0.35f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = stringResource(R.string.share_pill_reminders),
                        tint = PillBuddyDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = stringResource(R.string.pill_reminder_count, state.totalCount),
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = PillBuddyDarker
            )
            DailyProgress(
                completed = state.completedCount,
                total = state.totalCount,
                modifier = Modifier.padding(top = 14.dp)
            )

            SectionLabel(
                text = stringResource(R.string.current_pill_reminder),
                modifier = Modifier.padding(top = 22.dp, bottom = 10.dp)
            )
            if (current == null) {
                Text(
                    text = stringResource(
                        if (state.totalCount == 0) {
                            R.string.no_pill_reminders
                        } else {
                            R.string.all_pill_reminders_done
                        }
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = PillBuddyDarker,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            } else {
                CurrentRemedCard(
                    remed = current,
                    onDone = { onDone(current.id) },
                    onSnooze = {
                        onSnooze(current.id)
                        scope.launch { snackbarHostState.showSnackbar(snoozedMessage) }
                    }
                )
            }

            if (upcoming.isNotEmpty()) {
                SectionLabel(
                    text = stringResource(R.string.upcoming_pill_reminders),
                    modifier = Modifier.padding(top = 22.dp, bottom = 10.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    upcoming.forEach { remed ->
                        UpcomingRemedCard(remed)
                    }
                }
            }

            if (missed.isNotEmpty()) {
                SectionLabel(
                    text = stringResource(R.string.missed_pill_reminders),
                    modifier = Modifier.padding(top = 22.dp, bottom = 10.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    missed.forEach { remed ->
                        MissedRemedCard(
                            remed = remed,
                            onDone = { onDone(remed.id) },
                        )
                    }
                }
            }

        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = PillBuddyDark,
                contentColor = PillBuddyOnPrimary,
                shape = RoundedCornerShape(16.dp)
            )
        }
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(PillBuddyBackground)
        ) {
            HorizontalDivider(color = PillBuddyLighter)
            NewReminderButton(
                onClick = onAdd,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp)
            )
        }
    }
}

@Composable
private fun SectionLabel(text: String, modifier: Modifier = Modifier) {
    Text(
        text = text,
        modifier = modifier,
        style = MaterialTheme.typography.bodyMedium,
        color = PillBuddyDarker
    )
}

@Composable
private fun CurrentRemedCard(
    remed: Remed,
    onDone: () -> Unit,
    onSnooze: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(PillBuddyCard)
            .padding(16.dp)
    ) {
        Text(
            text = if (remed.dueNow) {
                stringResource(R.string.due_now, remed.time)
            } else {
                stringResource(R.string.today_at, remed.time)
            },
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(PillBuddyMedium.copy(alpha = 0.38f))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            color = PillBuddyDark,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = remed.name,
            modifier = Modifier.padding(top = 14.dp),
            style = MaterialTheme.typography.titleLarge,
            color = PillBuddyDark
        )
        if (remed.instructions.isNotBlank()) {
            Text(
                text = remed.instructions,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = PillBuddyDarker
            )
        }
        DoneButton(
            onClick = onDone,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 16.dp)
        )
        SnoozeButton(
            onClick = onSnooze,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 10.dp)
        )
    }
}

@Composable
private fun MissedRemedCard(
    remed: Remed,
    onDone: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PillBuddyCard)
            .padding(start = 14.dp, end = 4.dp, top = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(PillBuddyMedium.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.AccessTime,
                contentDescription = null,
                tint = PillBuddyDark,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp)
        ) {
            Text(
                text = remed.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PillBuddyDark
            )
            Text(
                text = stringResource(R.string.missed_at, remed.time),
                style = MaterialTheme.typography.bodyMedium,
                color = PillBuddyDarker
            )
        }
        TextButton(onClick = onDone) {
            Text(
                text = stringResource(R.string.done),
                color = PillBuddyDark,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun UpcomingRemedCard(remed: Remed) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(PillBuddyCard)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(PillBuddyMedium.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.AccessTime,
                contentDescription = null,
                tint = PillBuddyDarker,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = remed.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = PillBuddyDark
            )
            Text(
                text = stringResource(R.string.today_at, remed.time),
                style = MaterialTheme.typography.bodyMedium,
                color = PillBuddyDarker
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomePreview() {
    PillBuddyTheme {
        HomeScreenContent(
            state = sampleRemeds().toHomeUiState(System.currentTimeMillis()),
            onAdd = {},
            onOpenSettings = {},
            onDone = {},
            onSnooze = {},
        )
    }
}
