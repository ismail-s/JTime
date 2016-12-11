package com.ismail_s.jtime.android.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
import org.jetbrains.anko.support.v4.toast

/**
* Create a new masjid.
*
* Create a new masjid on the rest server. Only available for logged-in users.
*/
class AddMasjidFragment : BaseFragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener, View.OnClickListener {
    private var current_marker: Marker? = null
    private var googleMap: GoogleMap? = null
    private var savedInstanceState: Bundle? = null
    private val LATITUDE = "latitude"
    private val LONGITUDE = "longitude"

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_add_masjid, container, false)
        this.savedInstanceState = savedInstanceState
        val mapFragment = SupportMapFragment.newInstance()
        mapFragment.getMapAsync(this)
        childFragmentManager.beginTransaction()
            .add(R.id.map_container, mapFragment).commit()
        return rootView
    }

    override fun onSaveInstanceState(savedInstanceState: Bundle) {
        super.onSaveInstanceState(savedInstanceState)
        if (current_marker != null) {
            val lng = current_marker?.position?.longitude as Double
            val lat = current_marker?.position?.latitude as Double
            savedInstanceState.putDouble(LONGITUDE, lng)
            savedInstanceState.putDouble(LATITUDE, lat)
        }
	override fun onStart() {
		super.onStart()
		add_masjid_submit_button.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        if (view.id == R.id.add_masjid_submit_button) {
            val masjidName = masjid_name_textbox.text.toString()
            //1. Validate fields
            if (current_marker == null || masjidName == "") {
                return
            }
            //2. submit form
            val location = (current_marker as Marker).position
            RestClient(act).createMasjid(masjidName, location.latitude, location.longitude) successUi {
                toast("Masjid \"$masjidName\" created")
                //3. switch to AllMasjidsFragment
                (act as? MainActivity)?.switchToAllMasjidsFragment()
            } failUi {
                toast("Error when trying to create masjid: ${it.toString()}")
                add_masjid_submit_button.isEnabled = true
            }
            add_masjid_submit_button.isEnabled = false
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setOnMapLongClickListener(this)
        val submitButtonHeight = add_masjid_submit_button.height
        val mapHelpLabelHeight = map_help_label.height
        googleMap.setPadding(0, mapHelpLabelHeight, 0, submitButtonHeight)
        this.googleMap = googleMap
        if (savedInstanceState != null) {
            val lat = savedInstanceState?.getDouble(LATITUDE) as Double
            val lng = savedInstanceState?.getDouble(LONGITUDE) as Double
            val point = LatLng(lat, lng)
            addMarker(point)
        }
    }

    override fun onMapLongClick(point: LatLng) {
        addMarker(point)
    }

    /**
    * Add a marker to the map, removing a previous marker if there was one.
    */
    fun addMarker(point: LatLng) {
        current_marker?.remove()
        val marker_options = MarkerOptions().position(point)
            .title(getString(R.string.add_masjid_marker_title)).draggable(true)
        current_marker = googleMap?.addMarker(marker_options)
    }
}
