package com.koenig.chatapp.ui.mapManager

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import android.net.Uri
import android.os.Looper
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.location.*
import com.google.android.gms.maps.GoogleMap
import com.koenig.chatapp.firebase.FirebaseDBManager
import com.koenig.chatapp.firebase.FirebaseImageManager
import com.koenig.chatapp.models.UserModel

@SuppressLint("MissingPermission")
class MapsViewModel (application: Application) : AndroidViewModel(application) {

    lateinit var map: GoogleMap
    var currentLocation = MutableLiveData<Location>()
    private var locationClient: FusedLocationProviderClient

    private val userImageUri = MutableLiveData<Uri>()
    val observableUserImage: LiveData<Uri>
        get() = userImageUri

    private val userGroupImageUri = MutableLiveData<List<Uri>>()
    val observableGroupImage: LiveData<List<Uri>>
        get() = userGroupImageUri

    private val usersWithLocations = MutableLiveData<List<UserModel>>()

    val observableUsersWithLocation: LiveData<List<UserModel>>
        get() = usersWithLocations

    val isMapEnabled = MutableLiveData<Boolean>()

    val hasLocationPermission = MutableLiveData<Boolean>()

    var observableMap: LiveData<Boolean>
        get() = isMapEnabled
        set(value) {isMapEnabled.value = value.value}

    var observableLocationPermission: LiveData<Boolean>
        get() = hasLocationPermission
        set(value) {hasLocationPermission.value = value.value}


    private val locationRequest = LocationRequest.create().apply {
        interval = 10000
        fastestInterval = 5000
        priority = Priority.PRIORITY_HIGH_ACCURACY
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            currentLocation.value = locationResult.locations.last()
        }
    }

    init {
        locationClient = LocationServices.getFusedLocationProviderClient(application)
        locationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    fun updateCurrentLocation()
    {
        if(locationClient.lastLocation.isSuccessful)
        {
            locationClient.lastLocation.addOnSuccessListener { location: Location? -> currentLocation.value = location!! }
        }
    }

    fun setMapEnabled(userId: String, isMapEnabled: Boolean)
    {
        try {
            FirebaseDBManager.setMapEnabled(userId, isMapEnabled)
        }
        catch (e: Exception)
        {
            // TODO
        }
    }

    fun getIsMapEnabled(userId: String)
    {
        FirebaseDBManager.isMapEnabled(userId, isMapEnabled)
    }

    fun setUserLocation(userId: String, latitude: Double, longitude: Double)
    {
        FirebaseDBManager.setUsersLocation(userId, latitude, longitude)
    }

    fun getHasLocationPermission(userId: String)
    {
        FirebaseDBManager.hasLocationPermission(userId, hasLocationPermission)
    }

    fun setLocationPermission(userId: String, hasLocationPermission: Boolean)
    {
        try {
            FirebaseDBManager.setHasLocationPermission(userId, hasLocationPermission)
        }
        catch (e: Exception)
        {
            // TODO
        }
    }

    fun getLocationForUsers(userIds: ArrayList<String>)
    {
        try {
            FirebaseDBManager.getUsersWithLocation(userIds, usersWithLocations)
        }
        catch (e: Exception)
        {
            // TODO
        }
    }

    fun getImageUri(userId: String)
    {
        FirebaseImageManager.getUserImage(userId, userImageUri)
    }

    fun groupUserImages(userIds: ArrayList<String>)
    {
        FirebaseImageManager.getUserGroupImages(userIds, userGroupImageUri)
    }
}