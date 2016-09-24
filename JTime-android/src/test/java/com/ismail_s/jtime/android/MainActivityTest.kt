package com.ismail_s.jtime.android

import com.ismail_s.jtime.android.MockWebServer.createMockWebServerAndConnectToRestClient
import com.ismail_s.jtime.android.fragment.HomeFragment
import com.ismail_s.jtime.android.fragment.HelpFragment
import com.mikepenz.materialdrawer.model.AbstractDrawerItem
import org.hamcrest.CoreMatchers.instanceOf
import org.junit.Assert.assertThat
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import kotlin.test.assertNotNull
import kotlin.test.assertNull
import kotlin.test.assertTrue

@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(21))
class MainActivityTest {
    lateinit var activity: MainActivity

    @Before
    fun setupActivity() {
        createMockWebServerAndConnectToRestClient()
        activity = Robolectric.setupActivity(MainActivity::class.java)
    }

    @Test
    fun testThatHomeFragmentIsDisplayedFirst() {
        assertThat(activity.currentFragment, instanceOf(HomeFragment::class.java))
    }

    @Test
    fun testThatClickingHelpButtonInNavbarOpensHelpFragment() {
        assertNull(activity.findViewById(R.id.label_help))
        //Invoke the onItemClickListener for the help button
        val drawerItem = activity.drawer?.getDrawerItem(activity.HELP_DRAWER_ITEM_IDENTIFIER) as AbstractDrawerItem?
        drawerItem?.onDrawerItemClickListener?.onItemClick(null, 0, null)
        assertThat(activity.currentFragment, instanceOf(HelpFragment::class.java))
        assertNotNull(activity.findViewById(R.id.label_help)) {
            assertTrue(it.isShown, "Help text is not being displayed")
        }
    }
}
