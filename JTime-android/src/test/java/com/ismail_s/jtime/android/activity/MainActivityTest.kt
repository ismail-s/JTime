package com.ismail_s.jtime.android.activity

import com.ismail_s.jtime.android.BuildConfig
import com.ismail_s.jtime.android.R
import com.mikepenz.materialdrawer.model.AbstractDrawerItem

import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricGradleTestRunner
import org.robolectric.annotation.Config

import com.ismail_s.jtime.android.MockWebServer.createMockWebServerAndConnectToRestClient
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThat
import org.junit.Assert.assertTrue

@RunWith(RobolectricGradleTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
class MainActivityTest {
    lateinit var activity: MainActivity

    @Before
    fun setupActivity() {
        createMockWebServerAndConnectToRestClient()
        activity = Robolectric.setupActivity(MainActivity::class.java)
    }

    @Test
    fun testThatAllMasjidsFragmentIsDisplayedFirst() {
        assertThat(activity.currentFragment, instanceOf(AllMasjidsFragment::class.java))
    }

    @Test
    fun testThatClickingHelpButtonInNavbarOpensHelpFragment() {
        assertNull(activity.findViewById(R.id.label_help))
        //Invoke the onItemClickListener for the help button
        val drawerItem = activity.drawer?.getDrawerItem(activity.HELP_DRAWER_ITEM_IDENTIFIER) as AbstractDrawerItem?
        drawerItem?.onDrawerItemClickListener?.onItemClick(null, 0, null)
        assertThat(activity.currentFragment, instanceOf(HelpFragment::class.java))
        assertNotNull(activity.findViewById(R.id.label_help))
        assertTrue(activity.findViewById(R.id.label_help)!!.isShown)
    }
}
