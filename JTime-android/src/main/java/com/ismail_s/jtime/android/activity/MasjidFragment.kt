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
        val masjidId = activity.intent.getIntExtra(Constants.MASJID_ID, 1)
        RestClient(activity.applicationContext).getMasjidTimes(masjidId, cb, date)
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
