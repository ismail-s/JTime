package com.ismail_s.jtime.android.fragment

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.gms.location.places.ui.PlacePicker
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.ismail_s.jtime.android.R
import com.ismail_s.jtime.android.RestClient
import com.ismail_s.jtime.android.MainActivity
import kotlinx.android.synthetic.main.fragment_add_masjid.*
import nl.komponents.kovenant.ui.failUi
import nl.komponents.kovenant.ui.successUi
import org.jetbrains.anko.find
import org.jetbrains.anko.support.v4.act
import org.jetbrains.anko.support.v4.ctx
import org.jetbrains.anko.support.v4.toast

/**
* Create a new masjid.
*
* Create a new masjid on the rest server. Only available for logged-in users.
*/
class AddMasjidFragment : BaseFragment(), View.OnClickListener {
    private var savedInstanceState: Bundle? = null
	private var masjidLocation: LatLng? = null
    private val LATITUDE = "latitude"
    private val LONGITUDE = "longitude"
	private val ADD_MASJID_LOCATION_REQUEST = 83

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
            savedInstanceState: Bundle?): View? =
	    inflater?.inflate(R.layout.fragment_add_masjid, container, false)

	override fun onStart() {
		super.onStart()
		add_masjid_submit_button.setOnClickListener(this)
		get_masjid_location_button.setOnClickListener(this)
		masjidLocation?.let { get_masjid_location_button.text = "Change masjid location" }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
		super.onCreate(savedInstanceState)
		this.savedInstanceState = savedInstanceState
        if (savedInstanceState != null) {
            val lat = savedInstanceState.getDouble(LATITUDE, -999.0)
            val lng = savedInstanceState.getDouble(LONGITUDE, -999.0)
			if (lat != -999.0 && lng != -999.0)
                masjidLocation = LatLng(lat, lng)
        }
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
		val lng = masjidLocation?.longitude
        val lat = masjidLocation?.latitude
        lng?.let { savedInstanceState.putDouble(LONGITUDE, it) }
        lat?.let { savedInstanceState.putDouble(LATITUDE, it) }
    }

    override fun onClick(view: View) {
		when(view.id) {
			R.id.add_masjid_submit_button -> {
				val masjidName = masjid_name_textbox.text.toString()
				val masjidLoc = masjidLocation
				//1. Validate fields
				if (masjidLoc == null || masjidName == "") {
					return
				}
				//2. submit form
				RestClient(act).createMasjid(masjidName, masjidLoc.latitude, masjidLoc.longitude) successUi {
					toast("Masjid \"$masjidName\" created")
					//3. switch to AllMasjidsFragment
					(act as? MainActivity)?.switchToAllMasjidsFragment()
				} failUi {
					toast("Error when trying to create masjid: ${it.toString()}")
					add_masjid_submit_button.isEnabled = true
				}
				add_masjid_submit_button.isEnabled = false
			}
			R.id.get_masjid_location_button -> {
				val intent = PlacePicker.IntentBuilder().build(mainAct)
				startActivityForResult(intent, ADD_MASJID_LOCATION_REQUEST)
		    }
        }
    }

	override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (requestCode == ADD_MASJID_LOCATION_REQUEST) {
			val place = data?.let { PlacePicker.getPlace(ctx, it) }
			if (resultCode != Activity.RESULT_OK || place == null) {
				toast("No masjid was selected")
			} else {
				masjidLocation = place.latLng
				get_masjid_location_button.text = "Change masjid location"
            }
		}
    }
}
