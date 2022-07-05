package com.koenig.chatapp.ui.groupChatManger

import android.widget.ImageView
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.koenig.chatapp.firebase.FirebaseGroupChatManager
import com.koenig.chatapp.models.ContactModel
import com.koenig.chatapp.models.GroupModel
import com.koenig.chatapp.models.MessageModel

class CreateGroupChatViewModel : ViewModel() {

    val currentGroupMembers = MutableLiveData<List<ContactModel>>()

    var observableGroupMembers: LiveData<List<ContactModel>>
        get() = currentGroupMembers
        set(value) { currentGroupMembers.value = value.value }

    fun createGroupChat(newGroup: GroupModel, firstMessage: MessageModel, imageView: ImageView)
    {
        FirebaseGroupChatManager.createGroupChat(newGroup, firstMessage, imageView)
    }
}