package com.koenig.chatapp.models

import androidx.lifecycle.MutableLiveData

interface MessageStore {

    fun sendMessage(message: MessageModel)
    fun retrieveMessage(fromUserId: String, toUserId: String, messages: MutableLiveData<List<MessageModel>>)
}