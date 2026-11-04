package com.techlad.pillbuddy.ui.screens.new_reminder

import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.click
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onNodeWithContentDescription
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.compose.ui.test.performTouchInput
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import com.techlad.pillbuddy.R
import com.techlad.pillbuddy.data.model.AppSettings
import com.techlad.pillbuddy.data.model.Frequency
import com.techlad.pillbuddy.data.model.NewReminder
import com.techlad.pillbuddy.data.model.formatClock
import com.techlad.pillbuddy.ui.theme.PillBuddyTheme
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class NewPillBuddyScreenTest {
    @get:Rule
    val composeRule = createComposeRule()

    private val context = InstrumentationRegistry.getInstrumentation().targetContext
    private var created: NewReminder? = null
    private var backCount = 0

    @Before
    fun resetCallbacks() {
        created = null
        backCount = 0
    }

    @Test
    fun showsTheCreateFormWithTheSuppliedDefaults() {
        val defaults = AppSettings(
            defaultFrequency = Frequency.WEEK,
            defaultHour = 18,
            defaultMinute = 5,
            hourCycle = 24,
            notifyMinutesBefore = 60,
        )

        show(defaults)

        composeRule.onNodeWithText(text(R.string.new_pill_reminder)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.name_placeholder)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.instructions_placeholder)).assertIsDisplayed()
        composeRule.onNodeWithText(FormDate.today().format()).assertIsDisplayed()
        composeRule.onNodeWithText(FormDate.today().plusYears(1).format()).assertIsDisplayed()
        composeRule.onAllNodesWithText(formatClock(18, 5, 24)).assertCountEquals(4)
        composeRule.onNodeWithText(text(R.string.hour_before)).assertIsDisplayed()
        composeRule.onNodeWithText(text(R.string.today)).assertIsDisplayed()
        composeRule.onAllNodesWithText(text(R.string.tomorrow)).assertCountEquals(0)
    }

    @Test
    fun saveIgnoresABlankName() {
        show()

        clickSave()
        nameField().performTextReplacement("   ")
        instructionsField().performTextReplacement("Take with water")
        clickSave()

        assertNull(composeRule.runOnIdle { created })
    }

    @Test
    fun saveTrimsTextAndUsesTheScheduleChosenOnTheForm() {
        val defaults = AppSettings(
            defaultHour = 8,
            defaultMinute = 30,
            hourCycle = 24,
            notifyMinutesBefore = 0,
            defaultFrequency = Frequency.DAY,
        )
        show(defaults)

        nameField().performTextReplacement("  Vitamin D  ")
        instructionsField().performTextReplacement("  with breakfast  ")
        composeRule.onNodeWithText(text(R.string.every_week)).performClick()
        composeRule.onAllNodesWithText(text(R.string.tomorrow)).assertCountEquals(0)
        composeRule.onNodeWithText(context.getString(R.string.minutes_before, 0)).performTouchInput { click() }
        composeRule.onNodeWithText(context.getString(R.string.minutes_before, 15)).performClick()
        composeRule.onAllNodesWithText(text(R.string.name_placeholder)).assertCountEquals(0)
        clickSave()

        val draft = composeRule.runOnIdle { created }
        assertEquals("Vitamin D", draft?.name)
        assertEquals("with breakfast", draft?.instructions)
        assertEquals(Frequency.WEEK, draft?.frequency)
        assertEquals(8, draft?.hour)
        assertEquals(30, draft?.minute)
        assertEquals(15, draft?.notifyMinutesBefore)
        assertEquals(FormDate.today().toEpochDay(), draft?.startEpochDay)
        assertEquals(FormDate.today().plusYears(1).toEpochDay(), draft?.endEpochDay)
    }

    @Test
    fun monthScheduleOmitsTomorrowAndHourLeadTimeIsSaved() {
        show(AppSettings(defaultHour = 7, defaultMinute = 0, hourCycle = 12))

        composeRule.onNodeWithText(text(R.string.every_month)).performClick()
        composeRule.onNodeWithText(text(R.string.today)).assertIsDisplayed()
        composeRule.onAllNodesWithText(text(R.string.tomorrow)).assertCountEquals(0)
        composeRule.onNodeWithText(context.getString(R.string.minutes_before, 0)).performTouchInput { click() }
        composeRule.onNodeWithText(text(R.string.hour_before)).performClick()
        composeRule.onNodeWithText(text(R.string.hour_before)).performTouchInput { click() }
        composeRule.onNodeWithText(text(R.string.cancel)).performClick()
        nameField().performTextReplacement("Nexium")
        clickSave()

        val draft = composeRule.runOnIdle { created }
        assertEquals(Frequency.MONTH, draft?.frequency)
        assertEquals(60, draft?.notifyMinutesBefore)
        assertEquals(7, draft?.hour)
        assertEquals(0, draft?.minute)
    }

    @Test
    fun backLeavesWithoutCreatingAReminder() {
        show()

        composeRule.onNodeWithContentDescription(text(R.string.back)).performClick()

        assertEquals(1, composeRule.runOnIdle { backCount })
        assertNull(composeRule.runOnIdle { created })
    }

    @Test
    fun dismissingTheDateAndTimePickersKeepsTheDefaultSchedule() {
        val defaults = AppSettings(defaultHour = 8, defaultMinute = 30, hourCycle = 24)
        show(defaults)
        val clock = formatClock(8, 30, 24)

        composeRule.onNodeWithText(FormDate.today().format()).performTouchInput { click() }
        composeRule.onNodeWithText(text(R.string.cancel)).performClick()
        composeRule.onAllNodesWithText(text(R.string.cancel)).assertCountEquals(0)
        composeRule.onNodeWithText(FormDate.today().plusYears(1).format()).performTouchInput { click() }
        composeRule.onNodeWithText(text(R.string.cancel)).performClick()
        composeRule.onAllNodesWithText(clock)[0].performTouchInput { click() }
        composeRule.onNodeWithText(text(R.string.cancel)).performClick()
        composeRule.onAllNodesWithText(clock).assertCountEquals(4)
        nameField().performTextReplacement("Vitamin D")
        clickSave()

        val draft = composeRule.runOnIdle { created }
        assertEquals(8, draft?.hour)
        assertEquals(30, draft?.minute)
        assertEquals(FormDate.today().toEpochDay(), draft?.startEpochDay)
        assertEquals(FormDate.today().plusYears(1).toEpochDay(), draft?.endEpochDay)
    }

    private fun show(defaults: AppSettings = AppSettings()) {
        composeRule.setContent {
            PillBuddyTheme {
                NewPillBuddyScreen(
                    onBack = { backCount += 1 },
                    onCreate = { created = it },
                    defaults = defaults,
                )
            }
        }
    }

    private fun nameField() = composeRule.onAllNodes(hasSetTextAction())[0].performScrollTo()

    private fun instructionsField() = composeRule.onAllNodes(hasSetTextAction())[1].performScrollTo()

    private fun clickSave() {
        composeRule.onNodeWithText(text(R.string.save_pill_reminder)).performScrollTo().performClick()
    }

    private fun text(id: Int): String = context.getString(id)
}
