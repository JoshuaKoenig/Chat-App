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

    fun removeContactFromGroup(groupId: String, contactToRemove: ContactModel)
    {
        try
        {
            FirebaseGroupChatManager.removeUserFromGroupChat(groupId, contactToRemove)
        }
        catch (e: Exception)
        {
            //TODO: Catch Exception
        }
    }
}