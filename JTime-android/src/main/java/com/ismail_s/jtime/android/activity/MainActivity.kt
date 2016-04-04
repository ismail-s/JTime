package com.ismail_s.jtime.android.activity

import android.app.Activity
import android.app.Fragment
import android.os.Bundle
import com.ismail_s.jtime.android.R

class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {

            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
            }

            // Create a new Fragment to be placed in the activity layout
            val firstFragment = AllMasjidsFragment();

            // In case this activity was started with special instructions from an
            // Intent, pass the Intent's extras to the fragment as arguments
            firstFragment.arguments = intent.extras;

            // Add the fragment to the 'fragment_container' FrameLayout
            switchToFragment(firstFragment)
        }
    }

    fun switchToMasjidsFragment(masjidId: Int, masjidName: String) {
        val fragment = MasjidsFragment.newInstance(masjidId, masjidName)
        switchToFragment(fragment)
    }

    fun switchToFragment(fragment: Fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment).commit()
    }
}
