package com.koenig.chatapp.ui.searchContactManager

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.koenig.chatapp.firebase.FirebaseDBManager
import com.koenig.chatapp.models.UserModel

class SearchContactsViewModel : ViewModel() {

    private val userList = MutableLiveData<List<UserModel>>()

    val observableUserList: LiveData<List<UserModel>>
        get() = userList

    fun getFilteredUsers(currentUserId: String, contactIds: ArrayList<String>, userNameFilter: String)
    {
        try {
            FirebaseDBManager.getUsersByName(currentUserId, contactIds, userNameFilter, userList)
        }
        catch (e: Exception)
        {
            //TODO: Catch exception
        }
    }
}