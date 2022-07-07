package com.koenig.chatapp.firebase

import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.models.MessageModel
import com.koenig.chatapp.models.UserModel
import com.koenig.chatapp.models.UserStore
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

object FirebaseDBManager: UserStore {

    private var database: DatabaseReference = FirebaseDatabase.getInstance().reference

    override fun getUserById(userId: String, user: MutableLiveData<UserModel>) {

    database.child("users").child(userId).addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                user.value = snapshot.getValue<UserModel>()
                database.child("users").child(userId).removeEventListener(this)
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun createUser(firebaseUser: FirebaseUser) {

        val user = UserModel()
        val userId = firebaseUser.uid

        user.email = firebaseUser.email.toString()
        user.userName = firebaseUser.displayName.toString()
        user.userId = userId
        user.status = "Hello i'm new here"
        user.photoUri = firebaseUser.photoUrl.toString()
        user.contacts = hashMapOf()
        user.isMapEnabled = false
        user.hasLocationPermission = false
        user.hasNotificationEnabled = true
        user.amountLikes = 0
        user.latitude = 0.0
        user.longitude = 0.0

        val userValues = user.toMap()
        val childAdd = HashMap<String, Any>()
        childAdd["/users/$userId"] = userValues

        database.updateChildren(childAdd)
    }

    override fun updateUser(userId: String, user: UserModel) {

        val userValues = user.toMap()

        val childUpdate : MutableMap<String, Any?> = HashMap()
        childUpdate["users/$userId"] = userValues

        database.updateChildren(childUpdate)
    }

    override  fun updateUserImage(userId: String, photoUri: Uri)
    {
        database.child("users").child(userId).child("photoUri").setValue(photoUri.toString())
    }

    override fun getUsersByName(currentUserId: String, contactIds: ArrayList<String>, userNameFilter: String, userList: MutableLiveData<List<UserModel>>)
    {
        database.child("users").addValueEventListener(object : ValueEventListener{
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onDataChange(snapshot: DataSnapshot) {
                val localUserList = ArrayList<UserModel>()
                val users = snapshot.children

                users.forEach{

                    if(it.child("userName").value.toString().lowercase(Locale.getDefault()).contains(userNameFilter.lowercase(Locale.getDefault())))
                    {
                        val currentUser = UserModel()
                        currentUser.userId = it.child("userId").value.toString()
                        currentUser.userName = it.child("userName").value.toString()
                        currentUser.status = it.child("status").value.toString()
                        currentUser.photoUri = it.child("photoUri").value.toString()
                        currentUser.email = it.child("email").value.toString()
                        if(it.child("isMapEnabled").value != null) currentUser.isMapEnabled = it.child("isMapEnabled").value as Boolean
                        if(it.child("hasLocationPermission").value != null) currentUser.hasLocationPermission = it.child("hasLocationPermission").value as Boolean

                        val contactsForUser = it.child("contacts").children
                        contactsForUser.forEach { ds ->
                            val contactUserModel = ds.getValue(UserModel::class.java)
                            if (contactUserModel != null)
                            {
                                currentUser.contacts[ds.key.toString()] = contactUserModel
                            }
                        }

                        localUserList.add(currentUser)
                    }
                }

                // Remove all users included in the contact list
                contactIds.forEach {
                    localUserList.removeIf { currentUser -> currentUser.userId == it }
                }

                // Remove own user
                localUserList.removeIf{ currentUser -> currentUser.userId == currentUserId}


                database.child("users").removeEventListener(this)
                userList.value = localUserList
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun getAllContactsForUser(userId: String, contactList: MutableLiveData<List<ContactModel>>, isSelectMode: Boolean, groupContactIds: ArrayList<String>?, filterName: String) {
        database.child("users").child(userId).child("contacts").addValueEventListener(object : ValueEventListener {
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onDataChange(snapshot: DataSnapshot) {
                val localContactList = ArrayList<ContactModel>()
                val contacts = snapshot.children

                if(isSelectMode && groupContactIds != null)
                {
                    // Cannot filter contacts
                    contacts.forEach {
                        //val contactUserModel = it.getValue(ContactModel::class.java)
                        val currentContact = ContactModel()
                        currentContact.userId = it.child("userId").value.toString()
                        currentContact.userName = it.child("userName").value.toString()
                        currentContact.status = it.child("status").value.toString()
                        currentContact.photoUri = it.child("photoUri").value.toString()
                        currentContact.email = it.child("email").value.toString()
                        localContactList.add(currentContact)
                    }

                    // Remove all users included in the contact list
                    groupContactIds.forEach {
                        localContactList.removeIf { currentUser -> currentUser.userId == it }
                    }
                }
                else
                {
                    // Can filter contacts
                    contacts.forEach {
                        //val contactUserModel = it.getValue(ContactModel::class.java)
                        val currentContact = ContactModel()
                        if(it.child("userName").value.toString().lowercase(Locale.getDefault()).contains(filterName.lowercase(Locale.getDefault())))
                        {
                            currentContact.userId = it.child("userId").value.toString()
                            currentContact.userName = it.child("userName").value.toString()
                            currentContact.status = it.child("status").value.toString()
                            currentContact.photoUri = it.child("photoUri").value.toString()
                            currentContact.email = it.child("email").value.toString()
                            localContactList.add(currentContact)
                        }
                    }
                }

                database.child("users").child(userId).child("contacts").removeEventListener(this)

                contactList.value = localContactList
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })
    }

    override fun sendFriendRequest(contactToAdd: ContactModel, currentUser: ContactModel) {

        val contactValues = contactToAdd.toMap()
        val currentUserValues = currentUser.toMap()

        val childAdd = HashMap<String, Any>()
        // Add contact to add user to sent requests for own user
        childAdd["/users/${currentUser.userId}/openRequests/${contactToAdd.userId}"] = contactValues

        // Add own user to contact to received requests for add user
        childAdd["/users/${contactToAdd.userId}/receivedRequests/${currentUser.userId}"] = currentUserValues

        database.updateChildren(childAdd)

    }

    override fun receiveOpenFriendRequests(currentUserId: String, openFriendRequests: MutableLiveData<List<ContactModel>>) {

        database.child("users").child(currentUserId).child("openRequests").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val openRequests = ArrayList<ContactModel>()
                val children = snapshot.children

                children.forEach{
                    val contactUserModel = it.getValue(ContactModel::class.java)
                    openRequests.add(contactUserModel!!)
                }

                database.child("users").child(currentUserId).child("openRequests").removeEventListener(this)

                openFriendRequests.value = openRequests

            }

            override fun onCancelled(error: DatabaseError) {
               // TODO: Catch Error
            }
        })
    }

    override fun getReceivedFriendRequests(currentUserId: String, receivedFriendRequests: MutableLiveData<List<ContactModel>>)
    {
        database.child("users").child(currentUserId).child("receivedRequests").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val receivedRequests = ArrayList<ContactModel>()
                val children = snapshot.children

                children.forEach{
                    val contactUserModel = it.getValue(ContactModel::class.java)
                    receivedRequests.add(contactUserModel!!)
                }

                database.child("users").child(currentUserId).child("receivedRequests").removeEventListener(this)

                receivedFriendRequests.value = receivedRequests

            }

            override fun onCancelled(error: DatabaseError) {
                // TODO: Catch Error
            }
        })
    }

    override fun addContact(currentUser: ContactModel, userAdd: ContactModel) {

        val contactValues = userAdd.toMap()
        val currentUserValues = currentUser.toMap()

        val childAdd = HashMap<String, Any>()
        val childDelete : MutableMap<String, Any?> = HashMap()


        // Add userAdd to Contacts
        childAdd["/users/${currentUser.userId}/contacts/${userAdd.userId}"] = contactValues

        // Add ownUser to Contacts of userAdd
        childAdd["/users/${userAdd.userId}/contacts/${currentUser.userId}"] = currentUserValues

        // Remove receivedRequest of ownUser
        childDelete["/users/${currentUser.userId}/receivedRequests/${userAdd.userId}"] = null

        // Remove openRequest of userAdd
        childDelete["/users/${userAdd.userId}/openRequests/${currentUser.userId}"] = null


        database.updateChildren(childDelete)
        database.updateChildren(childAdd)
    }

    fun setUsersLocation(userId: String, latitude: Double, longitude: Double)
    {
        val childLocation = HashMap<String, Any>()

        childLocation["/users/$userId/latitude"] = latitude
        childLocation["/users/$userId/longitude"] = longitude

        database.updateChildren(childLocation)
    }

    override fun rejectRequest(currentUserId: String, userAddId: String) {

        val childDelete : MutableMap<String, Any?> = HashMap()

        // Remove receivedRequest of ownUser
        childDelete["/users/${currentUserId}/receivedRequests/${userAddId}"] = null

        // Remove openRequest of userAdd
        childDelete["/users/${userAddId}/openRequests/${currentUserId}"] = null

        database.updateChildren(childDelete)
    }

    override fun withdrawRequest(currentUserId: String, userAddId: String) {

        val childDelete : MutableMap<String, Any?> = HashMap()

        // Remove openRequest of ownUser
        childDelete["/users/${currentUserId}/openRequests/${userAddId}"] = null

        // Remove receivedRequest of userAdd
        childDelete["/users/${userAddId}/receivedRequests/${currentUserId}"] = null

        database.updateChildren(childDelete)
    }

    override fun getAllChatsForUser(currentUserId: String, chatContacts: MutableLiveData<List<ContactModel>>, contactNameFilter: String)
    {
        database.child("users").child(currentUserId).child("contacts").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val localChatContacts = ArrayList<ContactModel>()
                val children = snapshot.children

                children.forEach{ ds ->
                    // val currentContact = it.getValue(ContactModel::class.java)
                    if(ds.child("userName").value.toString().lowercase(Locale.getDefault()).contains(contactNameFilter.lowercase(Locale.getDefault())))
                    {
                        val currentContact = ContactModel()
                        currentContact.userId = ds.child("userId").value.toString()
                        currentContact.userName = ds.child("userName").value.toString()
                        currentContact.status = ds.child("status").value.toString()
                        currentContact.photoUri = ds.child("photoUri").value.toString()
                        currentContact.email = ds.child("email").value.toString()
                        currentContact.hasConversation = ds.child("hasConversation").value as Boolean
                        currentContact.hasNewMessage = ds.child("hasNewMessage").value as Boolean
                        currentContact.recentMessage = ds.child("recentMessage").getValue(MessageModel:: class.java)!!

                        if (currentContact.hasConversation)
                        {
                            localChatContacts.add(currentContact)
                        }
                    }
                }

                database.child("users").child(currentUserId).child("contacts").removeEventListener(this)
                chatContacts.value = localChatContacts
            }

            override fun onCancelled(error: DatabaseError) {
               // TODO: Catch error
            }
        })
    }

    override fun setMapEnabled(userId: String, isMapEnabled: Boolean)
    {
        database
            .child("users")
            .child(userId)
            .child("isMapEnabled")
            .setValue(isMapEnabled)
    }

    override fun isMapEnabled(userId: String, isMapEnabled: MutableLiveData<Boolean>) {

        database.child("users").child(userId).child("isMapEnabled").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.value != null)
                {
                    isMapEnabled.value = snapshot.value as Boolean
                }
                else{
                    isMapEnabled.value = false
                }

            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun setHasLocationPermission(userId: String, hasLocationPermission: Boolean) {
        database
            .child("users")
            .child(userId)
            .child("hasLocationPermission")
            .setValue(hasLocationPermission)
    }

    override fun hasLocationPermission(
        userId: String,
        hasLocationPermission: MutableLiveData<Boolean>
    ) {
        database.child("users").child(userId).child("hasLocationPermission").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

               if(snapshot.value != null)  hasLocationPermission.value = snapshot.value as Boolean
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun setAreNotificationsEnabled(userId: String, areNotificationsEnabled: Boolean) {
        database
            .child("users")
            .child(userId)
            .child("hasNotificationEnabled")
            .setValue(areNotificationsEnabled)
    }

    override fun areNotificationsEnabled(
        userId: String,
        areNotificationsEnabled: MutableLiveData<Boolean>
    ) {
        database.child("users").child(userId).child("hasNotificationEnabled").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.value != null) areNotificationsEnabled.value = snapshot.value as Boolean
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun getRecommendedContacts(
        currentUserId: String,
        contactIdsForCurrentUser: ArrayList<String>,
        recContacts: MutableLiveData<List<UserModel>>
    ) {

        database.child("users").addValueEventListener(object : ValueEventListener{
            @RequiresApi(Build.VERSION_CODES.N)
            override fun onDataChange(snapshot: DataSnapshot) {

                // Holds all users
                val allUsers = ArrayList<UserModel>()

                // The return list
                val allContactsFromContacts = ArrayList<UserModel>()

                val children = snapshot.children

                // All users = all users in database
                children.forEach {
                    val currentUser = it.getValue(UserModel::class.java)
                    allUsers.add(currentUser!!)
                }

                // All users = my contacts as users
                allUsers.removeAll{user -> user.userId !in contactIdsForCurrentUser}


                // All contacts from contacts = All existing contacts from current users contacts
                allUsers.forEach {
                    it.contacts.values.forEach { currentContact ->
                        if(!allContactsFromContacts.contains(currentContact))
                        {
                            allContactsFromContacts.add(currentContact)
                        }
                    }
                }

                // Remove all contacts already existing in current users contacts
                allContactsFromContacts.removeAll{user -> user.userId in contactIdsForCurrentUser}
                // Remove user himself/herself
                allContactsFromContacts.removeIf{user -> user.userId == currentUserId }


                database.child("users").removeEventListener(this)
                recContacts.value = allContactsFromContacts
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun getLikesForUser(currentUserId: String, amountLikes: MutableLiveData<Int>) {

        database.child("users").child(currentUserId).child("amountLikes").addValueEventListener(object: ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.value != null)
                {
                    val likeValue: Long = snapshot.value as Long
                    amountLikes.value = likeValue.toInt()
                }
                else
                {
                    amountLikes.value = 0
                }
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun increaseLikeForUser(userId: String, currentLikeAmount: Int) {

        val newLikeAmount = currentLikeAmount + 1
        Log.d("New Like Amount", newLikeAmount.toString())
        database
            .child("users")
            .child(userId)
            .child("amountLikes")
            .setValue(newLikeAmount)
    }

    override fun hasAlreadyLiked(currentUserId: String, contactId: String, hasAlreadyLiked: MutableLiveData<Boolean>)
    {
        database.child("users").child(currentUserId).child("contacts").child(contactId).child("hasAlreadyLiked")
            .addValueEventListener(object : ValueEventListener{
                override fun onDataChange(snapshot: DataSnapshot) {

                    if(snapshot.value != null)
                    {
                        hasAlreadyLiked.value = snapshot.value as Boolean
                    }
                    else
                    {
                        hasAlreadyLiked.value = false
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    TODO("Not yet implemented")
                }
            })
    }

  override fun setHasLiked(currentUserId: String, contactId: String, hasLiked: Boolean)
  {
      database
          .child("users")
          .child(currentUserId)
          .child("contacts")
          .child(contactId)
          .child("hasAlreadyLiked")
          .setValue(hasLiked)
  }

    override fun getUsersWithLocation(userIds: ArrayList<String>, usersWithLocation: MutableLiveData<List<UserModel>>)
    {
        database.child("users").addValueEventListener(object : ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {

                val localUsers = ArrayList<UserModel>()
                val children = snapshot.children

                children.forEach {
                    val currentUser = it.getValue(UserModel::class.java)

                    if(currentUser!!.userId in userIds)
                    {
                        val isMapEnabled: Boolean = snapshot.child(currentUser.userId).child("isMapEnabled").value as Boolean

                        if(isMapEnabled)
                        {
                            localUsers.add(currentUser)
                        }
                    }
                }

                usersWithLocation.value = localUsers
                database.child("users").removeEventListener(this)
            }
            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }
}