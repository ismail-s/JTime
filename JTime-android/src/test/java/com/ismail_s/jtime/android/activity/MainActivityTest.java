package com.ismail_s.jtime.android.activity;

import com.ismail_s.jtime.android.BuildConfig;
import com.ismail_s.jtime.android.R;
import com.mikepenz.materialdrawer.model.AbstractDrawerItem;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;

import static com.ismail_s.jtime.android.MockWebServer.MockWebServerKt.createMockWebServerAndConnectToRestClient;
import static org.hamcrest.CoreMatchers.instanceOf;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class MainActivityTest {
    MainActivity activity;

    @Before
    public void setupActivity() {
        createMockWebServerAndConnectToRestClient();
        activity = Robolectric.setupActivity(MainActivity.class);
        assertTrue(activity != null);
    }

    @Test
    public void testThatAllMasjidsFragmentIsDisplayedFirst() {
        assertThat(activity.getCurrentFragment(), instanceOf(AllMasjidsFragment.class));
    }

    @Test
    public void testThatClickingHelpButtonInNavbarOpensHelpFragment() {
        assertNull(activity.findViewById(R.id.label_help));
        //Invoke the onItemClickListener for the help button
        AbstractDrawerItem drawerItem = (AbstractDrawerItem) activity.getDrawer()
                .getDrawerItem(activity.HELP_DRAWER_ITEM_IDENTIFIER);
        drawerItem.getOnDrawerItemClickListener().onItemClick(null, 0, null);
        assertThat(activity.getCurrentFragment(), instanceOf(HelpFragment.class));
        assertTrue(activity.findViewById(R.id.label_help).isShown());
    }
}
