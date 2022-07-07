package com.koenig.chatapp.utils

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.util.Log
import androidx.core.app.ActivityCompat

const val REQUEST_PERMISSIONS_REQUEST_CODE = 34
const val REQUEST_PERMISSIONS_REQUEST_CODE_AGAIN = 35

fun checkLocationPermissions(activity: Activity) : Boolean {
    return if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
        true
    }
    else {
        ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_PERMISSIONS_REQUEST_CODE)
        false
    }
}

fun isPermissionGranted(code: Int, grantResults: IntArray): Boolean {
    var permissionGranted = false
    if (code == REQUEST_PERMISSIONS_REQUEST_CODE || code == REQUEST_PERMISSIONS_REQUEST_CODE_AGAIN) {
        when {
            grantResults.isEmpty() -> Log.d("Info", "User interaction was cancelled.")
            (grantResults[0] == PackageManager.PERMISSION_GRANTED) -> {
                permissionGranted = true
                Log.d("Info", "Permission Granted.")
            }
            else -> Log.d("Info", "Permission Denied.")
        }
    }
    return permissionGranted
}