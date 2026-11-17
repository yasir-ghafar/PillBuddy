package com.techlad.pillbuddy.ui.screens.new_reminder

import com.techlad.pillbuddy.data.model.Frequency
import com.techlad.pillbuddy.data.model.NewReminder
import com.techlad.pillbuddy.data.model.Reminder
import com.techlad.pillbuddy.data.repository.ReminderStore
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class NewReminderViewModelTest {
    private val store = FakeReminderStore()
    private val scheduler = FakeScheduler()
    private lateinit var viewModel: NewReminderViewModel

    @Before
    fun setUp() {
        viewModel = NewReminderViewModel(store, scheduler)
    }

    @Test
    fun createReminderStoresTheDraftAndSchedulesTheSavedRowImmediately() = runBlocking {
        assertTrue(viewModel.createReminder(draft(name = "Vitamin D")))

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
    fun eachCreateSchedulesItsOwnSavedReminder() = runBlocking {
        assertTrue(viewModel.createReminder(draft(name = "First")))
        assertTrue(viewModel.createReminder(draft(name = "Second")))

        assertEquals(listOf("First", "Second"), scheduler.scheduled.map { it.reminder.name })
        assertEquals(listOf(1L, 2L), scheduler.scheduled.map { it.reminder.id })
    }

    @Test
    fun createStillSucceedsWhenTheSavedReminderCannotBeRead() = runBlocking {
        store.missingIds = setOf(1L)

        assertTrue(viewModel.createReminder(draft(name = "Vitamin D")))

        assertEquals(1, store.inserted.size)
        assertTrue(scheduler.scheduled.isEmpty())
    }

    @Test
    fun createReturnsFalseWhenInsertFails() = runBlocking {
        store.failInsert = true

        assertFalse(viewModel.createReminder(draft(name = "Vitamin D")))
        assertTrue(store.inserted.isEmpty())
        assertTrue(scheduler.scheduled.isEmpty())
    }

    @Test
    fun updateReminderPersistsChangesCancelsAndReschedules() = runBlocking {
        assertTrue(viewModel.createReminder(draft(name = "Vitamin D")))
        scheduler.scheduled.clear()

        assertTrue(
            viewModel.updateReminder(
                id = 1L,
                draft = draft(name = "Vitamin D3").copy(
                    hour = 12,
                    minute = 0,
                    notifyMinutesBefore = 30,
                ),
            ),
        )

        assertEquals(listOf(1L), scheduler.cancelled)
        val updated = store.updated.single()
        assertEquals("Vitamin D3", updated.name)
        assertEquals(12, updated.hour)
        assertEquals(0, updated.minute)
        assertEquals(30, updated.notifyMinutesBefore)
        assertEquals(updated, scheduler.scheduled.single().reminder)
        assertTrue(scheduler.scheduled.single().fireImmediatelyIfLeadPassed)
    }

    @Test
    fun updateReturnsFalseWhenReminderIsMissing() = runBlocking {
        assertFalse(viewModel.updateReminder(id = 99L, draft = draft(name = "Gone")))

        assertTrue(store.updated.isEmpty())
        assertTrue(scheduler.cancelled.isEmpty())
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
        val updated = mutableListOf<Reminder>()
        var missingIds = emptySet<Long>()
        var failInsert = false
        private var nextId = 1L
        private val byId = mutableMapOf<Long, Reminder>()

        override suspend fun insert(draft: NewReminder): Long {
            if (failInsert) error("insert failed")
            val id = nextId++
            inserted += id to draft
            byId[id] = Reminder(
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
            return id
        }

        override suspend fun getById(id: Long): Reminder? {
            if (id in missingIds) return null
            return byId[id]
        }

        override suspend fun update(reminder: Reminder) {
            updated += reminder
            byId[reminder.id] = reminder
        }

        override suspend fun deleteById(id: Long) {
            byId.remove(id)
        }
    }

    private class FakeScheduler : SavedReminderScheduler {
        val scheduled = mutableListOf<Scheduled>()
        val cancelled = mutableListOf<Long>()

        override suspend fun schedule(reminder: Reminder, fireImmediatelyIfLeadPassed: Boolean) {
            scheduled += Scheduled(reminder, fireImmediatelyIfLeadPassed)
        }

        override fun cancel(reminderId: Long) {
            cancelled += reminderId
        }
    }

    private data class Scheduled(
        val reminder: Reminder,
        val fireImmediatelyIfLeadPassed: Boolean,
    )
}
