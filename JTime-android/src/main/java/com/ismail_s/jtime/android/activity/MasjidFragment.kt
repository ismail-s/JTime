package com.ismail_s.jtime.android.activity

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import com.ismail_s.jtime.android.MasjidPojo
import com.ismail_s.jtime.android.R
import com.ismail_s.jtime.android.RestClient
import com.ismail_s.jtime.android.CalendarFormatter.formatCalendarAsTime
import com.ismail_s.jtime.android.CalendarFormatter.formatCalendarAsDate
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
        val cb = object : RestClient.MasjidTimesCallback {
            override fun onSuccess(times: MasjidPojo) {
                if (times.fajrTime != null) {
                    val fTime = formatCalendarAsTime(times.fajrTime as GregorianCalendar)
                    (rootView.findViewById(R.id.fajr_date) as TextView).text = fTime
                }
                if (times.zoharTime != null) {
                    val zTime = formatCalendarAsTime(times.zoharTime as GregorianCalendar)
                    (rootView.findViewById(R.id.zohar_date) as TextView).text = zTime
                }
                if (times.asrTime != null) {
                    val aTime = formatCalendarAsTime(times.asrTime as GregorianCalendar)
                    (rootView.findViewById(R.id.asr_date) as TextView).text = aTime
                }
                if (times.magribTime != null) {
                    val mTime = formatCalendarAsTime(times.magribTime as GregorianCalendar)
                    (rootView.findViewById(R.id.magrib_date) as TextView).text = mTime
                }
                if (times.eshaTime != null) {
                    val eTime = formatCalendarAsTime(times.eshaTime as GregorianCalendar)
                    (rootView.findViewById(R.id.esha_date) as TextView).text = eTime
                }
            }

            override fun onError(t: Throwable) {
                val s = "Failed to get times: " + t.message
                Toast.makeText(activity.applicationContext, s, Toast.LENGTH_LONG).show()
            }
        }
        // TODO-instead of 1, what should the default value be here?
        val masjidId = arguments.getInt(Constants.MASJID_ID)
        RestClient(activity).getMasjidTimes(masjidId, cb, date)
        return rootView
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
        fun newInstance(masjidId: Int, date: GregorianCalendar): MasjidFragment {
            val fragment = MasjidFragment()
            val args = Bundle()
            args.putSerializable(ARG_DATE, date)
            args.putInt(Constants.MASJID_ID, masjidId)
            fragment.arguments = args
            return fragment
        }
    }

}
