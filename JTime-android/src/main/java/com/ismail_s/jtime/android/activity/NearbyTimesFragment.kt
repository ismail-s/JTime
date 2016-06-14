package com.ismail_s.jtime.android.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TextView
import com.ismail_s.jtime.android.CalendarFormatter.formatCalendarAsTime
import com.ismail_s.jtime.android.R
import com.ismail_s.jtime.android.RestClient
import com.ismail_s.jtime.android.SalaahType
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.jetbrains.anko.AnkoContext
import org.jetbrains.anko.AnkoLogger
import org.jetbrains.anko.debug
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast
import org.jetbrains.anko.tableRow
import org.jetbrains.anko.textView

class NearbyTimesFragment : BaseFragment(), AnkoLogger {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater.inflate(R.layout.fragment_nearby_times, container, false)
        val salaahType = arguments.getSerializable(SALAAH_TYPE) as SalaahType
        val salaahNameLabel = rootView.findViewById(R.id.label_salaah_name) as TextView
        salaahNameLabel.text = getString(R.string.nearby_times_title_text, salaahType.toString(ctx))
        val table = rootView.findViewById(R.id.salaah_times_table) as TableLayout
        (activity as MainActivity).location successUi {
            RestClient(activity).getTimesForNearbyMasjids(it.latitude, it.longitude, salaahType)
            .successUi {
                it.forEach {
                    table.addView(with(AnkoContext.create(activity, table)) {
                        tableRow {
                            textView(it.masjidName)
                            textView(formatCalendarAsTime(it.datetime))
                        }
                    })
                }
            } failUi {
                debug("Failed to get nearby times from masjid")
                toast(getString(R.string.get_masjid_times_failure_toast, it.message))
            }
        } failUi {
            debug("Failed to get current location")
            toast(getString(R.string.get_location_for_nearby_masjids_failure_toast))
        }
        return rootView
    }

    companion object {
        private val SALAAH_TYPE = "salaahType"

        fun newInstance(salaahType: SalaahType): NearbyTimesFragment {
            val fragment = NearbyTimesFragment()
            val args = Bundle()
            args.putSerializable(SALAAH_TYPE, salaahType)
            fragment.arguments = args
            return fragment
        }
    }

}
