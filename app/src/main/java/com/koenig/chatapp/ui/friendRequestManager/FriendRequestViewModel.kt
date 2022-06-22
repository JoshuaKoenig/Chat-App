package com.koenig.chatapp.ui.friendRequestManager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.koenig.chatapp.firebase.FirebaseDBManager
import com.koenig.chatapp.models.ContactModel

class FriendRequestViewModel : ViewModel() {

    private val openFriendRequests = MutableLiveData<List<ContactModel>>()
    private  val receivedFriendRequests = MutableLiveData<List<ContactModel>>()

    var observableOpenFriendReq: LiveData<List<ContactModel>>
        get() = openFriendRequests
        set(value) {openFriendRequests.value = value.value }

    var observableReceivedFriendReq: LiveData<List<ContactModel>>
        get() = receivedFriendRequests
        set(value) {receivedFriendRequests.value = value.value }


    fun sendFriendRequest(contactToAdd: ContactModel, currentUser: ContactModel)
    {
        FirebaseDBManager.sendFriendRequest(contactToAdd, currentUser)
    }

    fun getOpenFriendRequests(currentUserId: String)
    {
        FirebaseDBManager.receiveOpenFriendRequests(currentUserId, openFriendRequests)
    }

    fun getReceivedFriendRequests(currentUserId: String)
    {
        FirebaseDBManager.getReceivedFriendRequests(currentUserId, receivedFriendRequests)
    }

    fun acceptFriendRequest(currentUser: ContactModel, contactToAdd: ContactModel)
    {
        try {
            FirebaseDBManager.addContact(currentUser, contactToAdd)
        }
        catch (e: Exception)
        {
            // TODO: Catch exception
        }
    }

    fun rejectFriendRequest(currentUserId: String, userToAddId: String)
    {
        try {
            FirebaseDBManager.rejectRequest(currentUserId, userToAddId)
        }
        catch (e: Exception)
        {
            // TODO: Catch exception
        }
    }

    fun withdrawRequest(currentUserId: String, userToAddId: String)
    {
        try {
            FirebaseDBManager.withdrawRequest(currentUserId, userToAddId)
        }
        catch (e: Exception)
        {
            // TODO: Catch exception
        }
    }

}