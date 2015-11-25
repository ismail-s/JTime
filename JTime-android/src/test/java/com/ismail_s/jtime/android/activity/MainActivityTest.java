package com.ismail_s.jtime.android.activity;

import com.ismail_s.jtime.android.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MainActivityTest {

    @Test
    public void testSomething() throws Exception {
        assertTrue(Robolectric.setupActivity(MainActivity.class) != null);
    }
}
