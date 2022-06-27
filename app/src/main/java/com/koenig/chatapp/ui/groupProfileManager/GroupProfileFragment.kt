package com.koenig.chatapp.ui.groupProfileManager

import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.koenig.chatapp.adapters.GroupContactsAdapter
import com.koenig.chatapp.adapters.GroupContactsClickListener
import com.koenig.chatapp.databinding.FragmentGroupProfileBinding
import com.koenig.chatapp.enums.ContactClickModes
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.models.MessageModel
import com.koenig.chatapp.ui.auth.LoggedInViewModel
import com.koenig.chatapp.ui.chatManager.ChatViewModel
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import java.time.Instant

class GroupProfileFragment : Fragment(), GroupContactsClickListener {

    private var _fragBinding: FragmentGroupProfileBinding? = null
    private val fragBinding get() = _fragBinding!!

    private val args by navArgs<GroupProfileFragmentArgs>()

    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private val groupProfileViewModel: GroupProfileViewModel by activityViewModels()
    private val chatViewModel: ChatViewModel by activityViewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(false)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _fragBinding = FragmentGroupProfileBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        fragBinding.recyclerViewGroupContacts.layoutManager = LinearLayoutManager(activity)

        // Executed when come back from contacts fragment
        if (args.contactToAdd != null)
        {
            addContactToGroup()
        }
        else
        {
            // OBSERVERS
            loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner){
                renderGroupProfile(ArrayList(args.groupModel!!.groupMembers.values), it.uid)
            }
        }

        // CLICK LISTENERS
        fragBinding.buttonAddUser.setOnClickListener {
            val action = GroupProfileFragmentDirections.actionGroupProfileFragmentToContactsFragment(ContactClickModes.ADDCONTACTMODE, args.groupModel)
            findNavController().navigate(action)
        }

        return root
    }

    // FUNCTIONS

    private fun addContactToGroup()
    {
        if (args.contactToAdd != null && args.groupModel != null)
        {
            groupProfileViewModel.addContactToGroup(args.groupModel!!.groupId, args.contactToAdd!!)
            val updatedGroupMembers = ArrayList(args.groupModel!!.groupMembers.values)
            updatedGroupMembers.add(args.contactToAdd!!)
            renderGroupProfile(updatedGroupMembers, loggedInViewModel.liveFirebaseUser.value!!.uid)

            val messageUserAdded = MessageModel()
            messageUserAdded.firstMessage = true
            messageUserAdded.toUserId = args.groupModel!!.groupId
            messageUserAdded.message = "${args.contactToAdd!!.userName} added to group"
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) messageUserAdded.timeStamp = Instant.now().toString()
            messageUserAdded.fromUserId = loggedInViewModel.liveFirebaseUser.value!!.uid
            messageUserAdded.fromUserName = loggedInViewModel.liveFirebaseUser.value!!.displayName.toString()
            chatViewModel.sendGroupMessage(messageUserAdded, updatedGroupMembers)

        }
    }

    private fun renderGroupProfile(groupMembers: ArrayList<ContactModel>, currentUserId: String)
    {
        fragBinding.recyclerViewGroupContacts.adapter = GroupContactsAdapter(groupMembers, this, args.groupModel!!.adminUid, currentUserId)

        fragBinding.textGroupName.text = args.groupModel!!.groupName
        fragBinding.textGroupDescription.text = args.groupModel!!.description

        if (args.groupModel!!.photoUri.isNotEmpty())
        {
            Picasso.get().load(args.groupModel!!.photoUri)
                .resize(300, 300)
                .transform(customTransformation())
                .centerCrop()
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(fragBinding.imageGroup)
        }
    }

    private fun customTransformation() : Transformation =
        RoundedTransformationBuilder()
            .borderColor(Color.WHITE)
            .borderWidth(2f)
            .cornerRadius(35f)
            .oval(false)
            .build()

    override fun onClickShowUserProfile(user: ContactModel) {
        val action = GroupProfileFragmentDirections.actionGroupProfileFragmentToContactProfileFragment(user)
        findNavController().navigate(action)
    }

    override fun onClickRemoveUser(user: ContactModel) {
        groupProfileViewModel.removeContactFromGroup(args.groupModel!!.groupId, user)
        val updatedGroupMembers = ArrayList(args.groupModel!!.groupMembers.values)
        updatedGroupMembers.remove(user)
        renderGroupProfile(updatedGroupMembers, loggedInViewModel.liveFirebaseUser.value!!.uid)

        val messageUserRemoved = MessageModel()
        messageUserRemoved.firstMessage = true
        messageUserRemoved.toUserId = args.groupModel!!.groupId
        messageUserRemoved.message = "${user.userName} removed from group"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) messageUserRemoved.timeStamp = Instant.now().toString()
        messageUserRemoved.fromUserId = loggedInViewModel.liveFirebaseUser.value!!.uid
        messageUserRemoved.fromUserName = loggedInViewModel.liveFirebaseUser.value!!.displayName.toString()
        chatViewModel.sendGroupMessage(messageUserRemoved, updatedGroupMembers)
    }
}