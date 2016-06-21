package com.ismail_s.jtime.android.activity

import android.location.Location
import android.location.Location.distanceBetween
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ismail_s.jtime.android.MasjidPojo
import com.ismail_s.jtime.android.R
import com.ismail_s.jtime.android.RestClient
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.longToast

class AllMasjidsFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {
    private lateinit var layout: SwipeRefreshLayout
    private lateinit var rView: RecyclerView

    private fun hideRefreshIcon() {
        layout.isRefreshing = false
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        layout = inflater!!.inflate(R.layout.fragment_item_list, container, false) as SwipeRefreshLayout
        layout.setOnRefreshListener(this)
        rView = layout.find<RecyclerView>(R.id.list)
        rView.setHasFixedSize(true)

        layout.post { layout.isRefreshing = true }
        onRefresh()
        return layout
    }

    override fun onRefresh() {
        RestClient(act).getMasjids() successUi {
            val masjids = it
            if (activity != null) {
                mainAct.location successUi {
                    hideRefreshIcon()
                    if (activity != null)
                        rView.adapter = MyItemRecyclerViewAdapter(sortMasjidsByLocation(masjids, it), mainAct)
                } failUi {
                    hideRefreshIcon()
                    if (activity != null)
                        rView.adapter = MyItemRecyclerViewAdapter(sortMasjidsByName(masjids), mainAct)
                }
            }
        } failUi {
            hideRefreshIcon()
            if (activity != null)
                longToast(getString(R.string.get_masjids_failure_toast, it.message))
        }
    }

    private fun sortMasjidsByLocation(masjids: List<MasjidPojo>, userLocation: Location): List<MasjidPojo> {
        return masjids.sortedBy {
            //For some weird reason, distanceBetween doesn't return the distance, but instead
            //stores the computed distance on a result array that is passed in
            val result = FloatArray(size = 1)
            distanceBetween(userLocation.latitude, userLocation.longitude, it.latitude, it.longitude, result)
            result[0]
        }
    }

    private fun sortMasjidsByName(masjids: List<MasjidPojo>) = masjids.sortedBy {it.name}
}
