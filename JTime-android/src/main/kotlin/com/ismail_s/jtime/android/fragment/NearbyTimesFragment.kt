package com.ismail_s.jtime.android.fragment

import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ismail_s.jtime.android.CalendarFormatter.formatCalendarAsTime
import com.ismail_s.jtime.android.R
import com.ismail_s.jtime.android.RestClient
import com.ismail_s.jtime.android.pojo.SalaahType
import kotlinx.android.synthetic.main.fragment_nearby_times.*
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.debug
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.support.v4.withArguments
import org.jetbrains.anko.tableRow
import org.jetbrains.anko.textView

/**
 * Displays nearby salaah times for today, for a given salaah type.
 */
class NearbyTimesFragment : BaseFragment() {
    lateinit var salaahType: SalaahType

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_nearby_times, container, false)
        salaahType = arguments.getSerializable(SALAAH_TYPE) as SalaahType
        val salaahNameLabel = rootView.find<TextView>(R.id.label_salaah_name)
        salaahNameLabel.text = getString(R.string.nearby_times_title_text, salaahType.toString(ctx))
        mainAct.location successUi {
            getTimesAndDisplayInUi(it)
        } failUi {
            debug("Failed to get current location")
            toast(getString(R.string.get_location_for_nearby_masjids_failure_toast))
        }
        return rootView
    }

    override fun onLocationChanged(loc: Location) = getTimesAndDisplayInUi(loc)

    private fun getTimesAndDisplayInUi(loc: Location) {
        val table = salaah_times_table
        cancelPromiseOnFragmentDestroy {
            RestClient(act).getTimesForNearbyMasjids(loc.latitude, loc.longitude, salaahType = salaahType)
                    .successUi s@ {
                        table.removeAllViews()
                        val tSize = 18f
                        if (it.isEmpty()) {
                            table.addView(with(AnkoContext.create(act, table)) {
                                tableRow {
                                    textView(getString(R.string.no_salaah_times_nearby_masjids_toast)) {
                                        textSize = tSize
                                    }
                                }
                            })
                            return@s
                        }
                        it.sortedBy { it.datetime.timeInMillis }.forEach {
                            table.addView(with(AnkoContext.create(act, table)) {
                                tableRow {
                                    textView(it.masjidName) {
                                        textSize = tSize
                                    }
                                    textView(formatCalendarAsTime(it.datetime)) {
                                        textSize = tSize
                                    }
                                }
                            })
                        }
                    } failUi {
                        ifAttachedToAct {
                            debug("Failed to get nearby times from masjid")
                            toast(getString(R.string.get_masjid_times_failure_toast, it.message))
                        }
            }
        }
    }

    companion object {
        private val SALAAH_TYPE = "salaahType"

        fun newInstance(salaahType: SalaahType): NearbyTimesFragment =
                NearbyTimesFragment().withArguments(SALAAH_TYPE to salaahType)
    }

}
