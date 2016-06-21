package com.ismail_s.jtime.android.activity

import android.os.Bundle
import android.text.method.LinkMovementMethod
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.ismail_s.jtime.android.R
import org.jetbrains.anko.debug
import org.jetbrains.anko.find

class HelpFragment : BaseFragment() {
    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        debug("In onCreateView of HelpFragment with inflater $inflater & container $container")
        val rootView = inflater!!.inflate(R.layout.fragment_help, container, false)
        val helpText = rootView.find<TextView>(R.id.label_help)
        //Make the links in the help text clickable
        helpText.movementMethod = LinkMovementMethod.getInstance()
        return rootView
    }
}
