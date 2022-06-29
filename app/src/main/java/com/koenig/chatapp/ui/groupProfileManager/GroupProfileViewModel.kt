package com.koenig.chatapp.ui.groupProfileManager

import androidx.lifecycle.ViewModel
import com.koenig.chatapp.firebase.FirebaseGroupChatManager
import com.koenig.chatapp.models.ContactModel
import java.lang.Exception

class GroupProfileViewModel : ViewModel() {

    fun addContactToGroup(groupId: String, contactToAdd: ContactModel)
    {
        try
        {
            FirebaseGroupChatManager.addUserToGroupChat(groupId, contactToAdd)
        }
        catch (e: Exception)
        {
            //TODO: Catch Exception
        }
    }

    fun removeContactFromGroup(groupId: String, contactToRemoveId: String)
    {
        try
        {
            FirebaseGroupChatManager.removeUserFromGroupChat(groupId, contactToRemoveId)
        }
        catch (e: Exception)
        {
            //TODO: Catch Exception
        }
    }

    fun updateGroupName(groupId: String, newGroupName: String)
    {
        try {
            FirebaseGroupChatManager.updateGroupName(groupId, newGroupName)
        }
        catch (e: Exception)
        {
            // TODO
        }
    }

    fun updateDescription(groupId: String, newDescription: String)
    {
        try {
            FirebaseGroupChatManager.updateGroupDescription(groupId, newDescription)
        }
        catch (e: Exception)
        {
            // TODO
        }
    }

    fun updateGroupImage(groupId: String, newImageUri: String)
    {
        try {
            FirebaseGroupChatManager.updateGroupImage(groupId, newImageUri)
        }
        catch (e: Exception)
        {
            // TODO
        }
    }

    fun disbandGroup(groupId: String)
    {
        try {
            FirebaseGroupChatManager.disbandGroup(groupId)
        }
        catch (e: Exception)
        {
            // TODO
        }
    }
}