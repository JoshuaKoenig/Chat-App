package com.koenig.chatapp.ui.mapManager


import android.annotation.SuppressLint
import android.content.res.Configuration
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.navArgs
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.android.gms.maps.model.MarkerOptions
import com.google.maps.android.SphericalUtil
import com.koenig.chatapp.MainActivity
import com.koenig.chatapp.R
import com.koenig.chatapp.databinding.FragmentMapsBinding
import com.koenig.chatapp.enums.MapModes
import com.koenig.chatapp.models.UserModel
import com.koenig.chatapp.ui.profileManager.ProfileViewModel
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target
import com.squareup.picasso.Transformation
import kotlin.math.roundToLong


class MapsFragment : Fragment() {

    private val mapsViewModel: MapsViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val args by navArgs<MapsFragmentArgs>()
    private var _fragBinding: FragmentMapsBinding? = null
    private val fragBinding get() = _fragBinding!!

    @RequiresApi(Build.VERSION_CODES.P)
    private val callback = OnMapReadyCallback { googleMap ->

        mapsViewModel.map = googleMap
        when(args.mapMode)
        {
            MapModes.OWNMAP ->{

                fragBinding.informationContainer.visibility = View.GONE
                renderOwnMap()
            }
            MapModes.CONTACTMAP -> {

                fragBinding.informationContainer.visibility = View.VISIBLE
                (requireActivity() as MainActivity).toolbar.title = "${args.contact!!.userName}'s Location"
                profileViewModel.observableProfile.observe(viewLifecycleOwner){
                    it?.let {
                        if(it.userId == args.contact!!.userId)
                        {
                            renderContactMap(it)
                        }
                    }
                }
            }

            MapModes.GROUPMAP -> {

                fragBinding.informationContainer.visibility = View.GONE
                (requireActivity() as MainActivity).toolbar.title = "${args.group!!.groupName} Locations"
                mapsViewModel.observableUsersWithLocation.observe(viewLifecycleOwner, object:  Observer<List<UserModel>> {
                    override fun onChanged(t: List<UserModel>?) {

                        if (t != null)
                        {
                            renderGroupMap(t as ArrayList<UserModel>)
                            mapsViewModel.observableUsersWithLocation.removeObserver(this)
                        }
                    }

                })
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
    ): View {
        _fragBinding = FragmentMapsBinding.inflate(inflater, container, false)
        return fragBinding.root
    }

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val mapFragment = childFragmentManager.findFragmentById(R.id.map) as SupportMapFragment?
        mapFragment?.getMapAsync(callback)
    }

    @SuppressLint("MissingPermission")
    private fun renderOwnMap()
    {
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                mapsViewModel.map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_dark))
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                mapsViewModel.map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_contact))
            }
        }
        mapsViewModel.map.isMyLocationEnabled = true
        val loc = LatLng(mapsViewModel.currentLocation.value!!.latitude, mapsViewModel.currentLocation.value!!.longitude)

        mapsViewModel.map.uiSettings.isZoomControlsEnabled = true
        mapsViewModel.map.uiSettings.isMyLocationButtonEnabled = true
        mapsViewModel.map.moveCamera(CameraUpdateFactory.newLatLngZoom(loc, 14f))
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(Build.VERSION_CODES.P)
    private fun renderContactMap(user: UserModel)
    {
        val markerColor = BitmapDescriptorFactory.HUE_BLUE

        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                mapsViewModel.map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_dark))
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                mapsViewModel.map.setMapStyle(MapStyleOptions.loadRawResourceStyle(requireContext(), R.raw.map_style_contact))
            }
        }

        val locCurrentUser = LatLng(mapsViewModel.currentLocation.value!!.latitude, mapsViewModel.currentLocation.value!!.longitude)
        val locContact = LatLng(user.latitude, user.longitude)
        val distanceInMeter = SphericalUtil.computeDistanceBetween(locContact, locCurrentUser)
        val distanceInKm = (distanceInMeter / 1000).roundToLong()

        fragBinding.textDistanceBetween.setText("$distanceInKm km")

        mapsViewModel.map.addMarker(
            MarkerOptions().position(locContact)
                .title(user.userName)
                .snippet(user.status)
                .icon(BitmapDescriptorFactory.defaultMarker(markerColor)))

        mapsViewModel.observableUserImage.observe(viewLifecycleOwner)
        { uri ->
            Picasso.get().load(uri)
                .resize(150, 150)
                .transform(customTransformation())
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .centerCrop()
                .into(object : Target {
                    override fun onBitmapLoaded(bitmap: Bitmap?,
                                                from: Picasso.LoadedFrom?
                    ) {
                        mapsViewModel.map.addMarker(
                            MarkerOptions().position(locContact)
                                .title(user.userName)
                                .snippet(user.status)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap!!))
                        )
                    }

                    override fun onBitmapFailed(e: java.lang.Exception?,
                                                errorDrawable: Drawable?) {
                        mapsViewModel.map.addMarker(
                            MarkerOptions().position(locContact)
                                .title(user.userName)
                                .snippet(user.status)
                                .icon(BitmapDescriptorFactory.defaultMarker(markerColor)))
                    }
                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                })
            mapsViewModel.map.moveCamera(CameraUpdateFactory.newLatLngZoom(locContact, 5f))
        }
    }

    private fun renderGroupMap(users: ArrayList<UserModel>) {
        when (resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK) {
            Configuration.UI_MODE_NIGHT_YES -> {
                mapsViewModel.map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.map_style_dark
                    )
                )
            }
            Configuration.UI_MODE_NIGHT_NO -> {
                mapsViewModel.map.setMapStyle(
                    MapStyleOptions.loadRawResourceStyle(
                        requireContext(),
                        R.raw.map_style_contact
                    )
                )
            }
        }
        users.forEach {
            val markerColor = BitmapDescriptorFactory.HUE_BLUE
            val loc = LatLng(it.latitude, it.longitude)

            mapsViewModel.map.addMarker(
                MarkerOptions().position(loc)
                    .title(it.userName)
                    .snippet(it.status)
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor)))
        }

        mapsViewModel.observableGroupImage.observe(viewLifecycleOwner) {
            if (it.isNotEmpty()) {
                users.forEach { um ->
                    Thread.sleep(200)
                    loadGroupImages(um, it.find { uri -> uri.toString().contains(um.userId) }!!)
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        if(args.mapMode == MapModes.CONTACTMAP && args.contact != null)
        {
            profileViewModel.getProfile(args.contact!!.userId)
            mapsViewModel.getImageUri(args.contact!!.userId)
        }
        else if(args.mapMode == MapModes.GROUPMAP && args.group != null)
        {
            val contactIds = ArrayList<String>()
            args.group!!.groupMembers.values.forEach {
                contactIds.add(it.userId)
            }
            mapsViewModel.groupUserImages(contactIds)
            mapsViewModel.getLocationForUsers(contactIds)
        }
    }

    private fun loadGroupImages(user: UserModel, uri: Uri)
    {
        val markerColor = BitmapDescriptorFactory.HUE_BLUE
        val loc = LatLng(user.latitude, user.longitude)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            Picasso.get().load(uri)
                .resize(150, 150)
                .transform(customTransformation())
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .centerCrop()
                .into(object : Target {
                    override fun onBitmapLoaded(bitmap: Bitmap?,
                                                from: Picasso.LoadedFrom?
                    ) {
                        mapsViewModel.map.addMarker(
                            MarkerOptions().position(loc)
                                .title(user.userName)
                                .snippet(user.status)
                                .icon(BitmapDescriptorFactory.fromBitmap(bitmap!!))
                        )
                    }

                    override fun onBitmapFailed(e: java.lang.Exception?,
                                                errorDrawable: Drawable?) {
                        mapsViewModel.map.addMarker(
                            MarkerOptions().position(loc)
                                .title(user.userName)
                                .snippet(user.status)
                                .icon(BitmapDescriptorFactory.defaultMarker(markerColor)))
                    }
                    override fun onPrepareLoad(placeHolderDrawable: Drawable?) {}
                })
        }
    }


    private fun customTransformation() : Transformation =
        RoundedTransformationBuilder()
            .borderColor(Color.WHITE)
            .borderWidth(2f)
            .cornerRadius(35f)
            .oval(false)
            .build()
}