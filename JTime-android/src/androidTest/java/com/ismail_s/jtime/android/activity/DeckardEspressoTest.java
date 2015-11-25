package com.android.activity;

import com.android.R;
import com.ismail_s.activity.DeckardActivity;

import android.support.test.rule.ActivityTestRule;
import android.test.suitebuilder.annotation.LargeTest;

import org.junit.Rule;
import org.junit.Test;

import static android.support.test.espresso.Espresso.onView;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.withId;
import static android.support.test.espresso.matcher.ViewMatchers.withText;

@LargeTest
public class DeckardEspressoTest  {


    @Rule
    public ActivityTestRule<DeckardActivity> mActivityRule =
            new ActivityTestRule<>(DeckardActivity.class);

    @Test
    public void testActivityShouldHaveText() throws InterruptedException {
        onView(withId(R.id.text)).check(matches(withText("Hello Espresso!")));
    }
}
