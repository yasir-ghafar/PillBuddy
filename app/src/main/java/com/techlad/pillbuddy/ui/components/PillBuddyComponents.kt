package com.techlad.pillbuddy.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.techlad.pillbuddy.R
import com.techlad.pillbuddy.ui.theme.PillBuddyDark
import com.techlad.pillbuddy.ui.theme.PillBuddyDarker
import com.techlad.pillbuddy.ui.theme.PillBuddyLight
import com.techlad.pillbuddy.ui.theme.PillBuddyLighter
import com.techlad.pillbuddy.ui.theme.PillBuddyMedium
import com.techlad.pillbuddy.ui.theme.PillBuddyOnPrimary

private val PillShape = RoundedCornerShape(50)

@Composable
fun PillBuddyLogo(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(112.dp)
            .background(PillBuddyMedium.copy(alpha = 0.32f), CircleShape),
        contentAlignment = Alignment.Center
    ) {
        val capsuleDark = PillBuddyDark
        val capsuleLight = PillBuddyLight
        Canvas(Modifier.size(width = 44.dp, height = 26.dp)) {
            val radius = size.height / 2f
            val capsule = Path().apply {
                addRoundRect(
                    RoundRect(
                        left = 0f,
                        top = 0f,
                        right = size.width,
                        bottom = size.height,
                        cornerRadius = CornerRadius(radius, radius)
                    )
                )
            }
            clipPath(capsule) {
                drawRect(
                    color = capsuleDark,
                    size = Size(size.width / 2f, size.height)
                )
                drawRect(
                    color = capsuleLight,
                    topLeft = Offset(size.width / 2f, 0f),
                    size = Size(size.width / 2f, size.height)
                )
            }
        }
    }
}

@Composable
fun PrimaryPillButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    leading: @Composable (() -> Unit)? = null,
) {
    Button(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = PillShape,
        colors = ButtonDefaults.buttonColors(
            containerColor = PillBuddyDark,
            contentColor = PillBuddyOnPrimary
        ),
        elevation = ButtonDefaults.buttonElevation(
            defaultElevation = 0.dp,
            pressedElevation = 0.dp
        )
    ) {
        if (leading != null) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                leading()
                Text(text = text, style = MaterialTheme.typography.labelLarge)
            }
        } else {
            Text(text = text, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
fun SnoozeButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(52.dp),
        shape = PillShape,
        border = BorderStroke(1.dp, PillBuddyLighter),
        colors = ButtonDefaults.outlinedButtonColors(
            containerColor = ReMedCardWhite,
            contentColor = PillBuddyDark
        )
    ) {
        Text(
            text = stringResource(R.string.snooze),
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

private val ReMedCardWhite = Color.White

@Composable
fun DoneButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    PrimaryPillButton(
        text = stringResource(R.string.done),
        onClick = onClick,
        modifier = modifier,
        leading = {
            Box(
                modifier = Modifier
                    .size(22.dp)
                    .border(1.5.dp, PillBuddyOnPrimary, RoundedCornerShape(6.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Filled.Check,
                    contentDescription = null,
                    modifier = Modifier.size(14.dp),
                    tint = PillBuddyOnPrimary
                )
            }
        }
    )
}

@Composable
fun NewReminderButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val shape = PillShape
    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(52.dp)
            .clip(shape)
            .background(PillBuddyMedium.copy(alpha = 0.22f), shape)
            .dashedBorder(color = PillBuddyDarker.copy(alpha = 0.55f), cornerRadius = 26.dp)
            .clickable(role = Role.Button, onClick = onClick),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Add,
            contentDescription = null,
            tint = PillBuddyDark,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = stringResource(R.string.add_pill_reminder),
            modifier = Modifier.padding(start = 6.dp),
            color = PillBuddyDark,
            style = MaterialTheme.typography.labelLarge,
            fontWeight = FontWeight.Medium
        )
    }
}

@Composable
fun DailyProgress(
    completed: Int,
    total: Int,
    modifier: Modifier = Modifier,
) {
    val fraction = if (total == 0) 0f else completed / total.toFloat()
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Box(
            modifier = Modifier
                .weight(1f)
                .height(6.dp)
                .clip(CircleShape)
                .background(PillBuddyMedium.copy(alpha = 0.45f))
        ) {
            Box(
                modifier = Modifier
                    .fillMaxHeight()
                    .fillMaxWidth(fraction.coerceIn(0f, 1f))
                    .background(PillBuddyDarker)
            )
        }
        Text(
            text = stringResource(R.string.progress_label, completed, total),
            color = PillBuddyDarker,
            style = MaterialTheme.typography.labelMedium
        )
    }
}

private fun Modifier.dashedBorder(
    color: Color,
    cornerRadius: Dp,
    strokeWidth: Dp = 1.5.dp,
): Modifier = drawBehind {
    val inset = strokeWidth.toPx() / 2f
    drawRoundRect(
        color = color,
        topLeft = Offset(inset, inset),
        size = Size(size.width - strokeWidth.toPx(), size.height - strokeWidth.toPx()),
        cornerRadius = CornerRadius(cornerRadius.toPx(), cornerRadius.toPx()),
        style = Stroke(
            width = strokeWidth.toPx(),
            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10.dp.toPx(), 8.dp.toPx()))
        )
    )
}
