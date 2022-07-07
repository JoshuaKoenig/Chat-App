package com.koenig.chatapp.ui.groupProfileManager

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.koenig.chatapp.MainActivity
import com.koenig.chatapp.R
import com.koenig.chatapp.adapters.GroupContactsAdapter
import com.koenig.chatapp.adapters.GroupContactsClickListener
import com.koenig.chatapp.databinding.FragmentGroupProfileBinding
import com.koenig.chatapp.enums.ContactClickModes
import com.koenig.chatapp.enums.MapModes
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.models.MessageModel
import com.koenig.chatapp.ui.auth.LoggedInViewModel
import com.koenig.chatapp.ui.chatManager.ChatViewModel
import com.koenig.chatapp.utils.SwipeToRemoveCallback
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

    private val keyNewGroupName = "KEY_NEW_GROUP_NAME"
    private val keyNewDescription = "KEY_NEW_DESCRIPTION"
    private val keyNewImage = "KEY_NEW_IMAGE"

    private val keyHasNewGroupName = "KEY_HAS_NEW_GROUP_NAME"
    private val keyHasNewDescription = "KEY_HAS_NEW_DESCRIPTION"
    private val keyHasNewImage = "KEY_HAS_NEW_IMAGE"

    private var hasNewGroupName: Boolean = false
    private var hasNewGroupDescription: Boolean = false
    private var hasNewGroupImage: Boolean = false

    private var currentGroupName: String = ""
    private var currentGroupDescription: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _fragBinding = FragmentGroupProfileBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        fragBinding.recyclerViewGroupContacts.layoutManager = LinearLayoutManager(activity)

        (requireActivity() as MainActivity).toolbar.title = args.groupModel!!.groupName

        // Executed when come back from contacts fragment
        if (args.contactToAdd != null)
        {
            addContactToGroup()
            loadNewDescription()
            loadNewGroupName()
            loadNewImage()
            fragBinding.buttonSaveGroupName.visibility = View.GONE
            fragBinding.buttonSaveGroupName.isEnabled = false
            fragBinding.buttonSaveDescription.visibility = View.GONE
            fragBinding.buttonSaveDescription.isEnabled = false
            fragBinding.progressBar.visibility = View.GONE
        }
        // Just come back from contacts fragment without user to add
        else if(args.isEdited)
        {
            loadNewDescription()
            loadNewGroupName()
            loadNewImage()
            fragBinding.buttonSaveGroupName.visibility = View.GONE
            fragBinding.buttonSaveGroupName.isEnabled = false
            fragBinding.buttonSaveDescription.visibility = View.GONE
            fragBinding.buttonSaveDescription.isEnabled = false
            fragBinding.progressBar.visibility = View.GONE
        }
        else
        {
            // OBSERVERS
            loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner){
                renderGroupProfile(ArrayList(args.groupModel!!.groupMembers.values), it.uid)
                fragBinding.progressBar.visibility = View.GONE
            }

            currentGroupName = args.groupModel!!.groupName
            currentGroupDescription = args.groupModel!!.description
        }

        // CLICK LISTENERS
        fragBinding.buttonAddUser.setOnClickListener {
            val action = GroupProfileFragmentDirections.actionGroupProfileFragmentToContactsFragment(ContactClickModes.ADDCONTACTMODE, args.groupModel)
            findNavController().navigate(action)
        }

        fragBinding.buttonSaveGroupName.setOnClickListener {
            updateGroupName()
        }

        fragBinding.buttonSaveDescription.setOnClickListener {
            updateDescription()
        }

        fragBinding.imageGroup.setOnClickListener {
            openGalleryForImage()
        }

        fragBinding.buttonMap.setOnClickListener {
            val action = GroupProfileFragmentDirections.actionGroupProfileFragmentToMapsFragment(null, MapModes.GROUPMAP, args.groupModel)
            findNavController().navigate(action)
        }


        // INPUT FIELDS
        fragBinding.textGroupName.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString() != currentGroupName)
                {
                    // If both string arent equal => enable save button
                    fragBinding.buttonSaveGroupName.visibility = View.VISIBLE
                    fragBinding.buttonSaveGroupName.isEnabled = true
                }
                else
                {
                    // If both strings are equal => disable save button
                    fragBinding.buttonSaveGroupName.visibility = View.GONE
                    fragBinding.buttonSaveGroupName.isEnabled = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        fragBinding.textGroupDescription.addTextChangedListener(object: TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if (p0.toString() !=  currentGroupDescription)
                {
                    // If both string arent equal => enable save button
                    fragBinding.buttonSaveDescription.visibility = View.VISIBLE
                    fragBinding.buttonSaveDescription.isEnabled = true
                }
                else
                {
                    // If both strings are equal => disable save button
                    fragBinding.buttonSaveDescription.visibility = View.GONE
                    fragBinding.buttonSaveDescription.isEnabled = false
                }
            }
            override fun afterTextChanged(p0: Editable?) {}
        })

        // ITEM TOUCH HANDLER
        val swipeRemoveUserHandler = object : SwipeToRemoveCallback(requireContext()) {
            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                onClickRemoveUser(viewHolder.itemView.tag as ContactModel)
            }
        }

        if(loggedInViewModel.liveFirebaseUser.value!!.uid == args.groupModel!!.adminUid)
        {
            val itemTouchRemoveHelper = ItemTouchHelper(swipeRemoveUserHandler)
            itemTouchRemoveHelper.attachToRecyclerView(fragBinding.recyclerViewGroupContacts)
        }

        return root
    }

    // FUNCTIONS

    private fun loadNewGroupName()
    {
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_group_profile_key), Context.MODE_PRIVATE)

        if(sharedPreferences.getBoolean(keyHasNewGroupName, hasNewGroupName))
        {
            fragBinding.textGroupName.setText(sharedPreferences.getString(keyNewGroupName, null))
            currentGroupName = sharedPreferences.getString(keyNewGroupName, null).toString()
        }
    }

    private fun loadNewDescription()
    {
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_group_profile_key), Context.MODE_PRIVATE)

        if(sharedPreferences.getBoolean(keyHasNewDescription, hasNewGroupDescription))
        {
            fragBinding.textGroupDescription.setText(sharedPreferences.getString(keyNewDescription, null))
            currentGroupDescription = sharedPreferences.getString(keyNewDescription, null).toString()
        }
    }

    private fun loadNewImage()
    {
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_group_profile_key), Context.MODE_PRIVATE)

        if(sharedPreferences.getBoolean(keyHasNewImage, hasNewGroupImage))
        {
            Picasso.get().load(sharedPreferences.getString(keyNewImage, null))
                .resize(300, 300)
                .transform(customTransformation())
                .centerCrop()
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(fragBinding.imageGroup)
        }
    }

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

        fragBinding.textGroupName.setText(args.groupModel!!.groupName)
        fragBinding.textGroupDescription.setText(args.groupModel!!.description)

        if (args.groupModel!!.photoUri.isNotEmpty())
        {
            Picasso.get().load(args.groupModel!!.photoUri)
                .resize(200, 200)
                .transform(customTransformation())
                .centerCrop()
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(fragBinding.imageGroup)
        }
    }

    private fun updateGroupName()
    {
        hasNewGroupName = true

        groupProfileViewModel.updateGroupName(args.groupModel!!.groupId, fragBinding.textGroupName.text.toString())
        fragBinding.buttonSaveGroupName.visibility = View.GONE
        fragBinding.buttonSaveGroupName.isEnabled = false

        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_group_profile_key), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(keyHasNewGroupName, hasNewGroupName)
        editor.putString(keyNewGroupName, fragBinding.textGroupName.text.toString())
        editor.apply()
    }

    private fun updateDescription()
    {
        hasNewGroupDescription = true

        groupProfileViewModel.updateDescription(args.groupModel!!.groupId, fragBinding.textGroupDescription.text.toString())
        fragBinding.buttonSaveDescription.visibility = View.GONE
        fragBinding.buttonSaveDescription.isEnabled = false

        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_group_profile_key), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(keyHasNewDescription, hasNewGroupDescription)
        editor.putString(keyNewDescription, fragBinding.textGroupDescription.text.toString())
        editor.apply()
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

            if (intent!!.data != null)
            {
                hasNewGroupImage = true

                groupProfileViewModel.updateGroupImage(args.groupModel!!.groupId, intent.data.toString(), fragBinding.imageGroup )

                Picasso.get().load(intent.data)
                    .resize(200, 200)
                    .transform(customTransformation())
                    .centerCrop()
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(fragBinding.imageGroup)

                val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_group_profile_key), Context.MODE_PRIVATE)
                val editor = sharedPreferences.edit()
                editor.putBoolean(keyHasNewImage, hasNewGroupImage)
                editor.putString(keyNewImage, intent.data.toString())
                editor.apply()
            }
        }
    }


    private fun customTransformation() : Transformation =
        RoundedTransformationBuilder()
            .borderColor(Color.WHITE)
            .borderWidth(2f)
            .cornerRadius(35f)
            .oval(false)
            .build()


    // OVERRIDES
    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
        // TODO: If contact to leave is admin, write "Disband group"

        // Admin can disband the group
        if (args.groupModel!!.adminUid == loggedInViewModel.liveFirebaseUser.value!!.uid)
        {
            menu.findItem(R.id.action_profile).title = "Disband group"
        }
        // Group Members can leave the group
        else
        {
            menu.findItem(R.id.action_profile).title = "Leave Group"
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId)
        {
            R.id.action_profile -> {

                if (args.groupModel!!.adminUid == loggedInViewModel.liveFirebaseUser.value!!.uid)
                {
                    groupProfileViewModel.disbandGroup(args.groupModel!!.groupId)
                }
                else
                {
                    groupProfileViewModel.removeContactFromGroup(args.groupModel!!.groupId, loggedInViewModel.liveFirebaseUser.value!!.uid)

                    val updatedGroupMembers = ArrayList(args.groupModel!!.groupMembers.values)
                    val currentUser = updatedGroupMembers.find { it.userId == loggedInViewModel.liveFirebaseUser.value!!.uid }
                    updatedGroupMembers.remove(currentUser)
                    sendUserLeftTheGroupMessage(currentUser!!, updatedGroupMembers, "left the group")
                }

                findNavController().navigateUp()
            }
            android.R.id.home -> {
                clearPreferences()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    private fun clearPreferences()
    {
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_group_profile_key), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(keyHasNewGroupName)
        editor.remove(keyNewGroupName)
        editor.remove(keyHasNewDescription)
        editor.remove(keyNewDescription)
        editor.remove(keyHasNewImage)
        editor.remove(keyNewImage)
        editor.apply()
    }

    override fun onClickShowUserProfile(user: ContactModel) {
        val action = GroupProfileFragmentDirections.actionGroupProfileFragmentToContactProfileFragment(user)
        findNavController().navigate(action)
    }

    override fun onClickRemoveUser(user: ContactModel) {
        groupProfileViewModel.removeContactFromGroup(args.groupModel!!.groupId, user.userId)
        val updatedGroupMembers = ArrayList(args.groupModel!!.groupMembers.values)
        updatedGroupMembers.remove(user)
        renderGroupProfile(updatedGroupMembers, loggedInViewModel.liveFirebaseUser.value!!.uid)

        sendUserLeftTheGroupMessage(user, updatedGroupMembers, "removed from group")
    }

    // HELPERS
    private fun sendUserLeftTheGroupMessage(user: ContactModel,updatedGroupMembers: ArrayList<ContactModel>, message: String)
    {
        val messageUserRemoved = MessageModel()
        messageUserRemoved.firstMessage = true
        messageUserRemoved.toUserId = args.groupModel!!.groupId
        messageUserRemoved.message = "${user.userName} $message"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) messageUserRemoved.timeStamp = Instant.now().toString()
        messageUserRemoved.fromUserId = loggedInViewModel.liveFirebaseUser.value!!.uid
        messageUserRemoved.fromUserName = loggedInViewModel.liveFirebaseUser.value!!.displayName.toString()
        chatViewModel.sendGroupMessage(messageUserRemoved, updatedGroupMembers)
    }
}