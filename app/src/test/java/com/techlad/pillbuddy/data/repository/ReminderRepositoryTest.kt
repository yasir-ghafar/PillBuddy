package com.techlad.pillbuddy.data.repository

import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import com.techlad.pillbuddy.data.db.PillBuddyDatabase
import com.techlad.pillbuddy.data.model.Frequency
import com.techlad.pillbuddy.data.model.NewReminder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ReminderRepositoryTest {
    private lateinit var driver: SqlDriver
    private lateinit var reminders: ReminderRepository

    @Before
    fun setUp() {
        driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        PillBuddyDatabase.Schema.create(driver).value
        reminders = ReminderRepository(PillBuddyDatabase(driver), Dispatchers.Unconfined)
    }

    @After
    fun tearDown() {
        driver.close()
    }

    @Test
    fun updateReplacesStoredReminderFields() = runBlocking {
        val id = reminders.insert(draft(name = "Vitamin D", hour = 8, minute = 0))
        val saved = reminders.getById(id)!!

        reminders.update(
            saved.copy(
                name = "Vitamin D3",
                instructions = "with lunch",
                hour = 12,
                minute = 15,
                frequency = Frequency.MONTH,
                startEpochDay = 20_100,
                endEpochDay = 20_400,
                notifyMinutesBefore = 30,
            ),
        )

        val updated = reminders.getById(id)!!
        assertEquals("Vitamin D3", updated.name)
        assertEquals("with lunch", updated.instructions)
        assertEquals(12, updated.hour)
        assertEquals(15, updated.minute)
        assertEquals(Frequency.MONTH, updated.frequency)
        assertEquals(20_100L, updated.startEpochDay)
        assertEquals(20_400L, updated.endEpochDay)
        assertEquals(30, updated.notifyMinutesBefore)
    }

    @Test
    fun deleteByIdRemovesReminder() = runBlocking {
        val keepId = reminders.insert(draft(name = "Keep"))
        val deleteId = reminders.insert(draft(name = "Delete"))

        reminders.deleteById(deleteId)

        assertNull(reminders.getById(deleteId))
        assertEquals(listOf(keepId), reminders.listAll().map { it.id })
        assertTrue(reminders.getById(keepId) != null)
    }

    private fun draft(
        name: String,
        hour: Int = 9,
        minute: Int = 0,
    ) = NewReminder(
        name = name,
        instructions = "with water",
        hour = hour,
        minute = minute,
        frequency = Frequency.DAY,
        startEpochDay = 20_000,
        endEpochDay = 20_365,
        notifyMinutesBefore = 15,
    )
}
