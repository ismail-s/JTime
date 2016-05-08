package com.ismail_s.jtime.android.activity

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.ismail_s.jtime.android.R
import com.ismail_s.jtime.android.RestClient

class AddMasjidFragment : BaseFragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener, View.OnClickListener {
    private var current_marker: Marker? = null
    private var googleMap: GoogleMap? = null
    lateinit private var masjidNameTextbox: EditText
    lateinit private var submitButton: Button
    lateinit private var mapHelpLabel: TextView

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_add_masjid, container, false)
        val mapFragment = MapFragment.newInstance()
        mapFragment.getMapAsync(this)
        childFragmentManager.beginTransaction()
            .add(R.id.map_container, mapFragment).commit()
        masjidNameTextbox = rootView.findViewById(R.id.masjid_name_textbox) as EditText
        mapHelpLabel = rootView.findViewById(R.id.map_help_label) as TextView
        submitButton = rootView.findViewById(R.id.add_masjid_submit_button) as Button
        submitButton.setOnClickListener(this)
        return rootView
    }

    override fun onClick(view: View) {
        if (view.id == R.id.add_masjid_submit_button) {
            val masjidName = masjidNameTextbox.text.toString()
            //1. Validate fields
            if (current_marker == null || masjidName == "") {
                return
            }
            //2. submit form
            val showToast = {s: String -> (activity as MainActivity).showShortToast(s)}
            val cb = object: RestClient.MasjidCreatedCallback {
                override fun onSuccess() {
                    showToast("Masjid \"$masjidName\" created")
                    //3. switch to AllMasjidsFragment
                    (activity as MainActivity).switchToAllMasjidsFragment()
                }

                override fun onError(t: Throwable) {
                    showToast("Error when trying to create masjid: ${t.toString()}")
                    submitButton.isEnabled = true
                }
            }
            val location = (current_marker as Marker).position
            RestClient(activity).createMasjid(masjidName, location.latitude, location.longitude, cb)
            submitButton.isEnabled = false
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        googleMap.setOnMapLongClickListener(this)
        val submitButtonHeight = submitButton.height
        val mapHelpLabelHeight = mapHelpLabel.height
        googleMap.setPadding(0, mapHelpLabelHeight, 0, submitButtonHeight)
        this.googleMap = googleMap
    }

    override fun onMapLongClick(point: LatLng) {
        current_marker?.remove()
        val marker_options = MarkerOptions().position(point)
            .title("Masjid location").draggable(true)
        current_marker = googleMap?.addMarker(marker_options)
    }
}
