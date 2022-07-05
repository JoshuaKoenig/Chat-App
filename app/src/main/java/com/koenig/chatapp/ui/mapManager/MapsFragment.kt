package com.koenig.chatapp.ui.mapManager


import android.annotation.SuppressLint
import androidx.fragment.app.Fragment
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.koenig.chatapp.R
import com.koenig.chatapp.models.UserModel
import com.koenig.chatapp.ui.profileManager.ProfileViewModel

class MapsFragment : Fragment() {

    private val mapsViewModel: MapsViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val args by navArgs<MapsFragmentArgs>()


    @SuppressLint("MissingPermission")
    private val callback = OnMapReadyCallback { googleMap ->

        mapsViewModel.map = googleMap

        if (args.isOwnMap)
        {
            mapsViewModel.map.isMyLocationEnabled = true
            val loc = LatLng(mapsViewModel.currentLocation.value!!.latitude, mapsViewModel.currentLocation.value!!.longitude)

            mapsViewModel.map.uiSettings.isZoomControlsEnabled = true
            mapsViewModel.map.uiSettings.isMyLocationButtonEnabled = true
            mapsViewModel.map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14f))
        }
        else
        {
            profileViewModel.observableProfile.observe(viewLifecycleOwner){
                it?.let {
                   render(it)
                   Log.d("Map", it.userName)
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_maps, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    private fun render(user: UserModel)
    {
        mapsViewModel.map.addMarker(
            MarkerOptions().position(LatLng(user.latitude, user.longitude))
                .title(user.userName)
                .snippet(user.status)
                .icon(BitmapDescriptorFactory.defaultMarker())
        )
    }

    override fun onResume() {
        super.onResume()
        if(!args.isOwnMap && args.contact != null)
        {
            profileViewModel.getProfile(args.contact!!.userId)
        }
    }
}