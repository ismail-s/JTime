package com.ismail_s.jtime.android.activity;

import android.app.ListActivity;
import android.view.View;
import android.widget.ListView;

import com.ismail_s.jtime.android.BuildConfig;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowToast;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21)
public class AllMasjidsActivityTest {

    @Test
    public void testSomething() throws Exception {
        String[] masjids = new String[]{"one", "two", "three", "four", "five", "six"};
        ListActivity activity = Robolectric.setupActivity(AllMasjidsActivity.class);
        assertTrue(activity != null);
        ListView listView = activity.getListView();
        assertEquals(0, ShadowToast.shownToastCount());
        for (int i = 0; i <= 5; i++) {
            View item = listView.getAdapter().getView(i, null, listView);
            activity.getListView().performItemClick(item, i, item.getId());
            assertEquals(i+1, ShadowToast.shownToastCount());
            assertEquals(masjids[i], ShadowToast.getTextOfLatestToast());
        }
    }
}
