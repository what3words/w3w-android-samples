package com.what3words.samples.googlemaps.compose

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
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
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine

class LocationSourceImpl(private val context: Context) : W3WLocationSource {

    private val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
    private val _locationStatus = MutableStateFlow(LocationStatus.INACTIVE)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)
    private lateinit var receiver: BroadcastReceiver

    private val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 5000L)
        .setMinUpdateIntervalMillis(3000L)
        .build()

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.lastLocation?.let { _ ->
                _locationStatus.value = LocationStatus.ACTIVE
            }
        }
    }

    init {
        monitorGpsStatus()
    }

    override val locationStatus: StateFlow<LocationStatus>
        get() = _locationStatus.asStateFlow()

    override suspend fun fetchLocation(): Location {
        // Directly fetch the location without updating the status to SEARCHING
        if (!isLocationEnabled() || !isLocationPermissionGranted()) {
            _locationStatus.value = LocationStatus.INACTIVE
        }

        try {
            return suspendCoroutine { cont ->
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null).addOnCompleteListener { task ->
                    if (task.isSuccessful && task.result != null) {
                        _locationStatus.value = LocationStatus.ACTIVE
                        cont.resume(task.result)
                    }
                }
            }
        } catch (e: Exception) {
            _locationStatus.value = LocationStatus.INACTIVE
            throw e
        }
    }

    private fun monitorGpsStatus() {
        // Observe GPS status and update the state to SEARCHING when enabled
        gpsStatusFlow()
            .distinctUntilChanged()
            .onEach { isGpsEnabled ->
                if (isGpsEnabled) {
                    _locationStatus.value = LocationStatus.SEARCHING
                    startLocationUpdates()
                } else {
                    _locationStatus.value = LocationStatus.INACTIVE
                    stopLocationUpdates()
                }
            }.launchIn(CoroutineScope(Dispatchers.IO))
    }

    private fun startLocationUpdates() {
        if (isLocationPermissionGranted()) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private fun stopLocationUpdates() {
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }

    private fun gpsStatusFlow(): Flow<Boolean> = callbackFlow {
        val intentFilter = IntentFilter().apply {
            addAction(LocationManager.PROVIDERS_CHANGED_ACTION)
        }
        receiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                trySend(isLocationEnabled())
            }
        }
        context.registerReceiver(receiver, intentFilter)
        trySend(isLocationEnabled()) // Initial emit
        awaitClose {
            context.unregisterReceiver(receiver)
        }
    }

    private fun isLocationPermissionGranted(): Boolean {
        return ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun isLocationEnabled(): Boolean {
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) ||
                locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)
    }

    fun onDestroy() {
        if (this::receiver.isInitialized) {
            context.unregisterReceiver(receiver)
        }
        stopLocationUpdates()
    }
}