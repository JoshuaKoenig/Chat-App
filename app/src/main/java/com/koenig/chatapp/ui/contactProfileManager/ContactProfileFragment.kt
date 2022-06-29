package com.koenig.chatapp.ui.contactProfileManager

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.koenig.chatapp.R
import com.koenig.chatapp.databinding.FragmentContactProfileBinding
import com.koenig.chatapp.enums.ChatModes
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.ui.auth.LoggedInViewModel
import com.koenig.chatapp.ui.chatManager.ChatFragmentDirections
import com.koenig.chatapp.ui.contactsManager.ContactsViewModel
import com.koenig.chatapp.ui.friendRequestManager.FriendRequestViewModel
import com.koenig.chatapp.ui.mapManager.MapsViewModel
import com.koenig.chatapp.ui.profileManager.ProfileViewModel
import com.koenig.chatapp.ui.searchContactManager.SearchContactsViewModel
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation

class ContactProfileFragment : Fragment() {

    private var _fragBinding: FragmentContactProfileBinding? = null
    private val fragBinding get() = _fragBinding!!

    private val friendRequestViewModel: FriendRequestViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private val contactsViewModel: ContactsViewModel by activityViewModels()
    private val mapViewModel: MapsViewModel by activityViewModels()

    private val args by navArgs<ContactProfileFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _fragBinding = FragmentContactProfileBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        renderContactProfile()

        // CLICK LISTENERS
        fragBinding.buttonAddContact.setOnClickListener {
            profileViewModel.observableProfile.observe(viewLifecycleOwner) {

                val currentUser = ContactModel()
                currentUser.userId = it.userId
                currentUser.userName = it.userName
                currentUser.email = it.email
                currentUser.photoUri = it.photoUri
                currentUser.status = it.status

                friendRequestViewModel.sendFriendRequest(args.contactModel, currentUser)
            }
        }

        fragBinding.buttonMap.setOnClickListener {

            val action = ContactProfileFragmentDirections.actionContactProfileFragmentToMapsFragment(args.contactModel, false)
            findNavController().navigate(action)
        }


        // OBSERVE
        mapViewModel.observableMap.observe(viewLifecycleOwner){
            renderMapButton(it)
        }

        contactsViewModel.contacts.observe(viewLifecycleOwner){
            renderContactAddButton(it as ArrayList)
        }

        return root
    }

    private fun renderContactAddButton(contacts: List<ContactModel>)
    {
        if ((contacts.find { contactModel -> contactModel.userId == args.contactModel.userId  } == null) && args.contactModel.userId != loggedInViewModel.liveFirebaseUser.value!!.uid)
        {
            fragBinding.buttonAddContact.visibility = View.VISIBLE
        }
        else
        {
            fragBinding.buttonAddContact.visibility = View.GONE
        }
    }

    override fun onResume() {
        super.onResume()
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner) {
            profileViewModel.getProfile(it.uid)
            contactsViewModel.loadAllContacts(it.uid)
        }
        mapViewModel.getIsMapEnabled(args.contactModel.userId)
    }

    private fun renderContactProfile()
    {
        fragBinding.textContactName.text = args.contactModel.userName
        fragBinding.textContactStatus.text = args.contactModel.status
        fragBinding.textContactMail.text = args.contactModel.email

        if (args.contactModel.photoUri.isNotEmpty())
        {
            Picasso.get().load(args.contactModel.photoUri)
                .resize(300, 300)
                .transform(customTransformation())
                .centerCrop()
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(fragBinding.imageContactUser)
        }
    }

    private fun renderMapButton(isMapEnabled: Boolean)
    {
        fragBinding.buttonMap.isEnabled = isMapEnabled
    }

    private fun customTransformation() : Transformation =
        RoundedTransformationBuilder()
            .borderColor(Color.WHITE)
            .borderWidth(2f)
            .cornerRadius(35f)
            .oval(false)
            .build()
}