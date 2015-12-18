package com.ismail_s.jtime.android.activity;


import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import com.ismail_s.jtime.android.R;

import org.junit.Before;

import java.util.Calendar;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;

@LargeTest
public class MasjidActivityEspressoTest extends ActivityInstrumentationTestCase2<MasjidActivity> {

    public MasjidActivityEspressoTest() {
        super(MasjidActivity.class);
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        Intent intent = new Intent();
        intent.putExtra(Constants.MASJID_NAME, "one");
        setActivityIntent(intent);
        getActivity();
    }

    public void testActivityShouldHaveMasjidName() throws InterruptedException {
        onView(withId(R.id.masjid_name)).check(matches(withText("one")));
    }

    /**
     * Before calling this method, swipe to the correct fragment. Then call it to make sure that
     * the correct date is being displayed in the currently displayed fragment.
     *
     * @param cal is the date to check whether it is being displayed
     */
    private void checkCorrectDateIsDisplayedInFragment(Calendar cal) {
        sleepForSplitSecond();
        String[] months = {"Jan", "Feb", "Mar", "Apr", "may", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] date = new String[3];
        date[0] = String.valueOf(cal.get(Calendar.YEAR));
        date[1] = months[cal.get(Calendar.MONTH)];
        date[2] = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        for (String elem : date) {
            onView(allOf(withId(R.id.section_label), isCompletelyDisplayed())).check(matches(withText(containsString(elem))));
        }
    }

    public void testCorrectDateIsDisplayedinFragments() {
        Calendar today = Calendar.getInstance();
        Calendar yesterday = Calendar.getInstance();
        yesterday.add(Calendar.DAY_OF_YEAR, -1);
        Calendar tomorrow = Calendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_YEAR, 1);
        checkCorrectDateIsDisplayedInFragment(today);
        onView(withId(R.id.container)).perform(swipeLeft());
        checkCorrectDateIsDisplayedInFragment(tomorrow);
        onView(withId(R.id.container)).perform(swipeRight(), swipeRight());
        checkCorrectDateIsDisplayedInFragment(yesterday);
    }

    private void checkMasjidTimesAreCorrectForCurrentFragment() {
        sleepForSplitSecond();
        onView(allOf(withId(R.id.fajr_date), isCompletelyDisplayed())).check(matches(withText("05:30")));
        onView(allOf(withId(R.id.zohar_date), isCompletelyDisplayed())).check(matches(withText("12:00")));
        onView(allOf(withId(R.id.asr_date), isCompletelyDisplayed())).check(matches(withText("15:00")));
        onView(allOf(withId(R.id.magrib_date), isCompletelyDisplayed())).check(matches(withText("15:12")));
        onView(allOf(withId(R.id.esha_date), isCompletelyDisplayed())).check(matches(withText("19:45")));
    }

    private void sleepForSplitSecond() {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread running tests was interrupted.");
        }
    }

    public void testThatMasjidTimesAreDisplayedInEachFragment() {
        checkMasjidTimesAreCorrectForCurrentFragment();
        onView(withId(R.id.container)).perform(swipeLeft());
        checkMasjidTimesAreCorrectForCurrentFragment();
        onView(withId(R.id.container)).perform(swipeRight(), swipeRight());
        checkMasjidTimesAreCorrectForCurrentFragment();
    }
}
