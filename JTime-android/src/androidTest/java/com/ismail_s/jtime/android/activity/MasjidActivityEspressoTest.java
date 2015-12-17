package com.ismail_s.jtime.android.activity;


import android.content.Intent;
import android.support.test.InstrumentationRegistry;
import android.test.ActivityInstrumentationTestCase2;
import android.test.suitebuilder.annotation.LargeTest;

import com.ismail_s.jtime.android.R;

import org.junit.Before;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

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
}
