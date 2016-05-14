package com.ismail_s.jtime.android.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.view.View;
import android.widget.ListView;

import com.ismail_s.jtime.android.BuildConfig;
import static com.ismail_s.jtime.android.MockWebServer.MockWebServerKt.createMockWebServerAndConnectToRestClient;


import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class AllMasjidsActivityTest {
    MainActivity activity;

    @Before
    public void setupActivity() {
        createMockWebServerAndConnectToRestClient();
        activity = Robolectric.setupActivity(MainActivity.class);
    }

    @Test
    public void testListActivityDisplaysToastsOnClickingListItems() throws Exception {
        assertTrue(activity != null);
        assertThat(activity.getCurrentFragment(), instanceOf(AllMasjidsFragment.class));
    }
}
