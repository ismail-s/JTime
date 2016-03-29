package com.ismail_s.jtime.android.activity;

import android.app.ListActivity;
import android.content.Intent;
import android.view.View;
import android.widget.ListView;

import com.ismail_s.jtime.android.BuildConfig;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class AllMasjidsActivityTest {
    ListActivity activity;
    String[] masjids = new String[]{"one", "two", "three", "four", "five", "six"};

    @Before
    public void setupActivity() {
        activity = Robolectric.setupActivity(AllMasjidsActivity.class);
    }

    public void clickOnItem(int position) {
        ListView listView = activity.getListView();
        View item = listView.getAdapter().getView(position, null, listView);
        activity.getListView().performItemClick(item, position, item.getId());
    }
    @Test
    public void testListActivityDisplaysToastsOnClickingListItems() throws Exception {
        assertTrue(activity != null);
        assertEquals(0, ShadowToast.shownToastCount());
        for (int i = 0; i <= 5; i++) {
            clickOnItem(i);
            // Robolectric won't open a new activity, so despite the call being made to start
            // a new activity, we can ignore it for this test.
            assertEquals(i+1, ShadowToast.shownToastCount());
            assertEquals(masjids[i], ShadowToast.getTextOfLatestToast());
        }
    }

    @Test
    public void testListActivityStartsMasjidActivityWithCorrectIntent() {
        for (int i = 0; i <= 5; i++) {
            clickOnItem(i);
            Intent expectedIntent = new Intent(activity, MasjidActivity.class);
            expectedIntent.putExtra(Constants.MASJID_ID, masjids[i]);
            Intent actualIntent = Shadows.shadowOf(activity).getNextStartedActivity();
            assertEquals(expectedIntent, actualIntent);
        }
    }
}
