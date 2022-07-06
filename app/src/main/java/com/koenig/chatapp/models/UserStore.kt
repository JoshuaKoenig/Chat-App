package com.koenig.chatapp.models

import android.net.Uri
import androidx.lifecycle.MutableLiveData
import com.google.firebase.auth.FirebaseUser

interface UserStore {

    fun getUserById(userId: String, user: MutableLiveData<UserModel>)
    fun createUser(firebaseUser: FirebaseUser)
    fun updateUser(userId: String, user: UserModel)
    fun updateUserImage(userId: String, photoUri: Uri)
    fun getUsersByName(currentUserId: String, contactIds: ArrayList<String>, userNameFilter: String, userList: MutableLiveData<List<UserModel>>)

    fun addContact(currentUser: ContactModel, userAdd: ContactModel)
    fun rejectRequest(currentUserId: String, userAddId: String)
    fun withdrawRequest(currentUserId: String, userAddId: String)
    fun getAllContactsForUser(userId: String, contactList: MutableLiveData<List<ContactModel>>, isSelectMode: Boolean, groupContactIds: ArrayList<String>?)
    fun sendFriendRequest(contactToAdd: ContactModel, currentUser: ContactModel)
    fun receiveOpenFriendRequests(currentUserId: String, openFriendRequests: MutableLiveData<List<ContactModel>>)
    fun getReceivedFriendRequests(currentUserId: String, receivedFriendRequests: MutableLiveData<List<ContactModel>>)
    fun getAllChatsForUser(currentUserId: String, chatContacts: MutableLiveData<List<ContactModel>>, contactNameFilter: String)

    fun setMapEnabled(userId: String, isMapEnabled: Boolean)
    fun isMapEnabled(userId: String, isMapEnabled: MutableLiveData<Boolean>)
    fun setHasLocationPermission(userId: String, hasLocationPermission: Boolean)
    fun hasLocationPermission(userId: String, hasLocationPermission: MutableLiveData<Boolean>)
    fun setAreNotificationsEnabled(userId: String, areNotificationsEnabled: Boolean)
    fun areNotificationsEnabled(userId: String, areNotificationsEnabled: MutableLiveData<Boolean>)

    fun getRecommendedContacts( currentUserId: String, contactIdsForCurrentUser: ArrayList<String>, recContacts: MutableLiveData<List<UserModel>>)

    fun getLikesForUser(currentUserId: String, amountLikes: MutableLiveData<Int>)
    fun increaseLikeForUser(userId: String, currentLikeAmount: Int)
    fun setHasLiked(currentUserId: String, contactId: String, hasLiked: Boolean)
    fun hasAlreadyLiked(currentUserId: String, contactId: String, hasAlreadyLiked: MutableLiveData<Boolean>)
}