package com.ismail_s.jtime.android.activity

import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import com.ismail_s.jtime.android.R
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem

class MainActivity : Activity() {
    var drawer: Drawer? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        drawer = DrawerBuilder()
                .withActivity(this)
                .addDrawerItems(PrimaryDrawerItem()
                        .withName("All Masjids")
                        .withOnDrawerItemClickListener { view, position, drawerItem ->
                            switchToAllMasjidsFragment()
                            drawer?.closeDrawer()
                            true
                        })
                .build()

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }
            switchToAllMasjidsFragment()
        }
    }

    fun switchToMasjidsFragment(masjidId: Int, masjidName: String) {
        val fragment = MasjidsFragment.newInstance(masjidId, masjidName)
        switchToFragment(fragment)
    }

    fun switchToAllMasjidsFragment() = switchToFragment(AllMasjidsFragment())

    fun switchToFragment(fragment: Fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment).commit()
    }
}
