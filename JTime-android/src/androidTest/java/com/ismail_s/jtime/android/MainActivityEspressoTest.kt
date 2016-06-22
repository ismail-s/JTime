package com.ismail_s.jtime.android


import android.location.Location
import android.support.test.InstrumentationRegistry
import android.support.test.espresso.Espresso.onView
import android.support.test.espresso.action.*
import android.support.test.espresso.action.ViewActions.*
import android.support.test.espresso.assertion.ViewAssertions.matches
import android.support.test.espresso.matcher.ViewMatchers.*
import android.test.ActivityInstrumentationTestCase2
import android.test.suitebuilder.annotation.LargeTest
import android.view.View
import android.view.WindowManager.LayoutParams
import android.widget.TableLayout
import android.widget.TableRow
import com.ismail_s.jtime.android.MockWebServer.createMockWebServerAndConnectToRestClient
import nl.komponents.kovenant.deferred
import org.hamcrest.CoreMatchers.allOf
import org.hamcrest.CoreMatchers.containsString
import org.hamcrest.Description
import org.hamcrest.Matcher
import org.hamcrest.TypeSafeMatcher
import org.junit.Before
import java.util.*

@LargeTest
class MainActivityEspressoTest : ActivityInstrumentationTestCase2<MainActivity>(MainActivity::class.java) {

    @Before
    @Throws(Exception::class)
    public override fun setUp() {
        super.setUp()
        createMockWebServerAndConnectToRestClient()
        injectInstrumentation(InstrumentationRegistry.getInstrumentation())
        activity
        mockOutLocation(activity)
        val wakeUpDevice = Runnable { activity.window.addFlags(LayoutParams.FLAG_TURN_SCREEN_ON
                or LayoutParams.FLAG_SHOW_WHEN_LOCKED or LayoutParams.FLAG_KEEP_SCREEN_ON) }
        activity.runOnUiThread(wakeUpDevice)
    }

    private fun mockOutLocation(act: MainActivity) {
        val mockLocation = Location("mock location")
        mockLocation.latitude = 51.507
        mockLocation.longitude = -0.1275
        val newDeferred = deferred<Location, Exception>()
        newDeferred.resolve(mockLocation)
        act.locationDeferred = newDeferred
        act.location = newDeferred.promise
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

    fun testCanSeeNearbyMasjidTimes() {
        val clickOnDrawerItem = {text: String ->
            swipeInRightDrawer()
            onView(allOf(withId(R.id.material_drawer_name), withText(text))).perform(click())
        }
        val checkTextIsDisplayedAtPosition = {x: Int, y: Int, text: String ->
            onView(atTablePosition(x, y)).check(matches(allOf(isCompletelyDisplayed(), withText(text))))
        }
        clickOnDrawerItem("Fajr")
        onView(withId(R.id.label_salaah_name)).check(matches(isCompletelyDisplayed()))
        for ((x, y, text) in listOf(Triple(0, 1, "05:30"), Triple(1, 1, "06:00"), Triple(1, 0, "one"), Triple(0, 0, "two")))
            checkTextIsDisplayedAtPosition(x, y, text)

        clickOnDrawerItem("Zohar")
        for ((x, y, text) in listOf(Triple(0, 1, "12:25"), Triple(0, 0, "one")))
            checkTextIsDisplayedAtPosition(x, y, text)

        // Make sure clicking on the remaining drawer items doesn't crash the app
        for (i in listOf("Asr", "Esha"))
            clickOnDrawerItem(i)
    }

    private fun atTablePosition(x: Int, y: Int): Matcher<View> {
        return object: TypeSafeMatcher<View>() {
            override fun describeTo(description: Description) {
                description.appendText("is at position $x, $y")
            }

            override fun matchesSafely(item: View): Boolean {
                val tableRow: TableRow = item.parent as? TableRow ?: return false
                val table: TableLayout = tableRow.parent as? TableLayout ?: return false
                if (table.indexOfChild(tableRow) != x || tableRow.indexOfChild(item) != y)
                    return false
                return true
            }

        }
    }

    fun testNavigationDrawerHasButtonToReturnToMasjidsList() {
        clickOnMasjidNameToOpenMasjidFragment()
        swipeInNavigationDrawer()
        onView(allOf(withId(R.id.material_drawer_name), withText("All Masjids"))).perform(click())
        onView(allOf(withId(R.id.content), withText("one"))).check(matches(isCompletelyDisplayed()))
    }

    private fun swipeInNavigationDrawer() {
        swipeInDrawer(GeneralLocation.CENTER_LEFT, GeneralLocation.CENTER_RIGHT)
    }

    private fun swipeInRightDrawer() {
        swipeInDrawer(GeneralLocation.CENTER_RIGHT, GeneralLocation.CENTER_LEFT)
    }

    private fun swipeInDrawer(startCoord: CoordinatesProvider, endCoord: CoordinatesProvider) {
        val swipe = actionWithAssertions(GeneralSwipeAction(Swipe.FAST,
                startCoord, endCoord, Press.FINGER))
        onView(withId(R.id.fragment_container)).perform(swipe)
        sleepForSplitSecond()
    }
}
