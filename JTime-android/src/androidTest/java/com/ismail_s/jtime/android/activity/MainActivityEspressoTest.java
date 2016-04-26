package com.ismail_s.jtime.android.activity;


import android.support.test.InstrumentationRegistry;
import android.support.test.espresso.ViewAction;
import android.support.test.espresso.action.GeneralLocation;
import android.support.test.espresso.action.GeneralSwipeAction;
import android.support.test.espresso.action.Press;
import android.support.test.espresso.action.Swipe;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import com.ismail_s.jtime.android.R;
import com.ismail_s.jtime.android.RestClient;

import org.junit.Before;
import org.junit.BeforeClass;

import java.io.IOException;
import java.util.Calendar;

import okhttp3.mockwebserver.Dispatcher;
import okhttp3.mockwebserver.MockResponse;
import okhttp3.mockwebserver.MockWebServer;
import okhttp3.mockwebserver.RecordedRequest;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.action.ViewActions.actionWithAssertions;
import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.action.ViewActions.swipeLeft;
import static android.support.test.espresso.action.ViewActions.swipeRight;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isCompletelyDisplayed;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.CoreMatchers.allOf;
import static org.hamcrest.CoreMatchers.containsString;

@LargeTest
public class MainActivityEspressoTest extends ActivityInstrumentationTestCase2<MainActivity> {

    public MainActivityEspressoTest() {
        super(MainActivity.class);
    }

    /**
     * Try to unlock the emulator. The idea behind this method is to unlock the emulator
     * just before the tests run, so the chances of the emulator timing out and locking
     * are very slim.
     */
    @BeforeClass
    public static void unlockEmulator() throws IOException,InterruptedException {
        String command1 = "fb-adb shell input keyevent 82 || adb shell input keyevent 82";
        String command2 = "fb-adb shell input keyevent 92 || adb shell input keyevent 92";
        Runtime.getRuntime().exec(command1).waitFor();
        Runtime.getRuntime().exec(command2).waitFor();
    }

    @Before
    public void setUp() throws Exception {
        super.setUp();
        MockWebServer server = new MockWebServer();
        RestClient.Companion.setUrl(String.valueOf(server.url("/")));
        server.setDispatcher(new Dispatcher() {
            @Override
            public MockResponse dispatch(RecordedRequest recordedRequest) throws InterruptedException {
                if (recordedRequest.getPath().startsWith("/Masjids/1/times")) {
                    String mockJsonResponse = ""
                            + "{\"times\": ["
                            + "{\"id\": 1,\"type\": \"f\",\"datetime\": \"2016-03-28T05:30:00.000Z\"},"
                            + "{\"id\": 2,\"type\": \"z\",\"datetime\": \"2016-03-28T12:00:00.000Z\"},"
                            + "{\"id\": 3,\"type\": \"a\",\"datetime\": \"2016-03-28T15:00:00.000Z\"},"
                            + "{\"id\": 4,\"type\": \"m\",\"datetime\": \"2016-03-28T15:12:00.000Z\"},"
                            + "{\"id\": 5,\"type\": \"e\",\"datetime\": \"2016-03-28T19:45:00.000Z\"}"
                            + "]}";
                    return new MockResponse().setBody(mockJsonResponse);
                }
                if (recordedRequest.getPath().startsWith("/Masjids")) {
                    String mockJsonResponse = "[{\"id\": 1, \"name\": \"one\"}]";
                    return new MockResponse().setBody(mockJsonResponse);
                }
                return new MockResponse().setResponseCode(404);
            }
        });
        injectInstrumentation(InstrumentationRegistry.getInstrumentation());
        getActivity();
    }

    private void clickOnMasjidNameToOpenMasjidFragment() {
        onView(allOf(withId(R.id.content), withText("one"))).perform(click());
    }

    public void testActivityShouldHaveMasjidName() throws InterruptedException {
        clickOnMasjidNameToOpenMasjidFragment();
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
        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
        String[] date = new String[3];
        date[0] = String.valueOf(cal.get(Calendar.YEAR));
        date[1] = months[cal.get(Calendar.MONTH)];
        date[2] = String.valueOf(cal.get(Calendar.DAY_OF_MONTH));
        for (String elem : date) {
            onView(allOf(withId(R.id.section_label), isCompletelyDisplayed())).check(matches(withText(containsString(elem))));
        }
    }

    public void testCorrectDateIsDisplayedinFragments() {
        clickOnMasjidNameToOpenMasjidFragment();
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
            Thread.sleep(150);
        } catch (InterruptedException e) {
            throw new RuntimeException("Thread running tests was interrupted.");
        }
    }

    public void testThatMasjidTimesAreDisplayedInEachFragment() {
        clickOnMasjidNameToOpenMasjidFragment();
        checkMasjidTimesAreCorrectForCurrentFragment();
        onView(withId(R.id.container)).perform(swipeLeft());
        checkMasjidTimesAreCorrectForCurrentFragment();
        onView(withId(R.id.container)).perform(swipeRight(), swipeRight());
        checkMasjidTimesAreCorrectForCurrentFragment();
    }

    public void testNavigationDrawerHasButtonToReturnToMasjidsList() {
        clickOnMasjidNameToOpenMasjidFragment();
        onView(withId(R.id.fragment_container)).perform(swipeInNavigationDrawer());
        onView(allOf(withId(R.id.material_drawer_name), withText("All Masjids"))).perform(click());
        onView(allOf(withId(R.id.content), withText("one"))).check(matches(isCompletelyDisplayed()));
    }

    private ViewAction swipeInNavigationDrawer() {
        return actionWithAssertions(new GeneralSwipeAction(Swipe.FAST,
                GeneralLocation.CENTER_LEFT,
                GeneralLocation.CENTER_RIGHT, Press.FINGER));
    }
}
