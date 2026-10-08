package com.techlad.pillbuddy.data.repository

import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import com.techlad.pillbuddy.data.db.DoseEvent as DoseEventRow
import com.techlad.pillbuddy.data.db.PillBuddyDatabase
import com.techlad.pillbuddy.data.model.DoseEvent
import com.techlad.pillbuddy.data.model.DoseHistoryItem
import com.techlad.pillbuddy.data.model.DoseStatus
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class DoseHistoryRepository(
    private val database: PillBuddyDatabase,
    private val dispatcher: CoroutineDispatcher,
) {
    fun observeForDay(epochDay: Long): Flow<List<DoseEvent>> {
        return database.doseEventQueries.selectForDay(epochDay)
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.mapNotNull { it.toDomain() } }
    }

    fun observeRecent(limit: Long = RECENT_LIMIT): Flow<List<DoseHistoryItem>> {
        return database.doseEventQueries.selectRecent(limit)
            .asFlow()
            .mapToList(dispatcher)
            .map { rows ->
                rows.mapNotNull { row ->
                    val status = row.status.toDoseStatus() ?: return@mapNotNull null
                    DoseHistoryItem(
                        id = row.id,
                        reminderName = row.reminderName,
                        status = status,
                        actedAtMillis = row.actedAtMillis,
                    )
                }
            }
    }

    suspend fun record(
        reminderId: Long,
        epochDay: Long,
        status: DoseStatus,
        scheduledMinutes: Int,
        actedAtMillis: Long,
        snoozeUntilMillis: Long?,
    ) {
        withContext(dispatcher) {
            if (status == DoseStatus.TAKEN && alreadyRecorded(reminderId, epochDay, status)) return@withContext
            database.doseEventQueries.insert(
                reminderId = reminderId,
                epochDay = epochDay,
                status = status.name,
                scheduledMinutes = scheduledMinutes.toLong(),
                actedAtMillis = actedAtMillis,
                snoozeUntilMillis = snoozeUntilMillis,
            )
        }
    }

    suspend fun recordMissed(items: List<MissedDose>, actedAtMillis: Long) {
        if (items.isEmpty()) return
        withContext(dispatcher) {
            database.transaction {
                items.forEach { item ->
                    if (alreadyRecorded(item.reminderId, item.epochDay, DoseStatus.TAKEN)) return@forEach
                    if (alreadyRecorded(item.reminderId, item.epochDay, DoseStatus.MISSED)) return@forEach
                    database.doseEventQueries.insert(
                        reminderId = item.reminderId,
                        epochDay = item.epochDay,
                        status = DoseStatus.MISSED.name,
                        scheduledMinutes = item.scheduledMinutes.toLong(),
                        actedAtMillis = actedAtMillis,
                        snoozeUntilMillis = null,
                    )
                }
            }
        }
    }

    private fun alreadyRecorded(reminderId: Long, epochDay: Long, status: DoseStatus): Boolean {
        return database.doseEventQueries
            .countForReminderDayStatus(reminderId, epochDay, status.name)
            .executeAsOne() > 0L
    }

    private fun DoseEventRow.toDomain(): DoseEvent? {
        val parsed = status.toDoseStatus() ?: return null
        return DoseEvent(
            id = id,
            reminderId = reminderId,
            epochDay = epochDay,
            status = parsed,
            scheduledMinutes = scheduledMinutes.toInt(),
            actedAtMillis = actedAtMillis,
            snoozeUntilMillis = snoozeUntilMillis,
        )
    }

    private fun String.toDoseStatus(): DoseStatus? {
        return DoseStatus.entries.firstOrNull { it.name == this }
    }

    private companion object {
        const val RECENT_LIMIT = 20L
    }
}

data class MissedDose(
    val reminderId: Long,
    val epochDay: Long,
    val scheduledMinutes: Int,
)
