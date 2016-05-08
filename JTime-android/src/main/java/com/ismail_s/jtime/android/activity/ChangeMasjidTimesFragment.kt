package com.ismail_s.jtime.android.activity

import android.os.Bundle
import android.content.Context
import android.view.inputmethod.InputMethodManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import com.ismail_s.jtime.android.R
import com.ismail_s.jtime.android.CalendarFormatter.formatCalendarAsTime
import com.ismail_s.jtime.android.CalendarFormatter.formatCalendarAsDate
import com.ismail_s.jtime.android.RestClient
import com.ismail_s.jtime.android.MasjidPojo
import com.ismail_s.jtime.android.SalaahType
import java.util.*

class ChangeMasjidTimesFragment : BaseFragment(), View.OnClickListener {

    data class Time(val hour: Int, val minute: Int)

    private val timeRegex = Regex("""(\d\d)[:\- ](\d\d)""")
    private var masjidId: Int = -1
    lateinit private var masjidName: String
    lateinit private var date: GregorianCalendar
    private var currentMasjidPojo: MasjidPojo? = null
    private var currentSalaahType: SalaahType = SalaahType.FAJR
    lateinit private var masjidTimeTextbox: EditText
    lateinit private var dateLabel: TextView
    lateinit private var salaahTypeLabel: TextView
    lateinit private var buttons: List<Button>

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_change_masjid_times, container, false)
        masjidTimeTextbox = rootView.findViewById(R.id.masjid_time_textbox) as EditText
        dateLabel = rootView.findViewById(R.id.label_date) as TextView
        salaahTypeLabel = rootView.findViewById(R.id.label_salaah_type) as TextView
        val buttonIds = listOf(R.id.undo_button, R.id.up_button, R.id.down_button, R.id.left_button, R.id.right_button, R.id.copy_up_button, R.id.copy_down_button)
        buttons = buttonIds.map {rootView.findViewById(it) as Button}
        masjidId = arguments.getInt(Constants.MASJID_ID)
        masjidName = arguments.getString(MASJID_NAME)
        date = arguments.getSerializable(ARG_DATE) as GregorianCalendar
        setButtonOnClickListeners(rootView)
        //Get times for date
        val cb = object : RestClient.MasjidTimesCallback {
            override fun onSuccess(times: MasjidPojo) {
                currentMasjidPojo = times
                currentSalaahType = SalaahType.FAJR
                setLabels(date, currentSalaahType)
                if (times.fajrTime != null) {
                    val fTime = formatCalendarAsTime(times.fajrTime as GregorianCalendar)
                    masjidTimeTextbox.setText(fTime)
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
        showKeyboard()
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
        val helpButton = rootView.findViewById(R.id.help_button) as Button
        helpButton.setOnClickListener {
            hideKeyboard()
            (activity as MainActivity).switchToHelpFragment()
        }
    }

    override fun onDrawerOpened(drawerView: View) = hideKeyboard()

    override fun onDrawerClosed(drawerView: View) = showKeyboard()

    private fun showKeyboard() {
        if (masjidTimeTextbox.requestFocus()) {
            val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
    }

    private fun hideKeyboard() {
        val imm = activity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(masjidTimeTextbox.windowToken, 0)
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
                setTextboxTime(time)
            }
            R.id.up_button -> saveTimeAndSwitchToAnotherDay(dayOffset = -1)
            R.id.down_button -> saveTimeAndSwitchToAnotherDay(dayOffset = 1)
            R.id.left_button -> saveTimeAndSwitchToPrevSalaah()
            R.id.right_button -> saveTimeAndSwitchToNextSalaah()
            R.id.copy_up_button -> saveTimeAndSwitchToAnotherDayAndCopyTime(-1)
            R.id.copy_down_button -> saveTimeAndSwitchToAnotherDayAndCopyTime(1)
        }
    }

    private fun saveTimeAndSwitchToAnotherDay(dayOffset: Int) {
        saveTimeAndSwitchToAnotherDayAndThen(dayOffset) { previousDisplayedTime ->
                val times = currentMasjidPojo as MasjidPojo
                var timeToDisplay: GregorianCalendar? = null
                when (currentSalaahType) {
                    SalaahType.FAJR -> {timeToDisplay = times.fajrTime}
                    SalaahType.ZOHAR -> {timeToDisplay = times.zoharTime}
                    SalaahType.ASR -> {timeToDisplay = times.asrTime}
                    SalaahType.MAGRIB -> {timeToDisplay = times.magribTime}
                    SalaahType.ESHA -> {timeToDisplay = times.eshaTime}
                }
                setTextboxTime(timeToDisplay)
        }
    }

    private fun saveTimeAndSwitchToAnotherDayAndCopyTime(dayOffset: Int) {
        saveTimeAndSwitchToAnotherDayAndThen(dayOffset) { previousDisplayedTime ->
                setTextboxTime(previousDisplayedTime)
        }
    }

    /**
     * Validate time in the textbox, then change the time on the server. Also,
     * switch to a new day as per the dayOffset given. Then, run the "then"
     * parameter function which should sort out what the textbox now displays.
     */
    private fun saveTimeAndSwitchToAnotherDayAndThen(dayOffset: Int, then: (previousDisplayedTime: GregorianCalendar?) -> Unit) {
        saveTimeAndThen { newDate ->
            //Switch to next day
            val nextDate = date.clone() as GregorianCalendar
            nextDate.add(Calendar.DAY_OF_MONTH, dayOffset)
            val cb2 = object : RestClient.MasjidTimesCallback {
                override fun onSuccess(times: MasjidPojo) {
                    currentMasjidPojo = times
                    date = nextDate
                    //enable all buttons
                    buttons.map { it.isEnabled = true }
                    then(newDate)
                }

                override fun onError(t: Throwable) {
                    val s = "Failed to get times: " + t.message
                    showShortToast(s)
                    //TODO-should this (next) line be here
                    (activity as MainActivity).switchToMasjidsFragment(masjidId, masjidName)
                }
            }
            //disable all buttons here whilst we get the masjid times for the
            // new day
            buttons.map { it.isEnabled = false }
            RestClient(activity).getMasjidTimes(masjidId, cb2, nextDate)
            setLabels(nextDate, currentSalaahType)
        }
    }

    private fun saveTimeAndSwitchToNextSalaah() {
        saveTimeAndThen { newDate ->
            //Switch to next salaah
            var newTime: GregorianCalendar? = null
            when (currentSalaahType) {
                SalaahType.FAJR -> {
                    currentSalaahType = SalaahType.ZOHAR
                    newTime = currentMasjidPojo?.zoharTime
                }
                SalaahType.ZOHAR -> {
                    currentSalaahType = SalaahType.ASR
                    newTime = currentMasjidPojo?.asrTime
                }
                SalaahType.ASR, SalaahType.MAGRIB -> {
                    currentSalaahType = SalaahType.ESHA
                    newTime = currentMasjidPojo?.eshaTime
                }
                SalaahType.ESHA -> {
                    currentSalaahType = SalaahType.FAJR
                    newTime = currentMasjidPojo?.fajrTime
                }
            }
            setTextboxTime(newTime)
            setLabels(date, currentSalaahType)
        }
    }

    private fun saveTimeAndSwitchToPrevSalaah() {
        saveTimeAndThen { newDate ->
            //Switch to prev. salaah
            var newTime: GregorianCalendar? = null
            when (currentSalaahType) {
                SalaahType.FAJR -> {
                    currentSalaahType = SalaahType.ESHA
                    newTime = currentMasjidPojo?.eshaTime
                }
                SalaahType.ZOHAR -> {
                    currentSalaahType = SalaahType.FAJR
                    newTime = currentMasjidPojo?.fajrTime
                }
                SalaahType.ASR -> {
                    currentSalaahType = SalaahType.ZOHAR
                    newTime = currentMasjidPojo?.zoharTime
                }
                SalaahType.MAGRIB, SalaahType.ESHA -> {
                    currentSalaahType = SalaahType.ASR
                    newTime = currentMasjidPojo?.asrTime
                }
            }
            setTextboxTime(newTime)
            setLabels(date, currentSalaahType)
        }
    }

    private fun saveTimeAndThen(then: (newDate: GregorianCalendar?) -> Unit) {
        if (masjidTimeTextbox.text.length == 0) {
            //If the textbox is empty, don't save
            then(null)
            return
        }
        //If invalid time, return straightaway
        val time = getTextboxTimeIfValid()
        if (time == null) {
            val msg = "Time isn't valid. Change it so it is valid, " +
                    "or click on the undo button"
            showShortToast(msg)
            return
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
                val s = "Failed to update db with new time: $t"
                showShortToast(s)
            }
        }
        RestClient(activity).createOrUpdateMasjidTime(masjidId, currentSalaahType, newDate, cb1)
        when (currentSalaahType) {
            SalaahType.FAJR -> {
                currentMasjidPojo?.fajrTime = newDate
            }
            SalaahType.ZOHAR -> {
                currentMasjidPojo?.zoharTime = newDate
            }
            SalaahType.ASR -> {
                currentMasjidPojo?.asrTime = newDate
            }
            SalaahType.MAGRIB -> {
                currentMasjidPojo?.magribTime = newDate
            }
            SalaahType.ESHA -> {
                currentMasjidPojo?.eshaTime = newDate
            }
        }
        then(newDate)
    }

    /**
     * Get the text that is in the textbox, and see if it is a valid time.
     * If it is, then it is parsed and returned. Else, null is returned.
     */
    private fun getTextboxTimeIfValid(): Time? {
        val timeString = masjidTimeTextbox.text
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

    fun setTextboxTime(newTime: GregorianCalendar?) {
        if (newTime == null) {
            masjidTimeTextbox.setText("")
        } else {
            val formattedTime = formatCalendarAsTime(newTime)
            masjidTimeTextbox.setText(formattedTime)
        }
    }

    private fun setLabels(date: GregorianCalendar, salaahType: SalaahType) {
        dateLabel.text = formatCalendarAsDate(date)
        var salaahText = ""
        when (salaahType) {
            SalaahType.FAJR -> {
                salaahText = "Fajr"
            }
            SalaahType.ZOHAR -> {
                salaahText = "Zohar"
            }
            SalaahType.ASR -> {
                salaahText = "Asr"
            }
            SalaahType.MAGRIB -> {
                salaahText = "Magrib"
            }
            SalaahType.ESHA -> {
                salaahText = "Esha"
            }
        }
        salaahTypeLabel.text = salaahText
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
