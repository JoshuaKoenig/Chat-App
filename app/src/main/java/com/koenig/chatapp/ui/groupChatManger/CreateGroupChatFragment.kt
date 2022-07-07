package com.koenig.chatapp.ui.groupChatManger

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.content.res.Resources
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.text.TextUtils
import android.util.TypedValue
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.koenig.chatapp.R
import com.koenig.chatapp.databinding.FragmentCreateGroupChatBinding
import com.koenig.chatapp.enums.ContactClickModes
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.models.GroupModel
import com.koenig.chatapp.models.MessageModel
import com.koenig.chatapp.ui.auth.LoggedInViewModel
import com.koenig.chatapp.ui.profileManager.ProfileViewModel
import com.makeramen.roundedimageview.RoundedTransformationBuilder
import com.squareup.picasso.MemoryPolicy
import com.squareup.picasso.Picasso
import com.squareup.picasso.Transformation
import java.io.IOException
import java.time.Instant

class CreateGroupChatFragment : Fragment() {

    private var _fragBinding: FragmentCreateGroupChatBinding? = null
    private val fragBinding get() = _fragBinding!!

    private val groupViewModel: CreateGroupChatViewModel by activityViewModels()
    private val loggedInViewModel: LoggedInViewModel by activityViewModels()
    private val profileViewModel: ProfileViewModel by activityViewModels()

    private val args by navArgs<CreateGroupChatFragmentArgs>()

    private var groupContacts = ArrayList<ContactModel>()
    private var newGroup = GroupModel()

    private val keyGroupName: String = "KEY_GROUP_NAME_EDIT_TEXT"
    private val keyHasGroupName: String = "IS_GROUP_NAME_EDIT"
    private var hasGroupName: Boolean = false
    private val keyGroupDescription: String = "KEY_GROUP_DESCRIPTION_EDIT_TEXT"
    private val keyHasGroupDescription: String = "IS_GROUP_DESCRIPTION_EDIT"
    private var hasGroupDescription: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _fragBinding = FragmentCreateGroupChatBinding.inflate(inflater, container, false)
        val root = fragBinding.root

        // Executed when come back from contacts fragment
        if (args.contactToAdd != null)
        {
            addContactToGroup()
            loadCurrentInputFields()
        }
        else
        {
            addCurrentUserToGroup()
        }

        fragBinding.buttonCreateGroup.setOnClickListener {

            if(isValidated())
            {
                // Name
                newGroup.groupName = fragBinding.textGroupName.text.toString()

                // Description
                if (TextUtils.isEmpty(fragBinding.textGroupDescription.text)) newGroup.description = ""
                else newGroup.description = fragBinding.textGroupDescription.text.toString()

                // Group Picture
                if (newGroup.photoUri == "")
                {
                    newGroup.photoUri = "android.resource://com.koenig.chatapp/drawable/empty_profile"
                }

                // Group Members
                if (groupViewModel.currentGroupMembers.value != null)
                {
                    groupViewModel.currentGroupMembers.value!!.forEach{
                        newGroup.groupMembers[it.userId] = it
                    }
                }

                // Admin
                newGroup.adminUid = loggedInViewModel.liveFirebaseUser.value!!.uid

                // First Message
                val firstMessage = MessageModel()
                firstMessage.message = "Group '${newGroup.groupName}' created by: '${loggedInViewModel.liveFirebaseUser.value!!.displayName}'"
                firstMessage.fromUserId = newGroup.adminUid
                firstMessage.firstMessage = true

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                {
                    firstMessage.timeStamp = Instant.now().toString()
                }

                groupViewModel.createGroupChat(newGroup, firstMessage, fragBinding.imageGroupChat)
                findNavController().navigateUp()
                // CLEAR
                groupViewModel.currentGroupMembers.postValue(emptyList())
                requireArguments().clear()
                hasGroupName = false
                hasGroupDescription = false
                clearPreferences()
            }
            else
            {
                fragBinding.textGroupName.error = "Required"
            }
        }

        // Add User to group
        fragBinding.buttonAddUserToGroup.setOnClickListener {
            saveCurrentInputFields()
            val currentGroup = GroupModel()
            groupViewModel.currentGroupMembers.value!!.forEach {
                currentGroup.groupMembers[it.userId] = it
            }
            val action = CreateGroupChatFragmentDirections.actionCreateGroupChatFragmentToContactsFragment(ContactClickModes.CREATEGROUPMODE, currentGroup)
            findNavController().navigate(action)
        }

        // OBSERVE
        loggedInViewModel.liveFirebaseUser.observe(viewLifecycleOwner){
           // render(it)
        }

        groupViewModel.observableGroupMembers.observe(viewLifecycleOwner){
            fragBinding.progressBar.visibility = View.GONE
            renderCurrentMembers(it)
        }

        // CLICK LISTENERS
        fragBinding.imageGroupChat.setOnClickListener {
            openGalleryForImage()
        }

        return root

    }

    private fun addCurrentUserToGroup()
    {
        profileViewModel.observableProfile.observe(viewLifecycleOwner){

            val currentUserAsContact = ContactModel()
            currentUserAsContact.userName = it.userName
            currentUserAsContact.userId = it.userId
            currentUserAsContact.photoUri = it.photoUri
            currentUserAsContact.status = it.status
            currentUserAsContact.email = it.email

            if(groupViewModel.currentGroupMembers.value == null || groupViewModel.currentGroupMembers.value!!.isEmpty())
            {
                groupContacts.add(currentUserAsContact)
                groupViewModel.currentGroupMembers.postValue(groupContacts)
            }
        }
    }

    private fun addContactToGroup()
    {
        if (groupViewModel.currentGroupMembers.value != null)
        {
            groupContacts = groupViewModel.currentGroupMembers.value as ArrayList<ContactModel>
        }

        // add contact to local contacts list
        groupContacts.add(args.contactToAdd!!)

        groupViewModel.currentGroupMembers.postValue(groupContacts)
    }

    private fun renderCurrentMembers(currentMembers: List<ContactModel>)
    {
        fragBinding.GroupUsersContainer.removeAllViews()

        currentMembers.forEach {
            val imageView = ImageView(activity)

            val r: Resources = resources

            val pxFromDp50 = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                50f,
                r.displayMetrics
            )

            val pxFromDp5 = TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                5f,
                r.displayMetrics
            )

            val lp = LinearLayout.LayoutParams(pxFromDp50.toInt(), pxFromDp50.toInt())
            lp.setMargins(0, 0, pxFromDp5.toInt(), 0)
            imageView.layoutParams = lp

            Picasso.get().load("https://firebasestorage.googleapis.com/v0/b/chatapp-4dffc.appspot.com/o/photos%2F${it.userId}.jpg?alt=media&token=3a3b9aeb-8193-44bd-b1d3-54b96a8de90f")
                .resize(200, 200)
                .transform(customTransformation())
                .centerCrop()
                .memoryPolicy(MemoryPolicy.NO_CACHE)
                .into(imageView)

            fragBinding.GroupUsersContainer.addView(imageView)
        }
    }

    private fun isValidated(): Boolean
    {
        return !TextUtils.isEmpty(fragBinding.textGroupName.text)
    }

    private fun saveCurrentInputFields()
    {
        if(fragBinding.textGroupName.text != null)
        {
            hasGroupName = true
        }

        if(fragBinding.textGroupDescription.text != null)
        {
            hasGroupDescription = true
        }

        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_group_chat_key), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.putBoolean(keyHasGroupName, hasGroupName)
        editor.putBoolean(keyHasGroupDescription, hasGroupDescription)

        if(hasGroupName)
        {
            editor.putString(keyGroupName, fragBinding.textGroupName.text.toString())
        }

        if(hasGroupDescription)
        {
            editor.putString(keyGroupDescription, fragBinding.textGroupDescription.text.toString())
        }

        editor.apply()
    }

    private fun loadCurrentInputFields()
    {
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_group_chat_key), Context.MODE_PRIVATE)

        if(sharedPreferences.getBoolean(keyHasGroupName, hasGroupName))
        {
            fragBinding.textGroupName.setText(sharedPreferences.getString(keyGroupName, null))
        }

        if(sharedPreferences.getBoolean(keyHasGroupDescription, hasGroupDescription))
        {
            fragBinding.textGroupDescription.setText(sharedPreferences.getString(keyGroupDescription, null))
        }
    }

    private fun clearPreferences()
    {
        val sharedPreferences: SharedPreferences = requireActivity().getSharedPreferences(getString(R.string.preference_group_chat_key), Context.MODE_PRIVATE)
        val editor = sharedPreferences.edit()
        editor.remove(keyHasGroupName)
        editor.remove(keyGroupName)
        editor.remove(keyGroupDescription)
        editor.remove(keyHasGroupDescription)
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
               // newGroup.photoUri = intent.data.toString()
                newGroup.photoUri = readImageUri(result.resultCode, intent).toString()

                Picasso.get().load(intent.data)
                    .resize(200, 200)
                    .transform(customTransformation())
                    .centerCrop()
                    .memoryPolicy(MemoryPolicy.NO_CACHE)
                    .into(fragBinding.imageGroupChat)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        profileViewModel.getProfile(loggedInViewModel.liveFirebaseUser.value!!.uid)

    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.main, menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        when(item.itemId)
        {
            android.R.id.home ->
            {
                hasGroupName = false
                hasGroupDescription = false
                requireArguments().clear()
                groupViewModel.currentGroupMembers.postValue(emptyList())
                clearPreferences()
            }
        }

        return super.onOptionsItemSelected(item)
    }

    // Move to Helpers.kt
    private fun customTransformation() : Transformation =
        RoundedTransformationBuilder()
            .borderColor(Color.WHITE)
            .borderWidth(2f)
            .cornerRadius(35f)
            .oval(false)
            .build()

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