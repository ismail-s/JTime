package com.ismail_s.jtime.android.activity

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.ismail_s.jtime.android.R

class AddMasjidFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener, View.OnClickListener {
    private var current_marker: Marker? = null
    private var googleMap: GoogleMap? = null
    lateinit private var masjidNameTextbox: EditText

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_add_masjid, container, false)
        val mapFragment = MapFragment.newInstance()
        mapFragment.getMapAsync(this)
        childFragmentManager.beginTransaction()
            .add(R.id.map_container, mapFragment).commit()
        masjidNameTextbox = rootView.findViewById(R.id.masjid_name_textbox) as EditText
        val submitButton = rootView.findViewById(R.id.add_masjid_submit_button) as Button
        submitButton.setOnClickListener(this)
        return rootView
    }

    override fun onClick(view: View) {
        if (view.id == R.id.add_masjid_submit_button) {
            //1. Validate fields
            if (current_marker == null || masjidNameTextbox.text.toString() == "") {
                return
            }
            //2. TODO-submit form
            //3. switch to AllMasjidsFragment
            (activity as MainActivity).switchToAllMasjidsFragment()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        //TODO-add padding to take into account submit button
        googleMap.setOnMapLongClickListener(this)
        this.googleMap = googleMap
    }

    override fun onMapLongClick(point: LatLng) {
        current_marker?.remove()
        val marker_options = MarkerOptions().position(point)
            .title("Masjid location").draggable(true)
        current_marker = googleMap?.addMarker(marker_options)
    }
}
