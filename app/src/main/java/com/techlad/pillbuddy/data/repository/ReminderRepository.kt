package com.techlad.pillbuddy.data.repository

import com.techlad.pillbuddy.data.db.PillBuddyDatabase
import com.techlad.pillbuddy.data.db.Reminder as ReminderRow
import com.techlad.pillbuddy.data.model.Frequency
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

    suspend fun insert(draft: NewReminder): Long {
        return withContext(dispatcher) {
            database.transactionWithResult {
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
                database.reminderQueries.lastInsertedId().executeAsOne()
            }
        }
    }

    suspend fun getById(id: Long): Reminder? {
        return withContext(dispatcher) {
            database.reminderQueries.selectById(id).executeAsOneOrNull()?.toDomain()
        }
    }

    suspend fun listAll(): List<Reminder> {
        return withContext(dispatcher) {
            database.reminderQueries.selectAll().executeAsList().map { it.toDomain() }
        }
    }

    suspend fun deleteSeededSamples(): List<Long> {
        return withContext(dispatcher) {
            val ids = database.reminderQueries.selectSeededSampleIds(
                name = "Nexium",
                instructions = SEEDED_SAMPLE_INSTRUCTIONS,
                hour = 9,
                minute = 0,
            ).executeAsList()
            database.reminderQueries.deleteSeededSamples(
                name = "Nexium",
                instructions = SEEDED_SAMPLE_INSTRUCTIONS,
                hour = 9,
                minute = 0,
            )
            ids
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
        const val SEEDED_SAMPLE_INSTRUCTIONS =
            "One capsule daily, one hour before meals, with one cup of water."
    }
}
