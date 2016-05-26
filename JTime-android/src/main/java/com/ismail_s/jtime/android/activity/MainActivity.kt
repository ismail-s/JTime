package com.ismail_s.jtime.android.activity

import android.support.v4.app.Fragment
import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.View
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationServices
import com.ismail_s.jtime.android.R
import com.ismail_s.jtime.android.RestClient
import com.ismail_s.jtime.android.SharedPreferencesWrapper
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import java.util.*
import nl.komponents.kovenant.deferred
import nl.komponents.kovenant.Promise
import nl.komponents.kovenant.android.*

class MainActivity : AppCompatActivity(), GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    var drawer: Drawer? = null
    lateinit var header: AccountHeader
    lateinit var googleApiClient: GoogleApiClient
    lateinit var toolbar: Toolbar
    private var locationDeferred = deferred<Location, Exception>()
    var location = locationDeferred.promise
    /**
    * Login status is 0 for don't know, 1 for logged in and 2 for logged out
    */
    private var loginStatus = 0
    val currentFragment: BaseFragment?
        get() = supportFragmentManager.findFragmentById(R.id.fragment_container) as BaseFragment?
    private val LOGIN_DRAWER_ITEM_IDENTIFIER: Long = 546
    private val LOGOUT_DRAWER_ITEM_IDENTIFIER: Long = 232
    private val ADD_MASJID_DRAWER_ITEM_IDENTIFIER: Long = 785
    val HELP_DRAWER_ITEM_IDENTIFIER: Long = 365

    private val TOOLBAR_TITLE = "toolbar_title"
    private val LOGIN_STATUS = "login_status"

    private val logoutDrawerItem: PrimaryDrawerItem
        get() = PrimaryDrawerItem()
                .withName(getString(R.string.drawer_item_logout))
                .withIdentifier(LOGOUT_DRAWER_ITEM_IDENTIFIER)
                .withOnDrawerItemClickListener { view, i, iDrawerItem ->
                    val cb = object : RestClient.LogoutCallback {
                        override fun onSuccess() {
                            loginStatus = 2
                            showShortToast(getString(R.string.logout_success_toast))
                            //Remove logout button, add login button to nav drawer
                            header.removeProfile(0)
                            drawer?.removeItem(LOGOUT_DRAWER_ITEM_IDENTIFIER)
                            drawer?.addItemAtPosition(loginDrawerItem, 0)
                            //remove addMasjidDrawerItem
                            drawer?.removeItem(ADD_MASJID_DRAWER_ITEM_IDENTIFIER)
                            currentFragment?.onLogout()
                        }

                        override fun onError(t: Throwable) = showShortToast(getString(R.string.logout_failure_toast, t.message))
                    }
                    RestClient(this).logout(cb)
                    true
                }

    private val loginDrawerItem: PrimaryDrawerItem
        get() = PrimaryDrawerItem()
                .withName(getString(R.string.drawer_item_login))
                .withIdentifier(LOGIN_DRAWER_ITEM_IDENTIFIER)
                .withOnDrawerItemClickListener { view, i, iDrawerItem ->
                    val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
                   startActivityForResult(signInIntent, Constants.RC_SIGN_IN)
                    true
                }

    private val addMasjidDrawerItem: PrimaryDrawerItem
        get() = PrimaryDrawerItem()
                .withName(getString(R.string.drawer_item_add_masjid))
                .withIdentifier(ADD_MASJID_DRAWER_ITEM_IDENTIFIER)
                .withOnDrawerItemClickListener { view, i, iDrawerItem ->
                    switchToAddMasjidFragment()
                    true
                }

    /**
     * Called when Sign in with Google fails
     */
    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        showShortToast(getString(R.string.login_failure_toast, connectionResult.toString()))
    }

    override fun onConnected(connectionHint: Bundle?) {
       if (location.isDone()) {
           return
       }
       val loc = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
       if (loc == null) {
           locationDeferred.reject(Exception("Location could not be obtained"))
       } else {
           locationDeferred.resolve(loc)
       }
    }

    override fun onConnectionSuspended(cause: Int) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startKovenant()
        toolbar = findViewById(R.id.toolbar) as Toolbar
        toolbar.title = savedInstanceState?.getCharSequence(TOOLBAR_TITLE, "") ?: ""
        setSupportActionBar(toolbar)
        setUpGoogleApiClient()

        val cb = object: RestClient.SignedinCallback {
            override fun onLoggedOut() {
                loginStatus = 2
                //Set button to be login, create drawer
                setUpNavDrawer(loginDrawerItem, savedInstanceState)
            }

            override fun onLoggedIn() {
                loginStatus = 1
                //Set button to be logout, create drawer
                setUpNavDrawer(logoutDrawerItem, savedInstanceState)
                val email: String = SharedPreferencesWrapper(this@MainActivity).email
                header.addProfile(ProfileDrawerItem().withEmail(email), 0)
                //add addMasjidDrawerItem
                drawer?.addItem(addMasjidDrawerItem)
            }
        }
        val loggedInStatus = savedInstanceState?.getInt(LOGIN_STATUS, 0) ?: 0
        when (loggedInStatus) {
            1 -> cb.onLoggedIn()
            2 -> cb.onLoggedOut()
            else -> RestClient(this).checkIfStillSignedInOnServer(cb)
        }

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return
            }
            switchToAllMasjidsFragment()
        }
    }

    private fun setUpGoogleApiClient() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .requestIdToken("654477471044-i8156m316nreihgdqoicsh0gktgqjaua.apps.googleusercontent.com")
                .build()
        googleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
    }

    private fun setUpNavDrawer(loginOutButton: PrimaryDrawerItem, savedInstance: Bundle?) {
        val drawerListener = object: Drawer.OnDrawerListener {
            override fun onDrawerOpened(drawerView: View) {
                currentFragment?.onDrawerOpened(drawerView)
            }

            override fun onDrawerClosed(drawerView: View) {
                currentFragment?.onDrawerClosed(drawerView)
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
        }
        header = AccountHeaderBuilder().withActivity(this)
                .withProfileImagesVisible(false).withCompactStyle(true)
                .build()
        drawer = DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(header)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withShowDrawerOnFirstLaunch(true)
                .withOnDrawerListener(drawerListener)
                .withSavedInstance(savedInstance)
                .addDrawerItems(loginOutButton, PrimaryDrawerItem()
                        .withName(getString(R.string.drawer_item_all_masjids))
                        .withOnDrawerItemClickListener { view, position, drawerItem ->
                            switchToAllMasjidsFragment()
                            true
                        }, PrimaryDrawerItem()
                        .withName(getString(R.string.drawer_item_help))
                        .withIdentifier(HELP_DRAWER_ITEM_IDENTIFIER)
                        .withOnDrawerItemClickListener {view, position, drawerItem ->
                            switchToHelpFragment()
                            true
                        })
                .build()
    }

    override fun onStart() {
        googleApiClient.connect()
        super.onStart()
    }

    override fun onStop() {
        googleApiClient.disconnect()
        super.onStop()
    }

    override fun onDestroy() {
        stopKovenant()
        super.onDestroy()
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        //Save current title
        savedInstanceState.putCharSequence(TOOLBAR_TITLE, toolbar.title)
        //Save logged in state
        savedInstanceState.putInt(LOGIN_STATUS, loginStatus)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == Constants.RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                loginStatus = 1
                val acct = result.signInAccount as GoogleSignInAccount
                val cb = object: RestClient.LoginCallback {
                    override fun onSuccess(id: Int, accessToken: String) {
                        header.addProfile(ProfileDrawerItem().withEmail(acct.email), 0)
                        //Remove login button, add logout button to nav drawer
                        drawer?.removeItem(LOGIN_DRAWER_ITEM_IDENTIFIER)
                        drawer?.addItemAtPosition(logoutDrawerItem, 0)
                        //add addMasjidDrawerItem
                        drawer?.addItem(addMasjidDrawerItem)
                        showShortToast(getString(R.string.login_success_toast))
                        currentFragment?.onLogin()
                    }

                    override fun onError(t: Throwable) {
                        showShortToast(getString(R.string.login_failure_toast, t.message))
                    }
                }
                RestClient(this).login(acct.idToken!!, acct.email!!, cb)
            }
        }
    }

    fun switchToMasjidsFragment(masjidId: Int, masjidName: String) {
        val fragment = MasjidsFragment.newInstance(masjidId, masjidName)
        switchToFragment(fragment, R.string.fragment_title_masjid_times)
    }

    fun switchToAllMasjidsFragment() = switchToFragment(AllMasjidsFragment(),
            R.string.fragment_title_all_masjids)

    fun switchToAddMasjidFragment() = switchToFragment(AddMasjidFragment(),
            R.string.fragment_title_add_masjid)

    fun switchToHelpFragment() = switchToFragment(HelpFragment(), R.string.fragment_title_help)

    fun switchToChangeMasjidTimesFragment(masjidId: Int, masjidName: String, date: GregorianCalendar) {
        val fragment = ChangeMasjidTimesFragment.newInstance(masjidId, masjidName, date)
        switchToFragment(fragment, R.string.fragment_title_change_salaah_times)
    }

    fun switchToFragment(fragment: Fragment, title: Int) {
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment).commit()
        drawer?.closeDrawer()
        toolbar.title = getString(title)
    }

    fun showShortToast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
