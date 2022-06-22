package com.koenig.chatapp.models

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser

interface UserStore {

    fun getUserById(userId: String, user: MutableLiveData<UserModel>)
    fun createUser(firebaseUser: FirebaseUser)
    fun updateUser(userId: String, user: UserModel)
    fun updateUserImage(userId: String, photoUri: Uri)
    fun getAllUsers(userList: MutableLiveData<List<UserModel>>)

    fun addContact(currentUser: ContactModel, userAdd: ContactModel)
    fun rejectRequest(currentUserId: String, userAddId: String)
    fun withdrawRequest(currentUserId: String, userAddId: String)
    fun getAllContactsForUser(userId: String, contactList: MutableLiveData<List<ContactModel>>)
    fun sendFriendRequest(contactToAdd: ContactModel, currentUser: ContactModel)
    fun receiveOpenFriendRequests(currentUserId: String, openFriendRequests: MutableLiveData<List<ContactModel>>)
    fun getReceivedFriendRequests(currentUserId: String, receivedFriendRequests: MutableLiveData<List<ContactModel>>)
}