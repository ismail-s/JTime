package com.ismail_s.jtime.android.fragment

import android.location.Location
import android.location.Location.distanceBetween
import android.os.Bundle
import android.support.v4.widget.SwipeRefreshLayout
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ismail_s.jtime.android.R
import com.ismail_s.jtime.android.RestClient
import com.ismail_s.jtime.android.MasjidRecyclerViewAdapter
import com.ismail_s.jtime.android.pojo.MasjidPojo
import kotlinx.android.synthetic.main.fragment_item_list.*
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.longToast

/**
* Display a list of all masjids on the rest server.
*/
class AllMasjidsFragment : BaseFragment(), SwipeRefreshLayout.OnRefreshListener {

    private fun hideRefreshIcon() {
        pull_to_refresh_container.isRefreshing = false
    }

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
            savedInstanceState: Bundle?): View? =
        inflater?.inflate(R.layout.fragment_item_list, container, false)

    override fun onStart() {
        super.onStart()
        pull_to_refresh_container.setOnRefreshListener(this)
        masjid_list.setHasFixedSize(true)

        pull_to_refresh_container.post { pull_to_refresh_container.isRefreshing = true }
        onRefresh()
    }

    override fun onRefresh() {
        cancelPromiseOnFragmentDestroy {
            RestClient(act).getMasjids() successUi { masjids ->
                if (activity != null) {
                    mainAct.location successUi {
                        hideRefreshIcon()
                        if (activity != null)
                            masjid_list.adapter = MasjidRecyclerViewAdapter(sortMasjidsByLocation(masjids, it), mainAct)
                    } failUi {
                        hideRefreshIcon()
                        if (activity != null)
                            masjid_list.adapter = MasjidRecyclerViewAdapter(sortMasjidsByName(masjids), mainAct)
                    }
                }
            } failUi {
                hideRefreshIcon()
                if (activity != null)
                    longToast(getString(R.string.get_masjids_failure_toast, it.message))
            }
        }
    }

    override fun onLocationChanged(loc: Location) {
        pull_to_refresh_container.isRefreshing = true
        onRefresh()
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
