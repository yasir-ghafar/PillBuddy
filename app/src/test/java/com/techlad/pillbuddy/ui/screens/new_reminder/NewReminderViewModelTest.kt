package com.techlad.pillbuddy.ui.screens.new_reminder

import com.techlad.pillbuddy.data.model.Frequency
import com.techlad.pillbuddy.data.model.NewReminder
import com.techlad.pillbuddy.data.model.Reminder
import com.techlad.pillbuddy.data.repository.ReminderStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class NewReminderViewModelTest {
    private val store = FakeReminderStore()
    private val scheduler = FakeScheduler()
    private lateinit var viewModel: NewReminderViewModel

    @Before
    fun setUp() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = NewReminderViewModel(store, scheduler)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun createReminderStoresTheDraftAndSchedulesTheSavedRowImmediately() {
        viewModel.createReminder(draft(name = "Vitamin D"))

        val saved = scheduler.scheduled.single()
        assertEquals(1L, saved.reminder.id)
        assertEquals("Vitamin D", saved.reminder.name)
        assertEquals("with breakfast", saved.reminder.instructions)
        assertEquals(Frequency.WEEK, saved.reminder.frequency)
        assertEquals(8, saved.reminder.hour)
        assertEquals(30, saved.reminder.minute)
        assertEquals(15, saved.reminder.notifyMinutesBefore)
        assertTrue(saved.fireImmediatelyIfLeadPassed)
        assertEquals(store.inserted.single().second.name, saved.reminder.name)
    }

    @Test
    fun eachCreateSchedulesItsOwnSavedReminder() {
        viewModel.createReminder(draft(name = "First"))
        viewModel.createReminder(draft(name = "Second"))

        assertEquals(listOf("First", "Second"), scheduler.scheduled.map { it.reminder.name })
        assertEquals(listOf(1L, 2L), scheduler.scheduled.map { it.reminder.id })
    }

    @Test
    fun doesNotScheduleWhenTheSavedReminderCannotBeRead() {
        store.missingIds = setOf(1L)

        viewModel.createReminder(draft(name = "Vitamin D"))

        assertEquals(1, store.inserted.size)
        assertTrue(scheduler.scheduled.isEmpty())
    }

    private fun draft(name: String) = NewReminder(
        name = name,
        instructions = "with breakfast",
        hour = 8,
        minute = 30,
        frequency = Frequency.WEEK,
        startEpochDay = 20_000,
        endEpochDay = 20_365,
        notifyMinutesBefore = 15,
    )

    private class FakeReminderStore : ReminderStore {
        val inserted = mutableListOf<Pair<Long, NewReminder>>()
        var missingIds = emptySet<Long>()
        private var nextId = 1L

        override suspend fun insert(draft: NewReminder): Long {
            val id = nextId++
            inserted += id to draft
            return id
        }

        override suspend fun getById(id: Long): Reminder? {
            if (id in missingIds) return null
            val draft = inserted.first { it.first == id }.second
            return Reminder(
                id = id,
                name = draft.name,
                instructions = draft.instructions,
                hour = draft.hour,
                minute = draft.minute,
                frequency = draft.frequency,
                startEpochDay = draft.startEpochDay,
                endEpochDay = draft.endEpochDay,
                notifyMinutesBefore = draft.notifyMinutesBefore,
            )
        }
    }

    private class FakeScheduler : SavedReminderScheduler {
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
