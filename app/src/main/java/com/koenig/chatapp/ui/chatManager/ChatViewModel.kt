package com.koenig.chatapp.ui.chatManager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.koenig.chatapp.firebase.FirebaseDBManager
import com.koenig.chatapp.firebase.FirebaseGroupChatManager
import com.koenig.chatapp.firebase.FirebaseMessageManager
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.models.GroupModel
import com.koenig.chatapp.models.MessageModel
import com.koenig.chatapp.models.UserModel
import java.lang.Exception

class ChatViewModel : ViewModel() {

    private val selectedUser = MutableLiveData<UserModel>()
    private val selectedGroup = MutableLiveData<GroupModel>()

    private val messages = MutableLiveData<List<MessageModel>>()

    private val messageForUser = MutableLiveData<MessageModel>()
    val observableMessageForUser: LiveData<MessageModel>
            get() = messageForUser

    var lifeFirebaseUser = MutableLiveData<FirebaseUser>()

    var observableUser: LiveData<UserModel>
        get() = selectedUser
        set(value) {selectedUser.value = value.value}

    var observableGroup: LiveData<GroupModel>
        get() = selectedGroup
        set(value) {selectedGroup.value = value.value}


    var observableMessages: LiveData<List<MessageModel>>
        get() = messages
        set(value) {messages.value = value.value}

    fun getSelectedProfile(userId: String)
    {
        try {
            FirebaseDBManager.getUserById(userId, selectedUser)
        }
        catch (e: Exception)
        {
            //TODO: Catch exception
        }
    }

    fun getSelectedGroup(groupId: String)
    {
        FirebaseGroupChatManager.getGroupById(groupId, selectedGroup)
    }

    fun retrieveMessage(toUserId: String)
    {
        try {
            FirebaseMessageManager.retrieveMessages(lifeFirebaseUser.value!!.uid, toUserId, messages)
        }
        catch (e: Exception)
        {
            //TODO: Catch exception
        }
    }

    fun retrieveGroupMessages(toGroupId: String)
    {
        try {
            FirebaseMessageManager.retrieveGroupMessages(lifeFirebaseUser.value!!.uid, toGroupId, messages)
        }
        catch (e: Exception)
        {
            //TODO: Catch exception
        }
    }

    fun sendMessage(message: MessageModel)
    {
        FirebaseMessageManager.sendMessage(message)
    }

    fun sendGroupMessage(message: MessageModel, groupMembers: List<ContactModel>)
    {
        FirebaseMessageManager.sendGroupMessage(message, groupMembers)
    }

    fun receiveMessageForUser(currentUserId: String)
    {
        FirebaseMessageManager.receiveRecentMessagesForUser(currentUserId, messageForUser)
    }
}