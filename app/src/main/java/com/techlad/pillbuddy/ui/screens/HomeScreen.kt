package com.techlad.pillbuddy.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccessTime
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.techlad.pillbuddy.R
import com.techlad.pillbuddy.data.model.Remed
import com.techlad.pillbuddy.data.model.sampleRemeds
import com.techlad.pillbuddy.ui.components.DailyProgress
import com.techlad.pillbuddy.ui.components.DoneButton
import com.techlad.pillbuddy.ui.components.NewRemedButton
import com.techlad.pillbuddy.ui.components.SnoozeButton
import com.techlad.pillbuddy.ui.theme.PillBuddyTheme
import com.techlad.pillbuddy.ui.theme.ReMedCard
import com.techlad.pillbuddy.ui.theme.ReMedDark
import com.techlad.pillbuddy.ui.theme.ReMedDarker
import com.techlad.pillbuddy.ui.theme.ReMedMedium
import com.techlad.pillbuddy.ui.theme.ReMedOnPrimary
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    meds: List<Remed>,
    onMedsChange: (List<Remed>) -> Unit,
    onAdd: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val snackbarHostState = remember { SnackbarHostState() }
    val scope = rememberCoroutineScope()
    val snoozedMessage = stringResource(R.string.snoozed)
    val context = LocalContext.current
    val dateLabel = remember {
        SimpleDateFormat("EEEE, MMMM d, yyyy", Locale.getDefault()).format(Date())
    }

    val total = meds.size
    val completed = meds.count { it.completed }
    val current = meds.firstOrNull { !it.completed }
    val upcoming = meds.filter { !it.completed && it.id != current?.id }
    val shareSummary = stringResource(R.string.share_summary, total, completed)
    val shareChooser = stringResource(R.string.share_chooser)

    Box(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp)
                .padding(top = 8.dp, bottom = 24.dp)
        ) {
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.labelMedium,
                color = ReMedDarker
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
                    color = ReMedDark
                )
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
                        .border(1.dp, ReMedDarker.copy(alpha = 0.35f), CircleShape)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Share,
                        contentDescription = stringResource(R.string.share_today),
                        tint = ReMedDark,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
            Text(
                text = stringResource(R.string.remed_count, total),
                modifier = Modifier.padding(top = 4.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = ReMedDarker
            )
            DailyProgress(
                completed = completed,
                total = total,
                modifier = Modifier.padding(top = 14.dp)
            )

            SectionLabel(
                text = stringResource(R.string.current_remed),
                modifier = Modifier.padding(top = 22.dp, bottom = 10.dp)
            )
            if (current == null) {
                Text(
                    text = stringResource(if (total == 0) R.string.none_yet else R.string.all_done),
                    style = MaterialTheme.typography.bodyMedium,
                    color = ReMedDarker,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            } else {
                CurrentRemedCard(
                    remed = current,
                    onDone = {
                        onMedsChange(
                            meds.map { item ->
                                if (item.id == current.id) item.copy(completed = true, dueNow = false) else item
                            }
                        )
                    },
                    onSnooze = {
                        onMedsChange(meds.filter { it.id != current.id } + current.copy(dueNow = false))
                        scope.launch { snackbarHostState.showSnackbar(snoozedMessage) }
                    }
                )
            }

            if (upcoming.isNotEmpty()) {
                SectionLabel(
                    text = stringResource(R.string.upcoming_remeds),
                    modifier = Modifier.padding(top = 22.dp, bottom = 10.dp)
                )
                Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    upcoming.forEach { remed ->
                        UpcomingRemedCard(remed)
                    }
                }
            }

            Spacer(Modifier.height(18.dp))
            NewRemedButton(onClick = onAdd)
        }

        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
        ) { data ->
            Snackbar(
                snackbarData = data,
                containerColor = ReMedDark,
                contentColor = ReMedOnPrimary,
                shape = RoundedCornerShape(16.dp)
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
        color = ReMedDarker
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
            .background(ReMedCard)
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
                .background(ReMedMedium.copy(alpha = 0.38f))
                .padding(horizontal = 10.dp, vertical = 4.dp),
            color = ReMedDark,
            style = MaterialTheme.typography.labelMedium,
            fontWeight = FontWeight.Medium
        )
        Text(
            text = remed.name,
            modifier = Modifier.padding(top = 14.dp),
            style = MaterialTheme.typography.titleLarge,
            color = ReMedDark
        )
        if (remed.instructions.isNotBlank()) {
            Text(
                text = remed.instructions,
                modifier = Modifier.padding(top = 8.dp),
                style = MaterialTheme.typography.bodyMedium,
                color = ReMedDarker
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
private fun UpcomingRemedCard(remed: Remed) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .background(ReMedCard)
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(40.dp)
                .background(ReMedMedium.copy(alpha = 0.35f), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.AccessTime,
                contentDescription = null,
                tint = ReMedDarker,
                modifier = Modifier.size(20.dp)
            )
        }
        Column(modifier = Modifier.padding(start = 12.dp)) {
            Text(
                text = remed.name,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ReMedDark
            )
            Text(
                text = stringResource(R.string.today_at, remed.time),
                style = MaterialTheme.typography.bodyMedium,
                color = ReMedDarker
            )
        }
    }
}

@Preview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
private fun HomePreview() {
    PillBuddyTheme {
        HomeScreen(
            meds = sampleRemeds(),
            onMedsChange = {},
            onAdd = {},
        )
    }
}
