package com.koenig.chatapp.models

import androidx.lifecycle.MutableLiveData

interface GroupStore {
    fun createGroupChat(group: GroupModel, firstMessage: MessageModel)
    fun getGroupChatsForUser(userId: String, groups: MutableLiveData<List<GroupModel>>, groupFilter: String)
    fun addUserToGroupChat(groupId: String, contactToAdd: ContactModel)
    fun removeUserFromGroupChat(groupId: String, contactToRemoveId: String)
    fun getGroupById(groupId: String, group: MutableLiveData<GroupModel>)
    fun updateGroupName(groupId: String, newGroupName: String)
    fun updateGroupDescription(groupId: String, newDescription: String)
    fun updateGroupImage(groupId: String, newImageUrl: String)
    fun disbandGroup(groupId: String)
}