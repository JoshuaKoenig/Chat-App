package com.koenig.chatapp.models

import androidx.lifecycle.MutableLiveData

interface MessageStore {

    fun sendMessage(message: MessageModel)
    fun retrieveMessages(fromUserId: String, toUserId: String, messages: MutableLiveData<List<MessageModel>>)
    fun removeHasNewMessageFlag(fromUserId: String, toUserId: String)

    fun sendGroupMessage(message: MessageModel, groupMembers: List<ContactModel>)
    fun retrieveGroupMessages(ownUserId: String, toGroupId: String, messages: MutableLiveData<List<MessageModel>>)
    fun removeHasNewGroupMessageFlag(groupId: String, currentUserId: String)
}