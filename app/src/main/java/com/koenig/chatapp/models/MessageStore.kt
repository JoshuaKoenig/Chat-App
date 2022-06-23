package com.koenig.chatapp.models

import androidx.lifecycle.MutableLiveData

interface MessageStore {

    fun sendMessage(message: MessageModel)
    fun retrieveMessages(fromUserId: String, toUserId: String, messages: MutableLiveData<List<MessageModel>>)
    fun retrieveRecentMessage(fromUserId: String, toUserId: String, message: MutableLiveData<MessageModel>)
    fun removeHasNewMessageFlag(fromUserId: String, toUserId: String)
}