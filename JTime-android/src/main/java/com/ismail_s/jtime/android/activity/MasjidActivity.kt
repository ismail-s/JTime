package com.ismail_s.jtime.android.activity


import android.app.Activity
import android.app.Fragment
import android.app.FragmentManager
import android.os.Bundle
import android.support.v13.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.Menu
import android.view.MenuItem
import android.widget.TextView
import com.ismail_s.jtime.android.R
import java.util.*

class MasjidActivity : Activity() {
    /**
     * This should be a big number, so that it is as if there are an infinite number of pages.
     */
    private val NUM_OF_PAGES = 1000

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_masjid)
        actionBar?.setDisplayHomeAsUpEnabled(true)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        val mSectionsPagerAdapter = SectionsPagerAdapter(fragmentManager)

        // Set up the ViewPager with the sections adapter.
        val mViewPager = findViewById(R.id.container) as ViewPager
        mViewPager.adapter = mSectionsPagerAdapter
        mViewPager.currentItem = NUM_OF_PAGES / 2
        val masjidName = intent.getStringExtra(Constants.MASJID_NAME)
        val masjidNameView = findViewById(R.id.masjid_name) as TextView
        masjidNameView.text = masjidName


    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_masjid, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        val id = item.itemId

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true
        }

        return super.onOptionsItemSelected(item)
    }

    /**
     * A [FragmentStatePagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a MasjidFragment (defined as a static inner class below).
            val date = GregorianCalendar()
            date.add(GregorianCalendar.DAY_OF_YEAR, position - NUM_OF_PAGES / 2)
            return MasjidFragment.newInstance(date)
        }

        override fun getCount(): Int {
            return NUM_OF_PAGES
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                0 -> return "SECTION 1"
                1 -> return "SECTION 2"
                2 -> return "SECTION 3"
            }
            return null
        }
    }
}
