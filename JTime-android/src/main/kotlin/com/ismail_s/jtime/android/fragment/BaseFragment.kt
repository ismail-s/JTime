package com.ismail_s.jtime.android.fragment

import android.location.Location
import android.support.v4.app.Fragment
import android.view.View
import com.ismail_s.jtime.android.MainActivity
import org.jetbrains.anko.AnkoLogger


/**
* Base class for all fragments in the app. Fragments that want to handle
* various events can override the methods contained in this class.
*/
open class BaseFragment : Fragment(), AnkoLogger {
    /**
     * Helper property to avoid casting all over the codebase. Note that callers should make
     * sure [getActivity] returns a non-null result first.
     */
    val mainAct: MainActivity
        get() = activity as MainActivity

    /**
     * Called when either the nav drawer or the right drawer in [MainActivity] is opened. If the
     * nav drawer is opened, and then the right drawer, this method is only called once.
     */
    open fun onDrawerOpened(drawerView: View) {}

    /**
     * Called when either the nav drawer or the right drawer in [MainActivity] is closed.
     */
    open fun onDrawerClosed(drawerView: View) {}

    /**
     * Called when the user has successfully logged in.
     */
    open fun onLogin() {}

    /**
     * Called when the user has successfully logged out.
     */
    open fun onLogout() {}

    /**
     * Called when Google Play Services indicates that the users location has changed, and when
     * the users location has changed by at least 50 metres since the last time this method was
     * called or _mainAct.location_ was changed.
     */
    open fun onLocationChanged(loc: Location) {}
}
