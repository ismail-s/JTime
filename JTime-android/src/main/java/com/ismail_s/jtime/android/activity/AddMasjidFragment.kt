package com.ismail_s.jtime.android.activity

import android.app.Fragment
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.OnMapReadyCallback
import com.ismail_s.jtime.android.R

class AddMasjidFragment : Fragment(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {
    private var current_marker: Marker? = null
    private var googleMap: GoogleMap? = null

    override fun onCreateView(inflater: LayoutInflater?, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val rootView = inflater!!.inflate(R.layout.fragment_add_masjid, container, false)
        val mapFragment = MapFragment.newInstance()
        mapFragment.getMapAsync(this)
        childFragmentManager.beginTransaction()
            .add(R.id.map_container, mapFragment).commit()
        return rootView
    }

    override fun onMapReady(googleMap: GoogleMap) {
        //TODO-add padding to take into account submit button
        googleMap.setOnMapLongClickListener(this)
        this.googleMap = googleMap
    }

    override fun onMapLongClick(point: LatLng) {
        current_marker?.remove()
        val marker_options = MarkerOptions().position(point).title("Masjid location")
        current_marker = googleMap?.addMarker(marker_options)
    }
}
