package com.what3words.samples.googlemaps.compose

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.database.ContentObserver
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationServices
import com.what3words.components.compose.maps.models.W3WLocationSource
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.flowOf
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

@RequiresApi(Build.VERSION_CODES.P)
class LocationSourceImpl(private val context: Context): W3WLocationSource {
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    private val _isActive = MutableStateFlow(false)
    private val locationReceiver = object : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == LocationManager.MODE_CHANGED_ACTION) {
                _isActive.value = (context.getSystemService(Context.LOCATION_SERVICE) as LocationManager).isLocationEnabled
            }
        }
    }

    init {
        val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        _isActive.value = locationManager.isLocationEnabled

        // Register broadcast receiver
        val filter = IntentFilter(LocationManager.MODE_CHANGED_ACTION)
        context.registerReceiver(locationReceiver, filter)
    }

    override suspend fun fetchLocation(): Location {
        return if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == android.content.pm.PackageManager.PERMISSION_GRANTED) {
            suspendCoroutine { cont ->
                fusedLocationClient.lastLocation.addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        cont.resume(task.result)
                    }
                }
            }
        } else {
            Location("")
        }
    }

    override val isActive: StateFlow<Boolean>
        get() = _isActive
}