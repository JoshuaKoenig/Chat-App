package com.koenig.chatapp.models

import androidx.lifecycle.MutableLiveData

interface GroupStore {
    fun createGroupChat(group: GroupModel, firstMessage: MessageModel)
    fun getGroupChatsForUser(userId: String, groups: MutableLiveData<List<GroupModel>>)
    fun addUserToGroupChat(groupId: String, contactToAdd: ContactModel)
    fun removeUserFromGroupChat(groupId: String, contactToRemove: ContactModel)
    fun getGroupById(groupId: String, group: MutableLiveData<GroupModel>)
}