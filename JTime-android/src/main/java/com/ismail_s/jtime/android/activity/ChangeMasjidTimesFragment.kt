package com.ismail_s.jtime.android.activity

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.ismail_s.jtime.android.R
import com.ismail_s.jtime.android.CalendarFormatter.formatCalendarAsTime
import com.ismail_s.jtime.android.RestClient
import com.ismail_s.jtime.android.MasjidPojo
import java.util.*

class ChangeMasjidTimesFragment : Fragment(), View.OnClickListener {
    lateinit private val masjidId: Int
    lateinit private val masjidName: String
    lateinit private var date: GregorianCalendar
    private var currentMasjidPojo: MasjidPojo? = null
    lateinit private val masjidTimeTextbox: EditText

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_change_masjid_times, container, false)
        masjidTimeTextbox = rootView.findViewById(R.id.masjid_time_textbox) as EditText
        masjidId = arguments.getInt(Constants.MASJID_ID)
        masjidName = arguments.getString(MASJID_NAME)
        date = arguments.getSerializable(ARG_DATE) as GregorianCalendar
        //Get times for date
        val cb = object : RestClient.MasjidTimesCallback {
            override fun onSuccess(times: MasjidPojo) {
                currentMasjidPojo = times
                if (times.fajrTime != null) {
                    val fTime = formatCalendarAsTime(times.fajrTime as GregorianCalendar)
                    masjidTimeTextbox.text = fTime
                }
            }

            override fun onError(t: Throwable) {
                val s = "Failed to get times: " + t.message
                Toast.makeText(activity.applicationContext, s, Toast.LENGTH_LONG).show()
                // As we can't get the times (so can't edit them), we switch
                // back to viewing the times
                (activity as MainActivity).switchToMasjidsFragment(masjidId, masjidName)
            }
        }
        RestClient(activity).getMasjidTimes(masjidId, cb, date)
        return rootView
    }

    override fun onClick(view: View) {
    }

    companion object {
        private val ARG_DATE = "date"
        private val MASJID_NAME = "masjid_name"

        fun newInstance(masjidId: Int, masjidName: String, date: GregorianCalendar): ChangeMasjidTimesFragment {
            val fragment = ChangeMasjidTimesFragment()
            val args = Bundle()
            args.putSerializable(ARG_DATE, date)
            args.putInt(Constants.MASJID_ID, masjidId)
            args.putString(MASJID_NAME, masjidName)
            fragment.arguments = args
            return fragment
        }
    }
}
