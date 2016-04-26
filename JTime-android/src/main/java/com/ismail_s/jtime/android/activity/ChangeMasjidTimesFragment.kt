package com.ismail_s.jtime.android.activity

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.ismail_s.jtime.android.R
import com.ismail_s.jtime.android.RestClient

class ChangeMasjidTimesFragment : Fragment(), View.OnClickListener {

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_change_masjid_times, container, false)
        return rootView
    }

    override fun onClick(view: View) {
    }
}
