package com.ismail_s.jtime.android.activity;


import android.support.test.rule.ActivityTestRule;
import android.test.suitebuilder.annotation.LargeTest;

import com.ismail_s.jtime.android.R;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
public class MasjidActivityEspressoTest {


    @Rule
    public ActivityTestRule<MasjidActivity> mActivityRule =
            new ActivityTestRule<>(MasjidActivity.class);

    @Test
    public void testActivityShouldHaveText() throws InterruptedException {
        onView(withId(R.id.title)).check(matches(withText("Masjid Activity")));
    }
}