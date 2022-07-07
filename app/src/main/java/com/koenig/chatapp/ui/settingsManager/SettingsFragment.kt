package com.koenig.chatapp.ui.settingsManager

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import com.koenig.chatapp.databinding.FragmentSettingsBinding
import com.koenig.chatapp.ui.auth.LoggedInViewModel
import com.koenig.chatapp.ui.mapManager.MapsViewModel

class SettingsFragment : Fragment() {

    private var _fragBinding: FragmentSettingsBinding? = null
    private val fragBinding get() = _fragBinding!!
    private  val settingsViewModel: SettingsViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private val mapsViewModel: MapsViewModel by activityViewModels()

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setHasOptionsMenu(false)

    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        // BINDING
        _fragBinding = FragmentSettingsBinding.inflate(inflater, container, false)
        val root = fragBinding.root


        // OBSERVERS
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner){
            setUserId(it.uid)
            mapsViewModel.getHasLocationPermission(it.uid)
            mapsViewModel.getIsMapEnabled(it.uid)
            settingsViewModel.getNotificationEnabled(it.uid)
        }

        mapsViewModel.isMapEnabled.observe(viewLifecycleOwner)
        {
            setMapSwitchState(it)
        }

        mapsViewModel.hasLocationPermission.observe(viewLifecycleOwner)
        {
            setLocationPermission(it)
        }

        settingsViewModel.observableNotifications.observe(viewLifecycleOwner)
        {
            setPushUpNotification(it)
            fragBinding.progressBar.visibility = View.GONE
        }

        // LISTENERS
        listenForMapSwitchChanges()
        listenForPushUpNotificationChanges()


        return root
    }


    // FUNCTIONS
    @SuppressLint("SetTextI18n")
    private fun setUserId(userId: String)
    {
        fragBinding.textUserId.text = "UserId: $userId"

    }

    @SuppressLint("SetTextI18n")
    private fun setLocationPermission(hasLocationPermission: Boolean)
    {
        if(hasLocationPermission)
        {
            fragBinding.buttonLocationPermission.text = "ALLOWED"
        }
        else
        {
            fragBinding.buttonLocationPermission.text = "DISABLED"
        }
    }

    private fun setMapSwitchState(isMapEnabled: Boolean)
    {
        fragBinding.switchMap.isChecked = isMapEnabled

        // Cannot activate map for other users when location permission is missing
        if (!mapsViewModel.hasLocationPermission.value!!)
        {
            fragBinding.switchMap.isClickable = false
            fragBinding.MapContainer.setOnClickListener{
                Toast.makeText(requireContext(), "Cannot activate map due missing Location Permission", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setPushUpNotification(areNotificationsEnabled: Boolean)
    {
        fragBinding.switchNotifications.isChecked = areNotificationsEnabled
    }

    private fun listenForMapSwitchChanges()
    {
        fragBinding.switchMap.setOnCheckedChangeListener { _, p1 ->
            // When location permission is disabled => cannot set the map active for other users
            if (mapsViewModel.hasLocationPermission.value!!) {
                if (p1) {
                    // SET IS MAP ENABLED TO TRUE
                    mapsViewModel.setMapEnabled(
                        loggedInViewModel.liveFirebaseUser.value!!.uid,
                        true
                    )
                } else {
                    // SET IS MAP ENABLED TO FALSE
                    mapsViewModel.setMapEnabled(
                        loggedInViewModel.liveFirebaseUser.value!!.uid,
                        false
                    )
                }
            } else {
                fragBinding.switchMap.isClickable = false
            }
        }
    }

    private fun listenForPushUpNotificationChanges()
    {
        fragBinding.switchNotifications.setOnCheckedChangeListener { _, b ->

            if(b)
            {
                settingsViewModel.setNotificationEnabled(loggedInViewModel.liveFirebaseUser.value!!.uid, true)
            }
            else
            {
                settingsViewModel.setNotificationEnabled(loggedInViewModel.liveFirebaseUser.value!!.uid, false)
            }
        }
    }
}