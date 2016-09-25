package com.ismail_s.jtime.android

import android.content.Intent
import android.location.Location
import android.os.Bundle
import android.support.v4.app.Fragment
import android.support.v7.app.AppCompatActivity
import android.support.v7.widget.Toolbar
import android.view.Gravity
import android.view.View
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsStatusCodes
import com.ismail_s.jtime.android.fragment.*
import com.ismail_s.jtime.android.pojo.SalaahType
import com.mikepenz.materialdrawer.AccountHeader
import com.mikepenz.materialdrawer.AccountHeaderBuilder
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import com.mikepenz.materialdrawer.model.ProfileDrawerItem
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem
import com.mikepenz.materialdrawer.model.SectionDrawerItem
import nl.komponents.kovenant.android.startKovenant
import nl.komponents.kovenant.android.stopKovenant
import nl.komponents.kovenant.deferred
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.jetbrains.anko.*
import java.util.*

class MainActivity : AppCompatActivity(), AnkoLogger, GoogleApiClient.OnConnectionFailedListener, GoogleApiClient.ConnectionCallbacks {
    var drawer: Drawer? = null
    lateinit var header: AccountHeader
    lateinit var rightDrawer: Drawer
    lateinit var toolbar: Toolbar
    var locationDeferred = deferred<Location, Exception>()
    var location = locationDeferred.promise
    private val locationRequest: LocationRequest by lazy {
        val locRequest = LocationRequest()
        locRequest.interval = 20000
        locRequest.fastestInterval = 10000
        locRequest.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY
        locRequest
    }
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

    private val locationListener = LocationListener {
        /*Location should be updated unless the new location is less than
          50 metres from the last location we had. 50 is an arbitrarily
          chosen small-but-not-too-small number.*/
        val weShouldUpdateTheLocation = if (location.isSuccess())
                location.get().distanceTo(it) >= 50
            else true
        if (weShouldUpdateTheLocation) {
            locationDeferred = deferred<Location, Exception>()
            location = locationDeferred.promise
            locationDeferred resolve it
            currentFragment?.onLocationChanged(it)
        }
    }

    private val logoutDrawerItem: PrimaryDrawerItem
        get() = PrimaryDrawerItem()
                .withName(getString(R.string.drawer_item_logout))
                .withIdentifier(LOGOUT_DRAWER_ITEM_IDENTIFIER)
                .withOnDrawerItemClickListener { view, i, iDrawerItem ->
                    RestClient(this).logout() successUi {
                        loginStatus = 2
                        toast(getString(R.string.logout_success_toast))
                        //Remove logout button, add login button to nav drawer
                        header.removeProfile(0)
                        drawer?.removeItem(LOGOUT_DRAWER_ITEM_IDENTIFIER)
                        drawer?.addItemAtPosition(loginDrawerItem, 0)
                        //remove addMasjidDrawerItem
                        drawer?.removeItem(ADD_MASJID_DRAWER_ITEM_IDENTIFIER)
                        currentFragment?.onLogout()
                    } failUi {
                        toast(getString(R.string.logout_failure_toast, it.message))
                    }
                    true
                }

    private val loginDrawerItem: PrimaryDrawerItem
        get() = PrimaryDrawerItem()
                .withName(getString(R.string.drawer_item_login))
                .withIdentifier(LOGIN_DRAWER_ITEM_IDENTIFIER)
                .withOnDrawerItemClickListener { view, i, iDrawerItem ->
                    val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
                   startActivityForResult(signInIntent, RC_SIGN_IN)
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
        toast(getString(R.string.login_failure_toast, connectionResult.toString()))
    }

    override fun onConnected(connectionHint: Bundle?) {
        if (!location.isDone()) {
            val loc = LocationServices.FusedLocationApi.getLastLocation(googleApiClient)
            if (loc == null) {
                locationDeferred reject Exception(getString(R.string.no_location_exception))
            } else {
                locationDeferred resolve loc
            }
        }
        val builder = LocationSettingsRequest.Builder().addLocationRequest(locationRequest)
        LocationServices.SettingsApi.checkLocationSettings(googleApiClient, builder.build())
                .setResultCallback {
                    when(it.status.statusCode) {
                        LocationSettingsStatusCodes.SUCCESS ->{
                            //We can request the location now
                            startLocationUpdates()
                        }
                        LocationSettingsStatusCodes.RESOLUTION_REQUIRED -> {
                            //Show dialog prompting user to change location settings
                            it.status.startResolutionForResult(this, RC_CHECK_SETTINGS)
                            longToast("Please change your location settings in order to see nearby salaah times.")
                        }
                        LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE -> {
                            //Show a toast saying we can't get the location at all
                            toast(getString(R.string.no_location_exception))
                        }
                    }
                }
    }

    private fun startLocationUpdates() {
        LocationServices.FusedLocationApi.requestLocationUpdates(googleApiClient, locationRequest,
            locationListener)
    }

    override fun onConnectionSuspended(cause: Int) {}

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        startKovenant()
        toolbar = find<Toolbar>(R.id.toolbar)
        toolbar.title = savedInstanceState?.getCharSequence(TOOLBAR_TITLE, "") ?: ""
        setSupportActionBar(toolbar)
        setUpGoogleApiClient()
        setUpNavDrawer(savedInstanceState)
        setUpRightDrawer(savedInstanceState)

        val onLoggedOut = {
            loginStatus = 2
            //Set button to be login, create drawer
            drawer?.addItemAtPosition(loginDrawerItem, 0)
        }
        val onLoggedIn = {
            loginStatus = 1
            //Set button to be logout, create drawer
            drawer?.addItemAtPosition(logoutDrawerItem, 0)
            val email: String = SharedPreferencesWrapper(this@MainActivity).email
            header.addProfile(ProfileDrawerItem().withEmail(email), 0)
            //add addMasjidDrawerItem
            drawer?.addItem(addMasjidDrawerItem)
        }
        val loggedInStatus = savedInstanceState?.getInt(LOGIN_STATUS, 0) ?: 0
        when (loggedInStatus) {
            1 -> onLoggedIn()
            2 -> onLoggedOut()
            else -> RestClient(this).areWeStillSignedInOnServer()
                .successUi {onLoggedIn()}
                .failUi {onLoggedOut()}
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
            switchToHomeFragment()
        }
    }

    private fun setUpGoogleApiClient() {
        if (googleApiClient != null)
            return
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

    private fun setUpNavDrawer(savedInstance: Bundle?) {
        val drawerListener = object: Drawer.OnDrawerListener {
            override fun onDrawerOpened(drawerView: View) {
                currentFragment?.onDrawerOpened(drawerView)
            }

            override fun onDrawerClosed(drawerView: View) {
                if (!rightDrawer.isDrawerOpen)
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
                .addDrawerItems(PrimaryDrawerItem()
                        .withName(getString(R.string.drawer_item_home))
                        .withOnDrawerItemClickListener { view, position, drawerItem ->
                            switchToHomeFragment()
                            true
                        }, PrimaryDrawerItem()
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

    private fun setUpRightDrawer(savedInstance: Bundle?) {
        val drawerItems = SalaahType.values().filter { it != SalaahType.MAGRIB }.map {
                SecondaryDrawerItem()
                    .withName(it.toString(ctx))
                    .withOnDrawerItemClickListener { view, position, drawerItem ->
                        switchToNearbyTimesFragment(it)
                        true
                    }
            }.toTypedArray()
        val drawerListener = object: Drawer.OnDrawerListener {
            override fun onDrawerOpened(drawerView: View) {
                if (drawer?.isDrawerOpen == true) {
                    drawer?.closeDrawer()
                } else {
                    currentFragment?.onDrawerOpened(drawerView)
                }
            }

            override fun onDrawerClosed(drawerView: View) {
                currentFragment?.onDrawerClosed(drawerView)
            }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) {}
        }
        rightDrawer = DrawerBuilder()
                .withActivity(this)
                .withOnDrawerListener(drawerListener)
                .withDrawerGravity(Gravity.END)
                .withSavedInstance(savedInstance)
                .addDrawerItems(
                        SectionDrawerItem()
                            .withName(getString(R.string.drawer_item_nearby_times_header)),
                        *drawerItems)
                .build()
    }

    override fun onStart() {
        googleApiClient?.connect()
        super.onStart()
    }

    override fun onStop() {
        googleApiClient?.disconnect()
        super.onStop()
    }

    override fun onPause() {
        super.onPause()
        if (googleApiClient?.isConnected == true)
            LocationServices.FusedLocationApi.removeLocationUpdates(googleApiClient, locationListener)
    }

    override fun onResume() {
        super.onResume()
        if (googleApiClient?.isConnected == true)
            startLocationUpdates()
    }

    override fun onDestroy() {
        stopKovenant()
        googleApiClient = null
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
        when (requestCode) {
            RC_SIGN_IN -> {
                val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
                if (result.isSuccess) {
                    loginStatus = 1
                    val acct = result.signInAccount as GoogleSignInAccount
                    RestClient(this).login(acct.idToken!!, acct.email!!) successUi {
                        header.addProfile(ProfileDrawerItem().withEmail(acct.email), 0)
                        //Remove login button, add logout button to nav drawer
                        drawer?.removeItem(LOGIN_DRAWER_ITEM_IDENTIFIER)
                        drawer?.addItemAtPosition(logoutDrawerItem, 0)
                        //add addMasjidDrawerItem
                        drawer?.addItem(addMasjidDrawerItem)
                        toast(getString(R.string.login_success_toast))
                        currentFragment?.onLogin()
                    } failUi {
                        toast(getString(R.string.login_failure_toast, it.message))
                    }
                }
            }
            RC_CHECK_SETTINGS -> {
                when (resultCode) {
                    RESULT_OK -> {
                        //Location settings were successfully changed
                        toast("Location settings changed. Trying to get your location.")
                        startLocationUpdates()
                    }
                    RESULT_CANCELED -> {
                        //Location settings were not changed
                        toast("Location settings weren't changed. Some features in the app won't work fully.")
                    }
                }
            }
        }
    }

    fun switchToMasjidsFragment(masjidId: Int, masjidName: String) {
        val fragment = MasjidsFragment.newInstance(masjidId, masjidName)
        switchToFragment(fragment, R.string.fragment_title_masjid_times)
    }

    fun switchToAllMasjidsFragment() = switchToFragment(AllMasjidsFragment(),
            R.string.fragment_title_all_masjids)

    fun switchToHomeFragment() = switchToFragment(HomeFragment(),
            R.string.fragment_title_home)

    fun switchToAddMasjidFragment() = switchToFragment(AddMasjidFragment(),
            R.string.fragment_title_add_masjid)

    fun switchToHelpFragment() = switchToFragment(HelpFragment(), R.string.fragment_title_help)

    fun switchToChangeMasjidTimesFragment(masjidId: Int, masjidName: String, date: GregorianCalendar) {
        val fragment = ChangeMasjidTimesFragment.newInstance(masjidId, masjidName, date)
        switchToFragment(fragment, R.string.fragment_title_change_salaah_times)
    }

    fun switchToNearbyTimesFragment(salaahType: SalaahType) {
        val fragment = NearbyTimesFragment.newInstance(salaahType)
        switchToFragment(fragment, R.string.fragment_title_nearby_times)
    }

    fun switchToFragment(fragment: Fragment, title: Int) {
        debug("Switching to fragment $fragment")
        supportFragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment).commit()
        drawer?.closeDrawer()
        rightDrawer.closeDrawer()
        toolbar.title = getString(title)
    }

    companion object {
        private val RC_SIGN_IN = 9001
        private val RC_CHECK_SETTINGS = 9002
        var googleApiClient: GoogleApiClient? = null
    }
}
