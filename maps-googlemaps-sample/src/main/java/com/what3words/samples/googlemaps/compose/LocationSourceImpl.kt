package com.what3words.samples.googlemaps.compose

import android.Manifest
import android.content.Context
import android.location.Location
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.what3words.components.compose.maps.models.W3WLocationSource

class LocationSourceImpl(private val context: Context): W3WLocationSource {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    override fun fetchLocation(
        onLocationFetched: (Location) -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                location?.let {
                    onLocationFetched(it)
                }
            }
        }
    }
}