package com.koenig.chatapp.ui.contactProfileManager

import android.graphics.Color
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.navigation.navOptions
import com.koenig.chatapp.MainActivity
import com.koenig.chatapp.databinding.FragmentContactProfileBinding
import com.koenig.chatapp.enums.MapModes
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.ui.auth.LoggedInViewModel
import com.koenig.chatapp.ui.contactsManager.ContactsViewModel
import com.koenig.chatapp.ui.friendRequestManager.FriendRequestViewModel
import com.koenig.chatapp.ui.mapManager.MapsViewModel
import com.koenig.chatapp.ui.profileManager.ProfileViewModel
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
    private val contactProfileViewModel: ContactProfileViewModel by activityViewModels()

    private val args by navArgs<ContactProfileFragmentArgs>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _fragBinding = FragmentContactProfileBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        (requireActivity() as MainActivity).toolbar.title = args.contactModel.userName
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
            val action = ContactProfileFragmentDirections.actionContactProfileFragmentToMapsFragment(args.contactModel, MapModes.CONTACTMAP)
            findNavController().navigate(action, navOptions {
                anim {
                    enter = android.R.animator.fade_in
                }
            })
        }

        fragBinding.buttonLike.setOnClickListener {

            profileViewModel.observableLikes.observe(viewLifecycleOwner, object : Observer<Int>{
                override fun onChanged(t: Int?) {

                    it.let {
                        contactProfileViewModel.increaseLike(args.contactModel.userId, t!!)
                        fragBinding.buttonLike.visibility = View.GONE
                        fragBinding.buttonAlreadyLiked.visibility = View.VISIBLE
                        contactProfileViewModel.setHasLiked(loggedInViewModel.liveFirebaseUser.value?.uid!!, args.contactModel.userId, true)
                        profileViewModel.observableLikes.removeObserver(this)
                    }
                }
            })
        }

        // OBSERVE
        mapViewModel.observableMap.observe(viewLifecycleOwner){
            renderMapButton(it)
            fragBinding.progressBar.visibility = View.GONE
        }

        contactsViewModel.contacts.observe(viewLifecycleOwner){
            renderContactAddButton(it as ArrayList)
        }

        contactProfileViewModel.hasAlreadyLikedObserver.observe(viewLifecycleOwner){ hasLiked ->

            if(args.canLike)
            {
                if(hasLiked)
                {
                    fragBinding.buttonLike.visibility = View.GONE
                    fragBinding.buttonAlreadyLiked.visibility = View.VISIBLE
                }
                else
                {
                    fragBinding.buttonLike.visibility = View.VISIBLE
                    fragBinding.buttonAlreadyLiked.visibility = View.GONE
                }
            }
            else
            {
                fragBinding.buttonLike.visibility = View.GONE
                fragBinding.buttonAlreadyLiked.visibility = View.GONE
            }

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
            contactsViewModel.loadAllContacts(it.uid, false, null, "")
            contactProfileViewModel.getHasAlreadyLiked(it.uid, args.contactModel.userId)
        }
        mapViewModel.getIsMapEnabled(args.contactModel.userId)
        profileViewModel.getLikeAmount(args.contactModel.userId)
    }

    private fun renderContactProfile()
    {
        fragBinding.textContactName.setText(args.contactModel.userName)
        fragBinding.textContactStatus.setText(args.contactModel.status)
        fragBinding.textContactMail.text = args.contactModel.email

        Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatapp-4dffc.appspot.com/o/photos%2F${args.contactModel.userId}.jpg?alt=media&token=3a3b9aeb-8193-44bd-b1d3-54b96a8de90f")
            .resize(200, 200)
            .transform(customTransformation())
            .centerCrop()
            .memoryPolicy(MemoryPolicy.NO_CACHE)
            .into(fragBinding.imageContactUser)
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