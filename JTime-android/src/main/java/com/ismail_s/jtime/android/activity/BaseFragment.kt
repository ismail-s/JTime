package com.ismail_s.jtime.android.activity

import android.support.v4.app.Fragment
import android.view.View


open class BaseFragment : Fragment() {
    open fun onDrawerOpened(drawerView: View) {}

    open fun onDrawerClosed(drawerView: View) {}

    open fun onLogin() {}

    open fun onLogout() {}
}
