package com.what3words.samples.multiple.data

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.what3words.components.compose.maps.models.W3WLocationSource
import com.what3words.components.compose.maps.state.LocationStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LocationSourceImpl(private val context: Context) : W3WLocationSource {

    private val locationManager =
        context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val _locationStatus = MutableStateFlow(LocationStatus.INACTIVE)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    init {
        monitorGpsStatus()
    }

    override suspend fun fetchLocation(): Location {
        // Directly fetch the location without updating the status to SEARCHING
        if (!isLocationEnabled() || (ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
                    )
        ) {
            _locationStatus.value = LocationStatus.INACTIVE
        }

        return suspendCoroutine { cont ->
            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            _locationStatus.value = LocationStatus.ACTIVE
                            cont.resume(task.result)
                        }
                    }
            } catch (e: Exception) {
                _locationStatus.value = LocationStatus.INACTIVE
                cont.resumeWithException(e)
            }
        }
    }

    private fun monitorGpsStatus() {
        // Observe GPS status and update the state to SEARCHING when enabled
        gpsStatusFlow()
            .distinctUntilChanged()
            .onEach { isGpsEnabled ->
                if (isGpsEnabled) {
                    _locationStatus.value = LocationStatus.SEARCHING
                    fetchLocation()
                } else {
                    _locationStatus.value = LocationStatus.INACTIVE
                }
            }.launchIn(CoroutineScope(Dispatchers.IO))
    }

    private fun gpsStatusFlow(): Flow<Boolean> = callbackFlow {
        val intentFilter = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(isLocationEnabled())
            }
        }
        context.registerReceiver(receiver, intentFilter)
        trySend(isLocationEnabled()) // Initial emit
        awaitClose { context.unregisterReceiver(receiver) }
    }

    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    override val locationStatus: StateFlow<LocationStatus>
        get() = _locationStatus
}