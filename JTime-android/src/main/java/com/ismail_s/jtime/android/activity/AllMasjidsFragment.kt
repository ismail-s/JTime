package com.ismail_s.jtime.android.activity

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

class AllMasjidsFragment : BaseFragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_item_list, container, false) as RecyclerView
        view.setHasFixedSize(true)

        val cb = object: RestClient.MasjidsCallback {
            override fun onSuccess(masjids: List<MasjidPojo>) {
                view.adapter = MyItemRecyclerViewAdapter(sortMasjids(masjids), activity as MainActivity)
            }

            override fun onError(t: Throwable) {
                if (activity != null)
                    Toast.makeText(activity, getString(R.string.get_masjids_failure_toast), Toast.LENGTH_LONG)
                        .show()
            }
        }
        RestClient(activity).getMasjids(cb)
        return view
    }

    private fun sortMasjids(masjids: List<MasjidPojo>): List<MasjidPojo> {
        val userLocation = (activity as MainActivity).lastLocation
        if (userLocation == null) {
            return masjids
        }
        return masjids.sortedBy {
            //For some weird reason, distanceBetween doesn't return the distance, but instead
            //stores the computed distance on a result array that is passed in
            val result = FloatArray(size = 1)
            distanceBetween(userLocation.latitude, userLocation.longitude, it.latitude, it.longitude, result)
            result[0]
        }
    }
}
