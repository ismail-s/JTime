package com.ismail_s.jtime.android.fragment

import android.app.Activity
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.Menu
import android.view.View
import android.view.ViewGroup
import com.codetroopers.betterpickers.calendardatepicker.CalendarDatePickerDialogFragment
import com.google.android.flexbox.FlexboxLayout
import com.google.android.gms.location.places.ui.PlacePicker
import com.ismail_s.jtime.android.CalendarFormatter.formatCalendarAsTime
import com.ismail_s.jtime.android.CalendarFormatter.formatCalendarAsTodayOrDate
import com.ismail_s.jtime.android.MainActivity
import com.ismail_s.jtime.android.R
import com.ismail_s.jtime.android.RestClient
import com.ismail_s.jtime.android.pojo.SalaahTimePojo
import com.ismail_s.jtime.android.pojo.SalaahType
import kotlinx.android.synthetic.main.fragment_home.*
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.jetbrains.anko.*
import org.jetbrains.anko.cardview.v7.cardView
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast
import java.util.*
import kotlin.comparisons.compareBy
import kotlin.comparisons.thenBy

/**
 * Fragment that displays a summary of times for today for nearby masjids.
 */
class HomeFragment : BaseFragment() {
    private lateinit var card_view_container: FlexboxLayout
    private var date: GregorianCalendar = GregorianCalendar()
    private var location: Location? = null
    private var locationName: String? = null
    private var nextUpdateTime: Calendar? = null
    private val getAndDisplayTimesCallback = Runnable { getTimesAndLocAndDisplayInUi() }
    private val SELECT_LOCATION_REQUEST = 46

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (savedInstanceState != null) {
            location = savedInstanceState.getParcelable<Location>(LOCATION)
            locationName = savedInstanceState.getString(LOCATION_NAME)
            date = savedInstanceState.getSerializable(DATE) as GregorianCalendar? ?: GregorianCalendar()
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        savedInstanceState.putSerializable(DATE, date)
        savedInstanceState.putString(LOCATION_NAME, locationName)
        savedInstanceState.putParcelable(LOCATION, location)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        card_view_container = rootView.find<FlexboxLayout>(R.id.card_view_container)
        getTimesAndLocAndDisplayInUi()
        return rootView
    }

    override fun onLocationChanged(loc: Location) {
        if (location == null) getTimesAndDisplayInUi(loc)
    }

    override fun onCreateOptionsMenu(menu: Menu) {
        menu.add(getString(R.string.menu_item_home_fragment_change_date)).setOnMenuItemClickListener {
            val cdp = CalendarDatePickerDialogFragment()
                    .setThemeDark()
                    .setPreselectedDate(date.get(Calendar.YEAR), date.get(Calendar.MONTH), date.get(Calendar.DAY_OF_MONTH))
                    .setOnDateSetListener { unused, y, m, d ->
                        date = GregorianCalendar()
                        date.set(Calendar.YEAR, y)
                        date.set(Calendar.MONTH, m)
                        date.set(Calendar.DAY_OF_MONTH, d)
                        getTimesAndLocAndDisplayInUi()
                    }
            cdp.show(childFragmentManager, "datePickerDialog")
            true
        }
        menu.add(getString(R.string.menu_item_home_fragment_change_location)).setOnMenuItemClickListener {
            val intent = PlacePicker.IntentBuilder().build(mainAct)
			startActivityForResult(intent, SELECT_LOCATION_REQUEST)
            true
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (requestCode == SELECT_LOCATION_REQUEST) {
			val place = data?.let { PlacePicker.getPlace(ctx, it) }
			if (resultCode != Activity.RESULT_OK || place == null) {
				toast("No location selected")
			} else {
                val loc = Location("")
                loc.latitude = place.latLng.latitude
                loc.longitude = place.latLng.longitude
				location = loc
                locationName = place.name.toString()
				getTimesAndLocAndDisplayInUi()
            }
		}
    }

    override fun onStop() {
        super.onStop()
        Handler().removeCallbacks(getAndDisplayTimesCallback)
    }

    override fun onStart() {
        super.onStart()
        val delay = nextUpdateTime?.timeInMillis?.let { it - GregorianCalendar().timeInMillis }
        if (delay != null) Handler().postDelayed(getAndDisplayTimesCallback, delay)
    }

    private fun getTimesAndLocAndDisplayInUi() {
        ifAttachedToAct {
            val promise = location?.let { Promise.of(it) } ?: mainAct.location
            promise successUi {
                getTimesAndDisplayInUi(it)
            } failUi {
                label_title_or_error_message.text = getString(R.string.home_fragment_could_not_get_loc_text)
            }
        }
    }

    private fun getTimesAndDisplayInUi(loc: Location) {
        if (activity == null)
            return
        cancelPromiseOnFragmentDestroy {
            RestClient(ctx).getTimesForNearbyMasjids(loc.latitude, loc.longitude, date) successUi {
                ifAttachedToAct s@ {
                    /*We have a list of salaah times for different salaah types & masjids.
                    * We now need to:
                    * 1. Find the time closest to now, for the masjid nearest to us
                    * 2. Group the times by salaah type and order the groups
                    * 3. Show all this info
                    * If the list is empty, say so and exit*/
                    //Make sure the table of salaah times is empty
                    card_view_container.removeAllViews()
                    if (it.isEmpty()) {
                        label_title_or_error_message.text = getString(R.string.no_salaah_times_nearby_masjids_toast)
                        return@s
                    }
                    val locStr = locationName?.let { " for $it" } ?: ""
                    label_title_or_error_message.text = getString(R.string.label_home_fragment_title, formatCalendarAsTodayOrDate(date), locStr)
                    val now = GregorianCalendar()
                    val (closest, sortedSalaahTimesMap, updateTime) = calcHomeFragmentLayoutParams(it, now, loc)

                    val addToGrid = { x: AnkoContext<FlexboxLayout>.() -> View ->
                        card_view_container.addView(
                                with(AnkoContext.create(act, card_view_container), x))
                    }
                    val tSize = 18f
                    val tSubheaderSize = 22f
                    val closestId = 43
                    for ((type, times) in sortedSalaahTimesMap) {
                        addToGrid {
                            cardView {
                                onClick {
                                    (act as? MainActivity)?.switchToNearbyTimesFragment(type)
                                }
                                tableLayout {
                                    tableRow {
                                        textView(type.toString(ctx)) {
                                            textSize = tSubheaderSize
                                        }
                                    }
                                    times.sortedBy { it.datetime.timeInMillis }.forEachIndexed { i, t ->
                                        val textSize = if (t == closest) 20f else tSize
                                        tableRow {
                                            if (i % 2 == 0) backgroundColor = ContextCompat.getColor(ctx, R.color.md_grey_700)
                                            if (t == closest) {
                                                backgroundColor = ContextCompat.getColor(ctx, R.color.md_pink_500)
                                                id = closestId
                                            }
                                            textView(t.masjidName) {
                                                this.textSize = textSize
                                            }
                                            textView(formatCalendarAsTime(t.datetime)) {
                                                this.textSize = textSize
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                    // Delete any callbacks, add a callback to trigger at updateTime
                    Handler().removeCallbacks(getAndDisplayTimesCallback)
                    nextUpdateTime = updateTime
                    Handler().postDelayed(getAndDisplayTimesCallback,
                            updateTime.timeInMillis - GregorianCalendar().timeInMillis)
                    scrollToHighlightedRow(closestId)
                }
            } failUi {
                ifAttachedToAct {
                    toast(getString(R.string.get_masjid_times_failure_toast, it.message))
                }
            }
        }
    }

    /**
     * Scroll the the view with id [closestId].
     *
     * The view with id [closestId] must be a child or sub-child of the
     * scroll view of this fragment.
     */
    private fun scrollToHighlightedRow(closestId: Int) {
        // To prevent NullPointerExceptions, keep a reference to the
        // scroll view, as we'll be doing stuff in callbacks on the
        // ui thread rather than in one go
        val scrollView = scroll_view
        // This post is done to make sure that the layout all exists
        // before we start doing calculations based on it
        scrollView.post s@ {
            val row = scrollView.findOptional<View>(closestId) ?: return@s
            // Find y location of view relative to top of scroll view
            var yDistanceToRow = 0
            var parentView: View = row.parent as? View ?: return@s
            // iterate through parent views, adding up their y positions
            while (true) {
                if (parentView == scrollView) break
                yDistanceToRow += parentView.top
                parentView = parentView.parent as? View ?: break
            }
            val topInScrollView = yDistanceToRow + row.top
            val bottomInScrollView = yDistanceToRow + row.bottom
            // Scroll target view to almost the top of the screen (or as
            // close to there as possible)
            scrollView.smoothScrollTo(0, ((topInScrollView + bottomInScrollView) / 2) - row.height)
        }
    }

    companion object {
        private val LOCATION = "HomeFragment.LOCATION"
        private val LOCATION_NAME = "HomeFragment.LOCATION_NAME"
        private val DATE = "HomeFragment.DATE"
    }
}

/**
 * Calculate the variables that determine the content/layout of the [HomeFragment].
 *
 * This function is a pure function.
 *
 * @param times a list of salaah time objects to sort and find the most relevant time
 * @param now the current time
 * @param loc the current location
 * @return the following:
 * - the most relevant salaah time in terms of proximity to current location, and being the closest
 * to the current time
 * - a sorted map of all the salaah times, sorted by type, time and distance from current location
 * - a time at which this function will give different results ie when this function should
 * next be called and the layout refreshed
 */
fun calcHomeFragmentLayoutParams(times: List<SalaahTimePojo>, now: Calendar, loc: Location): HomeFragmentLayoutParams {
    val keyForTime = { x: SalaahTimePojo -> (3L * 60 * 1000) + x.datetime.timeInMillis - now.timeInMillis }
    var updateTime = GregorianCalendar()
    updateTime.add(Calendar.DAY_OF_MONTH, 1)
    listOf(Calendar.HOUR_OF_DAY, Calendar.MINUTE, Calendar.SECOND, Calendar.MILLISECOND).forEach { updateTime.set(it, 0) }
    if (times.isEmpty()) {
        val dummyClosestTime = SalaahTimePojo(1, SalaahType.FAJR, GregorianCalendar())
        return HomeFragmentLayoutParams(dummyClosestTime, sortedMapOf(), updateTime)
    }
    var setUpdateTimeBasedOnClosestTime = false
    val closestTime = times.sortedBy { keyForTime(it) }
            .dropWhile { keyForTime(it) < 0 }
            .let { sortedTimes ->
                if (sortedTimes.isEmpty()) {
                    times.sortedBy { keyForTime(it) }.reversed()
                } else {
                    if (sortedTimes.size > 1) setUpdateTimeBasedOnClosestTime = true
                    sortedTimes
                }
            }.let { sortedTimes ->
        sortedTimes.takeWhile { keyForTime(it) == keyForTime(sortedTimes.first()) }
                .sortedBy { loc.distanceTo(it.masjidLoc) }.first()
    }
    if (closestTime.datetime.timeInMillis - now.timeInMillis >= 0 && setUpdateTimeBasedOnClosestTime) {
        updateTime = closestTime.datetime.clone() as GregorianCalendar
        updateTime.add(Calendar.MINUTE, 3)
        updateTime.add(Calendar.MILLISECOND, 1)
    }
    val sortedSalaahTimesMap = times
            .groupBy { it.type }
            .mapValues {
                it.value
                        .sortedWith(compareBy<SalaahTimePojo> { it.datetime }
                                .thenBy { loc.distanceTo(it.masjidLoc) })
            }
            .toSortedMap()
    return HomeFragmentLayoutParams(closestTime, sortedSalaahTimesMap, updateTime)
}

data class HomeFragmentLayoutParams(val closestTime: SalaahTimePojo, val sortedTimeMap: SortedMap<SalaahType, List<SalaahTimePojo>>, val updateTime: Calendar)
