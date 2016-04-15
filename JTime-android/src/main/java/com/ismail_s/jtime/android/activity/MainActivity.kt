package com.ismail_s.jtime.android.activity

import android.app.Fragment
import android.content.Intent
import android.os.Bundle
import android.support.v4.app.FragmentActivity
import android.widget.Toast
import com.google.android.gms.auth.api.Auth
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.ismail_s.jtime.android.R
import com.mikepenz.materialdrawer.Drawer
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem

class MainActivity : FragmentActivity(), GoogleApiClient.OnConnectionFailedListener {
    var drawer: Drawer? = null
    var googleApiClient: GoogleApiClient? = null

    /**
     * Called when Sign in with Google fails
     */
    override fun onConnectionFailed(connectionResult: ConnectionResult) {
        val string = "Failed to login, with error: ${connectionResult.toString()}"
        Toast.makeText(this, string, Toast.LENGTH_SHORT)
        .show()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build()
        googleApiClient = GoogleApiClient.Builder(this)
                .enableAutoManage(this, this)
                .addApi(Auth.GOOGLE_SIGN_IN_API, gso)
                .build()

        drawer = DrawerBuilder()
                .withActivity(this)
                .addDrawerItems(PrimaryDrawerItem()
                        .withName("Login")
                        .withOnDrawerItemClickListener { view, i, iDrawerItem ->
                            // Do login
                            val signInIntent = Auth.GoogleSignInApi.getSignInIntent(googleApiClient)
                            startActivityForResult(signInIntent, Constants.RC_SIGN_IN)
                            true
                        }, PrimaryDrawerItem()
                        .withName("All Masjids")
                        .withOnDrawerItemClickListener { view, position, drawerItem ->
                            switchToAllMasjidsFragment()
                            drawer?.closeDrawer()
                            true
                        })
                .build()

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

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent) {
        if (requestCode == Constants.RC_SIGN_IN) {
            val result = Auth.GoogleSignInApi.getSignInResultFromIntent(data)
            if (result.isSuccess) {
                val acct = result.signInAccount
                Toast.makeText(this, "email: ${acct?.email}", Toast.LENGTH_SHORT).show()
                //TODO-login on server
            }
        }
    }

    fun switchToMasjidsFragment(masjidId: Int, masjidName: String) {
        val fragment = MasjidsFragment.newInstance(masjidId, masjidName)
        switchToFragment(fragment)
    }

    fun switchToAllMasjidsFragment() = switchToFragment(AllMasjidsFragment())

    fun switchToFragment(fragment: Fragment) {
        fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment).commit()
    }
}
