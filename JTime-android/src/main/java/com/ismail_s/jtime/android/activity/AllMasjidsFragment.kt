package com.ismail_s.jtime.android.activity

import android.app.Fragment
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
import java.util.*

class AllMasjidsFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater!!.inflate(R.layout.fragment_item_list, container, false) as RecyclerView

        // Set the adapter
        view.layoutManager = LinearLayoutManager(view.context)

        val cb = object: RestClient.MasjidsCallback {
            override fun onSuccess(masjids: List<MasjidPojo>) {
                view.adapter = MyItemRecyclerViewAdapter(masjids, activity as MainActivity)
            }

            override fun onError(t: Throwable) {
                Toast.makeText(activity, "Weren't able to get masjids", Toast.LENGTH_LONG)
                        .show()
            }
        }
        RestClient(activity).getMasjids(cb)
        return view
    }
}
