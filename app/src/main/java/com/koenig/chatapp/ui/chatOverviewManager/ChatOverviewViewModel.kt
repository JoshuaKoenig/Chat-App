package com.koenig.chatapp.ui.chatOverviewManager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.koenig.chatapp.firebase.FirebaseDBManager
import com.koenig.chatapp.firebase.FirebaseGroupChatManager
import com.koenig.chatapp.firebase.FirebaseMessageManager
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.models.GroupModel

class ChatOverviewViewModel : ViewModel() {

    private val chatContacts = MutableLiveData<List<ContactModel>>()
    private  val groupChats = MutableLiveData<List<GroupModel>>()

    val currentTab = MutableLiveData<Int>()

    var currentTabObserver: LiveData<Int>
        get() = currentTab
        set(value) {currentTab.value = value.value}

    val observableChatContacts: LiveData<List<ContactModel>>
        get() = chatContacts

    val observableGroupChats: LiveData<List<GroupModel>>
        get() = groupChats

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

    fun getAllGroupChats(currentUserId: String)
    {
        try {
            FirebaseGroupChatManager.getGroupChatsForUser(currentUserId, groupChats)
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

    fun removeHasNewGroupMessageFlag(groupId: String, currentUserId: String)
    {
        FirebaseMessageManager.removeHasNewGroupMessageFlag(groupId, currentUserId)
    }
}