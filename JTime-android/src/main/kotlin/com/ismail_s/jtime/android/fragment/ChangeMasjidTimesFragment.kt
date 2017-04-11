package com.ismail_s.jtime.android.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.ismail_s.jtime.android.*
import com.ismail_s.jtime.android.CalendarFormatter.formatCalendarAsDate
import com.ismail_s.jtime.android.CalendarFormatter.formatCalendarAsTime
import com.ismail_s.jtime.android.pojo.MasjidPojo
import com.ismail_s.jtime.android.pojo.SalaahType
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.jetbrains.anko.find
import org.jetbrains.anko.inputMethodManager
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.longToast
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.support.v4.withArguments
import java.util.*

/**
 * Fragment that allows the user to add/edit the salaah times for a particular masjid. The user
 * must be logged in to use this fragment. This fragment is reached from [MasjidFragment].
 */
class ChangeMasjidTimesFragment : BaseFragment(), View.OnClickListener {

    data class Time(val hour: Int, val minute: Int)

    private val timeRegex = Regex("""(\d?\d)[:\- ](\d\d)""")
    private var masjidId: Int = -1
    lateinit private var masjidName: String
    lateinit private var date: GregorianCalendar
    private var currentMasjidPojo: MasjidPojo? = null
    private var currentSalaahType: SalaahType = SalaahType.FAJR
    lateinit private var masjidTimeTextbox: EditText
    lateinit private var dateLabel: TextView
    lateinit private var salaahTypeLabel: TextView
    lateinit private var buttons: List<Button>
    private val DATE = "date"
    private val SALAAH_TYPE = "salaah_type"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        masjidId = arguments.getInt(Constants.MASJID_ID)
        masjidName = arguments.getString(MASJID_NAME)
        if (savedInstanceState != null) {
            currentSalaahType = savedInstanceState.getSerializable(SALAAH_TYPE) as SalaahType
            date = savedInstanceState.getSerializable(DATE) as GregorianCalendar
        } else {
            date = arguments.getSerializable(ARG_DATE) as GregorianCalendar
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putSerializable(DATE, date)
        savedInstanceState.putSerializable(SALAAH_TYPE, currentSalaahType)
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_change_masjid_times, container, false)
        masjidTimeTextbox = rootView.find<EditText>(R.id.masjid_time_textbox)
        dateLabel = rootView.find<TextView>(R.id.label_date)
        salaahTypeLabel = rootView.find<TextView>(R.id.label_salaah_type)
        val buttonIds = listOf(R.id.undo_button, R.id.up_button, R.id.down_button, R.id.left_button, R.id.right_button, R.id.copy_up_button, R.id.copy_down_button)
        buttons = buttonIds.map {rootView.findViewById(it) as Button }
        setButtonOnClickListeners(rootView, buttons)
        //Get times for date
        RestClient(act).getMasjidTimes(masjidId, date) successUi {
            currentMasjidPojo = it
            setLabels(date, currentSalaahType)
            handleUndoButtonClick()

        } failUi {
            longToast(getString(R.string.get_masjid_times_failure_toast, it.message))
            // As we can't get the times (so can't edit them), we switch
            // back to viewing the times
            (act as? MainActivity)?.switchToMasjidsFragment(masjidId, masjidName)
        }
        showKeyboard()
        return rootView
    }

    private fun setButtonOnClickListeners(rootView: View, buttons: List<Button>) {
        buttons.forEach {it.setOnClickListener(this)}
        val helpButton = rootView.find<Button>(R.id.help_button)
        helpButton.setOnClickListener {
            hideKeyboard()
            (act as? MainActivity)?.switchToHelpFragment()
        }
    }

    override fun onDrawerOpened(drawerView: View) = hideKeyboard()

    override fun onDrawerClosed(drawerView: View) = showKeyboard()

    private fun showKeyboard() {
        if (masjidTimeTextbox.requestFocus()) {
            val imm = act.inputMethodManager
            imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY)
        }
    }

    private fun hideKeyboard() {
        act.inputMethodManager.hideSoftInputFromWindow(masjidTimeTextbox.windowToken, 0)
    }

    override fun onClick(view: View) {
        when (view.id) {
            R.id.undo_button -> handleUndoButtonClick()
            R.id.up_button -> saveTimeAndSwitchToAnotherDay(dayOffset = -1)
            R.id.down_button -> saveTimeAndSwitchToAnotherDay(dayOffset = 1)
            R.id.left_button -> saveTimeAndSwitchToPrevSalaah()
            R.id.right_button -> saveTimeAndSwitchToNextSalaah()
            R.id.copy_up_button -> saveTimeAndSwitchToAnotherDayAndCopyTime(-1)
            R.id.copy_down_button -> saveTimeAndSwitchToAnotherDayAndCopyTime(1)
        }
    }

    fun handleUndoButtonClick() {
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

    private fun saveTimeAndSwitchToAnotherDay(dayOffset: Int) {
        saveTimeAndSwitchToAnotherDayAndThen(dayOffset) { _ ->
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
            //disable all buttons here whilst we get the masjid times for the
            // new day
            buttons.map { it.isEnabled = false }
            RestClient(act).getMasjidTimes(masjidId, nextDate) successUi {
                currentMasjidPojo = it
                date = nextDate
                //enable all buttons
                buttons.map { it.isEnabled = true }
                then(newDate)
            } failUi {
                val s = getString(R.string.get_masjid_times_failure_toast, it.message)
                toast(s)
                //TODO-should this (next) line be here
                (act as? MainActivity)?.switchToMasjidsFragment(masjidId, masjidName)
            }
            setLabels(nextDate, currentSalaahType)
        }
    }

    private fun saveTimeAndSwitchToNextSalaah() {
        saveTimeAndThen { _ ->
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
        saveTimeAndThen { _ ->
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
        if (masjidTimeTextbox.text.isEmpty()) {
            //If the textbox is empty, don't save
            then(null)
            return
        }
        //If invalid time, return straightaway
        val time = getTextboxTimeIfValid()
        if (time == null) {
            toast(getString(R.string.invalid_salaah_time_toast))
            return
        }
        //Get the salaah time that is currently saved on the server, so we can see if the time we
        // are about to save is different. If it is different, then we will save the new time to
        //the server.
        var currentSavedTimeOrNull: GregorianCalendar? = null
        when (currentSalaahType) {
            SalaahType.FAJR -> {currentSavedTimeOrNull = currentMasjidPojo?.fajrTime}
            SalaahType.ZOHAR -> {currentSavedTimeOrNull = currentMasjidPojo?.zoharTime}
            SalaahType.ASR -> {currentSavedTimeOrNull = currentMasjidPojo?.asrTime}
            SalaahType.MAGRIB -> {currentSavedTimeOrNull = currentMasjidPojo?.magribTime}
            SalaahType.ESHA -> {currentSavedTimeOrNull = currentMasjidPojo?.eshaTime}
        }
        val currentSavedTime: String
        if (currentSavedTimeOrNull == null) {
            currentSavedTime = ""
        } else {
            currentSavedTime = formatCalendarAsTime(currentSavedTimeOrNull)
        }
        //Save time
        val newDate = date.clone() as GregorianCalendar
        newDate.set(Calendar.HOUR_OF_DAY, time.hour)
        newDate.set(Calendar.MINUTE, time.minute)
        if (formatCalendarAsTime(newDate) != currentSavedTime) {
            RestClient(act).createOrUpdateMasjidTime(masjidId, currentSalaahType, newDate) successUi {
                toast("DB updated with ${time.hour}:${time.minute}")
            } failUi {
                toast(getString(R.string.salaah_time_update_failure_toast, it.message))
            }
        }
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
        if (timeString.length !in 4..5) {
            return null
        }
        val match = timeRegex.matchEntire(timeString)
        if (match == null) {
            return null
        } else {
            val (h, m) = match.destructured
            val hour = h.toInt()
            val minute = m.toInt()
            if (hour !in 0..23 || minute !in 0..59) {
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
        var salaahText = 0
        when (salaahType) {
            SalaahType.FAJR -> {
                salaahText = R.string.fajr
            }
            SalaahType.ZOHAR -> {
                salaahText = R.string.zohar
            }
            SalaahType.ASR -> {
                salaahText = R.string.asr
            }
            SalaahType.MAGRIB -> {
                salaahText = R.string.magrib
            }
            SalaahType.ESHA -> {
                salaahText = R.string.esha
            }
        }
        salaahTypeLabel.text = getString(salaahText)
    }

    companion object {
        private val ARG_DATE = "date"
        private val MASJID_NAME = "masjid_name"

        fun newInstance(masjidId: Int, masjidName: String, date: GregorianCalendar): ChangeMasjidTimesFragment =
                ChangeMasjidTimesFragment().withArguments(
                        ARG_DATE to date, Constants.MASJID_ID to masjidId,
                        MASJID_NAME to masjidName)
    }
}
