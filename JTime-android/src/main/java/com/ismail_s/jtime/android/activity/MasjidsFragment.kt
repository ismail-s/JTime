package com.ismail_s.jtime.android.activity


import android.support.v4.app.Fragment
import android.support.v4.app.FragmentManager
import android.os.Bundle
import android.support.v4.app.FragmentStatePagerAdapter
import android.support.v4.view.ViewPager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView

import com.ismail_s.jtime.android.R
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.withArguments
import java.util.*


class MasjidsFragment : BaseFragment() {
    /**
     * This should be a big number, so that it is as if there are an infinite number of pages.
     */
    private val NUM_OF_PAGES = 1000
    lateinit private var masjidName: String
    private var activeChildFragments: MutableMap<Int, BaseFragment> = mutableMapOf()

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        // Inflate the layout for this fragment
        val view = inflater!!.inflate(R.layout.fragment_masjids, container, false)
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.
        val mSectionsPagerAdapter = SectionsPagerAdapter(childFragmentManager)

        // Set up the ViewPager with the sections adapter.
        val mViewPager = view.find<ViewPager>(R.id.container)
        mViewPager.adapter = mSectionsPagerAdapter
        mViewPager.currentItem = NUM_OF_PAGES / 2
        masjidName = arguments.getString(Constants.MASJID_NAME)
        val masjidNameView = view.find<TextView>(R.id.masjid_name)
        masjidNameView.text = masjidName
        return view
    }

    override fun onLogin() {
        activeChildFragments.forEach {it.value.onLogin()}
    }

    override fun onLogout() {
        activeChildFragments.forEach {it.value.onLogout()}
    }


    companion object {
        fun newInstance(masjidId: Int, masjidName: String): MasjidsFragment =
                MasjidsFragment().withArguments(
                        Constants.MASJID_NAME to masjidName, Constants.MASJID_ID to masjidId)
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
            val masjidId = arguments.getInt(Constants.MASJID_ID)
            return MasjidFragment.newInstance(masjidId, masjidName, date)
        }

        override fun instantiateItem(container: ViewGroup, position: Int): Any {
            val fragment = super.instantiateItem(container, position) as BaseFragment
            activeChildFragments.put(position, fragment)
            return fragment
        }

        override fun destroyItem(container: ViewGroup, position: Int, obj: Any) {
            super.destroyItem(container, position, obj)
            activeChildFragments.remove(position)
        }

        override fun getCount(): Int = NUM_OF_PAGES
    }
}
