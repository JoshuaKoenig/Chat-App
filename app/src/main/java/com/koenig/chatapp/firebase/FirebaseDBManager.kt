package com.koenig.chatapp.firebase

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.google.firebase.database.ktx.getValue
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.models.UserModel
import com.koenig.chatapp.models.UserStore

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

    override fun getAllUsers(userList: MutableLiveData<List<UserModel>>) {

        database.child("users").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
               val localUserList = ArrayList<UserModel>()
                val users = snapshot.children

                users.forEach{
                    val currentUser = UserModel()
                    currentUser.userId = it.child("userId").value.toString()
                    currentUser.userName = it.child("userName").value.toString()
                    currentUser.status = it.child("status").value.toString()
                    currentUser.photoUri = it.child("photoUri").value.toString()
                    currentUser.email = it.child("email").value.toString()
                    //val userModel = it.getValue(UserModel::class.java)
                    localUserList.add(currentUser)
                }

                database.child("users").removeEventListener(this)
                userList.value = localUserList
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }
        })
    }

    override fun getAllContactsForUser(userId: String, contactList: MutableLiveData<List<ContactModel>>) {
        database.child("users").child(userId).child("contacts").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val localContactList = ArrayList<ContactModel>()
                val contacts = snapshot.children

                contacts.forEach {
                    val contactUserModel = it.getValue(ContactModel::class.java)
                    localContactList.add(contactUserModel!!)
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
                val openRequests = ArrayList<ContactModel>()
                val children = snapshot.children

                children.forEach{
                    val contactUserModel = it.getValue(ContactModel::class.java)
                    openRequests.add(contactUserModel!!)
                }

                database.child("users").child(currentUserId).child("receivedRequests").removeEventListener(this)

                receivedFriendRequests.value = openRequests

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


}