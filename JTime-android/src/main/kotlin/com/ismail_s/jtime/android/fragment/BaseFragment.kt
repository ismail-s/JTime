package com.ismail_s.jtime.android.fragment

import android.location.Location
import android.support.v4.app.Fragment
import android.view.View
import com.ismail_s.jtime.android.MainActivity
import nl.komponents.kovenant.CancelException
import nl.komponents.kovenant.Kovenant
import nl.komponents.kovenant.Promise
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

    private val promisesToCleanup: MutableList<Promise<*, Throwable>> = mutableListOf()

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

    override fun onDestroyView() {
        super.onDestroyView()
        promisesToCleanup.forEach { Kovenant.cancel(it, CancelException()) }
        promisesToCleanup.clear()
    }

    /**
     * Execute the provided block and schedule the promise returned to be cancelled
     * when the current fragment has its view destroyed.
     *
     * This is useful where callbacks on the promise interact with the fragment, and
     * would throw errors if the fragment view had been destroyed.
     */
    fun cancelPromiseOnFragmentDestroy(block: () -> Promise<*, Throwable>) {
        promisesToCleanup.add(block())
    }

    /**
    * Execute the provided block if this fragment is attached to an activity.
    */
    fun ifAttachedToAct(block: () -> Unit) {
        if (isAdded() && activity != null) {
            block()
        }
    }
}
