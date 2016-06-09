package com.ismail_s.jtime.android.activity

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ismail_s.jtime.android.R

class HelpFragment : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        Log.d("jtime", "In onCreateView of HelpFragment with inflater $inflater & container $container")
        val rootView = inflater!!.inflate(R.layout.fragment_help, container, false)
        val helpText = rootView.findViewById(R.id.label_help) as TextView
        //Make the links in the help text clickable
        helpText.setMovementMethod(LinkMovementMethod.getInstance())
        return rootView
    }
}
