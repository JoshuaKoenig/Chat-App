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

    init { load() }

    private fun load()
    {
        try {
            FirebaseDBManager.getAllUsers(userList)
        }
        catch (e: Exception)
        {
            //TODO: Catch exception
        }
    }

    fun addContact(currentUserId: String, addUser: UserModel)
    {
        try {
            FirebaseDBManager.addContact(currentUserId, addUser)
        }
        catch (e: Exception)
        {
            //TODO: Catch exception
        }
    }

}