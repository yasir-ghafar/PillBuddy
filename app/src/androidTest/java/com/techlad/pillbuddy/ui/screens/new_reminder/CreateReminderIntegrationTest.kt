package com.techlad.pillbuddy.ui.screens.new_reminder

import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import app.cash.sqldelight.db.SqlDriver
import app.cash.sqldelight.driver.android.AndroidSqliteDriver
import com.techlad.pillbuddy.R
import com.techlad.pillbuddy.data.db.PillBuddyDatabase
import com.techlad.pillbuddy.data.model.AppSettings
import com.techlad.pillbuddy.data.model.EpochDays
import com.techlad.pillbuddy.data.model.Frequency
import com.techlad.pillbuddy.data.model.Reminder
import com.techlad.pillbuddy.data.repository.ReminderRepository
import com.techlad.pillbuddy.ui.theme.PillBuddyTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.util.Calendar

@RunWith(AndroidJUnit4::class)
class CreateReminderIntegrationTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var driver: SqlDriver
    private lateinit var reminders: ReminderRepository
    private lateinit var scheduler: RecordingScheduler
    private lateinit var viewModel: NewReminderViewModel
    private var leftScreen = false

    @Before
    fun setUp() {
        driver = AndroidSqliteDriver(
            schema = PillBuddyDatabase.Schema,
            context = context,
            name = null,
        )
        reminders = ReminderRepository(PillBuddyDatabase(driver), Dispatchers.IO)
        scheduler = RecordingScheduler()
        viewModel = NewReminderViewModel(reminders, scheduler)
        leftScreen = false
        composeRule.setContent {
            PillBuddyTheme {
                NewPillBuddyScreen(
                    onBack = {},
                    defaults = AppSettings(
                        defaultFrequency = Frequency.DAY,
                        defaultHour = 8,
                        defaultMinute = 30,
                        hourCycle = 24,
                        notifyMinutesBefore = 0,
                    ),
                    onCreate = { draft ->
                        viewModel.createReminder(draft)
                        leftScreen = true
                    },
                )
            }
        }
    }

    @After
    fun tearDown() {
        driver.close()
    }

    @Test
    fun savingTheFormStoresTheReminderAndSchedulesIt() {
        composeRule.onAllNodes(hasSetTextAction())[0].performTextReplacement("  Vitamin D  ")
        composeRule.onAllNodes(hasSetTextAction())[1].performTextReplacement("  with breakfast  ")
        composeRule.onNodeWithText(context.getString(R.string.every_week)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.minutes_before, 0))
            .performTouchInput { click() }
        composeRule.onNodeWithText(context.getString(R.string.minutes_before, 15)).performClick()
        composeRule.onNodeWithText(context.getString(R.string.save_pill_reminder))
            .performScrollTo()
            .performClick()

        composeRule.waitForIdle()
        assertTrue(composeRule.runOnIdle { leftScreen })
        composeRule.waitUntil(timeoutMillis = 5_000) {
            runBlocking { reminders.listAll().size == 1 }
        }

        val saved = runBlocking { reminders.listAll().single() }
        val today = Calendar.getInstance()
        val nextYear = (today.clone() as Calendar).apply { add(Calendar.YEAR, 1) }
        assertEquals("Vitamin D", saved.name)
        assertEquals("with breakfast", saved.instructions)
        assertEquals(Frequency.WEEK, saved.frequency)
        assertEquals(8, saved.hour)
        assertEquals(30, saved.minute)
        assertEquals(15, saved.notifyMinutesBefore)
        assertEquals(epochDay(today), saved.startEpochDay)
        assertEquals(epochDay(nextYear), saved.endEpochDay)

        val scheduled = scheduler.snapshot().single()
        assertEquals(saved, scheduled.reminder)
        assertTrue(scheduled.fireImmediatelyIfLeadPassed)
    }

    @Test
    fun aBlankNameDoesNotStoreOrScheduleAReminder() {
        composeRule.onAllNodes(hasSetTextAction())[1].performTextReplacement("with breakfast")
        composeRule.onNodeWithText(context.getString(R.string.save_pill_reminder))
            .performScrollTo()
            .performClick()
        composeRule.waitForIdle()

        assertTrue(runBlocking { reminders.listAll().isEmpty() })
        assertTrue(scheduler.snapshot().isEmpty())
        assertFalse(composeRule.runOnIdle { leftScreen })
    }

    private fun epochDay(calendar: Calendar): Long {
        return EpochDays.of(
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH),
        )
    }

    private class RecordingScheduler : SavedReminderScheduler {
        private val scheduled = mutableListOf<Scheduled>()

        override suspend fun schedule(reminder: Reminder, fireImmediatelyIfLeadPassed: Boolean) {
            synchronized(scheduled) {
                scheduled += Scheduled(reminder, fireImmediatelyIfLeadPassed)
            }
        }

        fun snapshot(): List<Scheduled> = synchronized(scheduled) { scheduled.toList() }
    }

    private data class Scheduled(
        val reminder: Reminder,
        val fireImmediatelyIfLeadPassed: Boolean,
    )
}
