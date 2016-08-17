package com.ismail_s.jtime.android.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TableRow
import com.ismail_s.jtime.android.CalendarFormatter.formatCalendarAsTime
import com.ismail_s.jtime.android.R
import com.ismail_s.jtime.android.RestClient
import com.ismail_s.jtime.android.pojo.SalaahTimePojo
import kotlinx.android.synthetic.main.fragment_home.*
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.longToast
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.tableRow
import org.jetbrains.anko.textView
import java.util.*

/**
 * Fragment that displays a summary of times for today for nearby masjids.
 */
class HomeFragment: BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_home, container, false)
        mainAct.location successUi { loc ->
            RestClient(ctx).getTimesForNearbyMasjids(loc.latitude, loc.longitude) successUi s@ {
                /*We have a list of salaah times for different salaah types & masjids.
                * We now need to:
                * 1. Find the time closest to now, for the masjid nearest to us
                * 2. Group the remaining times by salaah type and order the groups
                * 3. Show all this info
                * If the list is empty, say so and exit*/
                if (it.isEmpty()) {
                    longToast(getString(R.string.no_salaah_times_nearby_masjids_toast))
                    return@s
                }
                val now = GregorianCalendar()
                val keyForTime = { x: SalaahTimePojo -> (3L * 60 * 1000) + x.datetime.timeInMillis - now.timeInMillis }
                var sortedTimes = it.sortedBy { keyForTime(it) }
                        .dropWhile { keyForTime(it) < 0 }
                val closest = if(sortedTimes.isEmpty()) {
                    val x = it.sortedBy { keyForTime(it) }.reversed()
                    x.takeWhile { keyForTime(it) == keyForTime(x.first()) }
                            .sortedBy { loc.distanceTo(it.masjidLoc) }.first()
                } else {
                    sortedTimes
                            .takeWhile { keyForTime(it) == keyForTime(sortedTimes.first()) }
                            .sortedBy { loc.distanceTo(it.masjidLoc) }.first()
                }
                val remaining = (it - closest).groupBy { it.type }.toSortedMap()

                val params = arrayOf(closest.type.toString(ctx), closest.masjidName, formatCalendarAsTime(closest.datetime))
                label_next_time.text = if (closest.datetime.timeInMillis >= now.timeInMillis)
                    getString(R.string.home_fragment_next_time_future_text, *params)
                else
                    getString(R.string.home_fragment_next_time_past_text, *params)
                label_other_times_today.visibility = View.VISIBLE

                val addToTable = {x: AnkoContext<TableLayout>.() -> TableRow ->
                    salaah_times_summary_table.addView(
                            with(AnkoContext.create(act, salaah_times_summary_table), x))}
                val tSize = 18f
                val tSubheaderSize = 20f
                for ((type, times) in remaining) {
                    addToTable {
                        tableRow {
                            textView(type.toString(ctx)) {
                                textSize = tSubheaderSize
                            }
                        }
                    }
                    times.sortedBy { it.datetime.timeInMillis }.forEach {
                        addToTable {
                            tableRow {
                                textView(it.masjidName) {
                                    textSize = tSize
                                }
                                textView(formatCalendarAsTime(it.datetime)) {
                                    textSize = tSize
                                }
                            }
                        }
                    }
                }
            } failUi {
                toast(getString(R.string.get_masjid_times_failure_toast, it.message))
            }
        } failUi {
            toast(getString(R.string.get_location_for_nearby_masjids_failure_toast))
        }
        return rootView
    }
}
