package com.what3words.advanced_sample

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.View.GONE
import android.view.View.VISIBLE
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.what3words.advanced_sample.databinding.ActivityMapsBinding
import com.what3words.advanced_sample.databinding.ActivityMapsBinding.inflate
import com.what3words.javawrapper.request.Coordinates
import com.what3words.javawrapper.response.SuggestionWithCoordinates
import kotlin.math.abs
import kotlin.math.pow

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var binding: ActivityMapsBinding

    private var pickup: LatLng? = null
    private var dropoff: LatLng? = null
    private lateinit var mMap: GoogleMap
    private lateinit var client: FusedLocationProviderClient

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        client = LocationServices.getFusedLocationProviderClient(this)
        binding = inflate(layoutInflater)

        // TODO: REPLACE GOOGLE MAPS API KEY ANDROIDMANIFEST
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // autosuggest using custom picker and custom error/invalid message
        binding.autosuggest.apiKey(BuildConfig.W3W_API_KEY)
            .voiceEnabled(true)
            .returnCoordinates(true)
            .customCorrectionPicker(binding.correctionPicker)
            .onSuggestionSelected(binding.picker, binding.message) { suggestion ->
                if (suggestion != null) populateMarker(suggestion)
            }.onError(binding.message) {
                Log.e("autosuggest", it.message)
            }

        setContentView(binding.root)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == 1 && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            moveInitialCamera()
        }
    }

    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        // request location permission
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        } else {
            moveInitialCamera()
        }

        binding.btnRestart.setOnClickListener {
            mMap.clear()
            binding.autosuggest.hint = getString(R.string.pick_up_hint)
            binding.autosuggest.setText("")
            pickup = null
            dropoff = null
            binding.autosuggest.visibility = VISIBLE
            binding.btnRestart.visibility = GONE
        }
    }

    private fun populateMarker(suggestion: SuggestionWithCoordinates) {
        if (pickup == null) {
            pickup = LatLng(suggestion.coordinates!!.lat, suggestion.coordinates!!.lng)
            mMap.addMarker(MarkerOptions().position(pickup!!).title(suggestion.words))
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngZoom(pickup!!, 14f)
            )
            binding.autosuggest.hint = getString(R.string.drop_off_hint)
            binding.autosuggest.setText("")
            // reset focus to pick-up point
            binding.autosuggest.focus(
                Coordinates(
                    suggestion.coordinates!!.lat,
                    suggestion.coordinates!!.lng
                )
            )
        } else {
            dropoff = LatLng(suggestion.coordinates!!.lat, suggestion.coordinates!!.lng)
            mMap.addMarker(MarkerOptions().position(dropoff!!).title(suggestion.words))
            drawLine(pickup!!, dropoff!!)
            mMap.animateCamera(
                CameraUpdateFactory.newLatLngBounds(
                    LatLngBounds.Builder().include(pickup!!).include(dropoff!!).build(), 50
                )
            )
            binding.autosuggest.visibility = GONE
            binding.btnRestart.visibility = VISIBLE
        }
    }

    //region Location and Map
    @SuppressLint("MissingPermission")
    private fun moveInitialCamera() {
        mMap.isMyLocationEnabled = true
        requestMyGpsLocation {
            // set focus to user current location
            binding.autosuggest.focus(
                Coordinates(
                    it.latitude,
                    it.longitude
                )
            )
            mMap.moveCamera(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(it.latitude, it.longitude),
                    14f
                )
            )
        }
    }

    private fun drawLine(pickup: LatLng, dropoff: LatLng) {
        var cLat: Double = (pickup.latitude + dropoff.latitude) / 2
        var cLon: Double = (pickup.longitude + dropoff.longitude) / 2

        if (abs(pickup.longitude - dropoff.longitude) < 0.0001) {
            cLon -= 0.0195
        } else {
            cLat += 0.0195
        }

        val alLatLng = mutableListOf<LatLng>()
        val tDelta = 1.0 / 50

        var t = 0.0
        while (t <= 1.0) {
            val oneMinusT = 1.0 - t
            val t2 = t.pow(2.0)
            val lon: Double =
                oneMinusT * oneMinusT * pickup.longitude + 2 * oneMinusT * t * cLon + t2 * dropoff.longitude
            val lat: Double =
                oneMinusT * oneMinusT * pickup.latitude + 2 * oneMinusT * t * cLat + t2 * dropoff.latitude
            alLatLng.add(LatLng(lat, lon))
            t += tDelta
        }

        val line = PolylineOptions()
        line.width(8f)
        line.color(Color.RED)
        line.addAll(alLatLng)
        mMap.addPolyline(line)
    }

    private fun requestMyGpsLocation(callback: (location: Location) -> Unit) {
        val request = LocationRequest()
        request.numUpdates = 1
        request.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        val permission = ContextCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        if (permission == PackageManager.PERMISSION_GRANTED) {
            client.requestLocationUpdates(
                request,
                object : LocationCallback() {
                    override fun onLocationResult(locationResult: LocationResult?) {
                        val location = locationResult?.lastLocation
                        if (location != null)
                            callback.invoke(location)
                    }
                },
                null
            )
        }
    }
    //endregion
}
