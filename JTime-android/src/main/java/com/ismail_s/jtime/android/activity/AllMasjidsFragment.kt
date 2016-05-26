package com.ismail_s.jtime.android.activity

import android.location.Location
import android.location.Location.distanceBetween
import android.os.Bundle
import android.support.v7.widget.LinearLayoutManager
import android.support.v7.widget.RecyclerView
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.ismail_s.jtime.android.MasjidPojo
import com.ismail_s.jtime.android.R
import com.ismail_s.jtime.android.RestClient
import nl.komponents.kovenant.ui.successUi
import nl.komponents.kovenant.ui.failUi

class AllMasjidsFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_item_list, container, false) as RecyclerView
        view.setHasFixedSize(true)

        RestClient(activity).getMasjids() successUi {
            val masjids = it
            if (activity != null) {
                (activity as MainActivity).location successUi {
                    if (activity != null)
                        view.adapter = MyItemRecyclerViewAdapter(sortMasjidsByLocation(masjids, it), activity as MainActivity)
                } failUi {
                    if (activity != null)
                        view.adapter = MyItemRecyclerViewAdapter(sortMasjidsByName(masjids), activity as MainActivity)
                }
            }
        } failUi {
            if (activity != null)
                Toast.makeText(activity, getString(R.string.get_masjids_failure_toast, it.message), Toast.LENGTH_LONG)
                    .show()
        }
        return view
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
