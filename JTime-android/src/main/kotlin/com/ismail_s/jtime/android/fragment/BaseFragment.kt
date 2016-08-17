package com.ismail_s.jtime.android.fragment

import android.support.v4.app.Fragment
import android.view.View
import com.ismail_s.jtime.android.MainActivity
import org.jetbrains.anko.AnkoLogger


open class BaseFragment : Fragment(), AnkoLogger {
    /**
     * Helper property to avoid casting all over the codebase. Note that callers should make
     * sure [getActivity] returns a non-null result first.
     */
    val mainAct: MainActivity
        get() = activity as MainActivity

    open fun onDrawerOpened(drawerView: View) {}

    open fun onDrawerClosed(drawerView: View) {}

    open fun onLogin() {}

    open fun onLogout() {}
}
