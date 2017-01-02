package com.ismail_s.jtime.android.fragment

import android.location.Location
import android.os.Bundle
import android.support.v4.content.ContextCompat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.flexbox.FlexboxLayout
import com.ismail_s.jtime.android.CalendarFormatter.formatCalendarAsTime
import com.ismail_s.jtime.android.MainActivity
import com.ismail_s.jtime.android.R
import com.ismail_s.jtime.android.RestClient
import com.ismail_s.jtime.android.pojo.SalaahTimePojo
import com.ismail_s.jtime.android.pojo.SalaahType
import kotlinx.android.synthetic.main.fragment_home.label_title_or_error_message
import kotlinx.android.synthetic.main.fragment_home.scroll_view
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
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        card_view_container = rootView.find<FlexboxLayout>(R.id.card_view_container)
        mainAct.location successUi {
            getTimesAndDisplayInUi(it)
        } failUi {
            label_title_or_error_message.text = "Could not get your location. Location is needed to show salaah times for the nearest masjids."
        }
        return rootView
    }

    override fun onLocationChanged(loc: Location) {
        getTimesAndDisplayInUi(loc)
    }

    private fun getTimesAndDisplayInUi(loc: Location) {
        if (activity == null)
            return
        cancelPromiseOnFragmentDestroy {
            RestClient(ctx).getTimesForNearbyMasjids(loc.latitude, loc.longitude) successUi {
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
                    val now = GregorianCalendar()
                    // TODO-use the updateTime variable
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
                }
            } failUi {
                ifAttachedToAct {
                    toast(getString(R.string.get_masjid_times_failure_toast, it.message))
                }
            }
        }
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
