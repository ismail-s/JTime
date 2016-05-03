package com.ismail_s.jtime.android.activity

import android.app.Fragment
import android.view.View


open class BaseFragment : Fragment() {
    fun onDrawerOpened(drawerView: View) {}

    fun onDrawerClosed(drawerView: View) {}
}
