package com.koenig.chatapp.ui.profileManager

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.google.firebase.auth.FirebaseUser
import com.koenig.chatapp.R
import com.koenig.chatapp.databinding.FragmentProfileBinding
import com.koenig.chatapp.enums.ContactClickModes
import com.koenig.chatapp.firebase.FirebaseImageManager
import com.koenig.chatapp.ui.auth.LoggedInViewModel
import com.koenig.chatapp.ui.mapManager.MapsViewModel
import java.io.IOException


class ProfileFragment : Fragment() {

    private var _fragBinding: FragmentProfileBinding? = null
    private val fragBinding get() = _fragBinding!!
    private  val profileViewModel: ProfileViewModel by activityViewModels()
    private  val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private val mapViewModel: MapsViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _fragBinding = FragmentProfileBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        // OBSERVERS
        profileViewModel.observableProfile.observe(viewLifecycleOwner, Observer {
            render()
            fragBinding.progressBar.visibility = View.GONE
        })

        // Enable own Map when location permission was given
        mapViewModel.observableLocationPermission.observe(viewLifecycleOwner){
            renderMapButton(it)
        }

        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner) { firebaseUser ->
            if (firebaseUser != null) {
                updateProfile(firebaseUser)
            }
        }

        // TEXT CHANGE LISTENER
        fragBinding.textUserName.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString() != loggedInViewModel.liveFirebaseUser.value!!.displayName.toString())
                {
                    // If both string arent equal => enable save button
                    fragBinding.buttonSave.visibility = View.VISIBLE
                    fragBinding.buttonSave.isEnabled = true
                }
                else
                {
                    // If both strings are equal => disable save button
                    fragBinding.buttonSave.visibility = View.INVISIBLE
                    fragBinding.buttonSave.isEnabled = false
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        })

        fragBinding.textStatus.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString() != profileViewModel.observableProfile.value!!.status)
                {
                    // If both string arent equal => enable save button
                    fragBinding.buttonSave.visibility = View.VISIBLE
                    fragBinding.buttonSave.isEnabled = true
                }
                else
                {
                    // If both strings are equal => disable save button
                    fragBinding.buttonSave.visibility = View.INVISIBLE
                    fragBinding.buttonSave.isEnabled = false
                }
            }

            override fun afterTextChanged(p0: Editable?) {}

        })

        // CLICK LISTENERS
        fragBinding.buttonSave.setOnClickListener {

            profileViewModel.updateProfile(loggedInViewModel.liveFirebaseUser.value?.uid!!,
                fragBinding.profilevm?.observableProfile!!.value!!,
                loggedInViewModel.liveFirebaseUser )

            fragBinding.buttonSave.visibility = View.INVISIBLE
            fragBinding.buttonSave.isEnabled = false
        }

        fragBinding.imageUser.setOnClickListener {
            openGalleryForImage()
        }

        fragBinding.buttonMap.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToMapsFragment()
            findNavController().navigate(action)
        }

        fragBinding.buttonContacts.setOnClickListener {
            val action = ProfileFragmentDirections.actionProfileFragmentToContactsFragment(
                ContactClickModes.DEFAULTMODE, null)

            findNavController().navigate(action, navOptions {
                anim {
                    enter = android.R.animator.fade_in
                    exit = android.R.animator.fade_out
                }
            })
        }
        return  root
    }

    private fun render()
    {
        fragBinding.profilevm = profileViewModel
    }

    @SuppressLint("IntentReset")
    private fun openGalleryForImage() {

        var pickIntent = Intent(Intent.ACTION_OPEN_DOCUMENT)
        pickIntent.type = "image/*"
        pickIntent = Intent.createChooser(pickIntent, "Select Profile Image")
        startForResult.launch(pickIntent)
    }

    private val startForResult = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            result: ActivityResult ->
        if (result.resultCode == Activity.RESULT_OK) {
            val intent = result.data

            // Update User
            profileViewModel.updateProfileImage(loggedInViewModel.liveFirebaseUser.value!!.uid, intent!!.data!!)

            // Update Image DB
            FirebaseImageManager.updateUserImage(
                loggedInViewModel.liveFirebaseUser.value!!.uid,
                readImageUri(result.resultCode, intent),
                fragBinding.imageUser,
                true
            )
        }
    }

    private fun updateProfile(currentUser: FirebaseUser)
    {
      FirebaseImageManager.imageUri.observe(viewLifecycleOwner) { result ->
            if (result == Uri.EMPTY)
            {
               if (currentUser.photoUrl != null)
               {
                   FirebaseImageManager.updateUserImage(
                       currentUser.uid,
                       currentUser.photoUrl,
                       fragBinding.imageUser,
                       false
                   )
               }
               else
               {
                   FirebaseImageManager.updateDefaultImage(
                       currentUser.uid,
                       R.drawable.empty_profile,
                       fragBinding.imageUser
                   )
               }
            }
            else
            {
                FirebaseImageManager.updateUserImage(
                    currentUser.uid,
                    FirebaseImageManager.imageUri.value,
                    fragBinding.imageUser,
                    false
                )
            }
          fragBinding.imageUser.visibility = View.VISIBLE
          fragBinding.loadImageUser.visibility = View.GONE
        }
    }

    private fun renderMapButton(isMapEnabled: Boolean)
    {
        fragBinding.buttonMap.isEnabled = isMapEnabled
    }

    override fun onResume() {
        super.onResume()
        profileViewModel.getProfile(loggedInViewModel.liveFirebaseUser.value?.uid!!)
        mapViewModel.getHasLocationPermission(loggedInViewModel.liveFirebaseUser.value?.uid!!)
    }

    // TODO: Outsource in Helpers.kt

    private fun readImageUri(resultCode: Int, data: Intent?): Uri? {
        var uri: Uri? = null
        if (resultCode == Activity.RESULT_OK && data != null && data.data != null) {
            try { uri = data.data }
            catch (e: IOException) {
                e.printStackTrace()
            }
        }
        return uri
    }
}