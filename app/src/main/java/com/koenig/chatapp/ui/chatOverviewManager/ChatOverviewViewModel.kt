package com.koenig.chatapp.ui.chatOverviewManager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.koenig.chatapp.firebase.FirebaseDBManager
import com.koenig.chatapp.firebase.FirebaseMessageManager
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.models.MessageModel
import com.koenig.chatapp.models.UserModel

class ChatOverviewViewModel : ViewModel() {

    private val chatContacts = MutableLiveData<List<ContactModel>>()

    val observableChatContacts: LiveData<List<ContactModel>>
        get() = chatContacts


    fun getAllChatContacts(currentUserId: String)
    {
        try {
            FirebaseDBManager.getAllChatsForUser(currentUserId, chatContacts)
        }
        catch (e: Exception)
        {
            //TODO: Catch exception
        }
    }

    fun removeHasNewMessageFlag(fromUserId: String, toUserId: String)
    {
        FirebaseMessageManager.removeHasNewMessageFlag(fromUserId, toUserId)
    }


}