package com.ismail_s.jtime.android.activity

import android.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ismail_s.jtime.android.R
import com.ismail_s.jtime.android.RestClient
import java.text.SimpleDateFormat
import java.util.*

/**
 * A placeholder fragment containing a simple view.
 */
class MasjidFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_masjid, container, false)
        val textView = rootView.findViewById(R.id.section_label) as TextView
        val date = arguments.getSerializable(ARG_DATE) as GregorianCalendar
        textView.text = getString(R.string.section_format, formatCalendarAsDate(date))
        val masjidName = activity.intent.getStringExtra(Constants.MASJID_NAME)
        val masjidTimes = RestClient().getMasjidTimes(masjidName)
        val dateTextViews = arrayOfNulls<TextView>(5)
        Log.d("", "am here")
        dateTextViews[0] = rootView.findViewById(R.id.fajr_date) as TextView
        dateTextViews[1] = rootView.findViewById(R.id.zohar_date) as TextView
        dateTextViews[2] = rootView.findViewById(R.id.asr_date) as TextView
        dateTextViews[3] = rootView.findViewById(R.id.magrib_date) as TextView
        dateTextViews[4] = rootView.findViewById(R.id.esha_date) as TextView
        //Toast.makeText()
        val times = masjidTimes.times
        for ((view, time) in dateTextViews.zip(times)) {
            val res = formatCalendarAsTime(time)
            Log.d("", "am in loop with" + res + "and" + view)
            view?.text = res
        }
        return rootView
    }

    private fun formatCalendarAsTime(calendar: GregorianCalendar): String {
        return formatCalendar(calendar, "HH:mm")
    }

    private fun formatCalendarAsDate(calendar: GregorianCalendar): String {
        return formatCalendar(calendar, "yyyy MMM dd")
    }

    private fun formatCalendar(calendar: GregorianCalendar, formatString: String): String {
        val formatter = SimpleDateFormat(formatString, Locale.getDefault())
        return formatter.format(calendar.time)
    }

    companion object {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private val ARG_DATE = "date"

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        fun newInstance(date: GregorianCalendar): MasjidFragment {
            val fragment = MasjidFragment()
            val args = Bundle()
            args.putSerializable(ARG_DATE, date)
            fragment.arguments = args
            return fragment
        }
    }

}
