package com.ismail_s.jtime.android.activity

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
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

class MainActivity : FragmentActivity(), GoogleApiClient.OnConnectionFailedListener {
     var drawer: Drawer? = null
    lateinit var header: AccountHeader
    lateinit var googleApiClient: GoogleApiClient
    val currentFragment: BaseFragment
        get() = fragmentManager.findFragmentById(R.id.fragment_container) as BaseFragment
    private val LOGIN_DRAWER_ITEM_IDENTIFIER: Long = 546
    private val LOGOUT_DRAWER_ITEM_IDENTIFIER: Long = 232
    private val ADD_MASJID_DRAWER_ITEM_IDENTIFIER: Long = 785

    private val logoutDrawerItem = PrimaryDrawerItem()
            .withName("Logout")
            .withIdentifier(LOGOUT_DRAWER_ITEM_IDENTIFIER)
            .withOnDrawerItemClickListener { view, i, iDrawerItem ->
                val cb = object : RestClient.LogoutCallback {
                    override fun onSuccess() {
                        showShortToast("Have successfully logged out")
                        //Remove logout button, add login button to nav drawer
                        header.removeProfile(0)
                        drawer?.removeItem(LOGOUT_DRAWER_ITEM_IDENTIFIER)
                        drawer?.addItemAtPosition(loginDrawerItem, 0)
                        //remove addMasjidDrawerItem
                        drawer?.removeItem(ADD_MASJID_DRAWER_ITEM_IDENTIFIER)
                    }

                    override fun onError(t: Throwable) = showShortToast("Logout unsuccessful: ${t.message}")
                }
                RestClient(this).logout(cb)
                true
            }

    private val loginDrawerItem = PrimaryDrawerItem()
            .withName("Login")
            .withIdentifier(LOGIN_DRAWER_ITEM_IDENTIFIER)
            .withOnDrawerItemClickListener { view, i, iDrawerItem ->
                val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
                startActivityForResult(signInIntent, Constants.RC_SIGN_IN)
                true
            }

    private val addMasjidDrawerItem = PrimaryDrawerItem()
            .withName("Add Masjid")
            .withIdentifier(ADD_MASJID_DRAWER_ITEM_IDENTIFIER)
            .withOnDrawerItemClickListener { view, i, iDrawerItem ->
                switchToAddMasjidFragment()
                true
            }

    /**
     * Called when Sign in with Google fails
     */
    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        showShortToast("Failed to login, with error: ${connectionResult.toString()}")
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setUpGoogleApiClient()

        val cb = object: RestClient.SignedinCallback {
            override fun onLoggedOut() {
                showShortToast("Not logged in atm")
                //Set button to be login, create drawer
                setUpNavDrawer(loginDrawerItem)
            }

            override fun onLoggedIn() {
                //Set button to be logout, create drawer
                setUpNavDrawer(logoutDrawerItem)
                val email: String = SharedPreferencesWrapper(this@MainActivity).email
                header.addProfile(ProfileDrawerItem().withEmail(email), 0)
                //add addMasjidDrawerItem
                drawer?.addItem(addMasjidDrawerItem)

            }
        }
        RestClient(this).checkIfStillSignedInOnServer(cb)

        // Check that the activity is using the layout version with
        // the fragment_container FrameLayout
        if (findViewById(R.id.fragment_container) != null) {
            // However, if we're being restored from a previous state,
            // then we don't need to do anything and should return or else
            // we could end up with overlapping fragments.
            if (savedInstanceState != null) {
                return;
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
                .build()
    }

    private fun setUpNavDrawer(loginOutButton: PrimaryDrawerItem) {
        header = AccountHeaderBuilder().withActivity(this)
                .withProfileImagesVisible(false).withCompactStyle(true)
                .build()
        drawer = DrawerBuilder()
                .withActivity(this)
                .withAccountHeader(header)
                .addDrawerItems(loginOutButton, PrimaryDrawerItem()
                        .withName("All Masjids")
                        .withOnDrawerItemClickListener { view, position, drawerItem ->
                            switchToAllMasjidsFragment()
                            true
                        })
                .build()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == Constants.RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                val acct = result.signInAccount as GoogleSignInAccount
                showShortToast("email: ${acct.email}")
                val cb = object: RestClient.LoginCallback {
                    override fun onSuccess(id: Int, accessToken: String) {
                        header.addProfile(ProfileDrawerItem().withEmail(acct.email), 0)
                        //Remove login button, add logout button to nav drawer
                        drawer?.removeItem(LOGIN_DRAWER_ITEM_IDENTIFIER)
                        drawer?.addItemAtPosition(logoutDrawerItem, 0)
                        //add addMasjidDrawerItem
                        drawer?.addItem(addMasjidDrawerItem)
                    }

                    override fun onError(t: Throwable) {
                        showShortToast("Error when trying to login on server: ${t.message}")
                    }
                }
                RestClient(this).login(acct.idToken!!, acct.email!!, cb)
            }
        }
    }

    fun switchToMasjidsFragment(masjidId: Int, masjidName: String) {
        val fragment = MasjidsFragment.newInstance(masjidId, masjidName)
        switchToFragment(fragment)
    }

    fun switchToAllMasjidsFragment() = switchToFragment(AllMasjidsFragment())

    fun switchToAddMasjidFragment() = switchToFragment(AddMasjidFragment())

    fun switchToChangeMasjidTimesFragment(masjidId: Int, masjidName: String, date: GregorianCalendar) {
        val fragment = ChangeMasjidTimesFragment.newInstance(masjidId, masjidName, date)
        switchToFragment(fragment)
    }

    fun switchToFragment(fragment: Fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment).commit()
        drawer?.closeDrawer()

    }

    fun showShortToast(s: String) = Toast.makeText(this, s, Toast.LENGTH_SHORT).show()
}
