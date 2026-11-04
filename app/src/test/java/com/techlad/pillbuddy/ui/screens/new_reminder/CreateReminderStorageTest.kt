package com.techlad.pillbuddy.ui.screens.new_reminder

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.techlad.pillbuddy.data.db.PillBuddyDatabase
import com.techlad.pillbuddy.data.model.Frequency
import com.techlad.pillbuddy.data.model.NewReminder
import com.techlad.pillbuddy.data.model.Reminder
import com.techlad.pillbuddy.data.repository.ReminderRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class CreateReminderStorageTest {
    private lateinit var driver: SqlDriver
    private lateinit var reminders: ReminderRepository
    private lateinit var scheduler: RecordingScheduler
    private lateinit var viewModel: NewReminderViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        PillBuddyDatabase.Schema.create(driver).value
        reminders = ReminderRepository(PillBuddyDatabase(driver), Dispatchers.Unconfined)
        scheduler = RecordingScheduler()
        viewModel = NewReminderViewModel(reminders, scheduler)
    }

    @After
    fun tearDown() {
        driver.close()
        Dispatchers.resetMain()
    }

    @Test
    fun createReminderPersistsTheDraftAndSchedulesTheStoredRow() {
        val draft = NewReminder(
            name = "Vitamin D",
            instructions = "with breakfast",
            hour = 8,
            minute = 30,
            frequency = Frequency.WEEK,
            startEpochDay = 20_000,
            endEpochDay = 20_365,
            notifyMinutesBefore = 15,
        )

        viewModel.createReminder(draft)

        val saved = runBlocking { reminders.listAll().single() }
        assertEquals(draft.name, saved.name)
        assertEquals(draft.instructions, saved.instructions)
        assertEquals(draft.hour, saved.hour)
        assertEquals(draft.minute, saved.minute)
        assertEquals(draft.frequency, saved.frequency)
        assertEquals(draft.startEpochDay, saved.startEpochDay)
        assertEquals(draft.endEpochDay, saved.endEpochDay)
        assertEquals(draft.notifyMinutesBefore, saved.notifyMinutesBefore)
        assertEquals(saved, scheduler.scheduled.single().reminder)
        assertTrue(scheduler.scheduled.single().fireImmediatelyIfLeadPassed)
    }

    private class RecordingScheduler : SavedReminderScheduler {
        val scheduled = mutableListOf<Scheduled>()

        override suspend fun schedule(reminder: Reminder, fireImmediatelyIfLeadPassed: Boolean) {
            scheduled += Scheduled(reminder, fireImmediatelyIfLeadPassed)
        }
    }

    private data class Scheduled(
        val reminder: Reminder,
        val fireImmediatelyIfLeadPassed: Boolean,
    )
}
