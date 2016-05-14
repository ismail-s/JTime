package com.ismail_s.jtime.android.activity


import android.support.test.InstrumentationRegistry
import android.support.test.espresso.ViewAction
import android.support.test.espresso.action.GeneralLocation
import android.support.test.espresso.action.GeneralSwipeAction
import android.support.test.espresso.action.Press
import android.support.test.espresso.action.Swipe
import android.test.ActivityInstrumentationTestCase2
import android.test.suitebuilder.annotation.LargeTest

import com.ismail_s.jtime.android.R
import com.ismail_s.jtime.android.RestClient
import com.ismail_s.jtime.android.MockWebServer.createMockWebServerAndConnectToRestClient


import org.junit.Before
import org.junit.BeforeClass

import java.io.IOException
import java.util.Calendar

import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.ViewActions.actionWithAssertions
import android.support.test.espresso.action.ViewActions.click
import android.support.test.espresso.action.ViewActions.swipeLeft
import android.support.test.espresso.action.ViewActions.swipeRight
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed
import android.support.test.espresso.matcher.ViewMatchers.withId
import android.support.test.espresso.matcher.ViewMatchers.withText
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString

@LargeTest
class MainActivityEspressoTest : ActivityInstrumentationTestCase2<MainActivity>(MainActivity::class.java) {

    @Before
    @Throws(Exception::class)
    public override fun setUp() {
        super.setUp()
        createMockWebServerAndConnectToRestClient()
        injectInstrumentation(InstrumentationRegistry.getInstrumentation())
        activity
    }

    private fun clickOnMasjidNameToOpenMasjidFragment() {
        onView(allOf(withId(R.id.content), withText("one"))).perform(click())
    }

    @Throws(InterruptedException::class)
    fun testActivityShouldHaveMasjidName() {
        clickOnMasjidNameToOpenMasjidFragment()
        onView(withId(R.id.masjid_name)).check(matches(withText("one")))
    }

    /**
     * Before calling this method, swipe to the correct fragment. Then call it to make sure that
     * the correct date is being displayed in the currently displayed fragment.

     * @param cal is the date to check whether it is being displayed
     */
    private fun checkCorrectDateIsDisplayedInFragment(cal: Calendar) {
        sleepForSplitSecond()
        val months = arrayOf("Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
        val date = arrayOfNulls<String>(3)
        date[0] = cal.get(Calendar.YEAR).toString()
        date[1] = months[cal.get(Calendar.MONTH)]
        date[2] = cal.get(Calendar.DAY_OF_MONTH).toString()
        for (elem in date) {
            onView(allOf(withId(R.id.section_label), isCompletelyDisplayed())).check(matches(withText(containsString(elem))))
        }
    }

    fun testCorrectDateIsDisplayedinFragments() {
        clickOnMasjidNameToOpenMasjidFragment()
        val today = Calendar.getInstance()
        val yesterday = Calendar.getInstance()
        yesterday.add(Calendar.DAY_OF_YEAR, -1)
        val tomorrow = Calendar.getInstance()
        tomorrow.add(Calendar.DAY_OF_YEAR, 1)
        checkCorrectDateIsDisplayedInFragment(today)
        onView(withId(R.id.container)).perform(swipeLeft())
        checkCorrectDateIsDisplayedInFragment(tomorrow)
        onView(withId(R.id.container)).perform(swipeRight(), swipeRight())
        checkCorrectDateIsDisplayedInFragment(yesterday)
    }

    private fun checkMasjidTimesAreCorrectForCurrentFragment() {
        sleepForSplitSecond()
        onView(allOf(withId(R.id.fajr_date), isCompletelyDisplayed())).check(matches(withText("05:30")))
        onView(allOf(withId(R.id.zohar_date), isCompletelyDisplayed())).check(matches(withText("12:00")))
        onView(allOf(withId(R.id.asr_date), isCompletelyDisplayed())).check(matches(withText("15:00")))
        onView(allOf(withId(R.id.magrib_date), isCompletelyDisplayed())).check(matches(withText("15:12")))
        onView(allOf(withId(R.id.esha_date), isCompletelyDisplayed())).check(matches(withText("19:45")))
    }

    private fun sleepForSplitSecond() = try {
        Thread.sleep(200)
    } catch (e: InterruptedException) {
        throw RuntimeException("Thread running tests was interrupted.")
    }

    fun testThatMasjidTimesAreDisplayedInEachFragment() {
        clickOnMasjidNameToOpenMasjidFragment()
        checkMasjidTimesAreCorrectForCurrentFragment()
        onView(withId(R.id.container)).perform(swipeLeft())
        checkMasjidTimesAreCorrectForCurrentFragment()
        onView(withId(R.id.container)).perform(swipeRight(), swipeRight())
        checkMasjidTimesAreCorrectForCurrentFragment()
    }

    fun testNavigationDrawerHasButtonToReturnToMasjidsList() {
        clickOnMasjidNameToOpenMasjidFragment()
        swipeInNavigationDrawer()
        onView(allOf(withId(R.id.material_drawer_name), withText("All Masjids"))).perform(click())
        onView(allOf(withId(R.id.content), withText("one"))).check(matches(isCompletelyDisplayed()))
    }

    fun testThatHelpFragmentCanBeReachedFromNavbar() {
        sleepForSplitSecond()
        swipeInNavigationDrawer()
        onView(allOf(withId(R.id.material_drawer_name), withText("Help"))).perform(click())
        onView(withId(R.id.label_help)).check(matches(withText(R.string.help_text)))
    }

    private fun swipeInNavigationDrawer() {
        val swipe = actionWithAssertions(GeneralSwipeAction(Swipe.FAST,
                GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT, Press.FINGER))
        onView(withId(R.id.fragment_container)).perform(swipe)
        sleepForSplitSecond()
    }

    companion object {

        /**
         * Try to unlock the emulator. The idea behind this method is to unlock the emulator
         * just before the tests run, so the chances of the emulator timing out and locking
         * are very slim.
         */
        @BeforeClass
        @Throws(IOException::class, InterruptedException::class)
        fun unlockEmulator() {
            val command1 = "fb-adb shell input keyevent 82 || adb shell input keyevent 82"
            val command2 = "fb-adb shell input keyevent 92 || adb shell input keyevent 92"
            Runtime.getRuntime().exec(command1).waitFor()
            Runtime.getRuntime().exec(command2).waitFor()
        }
    }
}
