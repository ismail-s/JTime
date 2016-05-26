package com.ismail_s.jtime.android.activity

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.ismail_s.jtime.android.R

class HelpFragment : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d("jtime", "In onCreateView of HelpFragment with inflater $inflater & container $container")
        return inflater!!.inflate(R.layout.fragment_help, container, false)
    }
}
