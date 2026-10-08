package com.techlad.pillbuddy.data.repository

import com.techlad.pillbuddy.data.db.PillBuddyDatabase
import com.techlad.pillbuddy.data.db.Reminder as ReminderRow
import com.techlad.pillbuddy.data.model.EpochDays
import com.techlad.pillbuddy.data.model.Frequency
import com.techlad.pillbuddy.data.model.NexiumInstructions
import com.techlad.pillbuddy.data.model.NewReminder
import com.techlad.pillbuddy.data.model.Reminder
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext

class ReminderRepository(
    private val database: PillBuddyDatabase,
    private val dispatcher: CoroutineDispatcher,
) {
    fun observeAll(): Flow<List<Reminder>> {
        return database.reminderQueries.selectAll()
            .asFlow()
            .mapToList(dispatcher)
            .map { rows -> rows.map { it.toDomain() } }
    }

    suspend fun insert(draft: NewReminder) {
        withContext(dispatcher) {
            database.reminderQueries.insert(
                name = draft.name,
                instructions = draft.instructions,
                hour = draft.hour.toLong(),
                minute = draft.minute.toLong(),
                frequency = draft.frequency.name,
                startEpochDay = draft.startEpochDay,
                endEpochDay = draft.endEpochDay,
                notifyMinutesBefore = draft.notifyMinutesBefore.toLong(),
                createdAtMillis = System.currentTimeMillis(),
            )
        }
    }

    suspend fun insertSamples(nowMillis: Long = System.currentTimeMillis()) {
        withContext(dispatcher) {
            if (database.reminderQueries.count().executeAsOne() > 0L) return@withContext
            val today = EpochDays.fromMillis(nowMillis)
            database.transaction {
                repeat(SAMPLE_COUNT) {
                    database.reminderQueries.insert(
                        name = "Nexium",
                        instructions = NexiumInstructions,
                        hour = 9,
                        minute = 0,
                        frequency = Frequency.DAY.name,
                        startEpochDay = today,
                        endEpochDay = null,
                        notifyMinutesBefore = 0,
                        createdAtMillis = nowMillis,
                    )
                }
            }
        }
    }

    private fun ReminderRow.toDomain(): Reminder {
        return Reminder(
            id = id,
            name = name,
            instructions = instructions,
            hour = hour.toInt(),
            minute = minute.toInt(),
            frequency = Frequency.entries.firstOrNull { it.name == frequency } ?: Frequency.DAY,
            startEpochDay = startEpochDay,
            endEpochDay = endEpochDay,
            notifyMinutesBefore = notifyMinutesBefore.toInt(),
        )
    }

    private companion object {
        const val SAMPLE_COUNT = 3
    }
}
