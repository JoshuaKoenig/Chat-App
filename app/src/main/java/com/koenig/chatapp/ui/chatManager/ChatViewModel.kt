package com.koenig.chatapp.ui.chatManager

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser
import com.koenig.chatapp.firebase.FirebaseDBManager
import com.koenig.chatapp.firebase.FirebaseMessageManager
import com.koenig.chatapp.models.MessageModel
import com.koenig.chatapp.models.UserModel
import java.lang.Exception

class ChatViewModel : ViewModel() {

    private val selectedUser = MutableLiveData<UserModel>()
    private val messages = MutableLiveData<List<MessageModel>>()

    var lifeFirebaseUser = MutableLiveData<FirebaseUser>()

    var observableUser: LiveData<UserModel>
        get() = selectedUser
        set(value) {selectedUser.value = value.value}

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

    fun sendMessage(message: MessageModel)
    {
        FirebaseMessageManager.sendMessage(message)
    }

}