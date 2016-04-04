package com.ismail_s.jtime.android.activity


import android.app.Fragment
import android.app.FragmentManager
import android.os.Bundle
import android.support.v13.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.ismail_s.jtime.android.R
import java.util.*


class MasjidsFragment : Fragment() {
    /**
     * This should be a big number, so that it is as if there are an infinite number of pages.
     */
    private val NUM_OF_PAGES = 1000

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_masjids, container, false)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        val mSectionsPagerAdapter = SectionsPagerAdapter(childFragmentManager)

        // Set up the ViewPager with the sections adapter.
        val mViewPager = view.findViewById(R.id.container) as ViewPager
        mViewPager.adapter = mSectionsPagerAdapter
        mViewPager.currentItem = NUM_OF_PAGES / 2
        val masjidName = arguments.getString(Constants.MASJID_NAME)
        val masjidNameView = view.findViewById(R.id.masjid_name) as TextView
        masjidNameView.text = masjidName
        return view
    }

    companion object {
        fun newInstance(masjidId: Int, masjidName: String): MasjidsFragment {
            val instance = MasjidsFragment()
            val bundle = Bundle()
            bundle.putString(Constants.MASJID_NAME, masjidName)
            bundle.putInt(Constants.MASJID_ID, masjidId)
            instance.arguments = bundle
            return instance
        }
    }

    /**
     * A [FragmentStatePagerAdapter] that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    inner class SectionsPagerAdapter(fm: FragmentManager) : FragmentStatePagerAdapter(fm) {

        override fun getItem(position: Int): android.app.Fragment {
            // getItem is called to instantiate the fragment for the given page.
            // Return a MasjidFragment (defined as a static inner class below).
            val date = GregorianCalendar()
            date.add(GregorianCalendar.DAY_OF_YEAR, position - NUM_OF_PAGES / 2)
            val masjidId = arguments.getInt(Constants.MASJID_ID)
            return MasjidFragment.newInstance(masjidId, date)
        }

        override fun getCount(): Int {
            return NUM_OF_PAGES
        }

        override fun getPageTitle(position: Int): CharSequence? {
            when (position) {
                //TODO-sort this out
                0 -> return "SECTION 1"
                1 -> return "SECTION 2"
                2 -> return "SECTION 3"
            }
            return null
        }
    }
}
