package com.ismail_s.jtime.android.activity

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import com.ismail_s.jtime.android.R
import com.ismail_s.jtime.android.CalendarFormatter.formatCalendarAsTime
import com.ismail_s.jtime.android.RestClient
import com.ismail_s.jtime.android.MasjidPojo
import com.ismail_s.jtime.android.SalaahType
import java.util.*

class ChangeMasjidTimesFragment : Fragment(), View.OnClickListener {

    data class Time(val hour: Int, val minute: Int)

    private val timeRegex = Regex("""(?<hour>\d\d)[:\- ](?<minute>\d\d)""")
    private var masjidId: Int = -1
    lateinit private var masjidName: String
    lateinit private var date: GregorianCalendar
    private var currentMasjidPojo: MasjidPojo? = null
    private var currentSalaahType: SalaahType = SalaahType.FAJR
    lateinit private var masjidTimeTextbox: EditText

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_change_masjid_times, container, false)
        masjidTimeTextbox = rootView.findViewById(R.id.masjid_time_textbox) as EditText
        masjidId = arguments.getInt(Constants.MASJID_ID)
        masjidName = arguments.getString(MASJID_NAME)
        date = arguments.getSerializable(ARG_DATE) as GregorianCalendar
        setButtonOnClickListeners(rootView)
        //Get times for date
        val cb = object : RestClient.MasjidTimesCallback {
            override fun onSuccess(times: MasjidPojo) {
                currentMasjidPojo = times
                if (times.fajrTime != null) {
                    val fTime = formatCalendarAsTime(times.fajrTime as GregorianCalendar)
                    masjidTimeTextbox.setText(fTime)
                    currentSalaahType = SalaahType.FAJR
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

    private fun setButtonOnClickListeners(rootView: View) {
        val buttonIds = listOf(R.id.undo_button, R.id.up_button,
                                R.id.down_button, R.id.left_button,
                                R.id.right_button, R.id.copy_up_button,
                                R.id.copy_down_button)
        for (buttonId in buttonIds) {
            val b = rootView.findViewById(buttonId) as Button
            b.setOnClickListener(this)
        }
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.undo_button -> {
                //Change the time to what it was originally
                var time: GregorianCalendar? = null
                when (currentSalaahType) {
                    SalaahType.FAJR -> {time = currentMasjidPojo?.fajrTime}
                    SalaahType.ZOHAR -> {time = currentMasjidPojo?.zoharTime}
                    SalaahType.ASR -> {time = currentMasjidPojo?.asrTime}
                    SalaahType.MAGRIB -> {time = currentMasjidPojo?.magribTime}
                    SalaahType.ESHA -> {time = currentMasjidPojo?.eshaTime}
                }
                if (time == null) {
                    masjidTimeTextbox.setText("")
                } else {
                    val t = formatCalendarAsTime(time)
                    masjidTimeTextbox.setText(t)
                }
            }
            R.id.up_button -> changeTimeAndSwitchToAnotherDay(dayOffset = -1)
            R.id.down_button -> changeTimeAndSwitchToAnotherDay(dayOffset = 1)
            R.id.left_button -> {
                //Save time, switch to prev. salaah
                //If invalid time, return straightaway
            }
            R.id.right_button -> {
                //Save time, switch to next salaah
                //If invalid time, return straightaway
            }
            R.id.copy_up_button -> changeTimeAndSwitchToAnotherDayAndCopyTime(-1)
            R.id.copy_down_button -> changeTimeAndSwitchToAnotherDayAndCopyTime(1)
        }
    }

    private fun changeTimeAndSwitchToAnotherDay(dayOffset: Int) {
        changeTimeAndSwitchToAnotherDayAndThen(dayOffset) {
            previousDisplayedTime -> {
                val times = currentMasjidPojo as MasjidPojo
                var timeToDisplay: GregorianCalendar? = null
                when (currentSalaahType) {
                    SalaahType.FAJR -> {timeToDisplay = times.fajrTime}
                    SalaahType.ZOHAR -> {timeToDisplay = times.zoharTime}
                    SalaahType.ASR -> {timeToDisplay = times.asrTime}
                    SalaahType.MAGRIB -> {timeToDisplay = times.magribTime}
                    SalaahType.ESHA -> {timeToDisplay = times.eshaTime}
                }
                if (timeToDisplay != null) {
                    val formattedTime = formatCalendarAsTime(timeToDisplay)
                    masjidTimeTextbox.setText(formattedTime)
                }
            }
        }
    }

    private fun changeTimeAndSwitchToAnotherDayAndCopyTime(dayOffset: Int) {
        changeTimeAndSwitchToAnotherDayAndThen(dayOffset) {
            previousDisplayedTime -> {
                val formattedTime = formatCalendarAsTime(previousDisplayedTime)
                masjidTimeTextbox.setText(formattedTime)
            }
        }
    }

    /**
     * Validate time in the textbox, then change the time on the server. Also,
     * switch to a new day as per the dayOffset given. Then, run the "then"
     * parameter function which should sort out what the textbox now displays.
     */
    private fun changeTimeAndSwitchToAnotherDayAndThen(dayOffset: Int, then: (previousDisplayedTime: GregorianCalendar) -> Unit) {
        //If invalid time, return straightaway
        val time = getTextboxTimeIfValid()
        if (time == null) {
            val msg = "Time isn't valid. Change it so it is valid, " +
                    "or click on the undo button"
            showShortToast(msg)
            return;
        }
        //Save time
        val newDate = date.clone() as GregorianCalendar
        newDate.set(Calendar.HOUR_OF_DAY, time.hour)
        newDate.set(Calendar.MINUTE, time.minute)
        val cb1 = object : RestClient.CreateOrUpdateMasjidTimeCallback {
            override fun onSuccess() {
                showShortToast("DB updated with ${time.hour}:${time.minute}")
            }
            override fun onError(t: Throwable) {
                val s = "Failed to update db with new time: " + t.message
                showShortToast(s)
            }
        }
        RestClient(activity).createOrUpdateMasjidTime(masjidId, currentSalaahType, newDate, cb1)
        //Switch to next day
        val nextDate = date.clone() as GregorianCalendar
        nextDate.add(Calendar.DAY_OF_MONTH, dayOffset)
        val cb2 = object : RestClient.MasjidTimesCallback {
            override fun onSuccess(times: MasjidPojo) {
                currentMasjidPojo = times
                date = nextDate
                then(newDate)
            }

            override fun onError(t: Throwable) {
                val s = "Failed to get times: " + t.message
                showShortToast(s)
                //TODO-should this (next) line be here
                (activity as MainActivity).switchToMasjidsFragment(masjidId, masjidName)
            }
        }
        RestClient(activity).getMasjidTimes(masjidId, cb2, nextDate)
    }

    /**
     * Get the text that is in the textbox, and see if it is a valid time.
     * If it is, then it is parsed and returned. Else, null is returned.
     */
    private fun getTextboxTimeIfValid(): Time? {
        val timeString = masjidTimeTextbox.getText()
        if (timeString.length != 5) {
            return null
        }
        val match = timeRegex.matchEntire(timeString)
        if (match == null) {
            return null
        } else {
            val (h, m) = match.destructured
            val hour = h.toInt()
            val minute = m.toInt()
            if (!(0 <= hour && hour <= 23 && 0 <= minute && minute <= 59)) {
                return null
            } else {
                return Time(hour, minute)
            }
        }
    }

    private fun showShortToast(s: String) {
        Toast.makeText(activity.applicationContext, s, Toast.LENGTH_SHORT).show()
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
