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

    private val recUserList = MutableLiveData<List<UserModel>>()

    val observableRecUserList: LiveData<List<UserModel>>
        get() = recUserList

    val currentTab = MutableLiveData<Int>()

    var currentTabObserver: LiveData<Int>
        get() = currentTab
        set(value) {currentTab.value = value.value}

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

    fun getRecommendedContacts(currentUserId: String, contactIds: ArrayList<String>)
    {
        try {
            FirebaseDBManager.getRecommendedContacts(currentUserId, contactIds, recUserList)
        }
        catch (e: Exception)
        {
            //TODO: Catch exception
        }
    }
}