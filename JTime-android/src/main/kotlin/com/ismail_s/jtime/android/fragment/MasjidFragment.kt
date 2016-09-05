package com.ismail_s.jtime.android.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import com.ismail_s.jtime.android.*
import com.ismail_s.jtime.android.CalendarFormatter.formatCalendarAsDate
import com.ismail_s.jtime.android.CalendarFormatter.formatCalendarAsTime
import com.ismail_s.jtime.android.pojo.MasjidPojo
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.longToast
import org.jetbrains.anko.support.v4.withArguments
import java.util.*

/**
 * A placeholder fragment containing a simple view.
 */
class MasjidFragment : BaseFragment() {
    lateinit private var editButton: Button

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_masjid, container, false)
        val textView = rootView.find<TextView>(R.id.section_label)
        val date = arguments.getSerializable(ARG_DATE) as GregorianCalendar
        textView.text = getString(R.string.section_format, formatCalendarAsDate(date))
        // TODO-instead of 1, what should the default value be here?
        val masjidId = arguments.getInt(Constants.MASJID_ID)
        editButton = rootView.find<Button>(R.id.edit_button)
        editButton.setOnClickListener {view ->
            val masjidName = arguments.getString(Constants.MASJID_NAME)
            if (activity != null)
                mainAct.switchToChangeMasjidTimesFragment(masjidId, masjidName, date)
        }
        if (SharedPreferencesWrapper(act).persistedLoginExists()) {
            onLogin()
        } else {
            onLogout()
        }
        RestClient(act).getMasjidTimes(masjidId, date) successUi {
            if (it.fajrTime != null) {
                val fTime = formatCalendarAsTime(it.fajrTime as GregorianCalendar)
                rootView.find<TextView>(R.id.fajr_date).text = fTime
            }
            if (it.zoharTime != null) {
                val zTime = formatCalendarAsTime(it.zoharTime as GregorianCalendar)
                rootView.find<TextView>(R.id.zohar_date).text = zTime
            }
            if (it.asrTime != null) {
                val aTime = formatCalendarAsTime(it.asrTime as GregorianCalendar)
                rootView.find<TextView>(R.id.asr_date).text = aTime
            }
            if (it.magribTime != null) {
                val mTime = formatCalendarAsTime(it.magribTime as GregorianCalendar)
                rootView.find<TextView>(R.id.magrib_date).text = mTime
            }
            if (it.eshaTime != null) {
                val eTime = formatCalendarAsTime(it.eshaTime as GregorianCalendar)
                rootView.find<TextView>(R.id.esha_date).text = eTime
            }
        } failUi {
            longToast(getString(R.string.get_masjid_times_failure_toast, it.message))
        }
        return rootView
    }

    override fun onLogin() {
        editButton.visibility = View.VISIBLE
    }

    override fun onLogout() {
        editButton.visibility = View.INVISIBLE
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
        fun newInstance(masjidId: Int, masjidName: String, date: GregorianCalendar): MasjidFragment =
                MasjidFragment().withArguments(
                        ARG_DATE to date, Constants.MASJID_ID to masjidId,
                        Constants.MASJID_NAME to masjidName)
    }

}
